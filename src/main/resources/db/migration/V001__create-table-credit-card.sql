create table public.credit_card (
	id uuid not null,
	created_at timestamp with time zone,
	customer_id uuid not null,
	last_numbers varchar(4) not null,
	brand varchar(50) not null,
	exp_month integer not null,
	exp_year integer not null,
	gateway_code varchar(255) not null,
	primary key (id)
);
