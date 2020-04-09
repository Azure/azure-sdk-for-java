# Azure Service Bus client library for Java

Microsoft Azure Service Bus is a fully managed enterprise integration message broker. Service Bus can decouple
applications and services. Service Bus offers a reliable and secure platform for asynchronous transfer of data
and state. Data is transferred between different applications and services using messages. If you would like to know
more about Azure Service Bus, you may wish to review: [What is Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging)?

The Azure Service Bus client library allows for sending and receiving of Azure Service Bus messages and may be used to:

- Messaging: Transfer business data, such as sales or purchase orders, journals, or inventory movements.
- Decouple applications: Improve reliability and scalability of applications and services. Client and service don't
have to be online at the same time.
- Topics and subscriptions: Enable 1:n relationships between publishers and subscribers.
- Message sessions. Implement work-flows that require message ordering or message deferral.

[Source code][source_code] | [API reference documentation][api_documentation]  | [Samples][sample_examples]

## Table of contents

- [Table of contents](#table-of-contents)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Adding the package to your product](#adding-the-package-to-your-product)
  - [Authenticate the client](#authenticate-the-client)
- [Key concepts](#key-concepts)
- [Examples](#examples)
  - [Sending messages](#send-message-examples)
  - [Receiving messages](#receive-message-examples)
- [Troubleshooting](#troubleshooting)
  - [Enable client logging](#enable-client-logging)
  - [Enable AMQP transport logging](#enable-amqp-transport-logging)
  - [Common exceptions](#common-exceptions)
  - [Handling transient AMQP exceptions](#handling-transient-amqp-exceptions)
  - [Default SSL library](#default-ssl-library)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Microsoft Azure subscription
  - You can create a free account at: https://azure.microsoft.com

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-messaging-servicebus;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

For the Service Bus client library to interact with an Service Bus, it will need to understand how to connect
and authorize with it.

#### Create an Service Bus client using Microsoft identity platform (formerly Azure Active Directory)

Azure SDK for Java supports an Azure Identity package, making it simple get credentials from Microsoft identity
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

All the implemented ways to request a credential can be found under the `com.azure.identity.credential` package. The
sample below shows how to use an Azure Active Directory (AAD) application client secret to authorize with Azure Service Bus.

#### Authorizing with AAD application client secret

Follow the instructions in [Creating a service principal using Azure Portal][application_client_secret] to create a
service principal and a client secret. The corresponding `clientId` and `tenantId` for the service principal can be
obtained from the [App registration page][app_registration_page].

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: 
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Use the returned token credential to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L48-L54 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .build();
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .credential("<<fully-qualified-namespace>>", credential)
    .receiver()
    .queueName("<<queue-name>>")
    .buildAsyncClient();
```
##### Service Bus Roles

When using Azure Active Directory, your principal must be assigned a role which allows access to Service Bus, such
as the `Azure Service Bus Data Owner`, `Azure Service Bus Data Sender` and `Azure Service Bus Data Receiver` 
[role][servicebus_roles]. 
For more information about using Azure Active Directory authorization with Service Bus, please refer to 
[the associated documentation][aad_authorization].

## Key concepts

You can interact with the primary resource types within a Service Bus Namespace, of which multiple can exist and 
on which actual message transmission takes place, the namespace often serving as an application container:

* [Queue][queue_concept]: Allows for Sending and Receiving of messages, ordered first-in-first-out.  Often used for 
point-to-point communication.

* [Topic][topic_concept]: As opposed to Queues, Topics are better suited to publish/subscribe scenarios.  A topic can 
be sent to, but requires a subscription, of which there can be multiple in parallel, to consume from.

* [Subscription][subscription_concept]: The mechanism to consume from a Topic.  Each subscription is independent, and 
receaves a copy of each message sent to the topic.

## Examples
### Create a sender or receiver using connection string
The easiest means for doing so is to use a connection string, which is created automatically when creating an Service Bus
namespace. If you aren't familiar with shared access policies in Azure, you may wish to follow the step-by-step guide to
[get an Service Bus connection string][service_bus_connection_string].

Both the asynchronous and synchronous Service Bus sender and receiver clients can be created using
`ServiceBusClientBuilder`.The examples are explained blow.

The snippet below creates an asynchronous Service Bus sender.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L23-L28 -->
```java
String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
    .connectionString(connectionString)
    .sender()
    .queueName("<< QUEUE NAME >>")
    .buildAsyncClient();
```

The snippet below creates an asynchronous Service Bus receiver.

<!-- embedme ./src/samples/java/com/azure/messaging/servicebus/ReadmeSamples.java#L35-L41 -->
```java
String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
    .connectionString(connectionString)
    .receiver()
    .topicName("<< TOPIC NAME >>")
    .subscriptionName("<< SUBSCRIPTION NAME >>")
    .buildAsyncClient();
```

### Send Message Examples

You'll need to create an asynchronous [`ServiceBusSenderAsyncClient`][ServiceBusSenderAsyncClient] or
a synchronous [`ServiceBusSenderClient`][ServiceBusSenderClient] to send message. Each sender can send message to either, a queue,
or topic.

* [Sending a message asynchronously][sample-send-async-message].
* [Sending a message asynchronously using active directory credential][sample-send-async-aad-message].
* [Send message in batch synchronously][sample-send-batch-messages].

### Receive Message Examples

You'll need to create an asynchronous [`ServiceBusReceiverAsyncClient`][ServiceBusReceiverAsyncClient] or
a synchronous [`ServiceBusReceiverClient`][ServiceBusReceiverClient]. Each receiver can receive message from either, a queue,
or subscriber.

* [Receiving a message asynchronously][sample-receive-async-message].
* [Receiving a message asynchronously using active directory credential][sample-receive-async-aad-message].
* [Receiving a message asynchronously and settling][sample-receive-message-and-settle].
* [Receiving messages synchronously][sample-receive-message-synchronously].

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][LogLevels].

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

The [`AmqpErrorContext`][AmqpErrorContext] in the [`AmqpException`][AmqpException] provides information about the AMQP
session, link, or connection that the exception occurred in. This is useful to diagnose which level in the transport
this exception occurred at and whether it was an issue in one of the producers or consumers.

#### Operation cancelled exception

It occurs when the underlying AMQP layer encounters an abnormal link abort or the connection is disconnected in an
unexpected fashion. It is recommended to attempt to verify the current state and retry if necessary.

### Handling transient AMQP exceptions

If a transient AMQP exception occurs, the client library retries the operation as many times as the
[AmqpRetryOptions][AmqpRetryOptions] allows. Afterwards, the operation fails and an exception is propagated back to the
user.

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

Beyond those discussed, the Azure Service Bus client library offers support for many additional scenarios to help take
advantage of the full feature set of the Azure Service Bus service. In order to help explore some of the these scenarios,
the following set of sample is available [here][samples_readme].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](./CONTRIBUTING.md) for more information.

<!-- Links -->
[amqp_transport_error]: https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-amqp-error
[aad_authorization]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/authenticate-application
[api_documentation]: https://aka.ms/java-docs
[app_registration_page]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#get-values-for-signing-in
[application_client_secret]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal#create-a-new-application-secret
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[maven]: https://maven.apache.org/
[oasis_amqp_v1_error]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-error
[oasis_amqp_v1]: http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-overview-v1.0-os.html
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[qpid_proton_j_apache]: http://qpid.apache.org/proton/
[queue_concept]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-messaging-overview#queues
[topic_concept]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-messaging-overview#topics
[subscription_concept]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-queues-topics-subscriptions#topics-and-subscriptions
[servicebus_roles]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/authenticate-application#built-in-rbac-roles-for-azure-service-bus
[samples_readme]: ./src/samples/README.md
[sample_examples]: ./src/samples/java/com/azure/messaging/servicebus/
[source_code]: ./
[AmqpException]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpException.java
[AmqpErrorCondition]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorCondition.java
[AmqpErrorContext]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/exception/AmqpErrorContext.java
[LogLevels]: ../../core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[RetryOptions]: ../../core/azure-core-amqp/src/main/java/com/azure/core/amqp/AmqpRetryOptions.java
[ServiceBusSenderAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderAsyncClient.java
[ServiceBusSenderClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusSenderClient.java
[ServiceBusReceiverClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverClient.java
[ServiceBusReceiverAsyncClient]: ./src/main/java/com/azure/messaging/servicebus/ServiceBusReceiverAsyncClient.java
[service_bus_connection_string]: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal#get-the-connection-string
[sample-send-async-message]: ./src/samples/java/com/azure/messaging/servicebus/SendMessageAsyncSample.java
[sample-receive-async-message]: ./src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAsyncSample.java
[sample-send-async-aad-message]: ./src/samples/java/com/azure/messaging/servicebus/SendMessageWithAzureIdentityAsyncSample.java
[sample-receive-async-aad-message]: ./src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAzureIdentityAsyncSample.java
[sample-send-batch-messages]: ./src/samples/java/com/azure/messaging/servicebus/SendMessageBatchSyncSample.java
[sample-receive-message-and-settle]: ./src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAndSettleAsyncSample.java
[sample-receive-message-synchronously]: ./src/samples/java/com/azure/messaging/servicebus/ReceiveMessageSyncSample.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FREADME.png)
