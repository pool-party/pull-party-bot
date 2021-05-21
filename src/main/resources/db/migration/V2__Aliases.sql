alter table parties rename to aliases;

create table parties (
    id    serial primary key,
    first_alias_id integer not null,
    users text not null
);

insert into parties (users, first_alias_id) select users, id from aliases;

alter table aliases drop column users;

alter table aliases add column party_id bigint;

update aliases A
set party_id = (
    select P.id
    from parties P
    where first_alias_id = A.id
);

alter table aliases alter column party_id set not null;

alter table aliases add constraint fk_aliases_party_id_chat_id
    foreign key (party_id)
    references parties(id)
    on update cascade
    on delete cascade;

alter table parties drop column first_alias_id;
