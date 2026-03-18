create table author (
    id uuid primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    birth_date date,
    death_date date,
    biography text,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_author_life_dates check (death_date is null or birth_date is null or death_date >= birth_date)
);

create index idx_author_name_search on author (lower(last_name), lower(first_name));
create unique index uq_author_identity on author (first_name, last_name, extract(year from birth_date));

create table publisher (
    id uuid primary key,
    name varchar(150) not null unique,
    country_code char(2),
    website varchar(255),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_publisher_country_code check (country_code is null or country_code ~ '^[A-Z]{2}$')
);

create table category (
    id uuid primary key,
    name varchar(100) not null unique,
    description varchar(255),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table book (
    id uuid primary key,
    isbn_13 char(13) not null unique,
    title varchar(200) not null,
    subtitle varchar(200),
    publisher_id uuid references publisher (id) on delete set null,
    publication_date date,
    language regconfig not null default 'english'::regconfig,
    language_code varchar(10) not null default 'en',
    page_count integer,
    summary text,
    search_vector tsvector generated always as (
        setweight(to_tsvector(language, coalesce(title, '')), 'A') ||
        setweight(to_tsvector(language, coalesce(subtitle, '')), 'B') ||
        setweight(to_tsvector(language, coalesce(summary, '')), 'C')
    ) stored,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_book_isbn_13_format check (isbn_13 ~ '^[0-9]{13}$'),
    constraint ck_book_language_code_format check (language_code ~ '^[a-z]{2,3}(-[A-Z]{2})?$'),
    constraint ck_book_page_count check (page_count is null or page_count > 0)
);

create index idx_book_title on book (title);
create index idx_book_title_search on book (lower(title));
create index idx_book_publisher on book (publisher_id);
create index idx_book_search_vector on book using gin (search_vector);

create table book_author (
    book_id uuid not null references book (id) on delete cascade,
    author_id uuid not null references author (id) on delete restrict,
    author_order smallint not null default 1,
    role varchar(50) not null default 'AUTHOR',
    created_at timestamptz not null default current_timestamp,
    primary key (book_id, author_id),
    constraint ck_book_author_role check (role in ('AUTHOR', 'CO_AUTHOR', 'EDITOR', 'TRANSLATOR', 'ILLUSTRATOR', 'CONTRIBUTOR')),
    constraint ck_book_author_order check (author_order > 0),
    constraint uq_book_author_order unique (book_id, author_order)
);

create index idx_book_author_author on book_author (author_id);

create table book_category (
    book_id uuid not null references book (id) on delete cascade,
    category_id uuid not null references category (id) on delete restrict,
    created_at timestamptz not null default current_timestamp,
    primary key (book_id, category_id)
);

create index idx_book_category_category on book_category (category_id);

create table inventory_copy (
    id uuid primary key,
    book_id uuid not null references book (id) on delete restrict,
    inventory_code varchar(30) not null unique,
    acquired_on date not null default current_date,
    shelf_location varchar(50) not null,
    status varchar(20) not null default 'AVAILABLE',
    condition_note varchar(255),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_inventory_copy_status check (status in ('AVAILABLE', 'LOANED', 'RESERVED', 'LOST', 'MAINTENANCE'))
);

create index idx_inventory_copy_book_status on inventory_copy (book_id, status);

create table library_member (
    id uuid primary key,
    membership_number varchar(30) not null unique,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(255) not null,
    phone varchar(30),
    joined_at timestamptz not null default current_timestamp,
    status varchar(20) not null default 'ACTIVE',
    updated_at timestamptz not null default current_timestamp,
    constraint ck_library_member_status check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

create index idx_library_member_name_search on library_member (lower(last_name), lower(first_name));
create unique index uq_library_member_email_lower on library_member (lower(email));

create table loan (
    id uuid primary key,
    copy_id uuid not null references inventory_copy (id) on delete restrict,
    member_id uuid not null references library_member (id) on delete restrict,
    loaned_at timestamptz not null default current_timestamp,
    due_at timestamptz not null,
    returned_at timestamptz,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_loan_due_after_loan check (due_at > loaned_at),
    constraint ck_loan_returned_after_loan check (returned_at is null or returned_at >= loaned_at)
);

create index idx_loan_member_returned_at on loan (member_id, returned_at);
create index idx_loan_copy_returned_at on loan (copy_id, returned_at);
create index idx_loan_due_at on loan (due_at);
create unique index uq_loan_open_copy on loan (copy_id) where returned_at is null;

create table fine (
    id uuid primary key,
    loan_id uuid not null references loan (id) on delete restrict,
    amount numeric(10, 2) not null,
    reason varchar(20) not null default 'OVERDUE',
    status varchar(20) not null default 'PENDING',
    issued_at timestamptz not null default current_timestamp,
    paid_at timestamptz,
    notes varchar(255),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint ck_fine_amount check (amount > 0),
    constraint ck_fine_reason check (reason in ('OVERDUE', 'DAMAGE', 'LOST', 'OTHER')),
    constraint ck_fine_status check (status in ('PENDING', 'PAID', 'WAIVED')),
    constraint ck_fine_paid_at check (paid_at is null or status = 'PAID')
);

create index idx_fine_loan on fine (loan_id);
create index idx_fine_loan_status on fine (loan_id, status);

create table reservation (
    id uuid primary key,
    copy_id uuid not null references inventory_copy (id) on delete restrict,
    member_id uuid not null references library_member (id) on delete restrict,
    created_at timestamptz not null default current_timestamp,
    reserved_at timestamptz not null default current_timestamp,
    expires_at timestamptz not null,
    fulfilled_at timestamptz,
    status varchar(20) not null default 'ACTIVE',
    updated_at timestamptz not null default current_timestamp,
    constraint ck_reservation_expiry check (expires_at > reserved_at),
    constraint ck_reservation_fulfilled_after_reserved check (fulfilled_at is null or fulfilled_at >= reserved_at),
    constraint ck_reservation_status check (status in ('ACTIVE', 'FULFILLED', 'CANCELLED', 'EXPIRED'))
);

create index idx_reservation_member_status on reservation (member_id, status);
create index idx_reservation_copy_status on reservation (copy_id, status);
create unique index uq_reservation_open_copy_member on reservation (copy_id, member_id) where status = 'ACTIVE';
