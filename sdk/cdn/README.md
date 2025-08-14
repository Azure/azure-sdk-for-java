# Azure CDN libraries for Java

Azure Content Delivery Network (CDN) is a global CDN solution for delivering high-bandwidth content. It can be hosted in Azure or any other location. With Azure CDN, you can cache static objects loaded from Azure Blob storage, a web application, or any publicly accessible web server, by using the closest point of presence (POP) server.

This library supports managing Azure CDN resources, including:

- CDN Profiles and Endpoints
- Custom Domains and SSL Certificates
- Origins and Origin Groups
- Rules Engine and Policies
- Purging and Pre-loading Content

The library is built on top of [azure-core](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md) and follows the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

## Library

- [azure-resourcemanager-cdn](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cdn/azure-resourcemanager-cdn/) supports managing Azure CDN resources.

## Contributing

See the [CONTRIBUTING.md](https://github.com/Azure/azure-sdk-for-java/tree/main/CONTRIBUTING.md) for details on building, testing, and contributing to these libraries.

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit [cla.microsoft.com](https://cla.microsoft.com).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.