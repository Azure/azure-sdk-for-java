# Azure Discovery client library for Java

This package contains Microsoft Azure Discovery client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Microsoft Discovery workspace and/or bookshelf resource, and its service endpoint

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-discovery;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-discovery</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

The Discovery data-plane library is organized around two service endpoints, each with its own client builder:

- **Workspace** — `WorkspaceClientBuilder` builds operation-group clients for a Discovery workspace: `ConversationsClient`, `InvestigationsClient`, `TasksClient`, and `ToolsClient` (each with an asynchronous variant) to manage conversations, investigations, tasks, and long-running tool runs.
- **Bookshelf** — `BookshelfClientBuilder` builds `BookshelfClient` / `BookshelfAsyncClient` to manage knowledge bases, including long-running create/update, indexing, and search.

Each client is created with its service endpoint and a `TokenCredential` such as `DefaultAzureCredential`. The Workspace and Bookshelf endpoints are distinct, so build the client that matches the operation you need.

## Examples

```java com.azure.ai.discovery.readme
ConversationsClient conversationsClient = new WorkspaceClientBuilder()
    .endpoint("https://<workspace-name>.discovery.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildConversationsClient();

PagedConversation conversations = conversationsClient.list();
for (Conversation conversation : conversations.getValue()) {
    System.out.println(conversation.getName());
}
```

List knowledge bases with a `BookshelfClient`:

```java com.azure.ai.discovery.readme.bookshelf
BookshelfClient bookshelfClient = new BookshelfClientBuilder()
    .endpoint("https://<bookshelf-name>.discovery.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

for (KnowledgeBase knowledgeBase : bookshelfClient.list()) {
    System.out.println(knowledgeBase.getName());
}
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

- **Authentication** — ensure your `TokenCredential` (for example `DefaultAzureCredential`) can obtain a token and that the identity has access to the target workspace or bookshelf resource.
- **Endpoints** — Workspace and Bookshelf operations use different endpoints; pointing a client at the wrong endpoint typically results in `403`/`404` responses.
- **HTTP logging** — set `httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))` on the client builder to inspect the underlying requests and responses. See the [logging wiki][logging] for details.

## Next steps

- Browse the [samples][samples] for more usage examples.
- Learn more about [Microsoft Discovery][product_documentation].
- Explore the [API reference documentation][docs].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/microsoft-discovery/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/discovery/azure-ai-discovery/src/samples
