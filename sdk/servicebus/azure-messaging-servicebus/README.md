# Azure Service Bus client library for Java

Microsoft Azure Service Bus is a fully managed enterprise integration message broker. Service Bus can decouple
applications and services. Service Bus offers a reliable and secure platform for asynchronous transfer of data and
state. Data is transferred between different applications and services using messages. If you would like to know more
about Azure Service Bus, you may wish to review: [What is Service Bus][product_docs]

The Azure Service Bus client library allows for sending and receiving of Azure Service Bus messages and may be used to:
- Transfer business data, such as sales or purchase orders, journals, or inventory movements.
- Decouple applications to improve reliability and scalability of applications and services. Clients and services don't
have to be online at the same time.
- Enable 1:n relationships between publishers and subscribers.
- Implement workflows that require message ordering or message deferral.

[Source code][source_code] | [API reference documentation][api_documentation]
| [Product documentation][product_docs] | [Samples][sample_examples] | [Package (Maven)][maven_package]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][java_development_kit] with version 8 or above
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: [https://azure.microsoft.com](https://azure.microsoft.com)
- Azure Service Bus instance
  - Step-by-step guide for [creating a Service Bus instance using Azure Portal][service_bus_create]

To quickly create the needed Service Bus resources in Azure and to receive a connection string for them, you can deploy our sample template by clicking:

[![](http://azuredeploy.net/deploybutton.png)](https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fraw.githubusercontent.com%2FAzure%2Fazure-sdk-for-net%2Fmaster%2Fsdk%2Fservicebus%2FAzure.Messaging.ServiceBus%2Fassets%2Fsamples-azure-deploy.json)

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
    <artifactId>azure-messaging-servicebus</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-messaging-servicebus;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.17.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

For the Service Bus client library to interact with Service Bus, it will need to understand how to connect and authorize
with it.

#### Create a Service Bus client using Azure Active Directory (Azure AD)

Azure SDK for Java supports the Azure Identity client library, making it simple to get credentials from Azure AD. 
First, add the package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

- Known Issue: The pom.xml file should list `azure-messaging-servicebus` before `azure-identity` client libraries. This
  issue is resolved with `azure-identity:1.2.1`.
  Check [here][known-issue-binarydata-notfound] for more details.

The implemented ways to request a credential are under the `com.azure.identity.credential` package. The sample below
shows how to use an Azure Active Directory (AAD) application client secret to authorize with Azure Service Bus.

##### Authorizing with DefaultAzureCredential

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Service Bus, please
refer to [the associated documentation][aad_authorization].

Use the returned token credential to authenticate the client:

```java com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation
TokenCredential credential = new DefaultAzureCredentialBuilder().build();

// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// 'disableAutoComplete' indicates that users will explicitly settle their message.
ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, credential)
    .receiver()
    .disableAutoComplete()
    .queueName(queueName)
    .buildAsyncClient();

// When users are done with the receiver, dispose of the receiver.
// Clients should be long-lived objects as they require resources
// and time to establish a connection to the service.
asyncReceiver.close();
```

## Key concepts

You can interact with the primary resource types within a Service Bus Namespace, of which multiple can exist and
on which actual message transmission takes place. The namespace often serves as an application container:

* A **[queue][queue_concept]** allows for the sending and receiving of messages, ordered first-in-first-out. It is often
  used for point-to-point communication.
* A **[topic][topic_concept]** is better suited to publisher and subscriber scenarios. A topic publishes messages to
  subscriptions, of which, multiple can exist simultaneously.
* A **[subscription][subscription_concept]** receives messages from a topic. Each subscription is independent and
  receives a copy of the message sent to the topic.

### Service Bus Clients
The builder [`ServiceBusClientBuilder`][ServiceBusClientBuilder] is used to create all the Service Bus clients.

* **[`ServiceBusSenderClient`][ServiceBusSenderClient]** A <b>synchronous</b> sender responsible for sending
[`ServiceBusMessage`][ServiceBusMessage] to specific queue or topic on Azure Service Bus.
* **[`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient]** A <b>asynchronous</b> sender responsible for sending
[`ServiceBusMessage`][ServiceBusMessage] to specific queue or topic on Azure Service Bus.
* **[`ServiceBusReceiverClient`][ServiceBusReceiverClient]** A <b>synchronous</b> receiver responsible for receiving
 [`ServiceBusMessage`][ServiceBusMessage] from a specific queue or topic on Azure Service Bus.
* **[`ServiceBusReceiverAsyncClient`][ServiceBusReceiverAsyncClient]** A <b>asynchronous</b> receiver responsible for
receiving [`ServiceBusMessage`][ServiceBusMessage] from a specific queue or topic on Azure Service Bus.

## Examples
 - [Send messages](#send-messages)
 - [Receive messages](#receive-messages)
 - [Send and receive from session enabled queues or topics](#send-and-receive-from-session-enabled-queues-or-topics)
 - [Create a dead-letter queue Receiver](#create-a-dead-letter-queue-receiver)
 - [Sharing a connection between clients](#sharing-of-connection-between-clients)
### Send messages

You'll need to create an asynchronous [`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient] or a synchronous
[`ServiceBusSenderClient`][ServiceBusSenderClient] to send messages. Each sender can send messages to either a queue or
a topic.

The snippet below creates a synchronous [`ServiceBusSenderClient`][ServiceBusSenderClient] to publish a message to a
queue.

```java com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch
TokenCredential credential = new DefaultAzureCredentialBuilder().build();

// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, credential)
    .sender()
    .queueName(queueName)
    .buildClient();

List<ServiceBusMessage> messages = Arrays.asList(
    new ServiceBusMessage("test-1"),
    new ServiceBusMessage("test-2"));

// Creating a batch without options set.
ServiceBusMessageBatch batch = sender.createMessageBatch();
for (ServiceBusMessage message : messages) {
    if (batch.tryAddMessage(message)) {
        continue;
    }

    // The batch is full. Send the current batch and create a new one.
    sender.sendMessages(batch);

    batch = sender.createMessageBatch();

    // Add the message we couldn't before.
    if (!batch.tryAddMessage(message)) {
        throw new IllegalArgumentException("Message is too large for an empty batch.");
    }
}

// Send the final batch if there are any messages in it.
if (batch.getCount() > 0) {
    sender.sendMessages(batch);
}

// Continue using the sender and finally, dispose of the sender.
// Clients should be long-lived objects as they require resources
// and time to establish a connection to the service.
sender.close();
```

### Receive messages

To receive messages, you will need to create a `ServiceBusProcessorClient` with callbacks for incoming messages and any error that occurs in the process. You can then start and stop the client as required.

When receiving message with [PeekLock][peek_lock_mode_docs] mode, it tells the broker that the application logic wants to settle (e.g. complete, abandon) received messages explicitly.

```java com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation
// Function that gets called whenever a message is received.
Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
    final ServiceBusReceivedMessage message = context.getMessage();
    // Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
    // handling message reaches desired state such that it doesn't require Service Bus to redeliver
    // the same message, then context.complete() should be called otherwise context.abandon().
    final boolean success = Math.random() < 0.5;
    if (success) {
        try {
            context.complete();
        } catch (RuntimeException error) {
            System.out.printf("Completion of the message %s failed.%n Error: %s%n",
                message.getMessageId(), error);
        }
    } else {
        try {
            context.abandon();
        } catch (RuntimeException error) {
            System.out.printf("Abandoning of the message %s failed.%nError: %s%n",
                message.getMessageId(), error);
        }
    }
};

// Sample code that gets called if there's an error
Consumer<ServiceBusErrorContext> processError = errorContext -> {
    if (errorContext.getException() instanceof ServiceBusException) {
        ServiceBusException exception = (ServiceBusException) errorContext.getException();

        System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
            exception.getReason());
    } else {
        System.out.printf("Error occurred: %s%n", errorContext.getException());
    }
};

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Create the processor client via the builder and its sub-builder
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, tokenCredential)
    .processor()
    .queueName(queueName)
    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
    .disableAutoComplete()  // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
    .processMessage(processMessage)
    .processError(processError)
    .disableAutoComplete()
    .buildProcessorClient();

// Starts the processor in the background. Control returns immediately.
processorClient.start();

// Stop processor and dispose when done processing messages.
processorClient.stop();
processorClient.close();
```

When receiving message with [ReceiveAndDelete][receive_and_delete_mode_docs] mode, tells the broker to consider all messages it sends to the receiving client as settled when sent.

```java com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation
// Function that gets called whenever a message is received.
Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
    final ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
        message.getSessionId(), message.getSequenceNumber(), message.getBody());
};

// Sample code that gets called if there's an error
Consumer<ServiceBusErrorContext> processError = errorContext -> {
    if (errorContext.getException() instanceof ServiceBusException) {
        ServiceBusException exception = (ServiceBusException) errorContext.getException();

        System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
            exception.getReason());
    } else {
        System.out.printf("Error occurred: %s%n", errorContext.getException());
    }
};

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Create the processor client via the builder and its sub-builder
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// 'disableAutoComplete()' will opt in to manual settlement (e.g. complete, abandon).
ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, tokenCredential)
    .processor()
    .queueName(queueName)
    .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
    .processMessage(processMessage)
    .processError(processError)
    .disableAutoComplete()
    .buildProcessorClient();

// Starts the processor in the background. Control returns immediately.
processorClient.start();

// Stop processor and dispose when done processing messages.
processorClient.stop();
processorClient.close();
```

There are four ways of settling messages using the methods on the message context passed to your callback.
  - Complete - causes the message to be deleted from the queue or topic.
  - Abandon - releases the receiver's lock on the message allowing for the message to be received by other receivers.
  - Defer - defers the message from being received by normal means. In order to receive deferred messages, the sequence
number of the message needs to be retained.
  - Dead-letter - moves the message to the [dead-letter queue][deadletterqueue_docs]. This will prevent the message from
    being received again. In order to receive messages from the dead-letter queue, a receiver scoped to the dead-letter
    queue is needed.

### Send and receive from session enabled queues or topics

> Using sessions requires you to create a session enabled queue or subscription. You can read more about how to
> configure this in "[Message sessions][message-sessions]".

Azure Service Bus sessions enable joint and ordered handling of unbounded sequences of related messages. Sessions can be
used in first in, first out (FIFO) and request-response patterns. Any sender can create a session when submitting
messages into a topic or queue by setting the `ServiceBusMessage.setSessionId(String)` property to some
application-defined identifier that is unique to the session.

Unlike non-session-enabled queues or subscriptions, only a single receiver can read from a session at any time. When a
receiver fetches a session, Service Bus locks the session for that receiver, and it has exclusive access to messages in
that session.

#### Send a message to a session

Create a [`ServiceBusSenderClient`][ServiceBusSenderClient] for a session enabled queue or topic subscription. Setting
`ServiceBusMessage.setSessionId(String)` on a `ServiceBusMessage` will publish the message to that session. If the
session does not exist, it is created.

```java com.azure.messaging.servicebus.servicebussenderclient.sendMessage-session
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
    .sender()
    .queueName(sessionEnabledQueueName)
    .buildClient();

// Setting sessionId publishes that message to a specific session, in this case, "greeting".
ServiceBusMessage message = new ServiceBusMessage("Hello world")
    .setSessionId("greetings");

sender.sendMessage(message);

// Dispose of the sender.
sender.close();
```

#### Receive messages from a session

Receiving messages from sessions is similar to receiving messages from a non session enabled queue or subscription. The difference is in the builder and the class you use.

In non-session case, you would use the sub builder `processor()`. In case of sessions, you would use the sub builder `sessionProcessor()`. Both sub builders will create an instance of `ServiceBusProcessorClient` configured to work on a session or a non-session Service Bus entity. In the case of the session processor, you can pass the maximum number of sessions you want the processor to process concurrently as well.

### Create a dead-letter queue Receiver

Azure Service Bus queues and topic subscriptions provide a secondary sub-queue, called a dead-letter queue (DLQ).
The dead-letter queue doesn't need to be explicitly created and can't be deleted or otherwise managed independent
of the main entity. For session enabled or non-session queue or topic subscriptions, the dead-letter receiver can be
created the same way as shown below. Learn more about dead-letter queue [here][dead-letter-queue].

```java com.azure.messaging.servicebus.servicebusreceiverclient.instantiation-deadLetterQueue
TokenCredential credential = new DefaultAzureCredentialBuilder().build();

// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// 'disableAutoComplete' indicates that users will explicitly settle their message.
ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, credential)
    .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
    .topicName(topicName)
    .subscriptionName(subscriptionName)
    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
    .buildClient();

// When users are done with the receiver, dispose of the receiver.
// Clients should be long-lived objects as they require resources
// and time to establish a connection to the service.
receiver.close();
```

### When to use `ServiceBusProcessorClient`

When to use `ServiceBusProcessorClient`, `ServiceBusReceiverClient` or `ServiceBusReceiverAsyncClient`? 
 
The best option for receiving messages in most common cases is `ServiceBusProcessorClient`. The processor can handle errors automatically and is designed to receive messages continuously. The processor has a simple API to set concurrency for processing messages in parallel.

The low-level client, `ServiceBusReceiverAsyncClient`, is for advanced users who want more control and flexibility over their Reactive application at the expense of more complexity in the application. Unlike the processor, the low-level `ServiceBusReceiverAsyncClient` does not have automatic recovery built into it. The reactive application using this client needs to deal with terminal events and choose the operators (to add recovery, manage backpressure, threading) in the Reactor chain. As stated earlier, for common asynchronous receive situations, the `ServiceBusProcessorClient` should be the first choice.

`ServiceBusReceiverClient` usage is discouraged unless you want to have pull semantics or want to migrate existing code base that uses synchronous receiver approach. For high-throughput and parallel message processing use cases, use `ServiceBusProcessorClient`.

### Sharing of connection between clients

All the clients created from a shared `ServiceBusClientBuilder` instance will share the same connection to the Service Bus namespace.


While using shared connection allows multiplexing operations among clients on one connection, sharing can also become a bottleneck if there are many clients, or the clients together generate high load. Each connection has one IO-Thread associated with it. The clients put their work on this shared IO-Thread's work-queue and the progress of each client depends on the timely completion of its work. The IO-Thread handles the enqueued work serially, which means, if the IO-Thread work-queue ends up with a lot of pending work to deal with, then it can be manifested as clients stalling, that leads to timeout, lost lock or slowdown in recovery path. Hence, if the application load to a Service Bus endpoint is reasonably high (in terms of overall number of sent-received messages or payload size), use a separate builder instance for each client you build. For example, for each entity (queue or topic and subscription), you can create a new `ServiceBusClientBuilder` and a build a client from it. In case of extremely high load to a specific entity, you might want to either create multiple client instances for that entity or run clients in multiple hosts (e.g., containers, VM) to load balance.

In the following example, a sender and receiver clients are built from a shared top-level builder, which means they use the same connection.

```java com.azure.messaging.servicebus.connection.sharing
TokenCredential credential = new DefaultAzureCredentialBuilder().build();

// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// Any clients created from this builder will share the underlying connection.
ServiceBusClientBuilder sharedConnectionBuilder = new ServiceBusClientBuilder()
    .credential(fullyQualifiedNamespace, credential);

// Create receiver and sender which will share the connection.
ServiceBusReceiverClient receiver = sharedConnectionBuilder
    .receiver()
    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
    .queueName(queueName)
    .buildClient();
ServiceBusSenderClient sender = sharedConnectionBuilder
    .sender()
    .queueName(queueName)
    .buildClient();

// Use the clients and finally close them.
try {
    sender.sendMessage(new ServiceBusMessage("payload"));
    receiver.receiveMessages(1);
} finally {
    // Clients should be long-lived objects as they require resources
    // and time to establish a connection to the service.
    sender.close();
    receiver.close();
}
```

## Troubleshooting

### Enable client logging

Azure SDK for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable AMQP transport logging

If enabling client logging is not enough to diagnose your issues. You can enable logging to a file in the underlying
AMQP library, [Qpid Proton-J][qpid_proton_j_apache]. Qpid Proton-J uses `java.util.logging`. You can enable logging by
create a configuration file with the contents below. Or set `proton.trace.level=ALL` and whichever configuration options
you want for the `java.util.logging.Handler` implementation. Implementation classes and their options can be found in
[Java 8 SDK javadoc][java_8_sdk_javadocs].

To trace the AMQP transport frames, set the environment variable: `PN_TRACE_FRM=1`.

#### Sample "logging.properties" file

The configuration file below logs trace output from proton-j to the file "proton-trace.log".

```
handlers=java.util.logging.FileHandler
.level=OFF
proton.trace.level=ALL
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=proton-trace.log
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=[%1$tF %1$tr] %3$s %4$s: %5$s %n
```

### Common exceptions

#### AMQP exception

This is a general exception for AMQP related failures, which includes the AMQP errors as `ErrorCondition` and the
context that caused this exception as `AmqpErrorContext`. `isTransient` is a boolean indicating if the exception is a
transient error or not. If a transient AMQP exception occurs, the client library retries the operation as many times
as the [AmqpRetryOptions][AmqpRetryOptions] allows. Afterwords, the operation fails and an exception is propagated back
to the user.

[`AmqpErrorCondition`][AmqpErrorCondition] contains error conditions common to the AMQP protocol and used by Azure
services. When an AMQP exception is thrown, examining the error condition field can inform developers as to why the AMQP
exception occurred and if possible, how to mitigate this exception. A list of all the AMQP exceptions can be found in
[OASIS AMQP Version 1.0 Transport Errors][oasis_amqp_v1_error].

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Service Bus Messaging Exceptions][servicebus_messaging_exceptions] guidance.

### Understanding the APIs behavior

The document [here][sync_receivemessages_implicit_prefetch] provides insights into the expected behavior of synchronous `receiveMessages` API when using it to obtain more than one message (a.k.a. implicit prefetching).

## Next steps

Beyond those discussed, the Azure Service Bus client library offers support for many additional scenarios to help take
advantage of the full feature set of the Azure Service Bus service. In order to help explore some of these scenarios,
the following set of sample is available [here][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) for more information.

<!-- Links -->
[aad_authorization]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[AmqpErrorCondition]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[AmqpRetryOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[api_documentation]: https://aka.ms/java-docs
[dead-letter-queue]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[deadletterqueue_docs]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[java_development_kit]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[maven]: https://maven.apache.org/
[maven_package]: https://central.sonatype.com/artifact/com.azure/azure-messaging-servicebus
[message-sessions]: https://docs.microsoft.com/azure/service-bus-messaging/message-sessions
[oasis_amqp_v1_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[product_docs]: https://docs.microsoft.com/azure/service-bus-messaging
[qpid_proton_j_apache]: https://qpid.apache.org/proton/
[queue_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#queues
[ReceiveMode]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/models/ReceiveMode.java
[RetryOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus
[service_bus_connection_string]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal#get-the-connection-string
[servicebus_create]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[servicebus_messaging_exceptions]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-exceptions
[servicebus_roles]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application#built-in-rbac-roles-for-azure-service-bus
[ServiceBusClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusClientBuilder.java
[ServiceBusMessage]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusMessage.java
[ServiceBusReceiverAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverAsyncClient.java
[ServiceBusReceiverClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverClient.java
[ServiceBusSenderAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusSenderAsyncClient.java
[ServiceBusSenderClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusSenderClient.java
[service_bus_create]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src
[subscription_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions
[topic_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#topics
[wiki_identity]: https://learn.microsoft.com/azure/developer/java/sdk/identity
[known-issue-binarydata-notfound]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/known-issues.md#can-not-resolve-binarydata-or-noclassdeffounderror-version-700
[sync_receivemessages_implicit_prefetch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/docs/SyncReceiveAndPrefetch.md
[peek_lock_mode_docs]: https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock
[receive_and_delete_mode_docs]: https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#receiveanddelete
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FREADME.png)
