package com.github.pool_party.pull_party_bot.database.transaction

import com.github.pool_party.pull_party_bot.database.ActivatedPacks
import com.github.pool_party.pull_party_bot.database.ActivatedPacks.aliasPack
import com.github.pool_party.pull_party_bot.database.AliasPack
import com.github.pool_party.pull_party_bot.database.AliasPacks
import com.github.pool_party.pull_party_bot.database.Chat
import com.github.pool_party.pull_party_bot.database.StickerAlias
import com.github.pool_party.pull_party_bot.database.StickerAliases
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun addAliasesTransaction(aliases: Map<String, String>, packName: String): Boolean =
    transaction {
        if (!AliasPack.find { AliasPacks.name eq packName }.empty()) {
            return@transaction false
        }
        val newPack = AliasPack.new { name = packName }
        aliases.forEach { (newStickerId, newAlias) ->
            StickerAlias.new {
                stickerId = newStickerId
                alias = newAlias
                pack = newPack
            }
        }
        true
    }

fun getPackNamesTransaction(): List<String> =
    transaction {
        AliasPack.all().map { it.name }.toList()
    }

fun getActivatedPacksTransaction(id: Long): List<AliasPack> =
    transaction {
        Chat.findById(id)?.aliasPacks?.toList() ?: emptyList()
    }

fun getPackNameTransaction(stickerAlias: StickerAlias) = transaction { stickerAlias.pack.name }

fun getStickerIdTransaction(stickerAlias: StickerAlias) = transaction { stickerAlias.stickerId }

fun getAliasesMapTransaction(pack: AliasPack) =
    transaction {
        pack.stickerAliases.associateBy { it.alias }.asSequence()
    }

fun packExistsTransaction(packName: String): Boolean =
    transaction {
        val pack = AliasPack.find { AliasPacks.name eq packName }.firstOrNull() ?: return@transaction false
        !StickerAlias.find { StickerAliases.packName eq pack.id }.empty()
    }

fun aliasExistsTransaction(stickerId: String, packName: String): Boolean =
    transaction {
        val pack = AliasPack.find { AliasPacks.name eq packName }.firstOrNull() ?: return@transaction false

        !StickerAlias
            .find { StickerAliases.packName.eq(pack.id) and StickerAliases.stickerId.eq(stickerId) }
            .empty()
    }

fun activatePackTransaction(newChatId: Long, packName: String) =
    transaction {
        val pack = AliasPack.find { AliasPacks.name eq packName }.firstOrNull() ?: return@transaction false

        ActivatedPacks.insert {
            it[chatId] = Chat.findById(newChatId)!!.id
            it[aliasPack] = pack.id
        }
        true
    }

fun deactivatePackTransaction(chatId: Long, packName: String) =
    transaction {
        val pack = AliasPack.find { AliasPacks.name eq packName }.first()
        ActivatedPacks.deleteWhere { ActivatedPacks.chatId.eq(chatId) and aliasPack.eq(pack.id) }
    }

fun removePackTransaction(packName: String): Boolean =
    transaction {
        val pack = AliasPack.find { AliasPacks.name eq packName }
        if (pack.empty()) {
            return@transaction false
        }

        pack.forEach {
            it.stickerAliases.forEach { it.delete() }
            ActivatedPacks.deleteWhere { aliasPack eq it.id }
            it.delete()
        }
        true
    }
