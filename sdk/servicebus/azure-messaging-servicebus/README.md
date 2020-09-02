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
| [Product documentation][product_docs]| [Samples][sample_examples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][java_development_kit] with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: https://azure.microsoft.com
- Azure Service Bus instance
  - Step-by-step guide for [creating a Service Bus instance using Azure Portal][service_bus_create]

To quickly create the needed Service Bus resources in Azure and to receive a connection string for them, you can deploy our sample template by clicking:

[![](http://azuredeploy.net/deploybutton.png)](https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fraw.githubusercontent.com%2FAzure%2Fazure-sdk-for-net%2Fmaster%2Fsdk%2Fservicebus%2FAzure.Messaging.ServiceBus%2Fassets%2Fsamples-azure-deploy.json)

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-messaging-servicebus;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.0.0-beta.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

For the Service Bus client library to interact with Service Bus, it will need to understand how to connect and authorize
with it.

#### Create Service Bus clients using a connection string

The easiest means for authenticating is to use a connection string, which automatically created when creating a Service Bus
namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the step-by-step guide to
[get a Service Bus connection string][service_bus_connection_string].

Both the asynchronous and synchronous Service Bus sender and receiver clients are instantiated using
`ServiceBusClientBuilder`. The snippets below create a synchronous Service Bus sender and an asynchronous receiver,
respectively.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L29-L33 -->
```java
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sender()
    .queueName("<< QUEUE NAME >>")
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L40-L45 -->
```java
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .buildAsyncClient();
```

#### Create a Service Bus client using Microsoft Identity platform (formerly Azure Active Directory)

Azure SDK for Java supports the Azure Identity package, making it simple to get credentials from the Microsoft identity
platform. First, add the package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.9</version>
</dependency>
```
[//]: # ({x-version-update-end})

The implemented ways to request a credential are under the `com.azure.identity.credential` package. The sample below
shows how to use an Azure Active Directory (AAD) application client secret to authorize with Azure Service Bus.

##### Authorizing with DefaultAzureCredential

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Service Bus, please
refer to [the associated documentation][aad_authorization].

Use the returned token credential to authenticate the client:

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L52-L58 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .build();
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .credential("<<fully-qualified-namespace>>", credential)
    .receiver()
    .queueName("<<queue-name>>")
    .buildAsyncClient();
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
 - [Settle messages](#settle-messages)
 - [Send and receive from session enabled queues or topics](#send-and-receive-from-session-enabled-queues-or-topics) 
 - [Create a dead-letter queue Receiver](#create-a-dead-letter-queue-receiver)
### Send messages

You'll need to create an asynchronous [`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient] or a synchronous
[`ServiceBusSenderClient`][ServiceBusSenderClient] to send messages. Each sender can send messages to either a queue or
a topic.

The snippet below creates a synchronous [`ServiceBusSenderClient`][ServiceBusSenderClient] to publish a message to a
queue.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L65-L77 -->
```java
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sender()
    .queueName("<< QUEUE NAME >>")
    .buildClient();
List<ServiceBusMessage> messages = Arrays.asList(
    new ServiceBusMessage("Hello world").setMessageId("1"),
    new ServiceBusMessage("Bonjour").setMessageId("2"));

sender.sendMessages(messages);

// When you are done using the sender, dispose of it.
sender.close();
```

### Receive messages

You'll need to create an asynchronous [`ServiceBusReceiverAsyncClient`][ServiceBusReceiverAsyncClient] or a synchronous
[`ServiceBusReceiverClient`][ServiceBusReceiverClient] to receive messages. Each receiver can consume messages from
either a queue or a topic subscription.

By default, the receive mode is [`ReceiveMode.PEEK_LOCK`][ReceiveMode]. This tells the broker that the receiving client
wants to manage settlement of received messages explicitly. The message is made available for the receiver to process,
while held under an exclusive lock in the service so that other, competing receivers cannot see it.
`ServiceBusReceivedMessage.getLockedUntil()` indicates when the lock expires and can be extended by the client using
`receiver.renewMessageLock()`.

#### Receive a batch of messages

The snippet below creates a [`ServiceBusReceiverClient`][ServiceBusReceiverClient] to receive messages from a topic
subscription.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L84-L101 -->
```java
ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .buildClient();

// Receives a batch of messages when 10 messages are received or until 30 seconds have elapsed, whichever
// happens first.
IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(10, Duration.ofSeconds(30));
messages.forEach(context -> {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Id: %s. Contents: %s%n", message.getMessageId(),
        new String(message.getBody(), StandardCharsets.UTF_8));
});

// When you are done using the receiver, dispose of it.
receiver.close();
```

#### Receive a stream of messages

The asynchronous [`ServiceBusReceiverAsyncClient`][ServiceBusReceiverAsyncClient] continuously fetches messages until
the `subscription` is disposed.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L108-L130 -->
```java
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .queueName("<< QUEUE NAME >>")
    .buildAsyncClient();

// receive() operation continuously fetches messages until the subscription is disposed.
// The stream is infinite, and completes when the subscription or receiver is closed.
Disposable subscription = receiver.receiveMessages().subscribe(context -> {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Id: %s%n", message.getMessageId());
    System.out.printf("Contents: %s%n", new String(message.getBody(), StandardCharsets.UTF_8));
}, error -> {
        System.err.println("Error occurred while receiving messages: " + error);
    }, () -> {
        System.out.println("Finished receiving messages.");
    });

// Continue application processing. When you are finished receiving messages, dispose of the subscription.
subscription.dispose();

// When you are done using the receiver, dispose of it.
receiver.close();
```

### Settle messages

When a message is received, it can be settled using any of the `complete()`, `abandon()`, `defer()`, or `deadLetter()`
overloads. The sample below completes a received message from synchronous
[`ServiceBusReceiverClient`][ServiceBusReceiverClient].

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L145-L151 -->
```java
// This fetches a batch of 10 messages or until the default operation timeout has elapsed.
receiver.receiveMessages(10).forEach(context -> {
    ServiceBusReceivedMessage message = context.getMessage();

    // Process message and then complete it.
    receiver.complete(message.getLockToken());
});
```

There are four ways of settling messages:
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

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L164-L168 -->
```java
// Setting sessionId publishes that message to a specific session, in this case, "greeting".
ServiceBusMessage message = new ServiceBusMessage("Hello world")
    .setSessionId("greetings");

sender.sendMessage(message);
```

#### Receive messages from a session

Receivers can fetch messages from a specific session or the first available, unlocked session.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L175-L181 -->
```java
// Creates a session-enabled receiver that gets messages from the session "greetings".
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sessionReceiver()
    .queueName("<< QUEUE NAME >>")
    .sessionId("greetings")
    .buildAsyncClient();
```

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L188-L193 -->
```java
// Creates a session-enabled receiver that gets messages from the first available session.
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sessionReceiver()
    .queueName("<< QUEUE NAME >>")
    .buildAsyncClient();
```

### Create a dead-letter queue Receiver

Azure Service Bus queues and topic subscriptions provide a secondary sub-queue, called a dead-letter queue (DLQ).
The dead-letter queue doesn't need to be explicitly created and can't be deleted or otherwise managed independent 
of the main entity. Learn more about dead-letter queue [here][dead-letter-queue].

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L200-L206 -->
```java
ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
    .buildClient();
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
as the [AmqpRetryOptons][AmqpRetryOptons] allows. Afterwords, the operation fails and an exception is propagated back 
to the user.

[`AmqpErrorCondition`][AmqpErrorCondition] contains error conditions common to the AMQP protocol and used by Azure
services. When an AMQP exception is thrown, examining the error condition field can inform developers as to why the AMQP
exception occurred and if possible, how to mitigate this exception. A list of all the AMQP exceptions can be found in
[OASIS AMQP Version 1.0 Transport Errors][oasis_amqp_v1_error].

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Service Bus Messaging Exceptions][] guidance.

## Next steps

Beyond those discussed, the Azure Service Bus client library offers support for many additional scenarios to help take
advantage of the full feature set of the Azure Service Bus service. In order to help explore some of these scenarios,
the following set of sample is available [here][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](./../../../CONTRIBUTING.md) for more information.

<!-- Links -->
[aad_authorization]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[AmqpErrorCondition]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[AmqpRetryOptons]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[api_documentation]: https://aka.ms/java-docs
[dead-letter-queue]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[deadletterqueue_docs]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[java_development_kit]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[maven]: https://maven.apache.org/
[message-sessions]: https://docs.microsoft.com/azure/service-bus-messaging/message-sessions
[oasis_amqp_v1_error]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[product_docs]: https://docs.microsoft.com/azure/service-bus-messaging
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[queue_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#queues
[ReceiveMode]: ./src/main/java/com/azure/messaging/servicebus/models/ReceiveMode.java
[RetryOptions]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[sample_examples]: ./src/samples/java/com/azure/messaging/servicebus/
[samples_readme]: ./src/samples/java/com/azure/messaging/servicebus
[service_bus_connection_string]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal#get-the-connection-string
[servicebus_create]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[servicebus_messaging_exceptions]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-exceptions
[servicebus_roles]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application#built-in-rbac-roles-for-azure-service-bus
[ServiceBusClientBuilder]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusClientBuilder.java
[ServiceBusMessage]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusMessage.java
[ServiceBusReceiverAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverAsyncClient.java
[ServiceBusReceiverClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverClient.java
[ServiceBusSenderAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderAsyncClient.java
[ServiceBusSenderClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderClient.java
[service_bus_create]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[source_code]: ./
[subscription_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions
[topic_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#topics
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FREADME.png)
