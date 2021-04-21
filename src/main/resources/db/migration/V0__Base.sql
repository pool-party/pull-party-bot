create table chats (
    id      bigserial not null,
    chat_id bigint primary key,
    is_rude boolean   not null default false
);

create table parties (
    id       serial primary key,
    name     varchar(50) not null,
    chat_id  bigint      not null,
    users    text        not null,
    last_use timestamp   not null default (current_timestamp),

    constraint fk_parties_chat_id_chat_id
        foreign key (chat_id)
        references chats(chat_id)
);

create index parties_name_chat_id on parties (name, chat_id);

alter table parties add constraint parties_chat_id_name_unique
    unique (chat_id, name);
