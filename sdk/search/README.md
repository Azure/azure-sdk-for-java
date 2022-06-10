# Azure Cognitive Search libraries for Java

Azure Cognitive Search ([formerly known as "Azure Search"](https://docs.microsoft.com/azure/search/whats-new#new-service-name))
is a search-as-a-service cloud solution that gives developers APIs and tools for adding a rich search experience over 
private, heterogeneous content in web, mobile, and enterprise applications. Your code or a tool invokes data ingestion
(indexing) to create and load an index. Optionally, you can add cognitive skills to apply AI processes during indexing. 
Doing so can add new information and structures useful for search and other scenarios.

On the other side of your service, your application code issues query requests and handles responses. The search 
experience is defined in your client using functionality from Azure Cognitive Search, with query execution over a
persisted index that you create, own, and store in your service.

Functionality is exposed through several client libraries:

- [azure-search-documents](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents) is 
  the Java client library for building applications with Azure Cognitive Search.  It is built on top of 
  [azure-core](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md) and the
  [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

- [azure-resourcemanager-search](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager/azure-resourcemanager-search/)
  supports managing Azure Cognitive Search services and API keys.

## Contributing

See the Azure.Search.Documents [CONTRIBUTING.md](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/CONTRIBUTING.md) 
for details on building, testing, and contributing to these libraries.

This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License 
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. 
For details, visit [cla.microsoft.com](https://cla.microsoft.com).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact 
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2FREADME.png)