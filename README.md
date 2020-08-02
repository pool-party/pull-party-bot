# Pull party bot

[![Travis CI](https://img.shields.io/travis/com/pool-party/pull-party-bot?logo=travis)](https://travis-ci.com/pool-party/pull-party-bot)
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/pool-party/pull-party-bot?logo=codefactor)](https://www.codefactor.io/repository/github/pool-party/pull-party-bot)
[![Kotlin Lang](https://img.shields.io/github/languages/top/pool-party/pull-party-bot?logo=kotlin)](https://kotlinlang.org)
[![Donations](https://img.shields.io/badge/buy_me_a-coffee-orange?logo=buy-me-a-coffee)](https://www.buymeacoffee.com/poolparty)

**Pull Party Telegram Bot** helps you pulling people up for a party!

## Usage guide

Add [`@PullPartyBot`](https://t.me/PullPartyBot) to the chat to be able to create custom parties and manage them via following commands:

+ `/create partyName user1 user2...` - create party with mentioned users

+ `/update partyName user2 user3...` - update an existing party

+ `/delete partyName1 partyName2...` - forget the parties like it never happened

Use these commands to mention members of needed parties, see the information and manage appereance:

+ `/start` - start the conversation and see welcoming message

+ `/help` - ask for a short usage guide

+ `/list` - show all the existing parties

+ `/rude on/off` - enable RUDE _(Caps Lock)_ mode

+ `/party partyName1 partyName2...` - mention users according to given parties

You can also tag the party members directly in your message with `@partyName` syntax.

## Tools and libraries

+ [Kotlin](https://kotlinlang.org) programming language

+ [Gradle](https://gradle.org) building system

    > To setup required version of gradle and dependencies run `gradlew` script (according to your system) with `build` target.

+ [Telegram Bot Api kotlin library](https://github.com/elbekD/kt-telegram-bot)

+ [Heroku deployment service](https://www.heroku.com)

+ [JetBrains Exposed SQL DSL](https://github.com/JetBrains/Exposed)

+ [PostgreSQL database](https://www.postgresql.org)

## Launching

In order to be able to launch the bot server locally you have to compile sources with gradle and set the following environment variables:

+ `TELEGRAM_TOKEN` - the one you get from [`BotFather`](https://t.me/BotFather)

+ `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD` - all provided by heroku on database deployment

+ `IS_LONGPOLL=true` for using long polling instead of web hooking (common usage case is local launch and debugging)

[![Pull Party Bot](info/logo-white-no_boarders.png)](https://t.me/PullPartyBot)
<p align="center">© 2020 Pool Party Corp.</p>
