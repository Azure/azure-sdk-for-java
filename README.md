> :warning: *NOTE: The **Azure Libraries for Java** project (resource management APIs with fluent interface design pattern) has moved to http://github.com/azure/azure-libraries-for-java, so please log issues for that project in that new repository. This repository is now dedicated to other, auto-generated, non-management Azure SDKs only.*

# Azure SDKs for Java

| Component | Build Status |
| --------- | ------------ |
| Management Libraries | [![Build Status](https://travis-ci.org/Azure/azure-sdk-for-java.svg?branch=master)](https://travis-ci.org/Azure/azure-sdk-for-java) |
| Client Libraries | [![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17)<br>[![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html)|

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
* [Azure Event Grid](#azure-event-grid)

## Other Azure SDKs

These other Azure SDKs for Java, that are not currently in this repository, can be found as follows:

* [Azure ActiveDirectory Authentication Library (ADAL)](https://github.com/AzureAD/azure-activedirectory-library-for-java)
* [Azure Batch SDK for Java](https://github.com/azure/azure-batch-sdk-for-java)
* [Azure Data Lake Store Client for Java](https://github.com/Azure/azure-data-lake-store-java)
* [Azure DocumentDB (CosmosDB) SDK for Java](https://github.com/Azure/azure-documentdb-java)
* [Azure CosmosDB Async SDK for Java](https://github.com/Azure/azure-cosmosdb-java)
* [Azure Key Vault SDK for Java](https://github.com/Azure/azure-keyvault-java)
* [Azure Service Bus SDK for Java](https://github.com/Azure/azure-service-bus-java)
* [Azure Storage SDK for Java](https://github.com/Azure/azure-storage-java)
* [Azure Storage SDK Async for Java](https://github.com/Azure/azure-storage-java-async)
* [Azure Functions SDK for Java](https://github.com/Azure/azure-functions-java-worker)
* [Azure IoT SDK for Java](https://github.com/Azure/azure-iot-sdk-java)
* [Azure Event Hub SDK for Java]()
* [Azure Notification Hubs for Java](https://github.com/Azure/azure-notificationhubs-java-backend)
* [Azure Management Libraries for Java](https://github.com/azure/azure-libraries-for-java)

Other libraries:
* [Microsoft JDBC Driver for SQL Server](https://github.com/Microsoft/mssql-jdbc)

## General Information
* [Prerequisites](#prerequisites)
* [Help and issues](#help-and-issues)
* [Contribute code](#contribute-code)

## Cognitive Services

The following projects provide Java APIs for [Azure Cognitive Services](https://azure.microsoft.com/en-us/services/cognitive-services/), empowering your applications with intelligent algorithms that enable them to see, hear, speak and understand.

* <a name="language"></a>*Language*
  * <a name="spell-check"></a>**Spell Check**
  
      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-spellcheck) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-spellcheck%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3ASpellCheck) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/spell-check/) |
      | --- | --- | --- | --- | --- |
  * <a name="text-analytics"></a>**Text Analytics**
  
      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-textanalytics) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-language%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/TextAnalytics) |  [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3ATextAnalytics) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/text-analytics/) |
      | --- | --- | --- | --- | --- |

* <a name="search"></a>*Search*
  * <a name="custom-search"></a>**Custom Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-customsearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-customsearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3ACustomSearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-custom-search-api/) |
    | --- | --- | --- | --- | --- |
  * <a name="entity-search"></a>**Entity Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-entitysearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-entitysearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AEntitySearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-entity-search-api/) |
    | --- | --- | --- | --- | --- |
  * <a name="image-search"></a>**Image Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-imagesearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-imagesearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AImageSearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-image-search-api/) |
    | --- | --- | --- | --- | --- |
  * <a name="news-search"></a>**News Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-newssearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-newssearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3ANewsSearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-news-search-api/) |
    | --- | --- | --- | --- | --- |
  * <a name="video-search"></a>**Video Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-videosearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-videosearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)| [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AVideoSearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-video-search-api/) |
    | --- | --- | --- | --- | --- |
  * <a name="web-search"></a>**Web Search**
  
    | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-websearch) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-websearch%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/BingSearchV7)  | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AWebSearch) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/bing-web-search-api/) |
    | --- | --- | --- | --- | --- |
	
* <a name="vision"></a>*Vision*
   * <a name="computer-vision"></a>**Computer Vision**

      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-computervision) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-computervision%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/ComputerVision) | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AComputerVision) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/computer-vision) |
      | --- | --- | --- | --- | --- |
	* <a name="content-moderator"></a>**Content Moderator**

      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-contentmoderator) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-cognitiveservices-contentmoderator%22) | [:pencil: Samples](https://github.com/Azure-Samples/cognitive-services-java-sdk-samples/tree/master/ContentModerator) |  [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AContentModerator) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/content-moderator) |
      | --- | --- | --- | --- | --- |
  * <a name="face-api"></a>**Face API**

      | [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/cognitiveservices/azure-vision) | [:arrow_down: Download](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22azure-faceapi%22) | :pencil: Samples | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AFaceAPI) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/cognitive-services/face/) |
      | --- | --- | --- | --- | --- |
	
## Media Services

This project provides Java APIs for Azure Media Services, enabling you to share media content through premium video workflows:

| [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/0.9/services/azure-media) | [:arrow_down: Download](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.microsoft.azure%22%20AND%20a%3A%22azure-media%22) | [:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AMediaServices) | [:book: Learn more...](https://azure.microsoft.com/en-us/services/media-services/) |
| --- | --- | --- | --- |

## Azure Event Grid

This project provides Java APIs for [Azure Event Grid](https://azure.com/eventgrid), enabling you to build reactive programs and applications in the cloud:

| [:page_facing_up: Sources...](https://github.com/Azure/azure-sdk-for-java/tree/master/azure-eventgrid) | :arrow_down: Download | :pencil: Samples |[:triangular_flag_on_post: Issues](https://github.com/azure/azure-sdk-for-java/issues?q=is%3Aopen+is%3Aissue+label%3AEventGrid) | [:book: Learn more...](https://azure.microsoft.com/services/event-grid/) |
| --- | --- | --- | --- | --- |

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


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2FREADME.png)
