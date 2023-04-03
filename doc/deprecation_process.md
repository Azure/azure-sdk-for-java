This page can be linked using: [aka.ms/azsdk/java/deprecation-process](https://aka.ms/azsdk/java/deprecation-process)

# Overview

This page describes how to mark a package deprecated on Maven. You likely need to read this if you are a package owner
and need to explain to your customers they shouldn't use the package you used to release anymore.

The over idea is that Maven does not support an official deprecation logic. We concluded that the best way was:

- Add a disclaimer on the main README file and the POM description and guide to the migration guide to the new package
  as necessary.
- Push a final release to Maven.

# Step 1: Update in the repository

Clone the repository and update the following files of your package:

- `README.md` add a disclaimer using this syntax:

> This package is no longer being maintained. Use the [azure-mynewpackage](https://central.sonatype.com/artifact/com.azure/azure-mynewpackage/<version>)
> package instead.
> 
> For migration instructions, see the [migration guide](https://aka.ms/azsdk/migrate/my-new-package).

- `CHANGELOG.md` add a new version with the current date and the same disclaimer. For instance:

> ## 1.2.3 (2023-04-03)
> 
> This package is no longer being maintained. Use the [azure-mynewpackage](https://central.sonatype.com/artifact/com.azure/azure-mynewpackage/<version>)
> package instead.
> 
> For migration instructions, see the [migration guide](https://aka.ms/azsdk/migrate/my-new-package).

- `pom.xml` add the disclaimer to the package description. For instance:

> This package is no longer being maintained. Use the [azure-mynewpackage](https://central.sonatype.com/artifact/com.azure/azure-mynewpackage/<version>)
> package instead.
>
> For migration instructions, see the [migration guide](https://aka.ms/azsdk/migrate/my-new-package).

Do a PR targeting the `main` branch. Post your PR in our [channel for Java](https://teams.microsoft.com/l/channel/19%3a5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/Language%2520-%2520Java?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47).

You're responsible to fix any CI issues related to this PR, if any.

Once the PR is merged, move to the next step.

# Step 2: Trigger a release

A release here is the same as usual, triggering the release pipeline of your SDK. More instructions can be found at:

https://aka.ms/azsdk/release-checklist
