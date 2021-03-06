# Changelog

## [1.2.0] - 21.05.2021

### Added

- Usage
  - Add `/alias` command to treat party names to parties as many-to-one relation
    - Deleting full party node suggestion
  - `/list` command pretty printing
- Developing
  - Database migrations

### Fixed

- Supergroup migration is handled properly
- `/list` command used to crash on too many parties in a group
- Party pulling suggestions are now unique
- `@admins` party is added to a `/list` output with a special mark

## [1.1.0.2] - 04.03.2021

### Fixed

-   Empty party creation is prohibited
-   Attach a reply message to a feedback command
-   Fix `null` chat name with providing a feedback in a developers' chat
-   Moving from Travis CI to the GitHub Actions

## [1.1.0.1] - 01.01.2021

### Fixed

-   Hotfixed deploy process

## [1.1.0] - 31.12.2020

### Added

-   `/list` command extension:
    -   shows all the parties with given user
    -   checks users within the group without tagging them
-   `/add` and `/remove` commands for more convenient party changing
-   `/feedback` command
-   Smart party suggestions:
    -   misspelled parties to pull via Jaro Winkler algorithm
    -   smart suggestions to remove stale abandoned parties
-   Subtle developing improvements
    -   Adequate configuration
    -   Databases indices
    -   Caching parties and chats
    -   Logging support
    -   Commands refactoring
    -   Miscellaneous tests

### Fixed

-   Stop tagging itself while pulling up `admins` party
-   Face control rejection on space sequence
-   Database insertion failures

## [1.0.2.1] - 17.10.2020

### Fixed

-   Heroku Continious deployment build fixed

## [1.0.2] - 16.10.2020

### Fixed

-   Implicit party pulling with media attached

## [1.0.1] - 16.09.2020

### Added

-   Bot replies on implicit tags
-   You can write trailing punctuation symbols when tagging (they will be ignored). \
    And corresponding party name validation rules were added
-   Extended help message for each command (e.g. `/help party`). \
    Make error message suggestions concrete respectively

### Changed

-   `/list` command lists members as well
-   If you tag multiple parties - their members will be gathered in a single message and will have no repeats

### Fixed

-   No party with no people, when all members had fc/dc problems
-   Implicit party calls are not case-sensitive now (`@pArTy` will call `party` members)
-   You unable to create a party called as the only user in the party (e.g. `/create PullPartyBot PullPartyBot`)

## [1.0.0] - 16.08.2020

### Added

-   Creating, modifying, tagging, and deleting parties
-   Implicit party pulling
-   Rude mode

[1.2.0]: https://github.com/pool-party/pull-party-bot/compare/v1.1.0.2...v1.2.0
[1.1.0.2]: https://github.com/pool-party/pull-party-bot/compare/v1.1.0.1...v1.1.0.2
[1.1.0.1]: https://github.com/pool-party/pull-party-bot/compare/v1.1.0...v1.1.0.1
[1.1.0]: https://github.com/pool-party/pull-party-bot/compare/v1.0.2.1...v1.1.0
[1.0.2.1]: https://github.com/pool-party/pull-party-bot/compare/v1.0.2...v1.0.2.1
[1.0.2]: https://github.com/pool-party/pull-party-bot/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/pool-party/pull-party-bot/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/pool-party/pull-party-bot/releases/tag/v1.0.0
