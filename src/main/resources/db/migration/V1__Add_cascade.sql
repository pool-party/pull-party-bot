alter table parties drop constraint fk_parties_chat_id_chat_id;

alter table parties add constraint fk_parties_chat_id_chat_id
    foreign key (chat_id)
    references chats(chat_id)
    on update cascade
    on delete cascade;
