---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-messaging-servicebus
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

- [Send messages][SendMessageAsyncSample]
- [Send messages using Azure Identity][SendMessageWithAzureIdentityAsyncSample]
- [Send message batches synchronously][SendMessageBatchSyncSample]
- [Schedule and cancel a message][MessageCancelScheduleAsyncSample]
- [Receive messages synchronously][ReceiveMessageSyncSample]
- [Receive and auto-complete messages][ReceiveMessageAsyncSample]
- [Receive messages using Azure Identity][ReceiveMessageAzureIdentityAsyncSample]
- [Receive and settle messages via complete or abandon][ReceiveMessageAndSettleAsyncSample]
- [Peek at a message][PeekMessageAsyncSample]

### Message sessions
- [Send and receive messages from a session][SendAndReceiveSessionMessageSample]
- [Receive messages from multiple available sessions][ReceiveMultipleSessionsAsyncSample]
- [Receive messages from a specific session][ReceiveNamedSessionAsyncSample]
- [Receive messages from the first available session][ReceiveSingleSessionAsyncSample]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](../../../../../CONTRIBUTING.md) for more information.

<!-- LINKS -->
[sdk_readme_key_concepts]: ../../README.md#key-concepts
[sdk_readme_getting_started]: ../../README.md#getting-started
[sdk_readme_troubleshooting]: ../../README.md#troubleshooting
[sdk_readme_next_steps]: ../../README.md#next-steps

[PeekMessageAsyncSample]: ./java/com/azure/messaging/servicebus/PeekMessageAsyncSample.java
[ReceiveMessageAndSettleAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveMessageAndSettleAsyncSample.java
[ReceiveMessageAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveMessageAsyncSample.java
[ReceiveMessageAzureIdentityAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveMessageAzureIdentityAsyncSample.java
[ReceiveMessageSyncSample]: ./java/com/azure/messaging/servicebus/ReceiveMessageSyncSample.java
[ReceiveMultipleSessionsAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveMultipleSessionsAsyncSample.java
[ReceiveNamedSessionAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveNamedSessionAsyncSample.java
[ReceiveSingleSessionAsyncSample]: ./java/com/azure/messaging/servicebus/ReceiveSingleSessionAsyncSample.java
[SendAndReceiveSessionMessageSample]: ./java/com/azure/messaging/servicebus/SendAndReceiveSessionMessageSample.java
[SendMessageAsyncSample]: ./java/com/azure/messaging/servicebus/SendMessageAsyncSample.java
[SendMessageBatchSyncSample]: ./java/com/azure/messaging/servicebus/SendMessageBatchSyncSample.java
[SendMessageWithAzureIdentityAsyncSample]: ./java/com/azure/messaging/servicebus/SendMessageWithAzureIdentityAsyncSample.java
[SendScheduledMessageAndCancelAsyncSample]: ./java/com/azure/messaging/servicebus/SendScheduledMessageAndCancelAsyncSample.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-messaging-servicebus%2Fsrc%2Fsamples%2FREADME.png)
