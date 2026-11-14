# Spring Kafka Microservices Lab

Octsoc (Costco spelled backwards) is a study e-commerce platform made of event-driven
microservices built with Spring Boot and Apache Kafka. Web and mobile channels
manage products and customers and create orders. Orders charges the customer through the bank,
and once the bank confirms the payment by webhook, the order flows through Kafka to billing
(invoice PDF built with JasperReports and stored in MinIO) and logistics (shipment tracking).

## Tech Stack
- Java 21
- Spring Boot 4.1
- Maven
- Spring for Apache Kafka (Kafka in KRaft mode, no ZooKeeper)
- Spring Data JPA and Hibernate
- PostgreSQL (one container with one database per service)
- springdoc-openapi
- MinIO (S3-compatible object storage) with the MinIO Java SDK
- JasperReports
- Docker and Docker Compose
- Testcontainers

## Repository layout
Each microservice is an independent Spring Boot Maven project in its own directory, with its
own ./mvnw and pom.xml:

| Service   | Port | Database        | Responsibility                                 |
|-----------|------|-----------------|------------------------------------------------|
| products  | 8081 | octsocproducts  | product registration (create, update, list)    |
| customers | 8082 | octsoccustomers | customer registration (create, update, list)   |
| orders    | 8083 | octsocorders    | orders, payment with the bank and order status |
| billing   | 8084 | none            | invoices for paid orders                       |
| logistics | 8085 | none            | shipment tracking for invoiced orders          |

- services/ holds the shared infrastructure, not a microservice.
- services/src/database/docker-compose.yaml starts one PostgreSQL container (db_octsoc, host port
  5555) that keeps its data in services/src/database/data/ (git-ignored). On the first start,
  docker-entrypoint-initdb.d/postgres-schema.sql creates the databases and their tables
  (products, customers, orders and order_items).
- To change a database or table, edit postgres-schema.sql. The file runs only while data/ is
  empty, so on an existing environment apply the change by hand too.
- Prefer plain SQL or configuration to shell scripts for simple setup tasks.
- Each service connects through spring.datasource in application.yaml
  (jdbc:postgresql://localhost:5555/octsoc<service>); tests use Testcontainers instead.
- docker-compose.yaml at the root starts the shared external services: Kafka and MinIO.
- No shared code module between services: each service declares its own event records.

## Commands
- Database: cd services/src/database && docker compose up -d   (PostgreSQL 5555)
- Shared services: docker compose up -d          (Kafka 9092, MinIO 9000, console 9001)
- Run a service: cd <service> && ./mvnw spring-boot:run   (the database must be running)
- Build and tests: cd <service> && ./mvnw verify  (Docker must be running for Testcontainers)
- Stop shared services: docker compose down (keeps the volumes; never add -v unless asked,
  it wipes all data)

## Service structure
- Maven groupId io.github.thomaspalma1.octsoc; the artifactId is the service name.
- Base package io.github.thomaspalma1.octsoc.<service>: products, customers, orders, billing,
  logistics.
- Databases are named octsoc<service>: octsocproducts, octsoccustomers, octsocorders.
- Subpackages: api (controllers, request and response records), domain (entities, repositories,
  services), messaging (producers, consumers, event records), config. Only where the service
  needs them: storage (MinIO, billing), report (JasperReports, billing), webhook (orders).

## Order flow
1. A web or mobile channel creates an order in orders.
2. orders sends the payment transaction to the bank.
3. The bank sends the payment status back to orders through a webhook.
4. When the payment is approved, orders publishes the order to orders.paid.
5. billing consumes orders.paid and creates the invoice.
6. billing publishes the invoiced order to orders.invoiced.
7. orders consumes orders.invoiced to update the order status, and logistics consumes it to
   register the shipment tracking.
8. logistics publishes the shipment to orders.shipped, and orders consumes it to update the
   order status.

## Service boundaries
- A service reads and writes only its own database. Never connect to another service's
  database or join data across services.
- products and customers are registration services used by the channels; they publish no events.
- The order flow after the payment is asynchronous through Kafka. A synchronous call between
  services needs a reason, a timeout and a fallback.

## Kafka
- Topics: orders.paid (produced by orders), orders.invoiced (billing), orders.shipped
  (logistics). New topics follow <domain>.<event> in kebab-case and past tense.
- The producing service declares its topics as NewTopic beans with partitions and replicas
  from configuration. Broker auto topic creation is off; consumers never create topics.
- The message key is the orderId, so every event of one order goes to the same partition and
  keeps its order.
- Payloads are JSON. Every event is a record with eventId (UUID), occurredAt and the business
  fields. Evolve events by adding fields only; never rename or remove a field consumers read.
- Producers send no Java type headers; each consumer deserializes into its own event record.
- The consumer group id is spring.application.name, so each service gets every event once
  (orders and logistics both receive orders.invoiced) and instances of the same service share
  the partitions.
- Services with a database publish events only after the transaction commits
  (@TransactionalEventListener AFTER_COMMIT).
- Consumers are idempotent because Kafka delivers at least once. Services with a database store
  the processed eventId and skip duplicates; services without one produce the same result for
  the same event (same object key, same published event).
- Failed records are retried with backoff by DefaultErrorHandler, then sent to the dead letter
  topic <topic>.DLT. Deserialization and validation errors are not retried.

## Persistence (products, customers, orders)
- Hibernate creates and updates the schema from the entities (ddl-auto: update). open-in-view is off.
- Enums with @Enumerated(EnumType.STRING); @ManyToOne with fetch LAZY.
- Listings use projections or @EntityGraph to avoid N+1 queries.
- No hand-written queries in src/main: no JPQL or native SQL in @Query. Use Spring Data derived
  queries; a query method without conditions ends in By (findAllWithItemsBy). Tests may run a
  native SELECT only to check what is stored in a column.
- Auditing (createdAt, updatedAt) with Spring Data Auditing.
- Optimistic locking with @Version; a version conflict returns 409.

## API
- REST endpoints under /api. DTOs are records with Bean Validation; entities never leave the API.
- Errors are ProblemDetail responses built by GlobalExceptionHandler.
- Constructor injection only; @Transactional on services, never on controllers.

## Payment and webhooks (orders)
- orders sends the payment transaction to the bank over HTTP, with a timeout. The payment status
  comes back later through the webhook.
- The bank calls POST /webhooks/bank. Verify the HMAC-SHA256 signature header with the shared
  secret before parsing the body; a missing or invalid signature returns 401.
- Answer with 2xx fast: validate, record the status and publish to Kafka after the commit.
- Deduplicate deliveries by the bank's delivery id.

## Object storage (MinIO)
- Files go to MinIO. Services store or send only the bucket and object key, never file bytes.
- Bucket names come from configuration; the service creates its bucket at startup if missing.
- Object keys follow <resource>/<id>/<file-name>, like invoices/{orderId}/invoice-{number}.pdf.
- Clients download files through presigned URLs with a configured expiry, never public buckets.
- All MinIO calls go through the storage package of the service.

## Reports (JasperReports)
- Templates (.jrxml) live in src/main/resources/reports/. They are compiled once at startup and
  cached, never per request.
- The service builds the report data and passes it as parameters and a
  JRBeanCollectionDataSource of records; templates never run SQL.
- Invoice PDFs are stored in MinIO, and the orders.invoiced event carries the object key.

## Clean code
Naming:
- Names reveal intent. Prefer `pendingOrders` to `list`, `orderId` to `id2`.
- No abbreviations or single-letter names anywhere, including lambdas and loops:
  `customer`, not `cust`; `invoice`, not `inv` or `i`; `product -> product.getName()`,
  not `p -> p.getName()`.
- Classes are nouns in PascalCase that say what the class is or does: `OrderService`,
  `InvoiceReportGenerator`, `ShipmentTracker`. Avoid vague names like `Manager`, `Helper`,
  `Utils`, `Data` or `Info`.
- Layer suffixes stay consistent: `Controller`, `Service`, `Repository`, `Request` and `Response`
  for DTOs, `Summary` for projections, `Exception` for exceptions, `Event` for Kafka event
  records, `Producer` and `Consumer` for Kafka classes.
- Methods are verbs in camelCase that say what they do: `createOrder`, `issueInvoice`,
  `registerShipment`.
- One word per concept across the code base: `find` for queries, `create` for new records,
  `get` for accessors. Do not mix `create`, `add` and `new` for the same action.
- Booleans read as questions: `isPaid`, `hasStock`, `canShip`.
- Collections are plural: `orders`, `orderItems`, `shipments`.
- Constants in UPPER_SNAKE_CASE, with the unit when there is one: `BANK_TIMEOUT_SECONDS`,
  `PRESIGNED_URL_EXPIRY_MINUTES`.
- No type or noise words in names: `orders`, not `orderList` or `theOrders`.
- Test methods describe the behavior they check: `skipsDuplicateOrderPaidEvent`,
  `rejectsWebhookWithInvalidSignature`.

General:
- Small methods that do one thing; extract a well-named method instead of adding a comment.
- No magic numbers or strings: use named constants or configuration properties.
- Prefer early returns to deep nesting.
- Comments explain why, never what. No commented-out code.

## Testing
- Layers: unit (JUnit 5, Mockito), repository (@DataJpaTest with Testcontainers PostgreSQL),
  web (@WebMvcTest), integration (@SpringBootTest with Testcontainers Kafka, PostgreSQL and MinIO).
- Testcontainers images use the same pinned versions as the compose files.
- Producers and consumers are tested against a real Kafka container. Wait with Awaitility,
  never Thread.sleep.
- Every consumer has a test for a duplicate event (one effect only) and for dead letter routing.
- The bank is replaced by MockRestServiceServer; webhook tests cover valid, invalid and missing
  signatures.
- Tests never call real external systems; CI must run without secrets.

## Rules
- Everything in English: packages, directories, files, classes, fields, tables, topics,
  endpoints, configuration, comments and docs.
- No "latest" versions: pin every Docker image tag, dependency and tool.
- Never read or write .env, keys or credential files. Database passwords, MinIO credentials and
  webhook secrets come from environment variables; local development defaults exist only in the
  compose files and application.yaml.
- Indent Java and XML with tabs, like the files generated by Spring Initializr.
