package com.github.pool_party.pull_party_bot.commands.handlers.testcontainers

import com.github.pool_party.pull_party_bot.commands.messages.ON_ARGUMENT_LIST_SUCCESS
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_EMPTY
import com.github.pool_party.pull_party_bot.commands.messages.ON_LIST_SUCCESS
import kotlin.test.Test

internal class ListingTest : AbstractTestContainerTest() {

    private val partyName = "party_name"
    private val aliasName = "alias_name"
    private val members = "@first_member @second_member @third_member"
    private val listMembers = members.filter { it != '@' }
    private val listOutput = "```$ON_ARGUMENT_LIST_SUCCESS\n- $aliasName: $listMembers```"

    @Test
    fun `list command test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/list $aliasName"
        +listOutput
    }

    @Test
    fun `list command with deleted party test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $partyName"
        -"/list $aliasName"
        +listOutput
    }

    @Test
    fun `list command with deleted full party test`() {
        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/delete $aliasName"
        -"/delete $partyName"
        -"/list"
        +ON_LIST_EMPTY
    }

    @Test
    fun `multiple parties list test`() {
        val secondPartyName = "second_party"
        val secondPartyMembers = "members"

        -"/create $partyName $members"
        -"/alias $aliasName $partyName"
        -"/create $secondPartyName $secondPartyMembers"
        -"/list"
        +"""
            ```$ON_LIST_SUCCESS
            - $listMembers
              ├── ${aliasName.toLowerCase()}
              └── ${partyName.toLowerCase()}
            - $secondPartyMembers
              └── $secondPartyName```
        """.trimIndent()
    }

    @Test
    fun `a horses herd list test`() {
        val partyNames = mutableListOf<String>()

        -"/create $partyName $members"

        for (i in 0..100) {
            val currentPartyName = "${"cheval".repeat(7)}-$i"
            partyNames.add(currentPartyName)

            -"/alias $currentPartyName $partyName"
        }

        -"/list"

        +(
            """
                ```$ON_LIST_SUCCESS
                - $listMembers
            """.trimIndent() +
                "\n${partyNames.asSequence().sorted().map { "  ├── $it" }.take(77).joinToString("\n")}```"
            )
        +(
            "```${partyNames.asSequence().sorted().map { "  ├── $it" }.drop(77).joinToString("\n")}\n" +
                "  └── $partyName```"
            )
    }
}
