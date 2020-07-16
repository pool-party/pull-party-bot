package com.github.pool_party.pull_party_bot.data_base

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = varchar("id", 10) // Column<String>
    val name = varchar("name", length = 50) // Column<String>
    val cityId = (integer("city_id") references Cities.id).nullable() // Column<Int?>

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID") // name is optional here
}

object Group : Table() {
    val id = varchar("id", )
}
