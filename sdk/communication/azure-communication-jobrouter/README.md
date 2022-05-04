# README.md template

Use the guidelines in each section of this template to ensure consistency and readability of your README. The README resides in your package's GitHub repository at the root of its directory within the repo. It's also used as the package distribution page (NuGet, PyPi, npm, etc.) and as a Quickstart on docs.microsoft.com. See [README-EXAMPLE.md](README-EXAMPLE.md) for an example following this template.

**Title**: The H1 of your README should be in the format: `# [Product Name] client library for [Language]`

* All headings, including the H1, should use **sentence-style capitalization**. Refer to the [Microsoft Style Guide][style-guide-msft] for more information.
* Example: `# Azure Batch client library for Python`

**Introduction**: The introduction appears directly under the title (H1) of your README.

* **DO NOT** use an "Introduction" or "Overview" heading (H2) for this section.
* First sentence: **Describe the service** briefly. You can usually use the first line of the service's docs landing page for this (Example: [Cosmos DB docs landing page](https://docs.microsoft.com/azure/cosmos-db/)).
* Include version of the service this library supports: "this library supports [X.Y version of the service] and below"
* Next, add a **bulleted list** of the **most common tasks** supported by the package or library, prefaced with "Use the client library for [Product Name] to:". Then, provide code snippets for these tasks in the [Examples](#examples) section later in the document. Keep the task list short but include those tasks most developers need to perform with your package.
* Include this set of links targeting your product's content at the bottom of the introduction, making any adjustments as necessary (for example, NuGet instead of PyPi):

    - [Package (PyPi)](https://pypi.org/project/azure-cosmos/)
    - [API reference documentation](https://docs.microsoft.com/python/api/overview/azure/cosmos-readme)
    - [Product documentation](https://docs.microsoft.com/azure/cosmos-db/)
    - [Source code](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/cosmos/azure-cosmos)
    - [ChangeLog](https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/cosmos/azure-cosmos/CHANGELOG.md)
    - [Samples](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/cosmos/azure-cosmos/samples)
    - [Versioned API References](https://azure.github.io/azure-sdk-for-python/cosmosdb.html) -- Note this link format may be updated in the future but it is the best we have for now.

> TIP: Your README should be as **brief** as possible but **no more brief** than necessary to get a developer new to Azure, the service, or the package up and running quickly. Keep it brief, but include everything a developer needs to make their first API call successfully.

## Link Guidelines

* **DO NOT** use relative links to other files in the source repository. While relative links work well when browsing directly on github they tend to break when we consume the content in other places, such as docs, samples, package managers, etc. For this reason the guidance is to use links to main in all our MD files in the repo as this will help ensure that links work in other contexts.

    * Given we own the publishing pipeline for docs we will transform main links to links based on release tags to help ensure the links remain over time and point at exact versions of the content when the docs were published.

    * If the CI validation fails because of link verification for a link to a file or section present in your PR and not in the main but will be a valid link after the PR is merged to main, then you can put the link as a comment like this `Here are some [samples]<!--(https://github.com/azure-sdk-for-python/blob/main/samples.json)--> to look at.` (Note you can also put a TODO in the comment if you like). Then in a follow-up PR you can remove the comment to get the link you wanted.

* **DO NOT** use locale in the urls like `en-us`. For example use `https://docs.microsoft.com/dotnet/api/azure.storage.blobs` instead of `https://docs.microsoft.com/en-us/dotnet/api/azure.storage.blobs`

* **DO NOT** use version numbers in the links as the version numbers will often be missed when releasing new versions thus point to the wrong thing or not exist at all. Instead avoid linking directly to versioned information. If you still need a link you should first try to link to a page that contains all the versions so someone can choose or link to the latest which may be out of sync with the content but should usually remain valid.

* **DO NOT** use upper case in the anchor tag links like `#Samples`. Using upper case in anchor tag links will not work when we convert them to html as they are usually case sensitive. For this reason the guidance is to use lower case for any anchor tags we have in all our MD files in the repo as this will help ensure that links work in other contexts. For example use `https://github.com/Azure/azure-sdk-for-java#client-new-releases` instead of `https://github.com/Azure/azure-sdk-for-java#Client-New-Releases`

* **DO** use `https` instead of `http` for links to ensure we are linking to secure servers.

* **DO** think about the longevity of your links and be sure to pick links we believe will remain stable and around for a long time. So no temporary links or links that point to moving targets like a repository branch which may not contain the files in the same locations over time.

## Getting started

This section should include everything a developer needs to do to install and create their first client connection *very quickly*.

### Install the package

First, provide instruction for obtaining and installing the package or library. This section might include only a single line of code, like `pip install package-name`, but should enable a developer to successfully install the package from NuGet, pip, npm, Maven, or even cloning a GitHub repository.

Include a **Prerequisites** line after the install command that details any requirements that must be satisfied before a developer can [authenticate](#authenticate-the-client) and test all of the snippets in the [Examples](#examples) section. For example, for Cosmos DB:

**Prerequisites**: You must have an [Azure subscription](https://azure.microsoft.com/free/), [Cosmos DB account](https://docs.microsoft.com/azure/cosmos-db/account-overview) (SQL API), and [Python 3.6+](https://www.python.org/downloads/) to use this package.

### Authenticate the client

If your library requires authentication for use, such as for Azure services, include instructions and example code needed for initializing and authenticating.

For example, include details on obtaining an account key and endpoint URI, setting environment variables for each, and initializing the client object.

## Key concepts

The *Key concepts* section should describe the functionality of the main classes. Point out the most important and useful classes in the package (with links to their reference pages) and explain how those classes work together. Feel free to use bulleted lists, tables, code blocks, or even diagrams for clarity.

## Examples

Include code snippets and short descriptions for each task you listed in the [Introduction](#introduction) (the bulleted list). Briefly explain each operation, but include enough clarity to explain complex or otherwise tricky operations.

If possible, use the same example snippets that your in-code documentation uses. For example, use the snippets in your `examples.py` that Sphinx ingests via its [literalinclude](https://www.sphinx-doc.org/en/1.5/markup/code.html?highlight=code%20examples#includes) directive. The `examples.py` file containing the snippets should reside alongside your package's code, and should be tested in an automated fashion.

Each example in the *Examples* section starts with an H3 that describes the example. At the top of this section, just under the *Examples* H2, add a bulleted list linking to each example H3. Each example should deep-link to the types and/or members used in the example.

* [Create the thing](#create-the-thing)
* [Get the thing](#get-the-thing)
* [List the things](#list-the-things)

### Create the thing

Use the [create_thing](not-valid-link) method to create a Thing reference; this method does not make a network call. To persist the Thing in the service, call [Thing.save](not-valid-link).

```Python
thing = client.create_thing(id, name)
thing.save()
```

### Get the thing

The [get_thing](not-valid-link) method retrieves a Thing from the service. The `id` parameter is the unique ID of the Thing, not its "name" property.

```Python
thing = client.get_thing(id)
```

### List the things

Use [list_things](not-valid-link) to get one or more Thing objects from the service. If there are no Things available, a `404` exception is thrown (see [Troubleshooting](#troubleshooting) for details on handling exceptions).

```Python
things = client.list_things()
```

## Troubleshooting

Describe common errors and exceptions, how to "unpack" them if necessary, and include guidance for graceful handling and recovery.

Provide information to help developers avoid throttling or other service-enforced errors they might encounter. For example, provide guidance and examples for using retry or connection policies in the API.

If the package or a related package supports it, include tips for logging or enabling instrumentation to help them debug their code.

## Next steps

* Provide a link to additional code examples, ideally to those sitting alongside the README in the package's `/samples` directory.
* If appropriate, point users to other packages that might be useful.
* If you think there's a good chance that developers might stumble across your package in error (because they're searching for specific functionality and mistakenly think the package provides that functionality), point them to the packages they might be looking for.

## Contributing

If you'd like to contribute to this library, please read the [contributing guide](../../../CONTRIBUTING.md) to learn more about how to build and test the code.

<!-- LINKS -->
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization


## Links to language repos

[Python Readme Template](https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/template/azure-template/README.md)

[JavaScript Readme Template](https://github.com/Azure/azure-sdk-for-js/blob/main/sdk/template/template/README.md)

[Java Readme Template](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/template/azure-sdk-template/README.md)

[.NET Readme Template](https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/template/Azure.Template/README.md)
