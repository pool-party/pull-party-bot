package com.github.pool_party.pull_party_bot.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable

object Parties : IntIdTable() {
    val name = varchar("name", 50)
    val chatId = reference("chat_id", Chats)
    val users = text("users")
}

object Chats : LongIdTable() {
    override val id = long("chat_id").entityId()
    val isRude = bool("is_rude").default(false)
}

object StickerAliases : IntIdTable() {
    val stickerId = varchar("sticker_id", 255)
    val alias = varchar("alias", 50)
    val packName = reference("pack_id", AliasPacks)
}

object AliasPacks : IntIdTable() {
    val name = varchar("pack_name", 50)
}

object ActivatedPacks : IntIdTable() {
    val chatId = reference("chat_id", Chats)
    val aliasPack = reference("alias_pack_id", AliasPacks)
}
