---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-service-bus
urlFragment: servicebus-samples
---

# Azure Service Bus Samples client library for Java
Azure Service Bus samples are a set of self-contained Java programs that demonstrate interacting with Azure Service Bus
using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Getting started
Please refer to the [Getting Started][sdk_readme_getting_started] section.

### Obtaining a Service Bus namespace connection string

Most of the samples authorize with Service Bus using a connection string generated for that Service Bus namespace. The
connection string value can be obtained by:

1. Going to your Service Bus namespace in Azure Portal.
1. Go to "Shared access policies" for your Service Bus namespace.
1. Click on the "RootManageSharedAccessKey" policy.
1. Copying the connection string from the policy's properties.

## Examples

### Asynchronously sending and receiving

- [Send a message][SendMessageAsyncSample]
- [Send messages using Azure Identity][SendMessageWithAzureIdentityAsyncSample]
- [Process all messages using processor][ServiceBusProcessorSample]
- [Receive and auto-complete messages][ReceiveMessageAsyncSample]
- [Receive and manually settle messages][ReceiveMessageAndSettleAsyncSample]
- [Receive messages with auto-lock renewal][ReceiveMessageAutoLockRenewal]
- [Schedule and cancel a message][SendScheduledMessageAndCancelAsyncSample]
- [Peek at a message][PeekMessageAsyncSample]

### Synchronous sending and receiving
- [Send message batches synchronously][SendMessageBatchSyncSample]
- [Receive messages synchronously][ReceiveMessageSample]

### Message sessions
- [Send messages to a session][SendSessionMessageSample]
- [Process all session messages using processor][ServiceBusSessionProcessorSample]
- [Receive messages from a specific session][ReceiveNamedSessionAsyncSample]
- [Receive messages from the first available session][ReceiveSingleSessionAsyncSample]

### Synchronous Administration Client operations
- [Update queue properties synchronously][AdministrationClientUpdateQueueSample]

### Generate Shared Access Signature
- [Generate SAS and receive message][ReceiveMessageAsyncUsingSasSample]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) for more information.

<!-- LINKS -->
[sdk_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/README.md#key-concepts
[sdk_readme_getting_started]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/README.md#getting-started
[sdk_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/README.md#troubleshooting
[sdk_readme_next_steps]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/README.md#next-steps

[PeekMessageAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/PeekMessageAsyncSample.java
[ReceiveMessageAndSettleAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAndSettleAsyncSample.java
[ReceiveMessageAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAsyncSample.java
[ReceiveMessageAutoLockRenewal]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageAutoLockRenewal.java
[ReceiveMessageSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageSample.java
[ReceiveNamedSessionAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveNamedSessionAsyncSample.java
[ReceiveNamedSessionSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveNamedSessionSample.java
[ReceiveSingleSessionAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveSingleSessionAsyncSample.java
[SendSessionMessageSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/SendSessionMessageAsyncSample.java
[SendMessageAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/SendMessageAsyncSample.java
[SendMessageBatchSyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/SendMessageBatchSample.java
[SendMessageWithAzureIdentityAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/SendMessageWithAzureIdentityAsyncSample.java
[SendScheduledMessageAndCancelAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/SendScheduledMessageAndCancelAsyncSample.java
[ServiceBusProcessorSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusProcessorSample.java
[ServiceBusSessionProcessorSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusSessionProcessorSample.java
[AdministrationClientUpdateQueueSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/AdministrationClientUpdateQueueSample.java
[ReceiveMessageAsyncUsingSasSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageUsingSasSample.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2Fsrc%2Fsamples%2FREADME.png)
