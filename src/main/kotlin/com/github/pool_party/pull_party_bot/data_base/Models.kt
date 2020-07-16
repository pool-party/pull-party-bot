package com.github.pool_party.pull_party_bot.data_base

import org.jetbrains.exposed.sql.*


// object Users : Table() {
//     val id = varchar("id", 10) // Column<String>
//     val name = varchar("name", 50) // Column<String>
//     val cityId = (integer("group_id") references Groups.id).nullable() // Column<Int?>

//     override val primaryKey = PrimaryKey(id, name = "Telegram_User_ID")
// }

object Groups : Table() {

    val id = integer("id").autoIncrement()
    val name = varchar("name", 50) 
    val chatId =  (integer("char_id") references Chats.id).nullable()

    override val primaryKey = PrimaryKey(id, name = "Telegram_Group_ID")

}

object Chats : Table () {
    val id = integer("id")
    val name = varchar("name", 50)
    var groups : Groups? = null
        
    override val primaryKey = PrimaryKey(id, name = "Telegram_Chat_ID")

}