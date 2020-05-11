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

## Table of contents

- [Azure Service Bus client library for Java](#azure-service-bus-client-library-for-java)
  - [Table of contents](#table-of-contents)
  - [Getting started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Include the package](#include-the-package)
    - [Authenticate the client](#authenticate-the-client)
  - [Key concepts](#key-concepts)
  - [Examples](#examples)
    - [Send messages](#send-messages)
    - [Receive messages](#receive-messages)
    - [Settle messages](#settle-messages)
    - [Send and receive from session enabled queues or topics](#send-and-receive-from-session-enabled-queues-or-topics)
  - [Troubleshooting](#troubleshooting)
    - [Enable client logging](#enable-client-logging)
    - [Enable AMQP transport logging](#enable-amqp-transport-logging)
    - [Common exceptions](#common-exceptions)
  - [Next steps](#next-steps)
  - [Contributing](#contributing)

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: https://azure.microsoft.com
- Azure Service Bus instance
  - Step-by-step guide for [creating a Service Bus instance using Azure Portal][service_bus_create]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-messaging-servicebus;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.0.0-beta.2</version>
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
    <version>1.0.4</version>
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

## Examples

### Send messages

You'll need to create an asynchronous [`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient] or a synchronous
[`ServiceBusSenderClient`][ServiceBusSenderClient] to send messages. Each sender can send messages to either a queue or
a topic.

The snippet below creates a synchronous `ServiceBusSender` to publish a message to a queue.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L65-L74 -->
```java
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sender()
    .queueName("<< QUEUE NAME >>")
    .buildClient();
List<ServiceBusMessage> messages = Arrays.asList(
    new ServiceBusMessage("Hello world".getBytes()).setMessageId("1"),
    new ServiceBusMessage("Bonjour".getBytes()).setMessageId("2"));

sender.send(messages);
```

### Receive messages

You'll need to create an asynchronous [`ServiceBusReceiverAsyncClient`][ServiceBusReceiverAsyncClient] or a synchronous
[`ServiceBusReceiverClient`][ServiceBusReceiverClient] to receive messages. Each receiver can consume messages from
either a queue or a topic subscription.

#### Receive a batch of messages

The snippet below creates a `ServiceBusReceiverClient` to receive messages from a topic subscription. It returns a batch
of messages when 10 messages are received or until 30 seconds have elapsed, whichever happens first.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L81-L94 -->
```java
ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .receiveMode(ReceiveMode.PEEK_LOCK)
    .buildClient();

IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receive(10, Duration.ofSeconds(30));
messages.forEach(context -> {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Id: %s. Contents: %s%n", message.getMessageId(),
        new String(message.getBody(), StandardCharsets.UTF_8));
});
```

#### Receive a stream of messages

The asynchronous `ServiceBusReceiverAsyncClient` continuously fetches messages until the `subscription` is disposed.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L101-L115 -->
```java
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver()
    .queueName("<< QUEUE NAME >>")
    .buildAsyncClient();

Disposable subscription = receiver.receive().subscribe(context -> {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Id: %s%n", message.getMessageId());
    System.out.printf("Contents: %s%n", new String(message.getBody(), StandardCharsets.UTF_8));
}, error -> {
        System.err.println("Error occurred while receiving messages: " + error);
    }, () -> {
        System.out.println("Finished receiving messages.");
    });
```

### Settle messages

When a message is received, it can be settled using any of the `complete()`, `abandon()`, `defer()`, or `deadLetter()`
overloads. The sample below completes a received message from synchronous `ServiceBusReceiverClient`.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L130-L135 -->
```java
receiver.receive(10).forEach(context -> {
    ServiceBusReceivedMessage message = context.getMessage();

    // Process message and then complete it.
    receiver.complete(message);
});
```

### Send and receive from session enabled queues or topics

> Using sessions requires you to create a session enabled queue or subscription. You can read more about how to
> configure this in "[Message sessions][message-sessions]".

Unlike non-session-enabled queues or subscriptions, only a single receiver can read from a session at any time. When a
receiver fetches a session, Service Bus locks the session for that receiver, and it has exclusive access to messages in
that session.

#### Send message to a session

Create a `ServiceBusSenderClient` for a session enabled queue or topic subscription. Setting `.setSessionId(String)` on
a `ServiceBusMessage` will publish the message to that session. If the session does not exist, it is created.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L148-L151 -->
```java
ServiceBusMessage message = new ServiceBusMessage("Hello world".getBytes())
    .setSessionId("greetings");

sender.send(message);
```

#### Receive messages from a session

Receivers can fetch messages from a specific session or the first available, unlocked session. The first snippet creates
a receiver for a session with id "greetings". The second snippet creates a receiver that fetches the first available
session.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L158-L163 -->
```java
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sessionReceiver()
    .queueName("<< QUEUE NAME >>")
    .sessionId("greetings")
    .buildAsyncClient();
```

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L170-L174 -->
```java
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sessionReceiver()
    .queueName("<< QUEUE NAME >>")
    .buildAsyncClient();
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
transient error or not. If true, then the request can be retried; otherwise not.

[`AmqpErrorCondition`][AmqpErrorCondition] contains error conditions common to the AMQP protocol and used by Azure
services. When an AMQP exception is thrown, examining the error condition field can inform developers as to why the AMQP
exception occurred and if possible, how to mitigate this exception. A list of all the AMQP exceptions can be found in
[OASIS AMQP Version 1.0 Transport Errors][oasis_amqp_v1_error].

The recommended way to solve the specific exception the AMQP exception represents is to follow the
[Service Bus Messaging Exceptions][] guidance.

## Next steps

Beyond those discussed, the Azure Service Bus client library offers support for many additional scenarios to help take
advantage of the full feature set of the Azure Service Bus service. In order to help explore some of these scenarios,
check out the [samples README][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](./../../../CONTRIBUTING.md) for more information.

<!-- Links -->
[aad_authorization]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[AmqpErrorCondition]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[api_documentation]: https://aka.ms/java-docs
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[maven]: https://maven.apache.org/
[message-sessions]: https://docs.microsoft.com/azure/service-bus-messaging/message-sessions
[oasis_amqp_v1_error]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[product_docs]: https://docs.microsoft.com/azure/service-bus-messaging
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[queue_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#queues
[RetryOptions]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[sample_examples]: ./src/samples/java/com/azure/messaging/servicebus/
[samples_readme]: ./src/samples/README.md
[service_bus_connection_string]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal#get-the-connection-string
[servicebus_create]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[servicebus_messaging_exceptions]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-exceptions
[servicebus_roles]: https://docs.microsoft.com/azure/service-bus-messaging/authenticate-application#built-in-rbac-roles-for-azure-service-bus
[ServiceBusReceiverAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverAsyncClient.java
[ServiceBusReceiverClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverClient.java
[ServiceBusSenderAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderAsyncClient.java
[ServiceBusSenderClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderClient.java
[source_code]: ./
[subscription_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions
[topic_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#topics
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FREADME.png)
