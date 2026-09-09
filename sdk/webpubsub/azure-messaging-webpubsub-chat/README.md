# Azure Web PubSub Chat service client library for Java

[Azure Web PubSub Chat][product_documentation] is a managed chat capability built on [Azure Web PubSub][webpubsub_documentation]. It provides purpose-built client and server APIs for chat scenarios. Applications use the SDKs to work with chat-native concepts such as rooms, messages, members, and users. The service handles real-time message delivery and ordering, fan-out across a user's devices and browser tabs, room membership, and message persistence and retrieval.

Use this client library in an application server to:

- Create and manage Chat roles and permissions.
- Create users, rooms, and room memberships.
- Get room conversations and query persisted message history.
- Update and delete persisted messages.
- Generate client access credentials for Chat WebSocket clients.

[Source code][source_code] | [Package][package] | [API reference documentation][docs] | [Product documentation][product_documentation] | [Samples][samples] | [Changelog][changelog]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk] with version 8 or later.
- An [Azure subscription][azure_subscription].
- An [Azure Web PubSub resource][create_instance].
- A Web PubSub hub with [Chat enabled][enable_chat].

### 1. Add the package to your project

[//]: # ({x-version-update-start;com.azure:azure-messaging-webpubsub-chat;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-webpubsub-chat</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### 2. Create and authenticate a `WebPubSubChatServiceClient`

The client supports a connection string, an `AzureKeyCredential`, or a Microsoft Entra ID token credential. The hub passed to the client must have Chat enabled.

#### Use a connection string

Get the connection string from the Azure portal or Azure CLI, and store it securely. See [Web PubSub authorization][connection_string] for details.

```java readme-sample-createChatClientWithConnectionString
WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
    .connectionString("<web-pubsub-connection-string>")
    .hub("chat")
    .buildClient();
```

#### Use an access key

```java readme-sample-createChatClientWithKey
WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
    .endpoint("https://<resource-name>.webpubsub.azure.com")
    .hub("chat")
    .credential(new AzureKeyCredential("<web-pubsub-access-key>"))
    .buildClient();
```

#### Use Microsoft Entra ID

For recommended passwordless authentication, add the [Azure Identity][azure_identity] package, assign an appropriate Web PubSub data-plane role to the principal, and authenticate with a token credential. The following example uses `DefaultAzureCredential`:

```java readme-sample-createChatClientWithEntraId
WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
    .endpoint("https://<resource-name>.webpubsub.azure.com")
    .hub("chat")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

For more information, see [Authenticate Azure-hosted Java applications][azure_identity_auth] and [Microsoft Entra authorization for Azure Web PubSub][entra_authorization].

## Key concepts

### Client

`WebPubSubChatServiceClient` is the entry point for managing Chat resources in one Web PubSub hub. Create one client for each endpoint and hub combination, and reuse the client for multiple operations. For nonblocking operations, use `WebPubSubChatServiceAsyncClient`, which is created by calling `buildAsyncClient` on the same builder.

### Hub

A hub is a logical collection of WebSocket connections. A standard hub offers event-based real-time messaging through the Web PubSub subprotocol or a custom subprotocol. A chat hub adds built-in rooms, member management, message persistence, and chat-specific APIs.

This SDK applies only to chat hubs. Chat must be enabled on the target hub before the SDK can manage roles, users, rooms, members, conversations, or messages.

### Role and permission

A role is a named collection of Chat permissions. User role names start with `user.`, and room role names start with `room.`. Do not combine user and room permissions in one role.

User roles control operations such as creating rooms. Room roles control what a member can do in a particular room, such as publishing messages or reading message history. `BuiltInChatRoles` provides role names for common scenarios, and `ChatPermission` provides the permissions used to define custom roles.

### User

A user represents an application identity that can send and receive messages. A user is identified by a user ID and assigned a user role. A human user also has a nickname. Client access credentials associate WebSocket connections with a user ID.

### Room

A room groups users together and is the primary organizational unit for chat interactions. Every room has an automatically created default conversation.

### Room member

A room member represents a user added to a room. Membership controls which users can receive and send messages in the room. Each room member is assigned a room role.

### Conversation and message history

A conversation is a message thread that belongs to a room. Every room has a default conversation and can contain multiple conversations.

Messages sent to a conversation are delivered in real time to the room's connected members. The Chat service manages ordering and persistence, allowing members to load message history after reconnecting or joining later. The service client can list, update, and delete persisted messages.

### Client access credentials

`getClientAccessToken` returns a token and WebSocket connection URL. Clients configured with a connection string or access key sign the token locally. Clients configured with Microsoft Entra ID request the token from the Web PubSub service.

## Examples

### Generate a client access token

```java readme-sample-getChatClientAccessToken
WebPubSubClientAccessToken accessToken = client.getClientAccessToken(
    new GetClientAccessTokenOptions().setUserId("alice").setExpiresAfter(Duration.ofHours(1)));
String clientConnectionUrl = accessToken.getUrl();
```

The returned URL contains an access token. Send it only to the intended client, and do not log or persist it in production.

### Work with built-in values

```java readme-sample-chatBuiltInValues
String memberRole = BuiltInChatRoles.ROOM_MEMBER;
ChatPermission publishPermission = ChatPermission.ROOM_PUBLISH_MESSAGE;
```

### Create and list a custom role

```java readme-sample-manageChatRoles
ChatRole moderator = new ChatRole(Arrays.asList(ChatPermission.ROOM_HISTORY,
    ChatPermission.ROOM_REMOVE_USER, ChatPermission.ROOM_PUBLISH_MESSAGE));
client.createOrReplaceRole("room.moderator", moderator);

client.listRoles().forEach(role -> System.out.println(role.getName()));
client.deleteRole("room.moderator");
```

### Create a user, room, and room membership

```java readme-sample-manageChatRooms
client.createOrReplaceRole("user.room_creator",
    new ChatRole(Arrays.asList(ChatPermission.USER_CREATE_ROOM)));
client.createOrReplaceRole("room.contributor",
    new ChatRole(Arrays.asList(ChatPermission.ROOM_PUBLISH_MESSAGE)));
client.createOrReplaceUser("alice", new HumanChatUser("Alice", "user.room_creator"));

ChatRoom room = client.createOrReplaceRoom("general", new ChatRoom("General"));
ChatRoomMember member = client.createOrReplaceRoomMember(
    room.getId(), "alice", new ChatRoomMember("room.contributor"));
System.out.printf("%s: %s%n", member.getUserId(), member.getRoleName());

client.deleteRoom(room.getId());
client.deleteUser("alice");
client.deleteRole("room.contributor");
client.deleteRole("user.room_creator");
```

Delete dependent resources in reverse order when they are no longer needed: room, user, and then roles.

### List persisted messages

```java readme-sample-listChatMessages
ChatRoom room = client.getRoom("general");
client.listMessages(room.getDefaultConversation()).forEach(message ->
    System.out.printf("%s: %s%n", message.getCreatedBy(), message.getContent().getText()));
```

### Use the asynchronous client

```java readme-sample-createAsyncChatClient
WebPubSubChatServiceAsyncClient asyncClient = new WebPubSubChatServiceClientBuilder()
    .connectionString("<web-pubsub-connection-string>")
    .hub("chat")
    .buildAsyncClient();

asyncClient.listRoles().subscribe(role -> System.out.println(role.getName()));
```

### Service API versions

The client library targets the latest service API version by default. To use another supported version, pass a `WebPubSubChatServiceVersion` to the builder's `serviceVersion` method. Verify that the selected version supports the operations and models used by your application.

## Troubleshooting

### Handle service errors

Service operations throw `HttpResponseException` or a more specific subclass when a request fails. Inspect the status code and response body before retrying an operation. For example, a missing resource results in a `ResourceNotFoundException`, while a failed ETag condition can result in a `ResourceModifiedException`.

### Logging

Enable SDK logging by setting the `AZURE_LOG_LEVEL` environment variable. See [Azure SDK logging][logging] for the supported levels and logging configuration. HTTP logs can contain sensitive information. Do not enable detailed logging in production without reviewing how logs are collected and protected, and never log connection strings, access keys, bearer tokens, or generated client access tokens.

### Authentication and authorization

- Confirm that the endpoint and hub name identify the Web PubSub resource and Chat-enabled hub you intend to use.
- For Microsoft Entra ID, confirm that the principal has an appropriate Web PubSub data-plane role and that role assignment propagation has completed.
- Connection-string and access-key authentication are unavailable when local authentication is disabled on the Web PubSub resource.


## Next steps

Explore the [complete package samples][samples] to learn how to:

- Authenticate with a connection string, access key, or Microsoft Entra ID.
- Manage roles, permissions, users, rooms, and room members.
- Generate client access credentials.
- Query, update, and delete message history.
- Use synchronous and asynchronous clients.

## Additional resources

- [Azure Web PubSub documentation][webpubsub_documentation]
- [Web PubSub Chat documentation][product_documentation]
- [Web PubSub Chat REST API][rest_api]
- [Azure SDK for Java design guidelines][design_guidelines]

## Contributing

This project welcomes contributions and suggestions. See the [contributing guide][contributing] for instructions on building, testing, and submitting changes.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information, see the [Code of Conduct FAQ][code_of_conduct_faq] or contact opencode@microsoft.com with questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/webpubsub/azure-messaging-webpubsub-chat/src
[package]: https://central.sonatype.com/artifact/com.azure/azure-messaging-webpubsub-chat
[docs]: https://azure.github.io/azure-sdk-for-java/
[product_documentation]: https://learn.microsoft.com/azure/azure-web-pubsub/chat-overview
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/webpubsub/azure-messaging-webpubsub-chat/src/samples
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/webpubsub/azure-messaging-webpubsub-chat/CHANGELOG.md
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free
[webpubsub_documentation]: https://learn.microsoft.com/azure/azure-web-pubsub/
[create_instance]: https://learn.microsoft.com/azure/azure-web-pubsub/howto-develop-create-instance
[enable_chat]: https://learn.microsoft.com/azure/azure-web-pubsub/chat-howto-enable-chat
[connection_string]: https://learn.microsoft.com/azure/azure-web-pubsub/howto-websocket-connect#authorization
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_identity_auth]: https://learn.microsoft.com/azure/developer/java/sdk/authentication/overview
[entra_authorization]: https://learn.microsoft.com/azure/azure-web-pubsub/concept-azure-ad-authorization
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[rest_api]: https://learn.microsoft.com/rest/api/webpubsub/dataplane/webpubsubchat/web-pub-sub-chat-service
[design_guidelines]: https://azure.github.io/azure-sdk/java_introduction.html
[contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[code_of_conduct_faq]: https://opensource.microsoft.com/codeofconduct/faq/
