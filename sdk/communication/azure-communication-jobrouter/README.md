# Azure Communication Job Router client library for Java

Azure Communication Job Router contains the APIs used in job router applications for Azure Communication Services.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.
### Include the package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-jobrouter</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-chat;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-jobrouter</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

## Key concepts

### Job

A Job is a unit of work (demand), which must be routed to an available Worker (supply). A real-world example is an incoming call or chat in the context of a call center.

### Worker

A Worker is the supply available to handle a Job. When you use the SDK to register a Worker to receive jobs, you can specify:

- One or more queues to listen on.
- The number of concurrent jobs per Channel that the Worker can handle.
- A set of Labels that can be used to group and select workers.

A real-world example is an agent in a call center.

### Queue

A Queue is an ordered list of jobs, that are waiting to be served to a worker. Workers register with a queue to receive work from it.

A real-world example is a call queue in a call center.

### Channel

A Channel is a grouping of jobs by some type. When a worker registers to receive work, they must also specify for which channels they can handle work, and how much of each can they handle concurrently. Channels are just a string discriminator and aren't explicitly created.

Real-world examples are `voice calls` or `chats` in a call center.

### Offer

An Offer is extended by Job Router to a worker to handle a particular job when it determines a match. You can either accept or decline the offer with the JobRouter SDK. If you ignore the offer, it expires according to the time to live configured on the Distribution Policy.

A real-world example is the ringing of an agent in a call center.


### Distribution Policy

A Distribution Policy is a configuration set that controls how jobs in a queue are distributed to workers registered with that queue. This configuration includes:

- How long an Offer is valid before it expires.
- The distribution mode, which define the order in which workers are picked when there are multiple available.
- How many concurrent offers can there be for a given job.

### Labels

You can attach labels to workers, jobs, and queues. Labels are key value pairs that can be of `string`, `number`, or `boolean` data types.

A real-world example is the skill level of a particular worker or the team or geographic location.

### Worker selectors

Worker selectors can be attached to a job in order to target a subset of workers on the queue.

A real-world example is a condition on an incoming call that the agent must have a minimum level of knowledge of a particular product.

### Classification policy

A classification policy can be used to programmatically select a queue, determine job priority, or attach worker label selectors to a job.

### Queue selectors

Queue selectors can be attached to a classification policy in order to target a queue which fulfills certain conditions.
This queue is used enqueueing an incoming job.

A real-world example is a condition on an incoming call that the call has to get queued to a queue which supports `chat`.


### Exception policy

An exception policy controls the behavior of a Job based on a trigger and executes a desired action. The exception policy is attached to a Queue so it can control the behavior of Jobs in the Queue.


## Examples

### Client Initialization
To initialize the SMS Client, the connection string can be used to instantiate.
Alternatively, you can also use Active Directory authentication using DefaultAzureCredential.

```java sample-client
RouterClient routerClient = new RouterClientBuilder()
    .connectionString(connectionString);
    .buildClient();
```

### Create or update exception policy

Upsert an exception policy using `RouterClient` created from builder.

```java 

```

- `identifier`: Id of the exception policy.
- `exception_rules`: (Optional) A dictionary collection of exception rules on the exception policy. Key is the Id of each exception rule.
- `name`: (Optional) An user-friendly name of the policy.
- `exception_policy`: (Optional) An instance of exception policy. If this is provided, then upsert request will be made using this. Generally it is expected to be used when updating an existing policy.

### Get an exception policy

Using `RouterClient` retrieve an existing exception policy.

```java

```

- `identifier`: Id of the exception policy.

### List exception policies

Using `RouterClient` to retrieve a list of exception policies that have been already created.

```java

```
- `results_per_page`: (Optional) The maximum number of policies to be returned per page.

### Delete an exception policy

Using `RouterClient` delete an exception policy.

```java

```

- `identifier`: Id of the exception policy.

## Troubleshooting

Running into issues? This section should contain details as to what to do there.

## Next steps
- [Read more about Router in Azure Communication Services][router_concepts]

### More sample code
Please take a look at the [samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-jobrouter/samples) directory for detailed examples of how to use this library.


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

- [Create the Chat Client](#create-the-chat-client)
- [Chat Thread Operations](#chat-thread-operations)
- [Chat Message Operations](#chat-message-operations)
- [Chat Thread Participant Operations](#chat-thread-participant-operations)
- [Read Receipt Operations](#read-receipt-operations)
- [Typing Notification Operations](#typing-notification-operations)

### Create the Chat Client

```java readme-sample-createChatClient
String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

// Your user access token retrieved from your trusted service
String token = "SECRET";
CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

// Initialize the chat client
final ChatClientBuilder builder = new ChatClientBuilder();
builder.endpoint(endpoint)
    .credential(credential);
ChatClient chatClient = builder.buildClient();
```

### Chat Thread Operations

#### Create a chat thread

To create a chat client, you will use the Communications Service endpoint and the access token that was generated as part of pre-requisite steps. User access tokens enable you to build client applications that directly authenticate to Azure Communication Services. Once you generate these tokens on your server, pass them back to a client device. You need to use the CommunicationTokenCredential class from the Common SDK to pass the token to your chat client.

Use the `createChatThread` method to create a chat thread.
`createChatThreadOptions` is used to describe the thread request, an example is shown in the code snippet below.

- Use `topic` to give a thread topic;
- Use `participants` to list the thread participants to be added to the thread;

`CreateChatThreadResult` is the response returned from creating a chat thread.
It contains a `getChatThread()` method which returns the `ChatThread` object that can be used to get the thread client from which you can get the `ChatThreadClient` for performing operations on the created thread: add participants, send message, etc.
The `ChatThread` object also contains the `getId()` method which retrieves the unique ID of the thread.

```java readme-sample-createChatThread
List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

ChatParticipant firstParticipant = new ChatParticipant()
    .setCommunicationIdentifier(user1)
    .setDisplayName("Participant Display Name 1");

ChatParticipant secondParticipant = new ChatParticipant()
    .setCommunicationIdentifier(user2)
    .setDisplayName("Participant Display Name 2");

participants.add(firstParticipant);
participants.add(secondParticipant);

CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions("Topic")
    .setParticipants(participants);
CreateChatThreadResult result = chatClient.createChatThread(createChatThreadOptions);

String chatThreadId = result.getChatThread().getId();
```

#### Get a chat thread properties

The `getChatThreadProperties` method retrieves a thread's properties from the service.

```java readme-sample-getChatThread
ChatThreadClient chatThreadClient = chatClient.getChatThreadClient("Id");
ChatThreadProperties chatThreadProperties = chatThreadClient.getProperties();
```

#### Delete a thread

Use `deleteChatThread` method to delete a chat thread
`chatThreadId` is the unique ID of the chat thread.

```java readme-sample-deleteChatThread
String chatThreadId = "Id";
chatClient.deleteChatThread(chatThreadId);
```

#### Get a chat thread client

The `getChatThreadClient` method returns a thread client for a thread that already exists. It can be used for performing operations on the created thread: add participants, send message, etc.
`chatThreadId` is the unique ID of the existing chat thread.

```java readme-sample-getChatThreadClient
String chatThreadId = "Id";
ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);
```

#### Update a chat thread topic

Use `updateTopic` method to update a thread's topic
`topic` is used to hold the new topic of the thread.

```java readme-sample-updateTopic
chatThreadClient.updateTopic("New Topic");
```

### Chat Message Operations

#### Send a chat message

Use the `sendMessage` method to send a chat message to the chat thread that the `chatThreadClient` was created with.
`sendChatMessageOptions` is used to describe the chat message request, an example is shown in the code snippet below.

- Use `content` to provide the chat message content;
- Use `priority` to specify the chat message priority level, such as 'Normal' or 'High';
- Use `senderDisplayName` to specify the display name of the sender;

A `SendChatMessageResult` response returned from sending a chat message, it contains an id, which is the unique ID of the message.

```java readme-sample-sendChatMessage
SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
    .setContent("Message content")
    .setSenderDisplayName("Sender Display Name");

SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions);
```

#### Get a chat message

The `getMessage` method retrieves a chat message from the service.
`chatMessageId` is the unique ID of the chat message.

```java readme-sample-getChatMessage
String chatMessageId = "Id";
ChatMessage chatMessage = chatThreadClient.getMessage(chatMessageId);
```

#### Get chat messages

You can retrieve chat messages using the `listMessages` method on the chat thread client at specified intervals (polling).

```java readme-sample-getChatMessages
PagedIterable<ChatMessage> chatMessagesResponse = chatThreadClient.listMessages();
chatMessagesResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getElements().forEach(message ->
        System.out.printf("Message id is %s.", message.getId()));
});
```

`listMessages` returns the latest version of the message, including any edits or deletes that happened to the message using `.editMessage()` and `.deleteMessage()`.

For deleted messages, `chatMessage.getDeletedOn()` returns a datetime value indicating when that message was deleted.

For edited messages, `chatMessage.getEditedOn()` returns a datetime indicating when the message was edited.

The original time of message creation can be accessed using `chatMessage.getCreatedOn()`, and it can be used for ordering the messages.

listMessages returns different types of messages which can be identified by `chatMessage.getType()`. These types are:

- `text`: Regular chat message sent by a thread participant.

- `html`: HTML chat message sent by a thread participant.

- `topicUpdated`: System message that indicates the topic has been updated.

- `participantAdded`: System message that indicates one or more participants have been added to the chat thread.

- `participantRemoved`: System message that indicates a participant has been removed from the chat thread.

For more details, see [Message Types](https://docs.microsoft.com/azure/communication-services/concepts/chat/concepts#message-types).

#### Update a chat message

Use `updateMessage` to update a chat message identified by chatThreadId and messageId.
`chatMessageId` is the unique ID of the chat message.
`updateChatMessageOptions` is used to describe the request of a chat message update, an example is shown in the code snippet below.

- Use `content` to provide a new chat message content;

```java readme-sample-updateChatMessage
String chatMessageId = "Id";
UpdateChatMessageOptions updateChatMessageOptions = new UpdateChatMessageOptions()
    .setContent("Updated message content");

chatThreadClient.updateMessage(chatMessageId, updateChatMessageOptions);
```

#### Delete a chat message

Use `updateMessage` to update a chat message identified by chatMessageId.
`chatMessageId` is the unique ID of the chat message.

```java readme-sample-deleteChatMessage
String chatMessageId = "Id";
chatThreadClient.deleteMessage(chatMessageId);
```

### Chat Thread Participant Operations

#### List chat participants

Use `listParticipants` to retrieve a paged collection containing the participants of the chat thread.

```java readme-sample-listChatParticipants
PagedIterable<ChatParticipant> chatParticipantsResponse = chatThreadClient.listParticipants();
chatParticipantsResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getElements().forEach(chatParticipant ->
        System.out.printf("Participant id is %s.", ((CommunicationUserIdentifier) chatParticipant.getCommunicationIdentifier()).getId()));
});
```

#### Add participants

Use `addParticipants` method to add participants to the chat thread.
`participants` list of participants to be added to the thread;

- `communicationIdentifier`, required, is the CommunicationIdentifier you've created by using the CommunicationIdentityClient. More info at: [Create A User](https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens?pivots=programming-language-java#create-a-user).
- `display_name`, optional, is the display name for the thread member.
- `share_history_time`, optional, is the time from which the chat history is shared with the member. To share history since the inception of the chat thread, set this property to any date equal to, or less than the thread creation time. To share no history previous to when the member was added, set it to the current date. To share partial history, set it to the required date.

```java readme-sample-addChatParticipants
List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

ChatParticipant firstParticipant = new ChatParticipant()
    .setCommunicationIdentifier(user1)
    .setDisplayName("Display Name 1");

ChatParticipant secondParticipant = new ChatParticipant()
    .setCommunicationIdentifier(user2)
    .setDisplayName("Display Name 2");

participants.add(firstParticipant);
participants.add(secondParticipant);

chatThreadClient.addParticipants(participants);
```

#### Remove participant

Use `removeParticipant` method to remove a participant from the chat thread.
`identifier` is the CommunicationIdentifier you've created.

```java readme-sample-removeChatParticipant
chatThreadClient.removeParticipant(user);
```

### Read Receipt Operations

#### Send read receipt

Use `sendReadReceipt` method to post a read receipt event to a chat thread, on behalf of a user.
`chatMessageId` is the unique ID of the chat message that was read.

```java readme-sample-sendReadReceipt
String chatMessageId = "Id";
chatThreadClient.sendReadReceipt(chatMessageId);
```

#### Get read receipts

`getReadReceipts` method retrieves read receipts for a chat thread.

```java readme-sample-listReadReceipts
PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();
readReceiptsResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getElements().forEach(readReceipt ->
        System.out.printf("Read message id is %s.", readReceipt.getChatMessageId()));
});
```

### Typing Notification Operations

#### Send typing notification

Use `sendTypingNotification` method to post a typing notification event to a chat thread, on behalf of a user.
`typingNotificationOptions` is used to describe the typing notification request.

- Use `senderDisplayName` to set the display name of the notification sender;

```java readme-sample-sendTypingNotification
TypingNotificationOptions options = new TypingNotificationOptions();
options.setSenderDisplayName("Sender Display Name");
chatThreadClient.sendTypingNotificationWithResponse(options, Context.NONE);
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
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-chat/src
