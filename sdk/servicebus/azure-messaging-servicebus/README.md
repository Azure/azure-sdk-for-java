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

[//]: # ({x-version-update-start;beta_com.azure:azure-messaging-servicebus;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.3.0</version>
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

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L30-L34 -->
```java
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .sender()
    .queueName("<< QUEUE NAME >>")
    .buildClient();
```

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L41-L46 -->
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
    <version>1.2.5</version>
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

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L53-L59 -->
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
 - [Send and receive from session enabled queues or topics](#send-and-receive-from-session-enabled-queues-or-topics)
 - [Create a dead-letter queue Receiver](#create-a-dead-letter-queue-receiver)
 - [Sharing a connection between clients](#sharing-of-connection-between-clients)
### Send messages

You'll need to create an asynchronous [`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient] or a synchronous
[`ServiceBusSenderClient`][ServiceBusSenderClient] to send messages. Each sender can send messages to either a queue or
a topic.

The snippet below creates a synchronous [`ServiceBusSenderClient`][ServiceBusSenderClient] to publish a message to a
queue.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L66-L78 -->
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

To receive messages, you will need to create a `ServiceBusProcessorClient` with callbacks for incoming messages and any error that occurs in the process. You can then start and stop the client as required.

By default, the `autoComplete` feature is enabled on the processor client which means that after executing your callback for the message, the client will complete the message i.e. remove it from the queue/subscription. If your callback throws an error, then the client will abandon the message i.e. make it available to be received again. You can disable this feature when creating the processor client.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L215-L242 -->
```java
// Sample code that processes a single message
Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
    try {
        System.out.println(messageContext.getMessage().getMessageId());
        // other message processing code
        messageContext.complete();
    } catch (Exception ex) {
        messageContext.abandon();
    }
};

// Sample code that gets called if there's an error
Consumer<ServiceBusErrorContext> processError = errorContext -> {
    System.err.println("Error occurred while receiving message: " + errorContext.getException());
};

// create the processor client via the builder and its sub-builder
ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                                .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
                                .processor()
                                .queueName("<< QUEUE NAME >>")
                                .processMessage(processMessage)
                                .processError(processError)
                                .disableAutoComplete()
                                .buildProcessorClient();

// Starts the processor in the background and returns immediately
processorClient.start();
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

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L165-L169 -->
```java
// Setting sessionId publishes that message to a specific session, in this case, "greeting".
ServiceBusMessage message = new ServiceBusMessage("Hello world")
    .setSessionId("greetings");

sender.sendMessage(message);
```

#### Receive messages from a session

Receiving messages from sessions is similar to receiving messages from a non session enabled queue or subscription. The difference is in the builder and the class you use.

In non-session case, you would use the sub builder `processor()`. In case of sessions, you would use the sub builder `sessionProcessor()`. Both sub builders will create an instance of `ServiceBusProcessorClient` configured to work on a session or a non-session Service Bus entity. In the case of the session processor, you can pass the maximum number of sessions you want the processor to process concurrently as well.

### Create a dead-letter queue Receiver

Azure Service Bus queues and topic subscriptions provide a secondary sub-queue, called a dead-letter queue (DLQ).
The dead-letter queue doesn't need to be explicitly created and can't be deleted or otherwise managed independent
of the main entity. For session enabled or non-session queue or topic subscriptions, the dead-letter receiver can be
created the same way as shown below. Learn more about dead-letter queue [here][dead-letter-queue].

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L202-L208 -->
```java
ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
    .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
    .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
    .buildClient();
```

### Sharing of connection between clients
The creation of physical connection to Service Bus requires resources. An application should share the connection  
between clients which can be achieved by sharing the top level builder as shown below.
<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L247-L258 -->
```java
    ServiceBusClientBuilder sharedConnectionBuilder = new ServiceBusClientBuilder()
        .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>");
    // Create receiver and sender which will share the connection.
    ServiceBusReceiverClient receiver = sharedConnectionBuilder
        .receiver()
        .queueName("<< QUEUE NAME >>")
        .buildClient();
    ServiceBusSenderClient sender = sharedConnectionBuilder
        .sender()
        .queueName("<< QUEUE NAME >>")
        .buildClient();
}
```
### When to use 'ServiceBusProcessorClient'.
 When to use 'ServiceBusProcessorClient', 'ServiceBusReceiverClient' or ServiceBusReceiverAsyncClient? The processor 
 is built using 'ServiceBusReceiverAsyncClient', it provides a convenient way of receiving messages with default 
 auto complete and auto renew of message locks in 'PEEK_LOCK' mode. The processor is appropriate where the 
 applications have not made complete move to async receiver client and want to process message in synchronous mode. 
 The processor receives messages forever because it recovers from the network errors internally. 
 'ServiceBusProcessorClient:processMessage()' function call is made for each message. Alternatively, You can also use 
 'ServiceBusReceiverClient', it is a lower level client and provides a wider range of APIs. If async processing is  
 suitable for your application, you can use 'ServiceBusReceiverAsyncClient'. 

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
[Service Bus Messaging Exceptions][servicebus_messaging_exceptions] guidance.

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
[AmqpRetryOptons]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[api_documentation]: https://aka.ms/java-docs
[dead-letter-queue]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[deadletterqueue_docs]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues
[java_development_kit]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[maven]: https://maven.apache.org/
[maven_package]: https://search.maven.org/artifact/com.azure/azure-messaging-servicebus
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
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/
[subscription_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions
[topic_concept]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messaging-overview#topics
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication
[known-issue-binarydata-notfound]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/known-issues.md#can-not-resolve-binarydata-or-noclassdeffounderror-version-700

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FREADME.png)
