alter table hotels add column location varchar(255) not null default 'Pending location';

create table drivers (
    available bit not null,
    id bigint not null auto_increment,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    dni varchar(255) not null,
    email varchar(255) not null,
    license_number varchar(255) not null,
    phone varchar(255),
    primary key (id)
) engine=InnoDB;

create table buses (
    available_seats integer not null,
    total_seats integer not null,
    driver_id bigint not null,
    id bigint not null auto_increment,
    plate_number varchar(255) not null,
    primary key (id)
) engine=InnoDB;

alter table drivers add constraint UK_drivers_dni unique (dni);
alter table drivers add constraint UK_drivers_email unique (email);
alter table drivers add constraint UK_drivers_license_number unique (license_number);
alter table buses add constraint UK_buses_plate_number unique (plate_number);
alter table buses add constraint FK_buses_driver foreign key (driver_id) references drivers (id);
