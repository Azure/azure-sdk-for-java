
# Azure SDK for Java

[![Packages](https://img.shields.io/badge/packages-latest-blue.svg)](https://azure.github.io/azure-sdk/releases/latest/java.html) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azure.github.io/azure-sdk-for-java)

This repository is for active development of the Azure SDK for Java. For consumers of the SDK we recommend visiting our [public developer docs](https://docs.microsoft.com/azure/developer/java/sdk/) or our versioned [developer docs](https://azure.github.io/azure-sdk-for-java).

## Getting started

To get started with a specific service library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk` directory. For a list of all the services we support access our [list of all existing libraries](https://azure.github.io/azure-sdk/releases/latest/all/java.html).

For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).

### Prerequisites

All libraries are baselined on Java 8, with testing and forward support up until the latest Java long-term support release (currently Java 21).

## Available packages

Each service can have both 'client' and 'management' libraries. 'Client' libraries are used to consume the service, whereas 'management' libraries are used to configure and manage the service.

### Client Libraries

Our client libraries follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java/guidelines/), and share a number of core features such as HTTP retries, logging, transport protocols, authentication protocols, etc., so that once you learn how to use these features in one client library, you will know how to use them in other client libraries. You can learn about these shared features [here](https://docs.microsoft.com/azure/developer/java/sdk/overview).
These libraries can be easily identified by folder, package, and namespaces names starting with `azure-`, e.g. `azure-keyvault`.

You can find the **[most up to date list of all of the new packages on our page](https://azure.github.io/azure-sdk/releases/latest/index.html#java)**. This list includes the most recent releases: both stable and beta.

> NOTE: If you need to ensure your code is ready for production use one of the stable, non-beta libraries.

### Management Libraries

Similar to our client libraries, the management libraries follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java/guidelines/). These libraries provide a high-level, object-oriented API for _managing_ Azure resources, that are optimized for ease of use, succinctness, and consistency. You can find the list of management libraries **[on this page](https://azure.github.io/azure-sdk/releases/latest/mgmt/java.html)**.

**For general documentation on how to use the new libraries for Azure Resource Management, please [visit here](https://aka.ms/azsdk/java/mgmt)**. We have also prepared **[plenty of code samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/SAMPLE.md)** as well as **[migration guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/MIGRATION_GUIDE.md)** in case you are upgrading from previous versions.

The management libraries can be identified by namespaces that start with `azure-resourcemanager`, e.g. `azure-resourcemanager-compute`.

### Historical Releases

Note that the latest libraries from Microsoft are in the `com.azure` Maven group ID, and have the package naming pattern of beginning with `com.azure`. If you're using libraries that are in `com.microsoft.azure` Maven group ID, or have this as the package structure, please consider migrating to the latest libraries. You can find a mapping table from these historical releases to their equivalent [here](https://azure.github.io/azure-sdk/releases/deprecated/index.html#java).

### Android Support

The Azure SDKs for Java do not provide support for Android. While we attempt to allow the SDKs to be used on Android, we do not test or support this scenario.

## Need help?

- For reference documentation visit the [Azure SDK for Java documentation](https://aka.ms/java-docs).
- For tutorials, samples, quick starts and other documentation, visit [Azure for Java Developers](https://docs.microsoft.com/java/azure/).
- For build reports on code quality, test coverage, etc, visit [Azure Java SDK](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html).
- File an issue via [Github Issues](https://github.com/Azure/azure-sdk-for-java/issues/new/choose).
- Check [previous questions](https://stackoverflow.com/questions/tagged/azure-java-sdk) or ask new ones on StackOverflow using `azure-java-sdk` tag.

## Navigating the repository

### Main branch

The main branch has the most recent code with new features and bug fixes. It does **not** represent latest released **stable** SDK.

### Release branches (Release tagging)

For each package we release there will be a unique git tag created that contains the name and the version of the package to mark the commit of the code that produced the package. This tag will be used for servicing via hotfix branches as well as debugging the code for a particular beta or stable release version.
Format of the release tags are `<package-name>_<package-version>`. For more information please see [our branching strategy](https://github.com/Azure/azure-sdk/blob/main/docs/policies/repobranching.md#release-tagging).

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, view [Microsoft's CLA](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Additional Helpful Links for Contributors

Many people all over the world have helped make this project better.  You'll want to check out:

- [What are some good first issues for new contributors to the repo?](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22)
- [How to build and test your change](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md#developer-guide)
- [How you can make a change happen!](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md#pull-requests)
- Frequently Asked Questions (FAQ) and Conceptual Topics in the detailed [Azure SDK for Java wiki](https://github.com/azure/azure-sdk-for-java/wiki).

### Reporting security issues and security bugs

Security issues and bugs should be reported privately, via email, to the Microsoft Security Response Center (MSRC) <secure@microsoft.com>. You should receive a response within 24 hours. If for some reason you do not, please follow up via email to ensure we received your original message. Further information, including the MSRC PGP key, can be found in the [Security TechCenter](https://www.microsoft.com/msrc/faqs-report-an-issue).

### License

Azure SDK for Java is licensed under the [MIT](https://github.com/Azure/azure-sdk-for-java/blob/main/LICENSE.txt) license.

<!-- Links -->
[java_guidelines]: https://azure.github.io/azure-sdk/java_introduction.html
[latest_release_page]: https://azure.github.io/azure-sdk/releases/2020-03/java.html
[feb_20_release_page]: https://azure.github.io/azure-sdk/releases/2020-02/java.html
[jan_20_release_page]: https://azure.github.io/azure-sdk/releases/2020-01/java.html
[dec_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-12/java.html
[nov_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-11/java.html
[oct_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-10-11/java.html
[sep_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-09-17/java.html
[aug_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-08-06/java.html
[jul_19_release_page]: https://azure.github.io/azure-sdk/releases/2019-07-10/java.html


