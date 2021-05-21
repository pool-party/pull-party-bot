# Pull party bot

[![Build Status](https://github.com/pool-party/pull-party-bot/workflows/test/badge.svg?branch=master)](https://github.com/pool-party/pull-party-bot/actions?query=workflow%3A%22test%22+branch%3Amaster)
[![Kotlin Lang](https://img.shields.io/github/languages/top/pool-party/pull-party-bot?logo=kotlin)](https://kotlinlang.org)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/pool-party/pull-party-bot?logo=codefactor)](https://www.codefactor.io/repository/github/pool-party/pull-party-bot)
[![Donations](https://img.shields.io/badge/buy_me_a-coffee-orange?logo=buy-me-a-coffee)](https://www.buymeacoffee.com/poolparty)
[![Pull Party Bot](https://img.shields.io/badge/telegram-Pull_Party_Bot-blue?logo=Telegram)](https://t.me/PullPartyBot/)

<div align="center">
    <img src="assets/logo-white-no_boarders.png" width=50% alt="logo">
</div>
<p align="center">© 2020 Pool Party Corp. All rights reserved.</p>

**Pull Party Telegram Bot** helps you pulling people up for a party!

## Usage guide

Add [`@PullPartyBot`](https://t.me/PullPartyBot) to the chat to be able to create custom parties and manage them via following commands:

> all commands are not case-sensitive to support RUDE mode \
> usernames are char sequences of letters, digits and underscores of 5-32 length

-   `/create partyName user1 user2...` - create new party with mentioned users

    > Users within the party are not repeating \
    >  Party should consist of at least one user \
    >  You can enter users with or without `@` symbol \
    >  `@admins` is a reserved party and already exists \
    >  Party name should be no longer than 50 characters \
    >  `@`, `!`, `,`, `,`, `?`, `:`, `;`, `(`, `)` symbols and trailing `-` are not allowed in the party name

-   `/alias  <alias-name party-name>` - create an alias for the same party

    > Alias is a reference on the same party, not just a copy \
    > Changing single alias instance applies to the party and seen in the other aliases \
    > If you want to delete a party with all aliases, delete a single alias and press the suggesting button

-   `/change partyName user1 user2...` - change an existing party

-   `/add partyName user1 user2...` - extend party with provided users

-   `/remove partyName user1 user2...` - delete mentioned users from the given party

    > Follow all `/create` method's rules \
    >  You can't change `@admins` party

-   `/delete partyName1 partyName2...` - delete the parties you provided

    > Only admins have access to `/delete` \
    >  @admins is a reserved party and can't be deleted

-   `/clear` - delete all parties of the chat

    > Follows all `/delete` method's rules

Use these commands to mention members of needed parties, see the information and manage appearance:

-   `@partyName` - syntax for tagging parties implicitly right inside your messages

    > To enable this function you need to grant admins rights to the bot \
    > You will see the suggesting button in case of misspelling

-   `/party partyName1 partyName2...` - tag the members of existing parties

    > If you mention multiple parties - their members will be gathered in a single message and will have no repeats

-   `/list entry1? entry2?...` - show the parties of the chat and their members

    > Returns all parties on empty input \
    > Entries are either users or party names: \
    >     - on the given user shows all parties he is part of \
    >     - on the given party shows its members \
    > List doesn't contain repetitive values \
    > Suggests deleting least recently used parties

-   `/start` - start the conversation and see welcoming message

-   `/help command?` - ask for a usage guide

    > Provides general guide on empty input \
    >  Shows command usage guide while argument is given

-   `/rude on/off` - switch RUDE _(Caps Lock)_ mode

    > Set `off` by default \
    >  Takes only `on` and `off` as correct arguments \
    >  Doesn't affect error messages

-   `/feedback message` - leave a feedback for developers in a free-form

## Tools and libraries

-   [Kotlin](https://kotlinlang.org) programming language

-   [Gradle](https://gradle.org) building system

    > To set up required version of gradle and dependencies run `gradlew` script (according to your system) with `build` target.

-   [Heroku deployment service](https://www.heroku.com)

-   [JetBrains Exposed SQL DSL](https://github.com/JetBrains/Exposed)

-   [PostgreSQL database](https://www.postgresql.org)

-   [Flyway](https://flywaydb.org/) migration tool

-   [Emoji commit messages guide](https://gitmoji.dev/)

-   [Telegram Bot Api kotlin library](https://github.com/elbekD/kt-telegram-bot)

-   [String Similarity Library](https://github.com/tdebatty/java-string-similarity)

-   [Type Safe Configuration API Library](https://github.com/npryce/konfig)

-   [Lightweight logging framework](https://github.com/MicroUtils/kotlin-logging)

-   [MockK mocking library](https://github.com/mockk/mockk)

-   [Test Containers](https://testcontainers.org/)

## Launching

In order to be able to launch the bot server locally you have to compile sources with gradle and set the following environment variables
or provide corresponding values in [`defaults.properties`](src/main/resources/defaults.properties):

-   `TELEGRAM_TOKEN` - the one you get from [`BotFather`](https://t.me/BotFather)

-   `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD` - all provided by heroku on database deployment

-   `LONGPOLL=true` for using long polling instead of web hooking (common usage case is local launch and debugging)

### Database migrations

You can simply apply all the migrations to your database via flyway tool, in particular flyway gradle plugin:

```sh
./gradlew -Pflyway.url=<jdbc-url> -Pflyway.user=<username> -Pflyway.password=<password> flywayMigrate
```

If you have initialized your database before, you can add `-Pflyway.baselineOnMigrate=true` key to process this case.
