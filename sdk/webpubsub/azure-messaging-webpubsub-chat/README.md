# Azure Web PubSub Chat service client library for Java

[Azure Web PubSub Chat][product_documentation] is a managed chat capability built on [Azure Web PubSub][webpubsub_documentation]. It provides purpose-built client and server APIs for chat scenarios. Applications use the SDKs to work with chat-native concepts such as rooms, messages, members, and users. The service handles real-time message delivery and ordering, fan-out across a user's devices and browser tabs, room membership, and message persistence and retrieval.

Use this client library in an application server to:

- Create and manage Chat roles and permissions.
- Create users, rooms, and room memberships.
- List rooms and users in a hub, or list the rooms associated with a user.
- Create, get, update, list, and soft delete topics in a room.
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

### Install the package

The following dependency shows the current development version. It has not been
released to Maven Central; build it locally when evaluating the new API.

[//]: # ({x-version-update-start;com.azure:azure-messaging-webpubsub-chat;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-webpubsub-chat</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create and authenticate a client

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

### Topic

A topic is a named conversation in a room. Each topic has a title, creation time,
creator, conversation ID, ETag, and lifecycle state. `ChatTopicState` includes
`NORMAL`, `ARCHIVED`, and `SOFT_DELETED`. The August API adds topic management;
topic deletion is a soft delete rather than a room deletion.

### User's rooms

`listRoomsForUser` returns read-only `ChatUserRoom` projections of the rooms
associated with a user. A projection contains the room ID, title, default
conversation ID, and ETag; it does not provide a separate room lifecycle.

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

The following examples are independent operations. Where an operation refers to
`general`, `alice`, or `announcements`, the corresponding resources must already
exist. All examples use the default `2026-08-01-preview` API version.

### Create a custom role

```java readme-sample-createChatRole
ChatRole moderator = new ChatRole(Arrays.asList(ChatPermission.ROOM_HISTORY,
    ChatPermission.ROOM_REMOVE_USER, ChatPermission.ROOM_PUBLISH_MESSAGE));
client.createOrReplaceRole("room.moderator", moderator);
```

### List roles

```java readme-sample-listChatRoles
client.listRoles().forEach(role -> System.out.println(role.getName()));
```

### Delete a custom role

```java readme-sample-deleteChatRole
client.deleteRole("room.moderator");
```

Remove references to a custom role before deleting it.

### Create a user

```java readme-sample-createChatUser
client.createOrReplaceUser("alice", new HumanChatUser("Alice", BuiltInChatRoles.USER_NORMAL));
```

### Create a room

```java readme-sample-createChatRoom
ChatRoom room = client.createOrReplaceRoom("general", new ChatRoom("General"));
String defaultConversationId = room.getDefaultConversation();
```

### Add a room member

```java readme-sample-addChatRoomMember
ChatRoomMember member = client.createOrReplaceRoomMember(
    "general", "alice", new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));
System.out.printf("%s: %s%n", member.getUserId(), member.getRoleName());
```

### List rooms

```java readme-sample-listChatRooms
client.listRooms().forEach(room -> System.out.printf("%s: %s%n", room.getId(), room.getTitle()));
```

### List users

```java readme-sample-listChatUsers
client.listUsers().forEach(user -> System.out.printf("%s: %s%n", user.getId(), user.getNickname()));
```

### List rooms for a user

```java readme-sample-listChatRoomsForUser
client.listRoomsForUser("alice").forEach(room ->
    System.out.printf("%s: %s%n", room.getId(), room.getTitle()));
```

List operations return `PagedIterable` on the synchronous client and `PagedFlux`
on the asynchronous client. Iteration fetches subsequent pages automatically.
To process synchronous results page by page, use `iterableByPage(pageSize)`.

### Create a topic

The parent room must already exist.

```java readme-sample-createChatTopic
ChatTopic topic = client.createOrReplaceTopic("general", "announcements",
    new ChatTopic().setTitle("Announcements"));
String conversationId = topic.getConversationId();
```

Use the returned conversation ID when a Chat WebSocket client sends messages to
the topic. This server-side library manages resources and persisted messages;
it is not a WebSocket chat client.

### Get a topic

```java readme-sample-getChatTopic
ChatTopic topic = client.getTopic("general", "announcements");
System.out.printf("%s: %s%n", topic.getTitle(), topic.getState());
```

### Update a topic

Only explicitly set properties are included in the merge-patch request.
Use the overload accepting `MatchConditions` and the current ETag to prevent
overwriting another caller's changes.

```java readme-sample-updateChatTopic
ChatTopic updated = client.updateTopic("general", "announcements",
    new ChatTopic().setTitle("Archived announcements").setState(ChatTopicState.ARCHIVED));
```

### List topics

```java readme-sample-listChatTopics
client.listTopics("general").forEach(topic ->
    System.out.printf("%s: %s%n", topic.getId(), topic.getTitle()));
```

### Soft delete a topic

```java readme-sample-deleteChatTopic
client.deleteTopic("general", "announcements");
```

### List persisted messages

```java readme-sample-listChatMessages
String conversationId = "<conversation-id>";
client.listMessages(conversationId).forEach(message ->
    System.out.printf("%s: %s%n", message.getCreatedBy(), message.getContent().getText()));
```

Use a conversation ID returned by room or topic creation. Message history
queries return messages from latest to earliest.

### Use the asynchronous client

```java readme-sample-createAsyncChatClient
WebPubSubChatServiceAsyncClient asyncClient = new WebPubSubChatServiceClientBuilder()
    .connectionString("<web-pubsub-connection-string>")
    .hub("chat")
    .buildAsyncClient();
```

Subscribe to the returned publisher to execute an asynchronous operation, and
handle errors explicitly:

```java readme-sample-listChatTopicsAsync
asyncClient.listTopics("general").subscribe(
    topic -> System.out.println(topic.getTitle()),
    error -> System.err.println("Could not list topics: " + error.getMessage()));
```

#### Asynchronous paging

Use `byPage(pageSize)` on typed asynchronous collections to request a page size:

```java readme-sample-listChatTopicsAsyncByPage
asyncClient.listTopics("general").byPage(10).subscribe(
    page -> page.getValue().forEach(topic -> System.out.println(topic.getTitle())),
    error -> System.err.println("Could not list topic pages: " + error.getMessage()));
```

### Service API versions

The default is `WebPubSubChatServiceVersion.V2026_08_01_PREVIEW`. The library also
supports `V2026_02_01_PREVIEW` for existing operations. Topic operations and the
new room/user listing operations require the August version. To select a version,
pass it to the builder's `serviceVersion` method. All operations are supported
with the August API; no separate client is needed for existing operations.

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
- List rooms and users, and query rooms associated with a user.
- Manage topics and their lifecycle states.
- Generate client access credentials.
- Query, update, and delete message history.
- Use synchronous and asynchronous clients.

## Additional resources

- [Azure Web PubSub documentation][webpubsub_documentation]
- [Web PubSub Chat documentation][product_documentation]
- [Web PubSub Chat REST API specification][rest_api]
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
[rest_api]: https://github.com/Azure/azure-rest-api-specs/tree/main/specification/webpubsub/data-plane/WebPubSubChat
[design_guidelines]: https://azure.github.io/azure-sdk/java_introduction.html
[contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[code_of_conduct_faq]: https://opensource.microsoft.com/codeofconduct/faq/
