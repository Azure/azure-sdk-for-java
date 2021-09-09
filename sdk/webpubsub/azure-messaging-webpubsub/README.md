# Azure Web PubSub service client library for Java

Azure Web PubSub service client library for Java allows sending messages to Web PubSub. Azure Web PubSub service 
enables you to build real-time messaging web applications using WebSockets and the publish-subscribe pattern. Any 
platform supporting WebSocket APIs can connect to the service easily, e.g. web pages, mobile applications, edge devices,
etc. The service manages the WebSocket connections for you and allows up to 100K concurrent connections. It provides 
powerful APIs for you to manage these clients and deliver real-time messages.

Any scenario that requires real-time publish-subscribe messaging between server and clients or among clients, can use
Azure Web PubSub service. Traditional real-time features that often require polling from server or submitting HTTP
requests, can also use Azure Web PubSub service.

[Source code][source_code] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-messaging-webpubsub;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-webpubsub</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Create a Web PubSub client using connection string

<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L21-L24 -->
```java
WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
    .connectionString("{connection-string}")
    .hub("chat")
    .buildClient();
```

### Create a Web PubSub client using access key

<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L31-L35 -->
```java
WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
    .credential(new AzureKeyCredential("{access-key}"))
    .endpoint("<Insert endpoint from Azure Portal>")
    .hub("chat")
    .buildClient();
```

## Key concepts

### Hub

Hub is a logic set of connections. All connections to Web PubSub connect to a specific hub. Messages that are broadcast
to the hub are dispatched to all connections to that hub. For example, hub can be used for different applications,
different applications can share one Azure Web PubSub service by using different hub names.

### Group

Group allow broadcast messages to a subset of connections to the hub. You can add and remove users and connections as
needed. A client can join multiple groups, and a group can contain multiple clients.

### User

Connections to Web PubSub can belong to one user. A user might have multiple connections, for example when a single user
is connected across multiple devices or multiple browser tabs.

### Connection

Connections, represented by a connection id, represent an individual websocket connection to the Web PubSub service.
Connection id is always unique.

### Message

A message is either an UTF-8 encoded string or raw binary data.

## Examples

* [Broadcast message to entire hub](#broadcast-all "Broadcast message to entire hub")
* [Broadcast message to a group](#broadcast-group "Broadcast message to a group")
* [Send message to a connection](#send-to-connection "Send message to a connection")
* [Send message to a user](#send-to-user "Send message to a user")

### Broadcast message to entire hub

<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L47-L47 -->
```java
webPubSubServiceClient.sendToAll("Hello world!", "text/plain");
```

### Broadcast message to a group

<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L59-L59 -->
```java
webPubSubServiceClient.sendToGroup("java", "Hello Java!", "text/plain");
```

### Send message to a connection

<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L71-L71 -->
```java
webPubSubServiceClient.sendToConnection("myconnectionid", "Hello connection!", "text/plain");
```

### Send message to a user
<!-- embedme ./src/samples/java/com/azure/messaging/webpubsub/ReadmeSamples.java#L83-L83 -->
```java
webPubSubServiceClient.sendToUser("Andy", "Hello Andy!", "text/plain");
```

## Troubleshooting

### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][log_levels].

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to
a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see
the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or
comments.

<!-- LINKS -->

[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/webpubsub/azure-messaging-webpubsub/src
[product_documentation]: https://aka.ms/awps/doc
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/webpubsub/azure-messaging-webpubsub/src/samples/README.md
[log_levels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fwebpubsub%2Fazure-messaging-webpubsub%2FREADME.png)
