create table public.invoice (
    id uuid not null,
    order_id varchar(255) not null,
    customer_id uuid not null,
    issued_at timestamp with time zone,
    paid_at timestamp with time zone,
    canceled_at timestamp with time zone,
    expires_at timestamp with time zone,
    total_amount numeric(19, 2) not null,
    status varchar(20) not null,
    cancel_reason varchar(255),
    payer_full_name varchar(255),
    payer_document varchar(255),
    payer_phone varchar(255),
    payer_email varchar(255),
    payer_address_street varchar(255),
    payer_address_number varchar(255),
    payer_address_complement varchar(255),
    payer_address_neighborhood varchar(255),
    payer_address_city varchar(255),
    payer_address_state varchar(255),
    payer_address_zip_code varchar(255),
    payment_settings_id uuid,
    created_by_user_id uuid,
    created_at timestamp with time zone,
    last_modified_by_user_id uuid,
    last_modified_date timestamp with time zone,
    version bigint not null default 0,
    primary key (id)
);

create table public.payment_settings (
    id uuid not null,
    credit_card_id uuid,
    gateway_code varchar(255),
    method varchar(50) not null,
    invoice_id uuid,
    primary key (id),
    constraint fk_payment_settings_invoice foreign key (invoice_id) references public.invoice(id)
);

alter table public.invoice
    add constraint fk_invoice_payment_settings foreign key (payment_settings_id) references public.payment_settings(id);

create table public.invoice_line_item (
    invoice_id uuid not null,
    number integer not null,
    name varchar(255) not null,
    amount numeric(19, 2) not null,
    constraint fk_line_item_invoice foreign key (invoice_id) references public.invoice(id)
);