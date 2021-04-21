alter table parties rename to aliases;

create table parties (
    id    serial primary key,
    users text not null
);

insert into parties (users) select users from aliases;

alter table aliases drop column users;

alter table aliases add column party_id bigint not null;

alter table aliases add constraint fk_aliases_party_id_chat_id
    foreign key (party_id)
    references parties(id)
    on update cascade
    on delete cascade;
