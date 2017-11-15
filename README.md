[![Build Status](https://travis-ci.org/Azure/azure-libraries-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-libraries-for-java)

> :warning: *NOTE: The **Azure Libraries for Java** project (resource management APIs with fluent interface design pattern) has moved to http://github.com/azure/azure-libraries-for-java, so please log issues for that project in that new repository. This repository is now dedicated to other, auto-generated, non-management Azure SDKs only.*

# Azure SDKs for Java

:+1: [Try Azure for FREE](http://go.microsoft.com/fwlink/?LinkId=330212)

This repository contains Azure SDKs enabling the programmatic *consumption* of miscellaneous Azure services (i.e. *not management* - for that see http://github.com/azure/azure-libraries-for-java)

Currently, this repository contains the following Azure SDKs:

* [Media Services](#media-services)
* [Cognitive Services](#cognitive-services)
  * [Search](#search)
  	* [Entity Search](#entity-search)
  * [Language](#language)
    * [Text Analytics](#text-analytics)
  * [Vision](#vision)
    * [Face API](#face-api)

## Other Azure SDKs

These other Azure SDKs for Java, that are not currently in this repository, can be found as follows:

* [Azure Batch SDK for Java](https://github.com/azure/azure-batch-sdk-for-java)
* [Azure Data Lake Store Client for Java](https://github.com/Azure/azure-data-lake-store-java)
* [Azure DocumentDB SDK for Java](https://github.com/Azure/azure-documentdb-java)
* [Azure Key Vault SDK for Java](https://github.com/Azure/azure-keyvault-java)
* [Azure Management Libraries for Java](https://github.com/azure/azure-libraries-for-java)
* [Azure Service Bus SDK for Java](https://github.com/Azure/azure-service-bus-java)
* [Azure Storage SDK for Java](https://github.com/Azure/azure-storage-java)
* [Microsoft JDBC Driver for SQL Server](https://github.com/Microsoft/mssql-jdbc)

## General Information
* [Prerequisites](#prerequisites)
* [Help and issues](#help-and-issues)
* [Contribute code](#contribute-code)

## Cognitive Services

The following projects provide Java APIs for [Azure Cognitive Services](https://azure.microsoft.com/en-us/services/cognitive-services/), empowering your applications with intelligent algorithms that enable them to see, hear, speak and understand.

* <a name="search"></a>*Search*
  * <a name="entity-search"></a>**Entity Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-entitysearch) | :arrow_down: Download - not yet released | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AEntitySearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-entity-search-api/) |
    | --- | --- | --- | --- |

* <a name="language"></a>*Language*
  * <a name="text-analytics"></a>**Text Analytics**
  
      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-language) | :arrow_down: Download - not yet released | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3ATextAnalytics) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/text-analytics/) |
    | --- | --- | --- | --- |

* <a name="vision"></a>*Vision*
  * <a name="face-api"></a>**Face API**

      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-vision) | :arrow_down: Download - not yet released | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AFaceAPI) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/face/) |
    | --- | --- | --- | --- |

## Media Services

This project provides Java APIs for Azure Media Services, enabling you to share media content through premium video workflows:

| [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/0.9/services/azure-media) | [:arrow_down: Download](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-media%22) | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AMediaServices) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/media-services/) |
| --- | --- | --- | --- |

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
