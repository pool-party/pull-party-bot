# Pull party bot

<p align="center"><img src="info/logo.jpg" width=256></p>

<p align="center">
    <a href="https://kotlinlang.org">
        <img src="https://img.shields.io/github/languages/top/pool-party/pull-party-bot?logo=kotlin">
    </a>
    <a href="https://travis-ci.com/pool-party/pull-party-bot">
        <img src="https://img.shields.io/travis/com/pool-party/pull-party-bot?logo=travis">
    </a>
    <a href="https://www.codefactor.io/repository/github/pool-party/pull-party-bot">
        <img src="https://img.shields.io/codefactor/grade/github/pool-party/pull-party-bot?logo=codefactor">
    </a>
    <a href="https://www.buymeacoffee.com/poolparty">
        <img src="https://img.shields.io/badge/buy_me_a-coffee-orange?logo=buy-me-a-coffee">
    </a>
</p>

**Pull party Telegram bot** helps you pulling people up for a party.

## Usage

You need to add [`@PullPartyBot`](https://t.me/PullPartyBot) to a group and then
you'll be able to create custom parties and manage them via follwwing commands:

+ `/create partyName user1 user2` - create party with mentioned users

+ `/update partyName user2 user3` - update an existing party

+ `/delete partyName` - forget the party like it never happened

+ `/list` - show all the existing parties

It is possible to pull your party with a speacial command `/party partyName` or
just mentioning it directly in your message `@partyName`.

## Tools and libraries

+ [Kotlin](https://kotlinlang.org) programming language

+ [Gradle](https://gradle.org) building system

    To setup required version of gradle and dependencies run `gradlew` script
    (according to your system) with `build` target.

+ [Telegram Bot Api library](https://github.com/elbekD/kt-telegram-bot)

+ [JetBrains Exposed SQL DSL](https://github.com/JetBrains/Exposed)

+ [PostgreSQL database](https://www.postgresql.org)

## Launching

In order to be able to launch the bot server locally you have to compile sources with
gradle and set the following environment variables:

+ `TELEGRAM_TOKEN` - the one you get from BotFather

+ `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`

+ `IS_LONGPOLL=true` for using long polling instead of web hooking
