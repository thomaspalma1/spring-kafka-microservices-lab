# Spring Kafka Microservices Lab

Event-driven microservices built with Spring Boot and Apache Kafka. Five independent services
(products, customers, orders, billing and logistics) each own their database and talk to each
other through Kafka events. Billing generates invoices as JasperReports PDFs stored in MinIO,
and external systems integrate through webhooks.

## Tech Stack
- Java 21
- Spring Boot 4.1
- Maven
- Spring for Apache Kafka (Kafka in KRaft mode, no ZooKeeper)
- Spring Data JPA and Hibernate
- PostgreSQL (one database per service)
- springdoc-openapi
- MinIO (S3-compatible object storage) with the MinIO Java SDK
- JasperReports
- Docker and Docker Compose (external services only)
- Testcontainers

## Repository layout
Each microservice is an independent Spring Boot Maven project in its own directory, with its
own ./mvnw, pom.xml and database:

| Service           | Port | Owns                                  |
|-------------------|------|---------------------------------------|
| product-service   | 8081 | products, prices and stock            |
| customer-service  | 8082 | customers and addresses               |
| order-service     | 8083 | orders and order items                |
| billing-service   | 8084 | invoices and their PDF files          |
| logistics-service | 8085 | shipments and delivery status         |

- docker-compose.yaml at the root starts only the external services: Kafka, PostgreSQL (one
  container, one database per service) and MinIO. The microservices run on the host.
- No shared code module between services: each service declares its own event records.
- Do not add spring-boot-docker-compose; the root compose file is shared by all services.

## Commands
- External services: docker compose up -d       (Kafka 9092, PostgreSQL 5432, MinIO 9000, console 9001)
- Build and tests: cd <service> && ./mvnw verify  (Docker must be running for Testcontainers)
- Run a service: cd <service> && ./mvnw spring-boot:run
- Stop: docker compose down (keeps the volumes; never add -v unless asked, it wipes all data)

## Service structure
- Base package io.github.thomaspalma1.<domain>: product, customer, order, billing, logistics.
- Subpackages: api (controllers, request and response records), domain (entities, repositories,
  services), messaging (producers, consumers, event records), config. Only where the service
  needs them: storage (MinIO), report (JasperReports), webhook.

## Service boundaries
- A service reads and writes only its own database. Never connect to another service's
  database or join data across services.
- Services communicate asynchronously through Kafka. Data a service needs from another one
  (customer name, product price) is copied from that service's events into its own tables.
- Prefer events to synchronous calls between services; a synchronous call needs a reason,
  a timeout and a fallback.

## Kafka
- Topic names are <domain>.<event> in kebab-case and past tense: order.created,
  invoice.issued, shipment.status-changed.
- The producing service declares its topics as NewTopic beans with partitions and replicas
  from configuration. Broker auto topic creation is off; consumers never create topics.
- The message key is the aggregate id (orderId, invoiceId), so every event of one aggregate
  goes to the same partition and keeps its order.
- Payloads are JSON. Every event is a record with eventId (UUID), occurredAt and the business
  fields. Evolve events by adding fields only; never rename or remove a field consumers read.
- Producers send no Java type headers; each consumer deserializes into its own event record.
- The consumer group id is spring.application.name, so each service gets every event once and
  instances of the same service share the partitions.
- Events are published only after the database transaction commits
  (@TransactionalEventListener AFTER_COMMIT).
- Consumers are idempotent: Kafka delivers at least once, so they store the processed eventId
  and skip duplicates.
- Failed records are retried with backoff by DefaultErrorHandler, then sent to the dead letter
  topic <topic>.DLT. Deserialization and validation errors are not retried.

## Persistence
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

## Object storage (MinIO)
- Files go to MinIO. The database stores only the bucket and object key, never the file bytes.
- Bucket names come from configuration; the service creates its bucket at startup if missing.
- Object keys follow <resource>/<id>/<file-name>, like invoices/{invoiceId}/invoice-{number}.pdf.
- Clients download files through presigned URLs with a configured expiry, never public buckets.
- All MinIO calls go through the storage package of the service.

## Reports (JasperReports)
- Templates (.jrxml) live in src/main/resources/reports/. They are compiled once at startup and
  cached, never per request.
- The service builds the report data and passes it as parameters and a
  JRBeanCollectionDataSource of records; templates never run SQL.
- Generated PDFs are stored in MinIO, and the owning entity keeps the object key.

## Webhooks
- Incoming webhooks arrive at POST /webhooks/<source>. Verify the HMAC-SHA256 signature header
  with the shared secret before parsing the body; a missing or invalid signature returns 401.
- Answer with 2xx fast: validate, publish the event to Kafka and process it asynchronously.
- Deduplicate deliveries by the sender's delivery id.
- Outgoing webhooks are signed the same way, retried with backoff, and every delivery attempt
  is recorded.

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
  `dispatchShipment`.
- One word per concept across the code base: `find` for queries, `create` for new records,
  `get` for accessors. Do not mix `create`, `add` and `new` for the same action.
- Booleans read as questions: `isPaid`, `hasStock`, `canShip`.
- Collections are plural: `orders`, `orderItems`, `shipments`.
- Constants in UPPER_SNAKE_CASE, with the unit when there is one: `MAX_DELIVERY_ATTEMPTS`,
  `PRESIGNED_URL_EXPIRY_MINUTES`.
- No type or noise words in names: `orders`, not `orderList` or `theOrders`.
- Test methods describe the behavior they check: `skipsDuplicateOrderCreatedEvent`,
  `rejectsWebhookWithInvalidSignature`.

General:
- Small methods that do one thing; extract a well-named method instead of adding a comment.
- No magic numbers or strings: use named constants or configuration properties.
- Prefer early returns to deep nesting.
- Comments explain why, never what. No commented-out code.

## Testing
- Layers: unit (JUnit 5, Mockito), repository (@DataJpaTest with Testcontainers PostgreSQL),
  web (@WebMvcTest), integration (@SpringBootTest with Testcontainers Kafka, PostgreSQL and MinIO).
- Producers and consumers are tested against a real Kafka container. Wait with Awaitility,
  never Thread.sleep.
- Every consumer has a test for a duplicate event (one effect only) and for dead letter routing.
- Webhook tests cover valid, invalid and missing signatures.
- Tests never call real external systems; CI must run without secrets.

## Rules
- Everything in English: packages, directories, files, classes, fields, tables, topics,
  endpoints, configuration, comments and docs.
- No "latest" versions: pin every Docker image tag, dependency and tool.
- Never read or write .env, keys or credential files. Database passwords, MinIO credentials and
  webhook secrets come from environment variables; defaults exist only for the local compose stack.
- Indent Java and XML with tabs, like the files generated by Spring Initializr.
