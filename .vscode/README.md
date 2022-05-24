# CSpell Extension Configuration

## Setup for testing changes locally before submit

1. Install VS Code.
2. Install the Code Spell Checker by Street Side Software.
This will allow testing of the changes locally with minimal effort. Literally, you'll just need to load a file and anything it flags will be underlined in blue. The configuration definition can be found at <https://cspell.org/configuration/>
3. The main spellcheck document can be found [here](https://github.com/Azure/azure-sdk-tools/blob/main/doc/common/spellcheck.md) in the [azure-sdk-tools](https://github.com/Azure/azure-sdk-tools) repository.

## Words and word boundaries

"-" and "." are word boundaries. For example: An artifactId, azure-core-amqp, will verify azure, core and amqp.AMQP, not being in the dictionary would get flagged and that's the only word that would be required to add to the list.

## Case insensitivity

The ignored words are case insensitive. AMQP, amqp, Amqp etc would all be picked up from "amqp"

## Regular expressions

There are times when a single word really isn't enough. For example: Instead of adding each unique word from each and every groupId and artifactId from every Azure SDK library and external dependency, it's cleaner to regex away the line that contains the artifactId. Always match

### Multi-line regular expression

Multi-line regular expressions should be used with extreme caution as it's far too easy to accidentally regex away too much. Any multi-line regular expression should have hard tags that delineate a clear beginning and a clear ending with the multi-line regular expression in between these tags. These tags should be as close to what you want to regex away as possible. For example: A `<properties>` tag may contain a number other tags that you with to regex away but they can also contain things, like a `<legal>`, which contains a string that needs to get checked. For any set of tags that may contain sub-tags, only the sub-tags should be regex'd unless you can guarantee that the all of possible sub-tags will never need to be scanned. `<compilerArguments>` would be an example where all of the sub-tags would be skipped.

**If a multi-line regular expression is needed then a member of the Azure SDK Engineering System Team should be added to the code review.**

### What type of things should regular expressions be used for?

1. Anything that would contain a groupId
2. Anything that would contain an artifactId
3. Anything that would contain a version. There are two reasons for this. The first reason because of version tags in pom files. Each version tag should have a version update tag which contains the group and artifact. The second reason is that while our Azure SDK for Java versions will successfully pass spelling checks, it's not a guarantee that external dependency version will. For ex. 2.0-groovy-3.0. Maven accepts these type of version strings.
4. Anything that contains class names. Classes often contain abbreviations or words that won't pass spellcheck. For example: AMQP.
5. Anything that contain lists 1-4. For example `<excludePackageNames>` is a multi-line regular expression because it contains a list of packages to exclude.

## Do nots

1. Do not add a name, initials or aliases to the skip list. Individual names, initials or aliases does not belong in anything that ships.
2. Do not exclude a filetype or directory that contains artifacts that ship.
3. Do not write a multi-line regular expression for a tag that contains other tags. Unless you can guarantee each sub-tag won't contain items that need to be scanned.
