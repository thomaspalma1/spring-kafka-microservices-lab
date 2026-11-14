create database octsocproducts;
\c octsocproducts

create table products (
	id serial not null primary key,
	name varchar(100) not null,
	unit_price decimal(16,2) not null
);


create database octsoccustomers;
\c octsoccustomers

create table customers (
	id serial not null primary key,
	name varchar(150) not null,
	cpf char(11) not null, -- Brazilian personal ID number
	street varchar(100),
	street_number varchar(10),
	neighborhood varchar(100),
	email varchar(150),
	phone varchar(20)
);


create database octsocorders;
\c octsocorders

create table orders (
	id serial not null primary key,
	customer_id bigint not null,
	order_date timestamp not null default now(),
	payment_key text,
	notes text,
	status varchar(20) check (
		status in ('PLACED', 'PAID', 'INVOICED', 'SHIPPED', 'PAYMENT_ERROR', 'PREPARING_SHIPMENT')
	),
	total decimal(16,2) not null,
	tracking_code varchar(255),
	invoice_url text
);

create table order_items (
	id serial not null primary key,
	order_id bigint not null references orders (id),
	product_id bigint not null,
	quantity int not null,
	unit_price decimal(16,2) not null
);
