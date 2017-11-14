[![Build Status](https://travis-ci.org/Azure/azure-libraries-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-libraries-for-java)

> :triangular_flag_on_post: NOTE: The **Azure Libraries for Java** project (resource management APIs with fluent interface design pattern) has moved to http://github.com/azure/azure-libraries-for-java, so please log issues for that project in the new rerpo. This repository is now dedicated to all the other, auto-generated, non-fluent Azure SDKs only.

# Azure SDKs for Java

:+1: [Try Azure for FREE](http://go.microsoft.com/fwlink/?LinkId=330212)

This repository contains auto-generated, non-fluent Azure SDKs enabling the programmatic *consumption* of miscellaneous Azure services (i.e. *not management* - for that see http://github.com/azure/azure-libraries-for-java)


Currently, this repository contains the following Azure SDKs, at varying stages of stability:

* [Cognitive Services](#cognitive-services)
  * [Entity Search](#entity-search)
  * [Language](#language)
  * [Vision](#vision)

* [Media Services](#media-services)

## Other Azure SDKs

These other Azure Java SDKs, that are not currently in this repository, can be found as follows:

* [Azure Batch SDK for Java]
* [Azure DocumentDB SDK for Java]
* [Azure Key Vault SDK for Java]
* [Azure Service Bus SDK for Java]
* [Azure Storage SDK for Java]

## General Information
* [Prerequisites](#prerequisites)
* [Help and issues](#help-and-issues)
* [Contribute code](#contribute-code)


## Cognitive Services

The following projects provide Java APIs for [Azure Cognitive Services](https://azure.microsoft.com/en-us/services/cognitive-services/):

| Project | Status | Download | Issue Label |
| ------- | ------ | -------- | ----------- |
| <a name="entity-search"></a>[Entity Search](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-entitysearch)
| Under construction
| TBD
| [*EntitySearch*](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AEntitySearch) |

| <a name="language"></a>[Language](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-language)
| Under construction
| TBD
| [*Language*](https://github.com/azure/azure-sdk-for-java/issues?utf8=%E2%9C%93&q=is%3Aopen%20is%3Aissue%20label%3ALanguage) |

| <a name="vision"></a>[Vision](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-vision)
| Under construction
| TBD
| [*Vision*](https://github.com/azure/azure-sdk-for-java/issues?utf8=%E2%9C%93&q=is%3Aopen%20is%3Aissue%20label%3AVision) |

## Media Services

### Download

Maven artifact ID: [**azure-media**](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-media%22)

### Sources

The project that provides Java APIs for [Azure Media Services](https://azure.microsoft.com/en-us/services/media-services/) is [here](https://github.com/Azure/azure-sdk-for-java/tree/0.9/services/azure-media).

###Issues

When logging [issues related to the Media SDK](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AMediaServices), please use the **MediaServices** label.


## Prerequisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Help and Issues

If you encounter any bugs with these SDKs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

## Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
