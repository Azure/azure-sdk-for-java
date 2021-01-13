# # Azure Communication Chat client library for Java

Azure Communication Chat contains the APIs used in chat applications for Azure Communication Services.  

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource.
### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-chat;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-chat</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```

## Key concepts

A chat conversation is represented by a chat thread. Each user in the chat thread is called a thread member. Thread members can chat with one another privately in a 1:1 chat or huddle up in a 1:N group chat.

Once you initialized a `ChatClient` and a `ChatThreadClient` class, you can do the following chat operations:

### Create, get, list, update, and delete chat threads

### Send, get, list, update, and delete chat messages

### Get, add, and remove members

### Send and get read receipts

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

### Set Azure Communication Resource endpoint after it is created

endpoint = "https://*Azure-Communication-Resource-Name*.communications.azure.com"

### Request a User Access Token

User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. 
You generate these tokens on your server, pass them back to a client device, and then use them to initialize the Communication Services SDKs. 

Learn how to generate user access tokens from [User Access Tokens](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens?pivots=programming-language-java#issue-user-access-tokens)

## Examples

The following sections provide several code snippets covering some of the most common tasks, including:

- [Chat Thread Operations](#thread-operations)
- [Chat Message Operations](#message-operations)
- [Chat Thread Member Operations](#thread-member-operations)
- [Read Receipt Operations](#read-receipt-operations)
- [Typing Notification Operations](#typing-notification-operations)

## Create the chat client

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L41-L57 -->
```Java
String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

// Create an HttpClient builder of your choice and customize it
// Use com.azure.core.http.netty.NettyAsyncHttpClientBuilder if that suits your needs
NettyAsyncHttpClientBuilder httpClientBuilder = new NettyAsyncHttpClientBuilder();
HttpClient httpClient = httpClientBuilder.build();

// Your user access token retrieved from your trusted service
String token = "SECRET";
CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

// Initialize the chat client
final ChatClientBuilder builder = new ChatClientBuilder();
builder.endpoint(endpoint)
    .credential(credential)
    .httpClient(httpClient);
ChatClient chatClient = builder.buildClient();
```

### Chat Thread Operations

#### Create a chat thread

To create a chat client, you will use the Communications Service endpoint and the access token that was generated as part of pre-requisite steps. User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. Once you generate these tokens on your server, pass them back to a client device. You need to use the CommunicationTokenCredential class from the Common SDK to pass the token to your chat client.

Use the `createChatThread` method to create a chat thread.
`createChatThreadOptions` is used to describe the thread request, an example is shown in the code snippet below.

- Use `topic` to give a thread topic;
- Use `members` to list the thread members to be added to the thread;

`chatThreadClient` is the response returned from creating a thread. It can be used for performing operations on the created thread: add members, send message, etc.
It contains a `chatThreadId` property which is the unique ID of the thread. The property is accessible by the public method getChatThreadId().

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L71-L88 -->
```Java
List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();

ChatThreadMember firstThreadMember = new ChatThreadMember()
    .setUser(user1)
    .setDisplayName("Member Display Name 1");

ChatThreadMember secondThreadMember = new ChatThreadMember()
    .setUser(user2)
    .setDisplayName("Member Display Name 2");

members.add(firstThreadMember);
members.add(secondThreadMember);

CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
    .setTopic("Topic")
    .setMembers(members);
ChatThreadClient chatThreadClient = chatClient.createChatThread(createChatThreadOptions);
String chatThreadId = chatThreadClient.getChatThreadId();
```

#### Get a chat thread

The `getChatThread` method retrieves a thread from the service.
`chatThreadId` is the unique ID of the chat thread.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L97-L98 -->
```Java
String chatThreadId = "Id";
ChatThread chatThread = chatClient.getChatThread(chatThreadId);
```

#### Delete a thread

Use `deleteChatThread` method to delete a chat thread
`chatThreadId` is the unique ID of the chat thread.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L107-L108 -->
```Java
String chatThreadId = "Id";
chatClient.deleteChatThread(chatThreadId);
```

#### Get a chat thread client

The `getChatThreadClient` method returns a thread client for a thread that already exists. It can be used for performing operations on the created thread: add members, send message, etc.
`chatThreadId` is the unique ID of the existing chat thread.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L119-L120 -->
```Java
String chatThreadId = "Id";
ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);
```

#### Update a chat thread

Use `updateChatThread` method to update a thread's properties
`updateChatThreadOptions` is used to describe the property change of the chat thread.

- Use `topic` to give chat thread a new topic;

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L131-L133 -->
```Java
UpdateChatThreadOptions updateChatThreadOptions = new UpdateChatThreadOptions()
    .setTopic("New Topic");
chatThreadClient.updateChatThread(updateChatThreadOptions);
```

### Chat Message Operations

#### Send a chat message

Use the `sendMessage` method to sends a chat message to a chat thread identified by chatThreadId.
`sendChatMessageOptions` is used to describe the chat message request, an example is shown in the code snippet below.

- Use `content` to provide the chat message content;
- Use `priority` to specify the chat message priority level, such as 'Normal' or 'High';
- Use `senderDisplayName` to specify the display name of the sender;

`sendChatMessageResult` is the response returned from sending a chat message, it contains an id, which is the unique ID of the message.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L142-L148 -->
```Java
SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
    .setContent("Message content")
    .setPriority(ChatMessagePriority.NORMAL)
    .setSenderDisplayName("Sender Display Name");

SendChatMessageResult sendChatMessageResult = chatThreadClient.sendMessage(sendChatMessageOptions);
String chatMessageId = sendChatMessageResult.getId();
```

#### Get a chat message

The `getMessage` method retrieves a chat message from the service.
`chatMessageId` is the unique ID of the chat message.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L157-L158 -->
```Java
String chatMessageId = "Id";
ChatMessage chatMessage = chatThreadClient.getMessage(chatMessageId);
```

#### Get chat messages

You can retrieve chat messages using the `listMessages` method on the chat thread client at specified intervals (polling).

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L167-L174 -->
```Java
PagedIterable<ChatMessage> chatMessagesResponse = chatThreadClient.listMessages();
chatMessagesResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(message -> {
        System.out.printf("Message id is %s.", message.getId());
    });
});
```

`listMessages` returns the latest version of the message, including any edits or deletes that happened to the message using `.editMessage()` and `.deleteMessage()`. 

For deleted messages, `chatMessage.getDeletedOn()` returns a datetime value indicating when that message was deleted. 

For edited messages, `chatMessage.getEditedOn()` returns a datetime indicating when the message was edited. 

The original time of message creation can be accessed using `chatMessage.getCreatedOn()`, and it can be used for ordering the messages.

listMessages returns different types of messages which can be identified by `chatMessage.getType()`. These types are:

-`Text`: Regular chat message sent by a thread member.

-`ThreadActivity/TopicUpdate`: System message that indicates the topic has been updated.

-`ThreadActivity/AddMember`: System message that indicates one or more members have been added to the chat thread.

-`ThreadActivity/DeleteMember`: System message that indicates a member has been removed from the chat thread.

For more details, see [Message Types](https://docs.microsoft.com/azure/communication-services/concepts/chat/concepts#message-types).

#### Update a chat message

Use `updateMessage` to update a chat message identified by chatThreadId and messageId.
`chatMessageId` is the unique ID of the chat message.
`updateChatMessageOptions` is used to describe the request of a chat message update, an example is shown in the code snippet below.

- Use `content` to provide a new chat message content;

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L183-L187 -->
```Java
String chatMessageId = "Id";
UpdateChatMessageOptions updateChatMessageOptions = new UpdateChatMessageOptions()
    .setContent("Updated message content");

chatThreadClient.updateMessage(chatMessageId, updateChatMessageOptions);
```

#### Delete a chat message

Use `updateMessage` to update a chat message identified by chatThreadId and chatMessageId.
`chatMessageId` is the unique ID of the chat message.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L196-L197 -->
```Java
String chatMessageId = "Id";
chatThreadClient.deleteMessage(chatMessageId);
```

### Chat Thread Member Operations

#### List chat thread members

Use `listMembers` to retrieve a paged collection containing the members of the chat thread identified by chatThreadId.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L206-L213 -->
```Java
PagedIterable<ChatThreadMember> chatThreadMembersResponse = chatThreadClient.listMembers();
chatThreadMembersResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(chatMember -> {
        System.out.printf("Member id is %s.", chatMember.getUser().getId());
    });
});
```

#### Add thread members

Use `addMembers` method to add thread members to the thread identified by threadId.
`addChatThreadMembersOptions` describes the request object containing the members to be added; Use `.setMembers()` to set the thread members to be added to the thread;

- `user`, required, is the CommunicationUser you've created by using the CommunicationIdentityClient. More info at: [Create A User](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens?pivots=programming-language-java#create-a-user).
- `display_name`, optional, is the display name for the thread member.
- `share_history_time`, optional, is the time from which the chat history is shared with the member. To share history since the inception of the chat thread, set this property to any date equal to, or less than the thread creation time. To share no history previous to when the member was added, set it to the current date. To share partial history, set it to the required date.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L225-L240 -->
```Java
List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();

ChatThreadMember firstThreadMember = new ChatThreadMember()
    .setUser(user1)
    .setDisplayName("Display Name 1");

ChatThreadMember secondThreadMember = new ChatThreadMember()
    .setUser(user2)
    .setDisplayName("Display Name 2");

members.add(firstThreadMember);
members.add(secondThreadMember);

AddChatThreadMembersOptions addChatThreadMembersOptions = new AddChatThreadMembersOptions()
    .setMembers(members);
chatThreadClient.addMembers(addChatThreadMembersOptions);
```

#### Remove chat thread member

Use `removeMember` method to remove chat thread member from the chat thread identified by chatThreadId.
`user` is the CommunicationUser you've created.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L251-L251 -->
```Java
chatThreadClient.removeMember(user);
```

### Read Receipt Operations

#### Send read receipt

Use `sendReadReceipt` method to post a read receipt event to a chat thread, on behalf of a user.
`chatMessageId` is the unique ID of the chat message that was read.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L260-L261 -->
```Java
String chatMessageId = "Id";
chatThreadClient.sendReadReceipt(chatMessageId);
```

#### Get read receipts

`getReadReceipts` method retreives read receipts for a chat thread.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L270-L277 -->
```Java
PagedIterable<ReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();
readReceiptsResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(readReceipt -> {
        System.out.printf("Read message id is %s.", readReceipt.getChatMessageId());
    });
});
```

### Typing Notification Operations

#### Send typing notification

Use `sendTypingNotification` method to post a typing notification event to a chat thread, on behalf of a user.

<!-- embedme ./src/samples/java/com/azure/communication/chat/ReadmeSamples.java#L286-L286 -->
```Java
chatThreadClient.sendTypingNotification();
```



## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure communication service

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-chat
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/communication/azure-communication-chat/src
