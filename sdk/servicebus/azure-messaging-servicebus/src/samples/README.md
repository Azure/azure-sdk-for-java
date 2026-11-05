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
- [Process all messages using processor][ServiceBusProcessorPeekLockReceiveSample]
- [Close a processor with a drain timeout][ServiceBusProcessorDrainTimeoutSample]
- [Process messages with reactive handlers and manual settlement][ServiceBusProcessorAsyncSample]
- [Processor client Java doc code samples][ServiceBusProcessorClientJavaDocCodeSamples]
- [Receive and auto-complete messages][ReceiveMessageAsyncSample]
- [Receive and manually settle messages][ReceiveMessageAndSettleAsyncSample]
- [Receive messages with auto-lock renewal][ReceiveMessageAutoLockRenewal]
- [Schedule and cancel a message][SendScheduledMessageAndCancelAsyncSample]
- [Peek at a message][PeekMessageAsyncSample]

To run the [processor drain-timeout sample][ServiceBusProcessorDrainTimeoutSample], set
`AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING` to a connection string with Send and Listen permissions and
`AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME` to an empty test queue that is not session-enabled.
Run `ServiceBusProcessorDrainTimeoutSample.main`: it sends a uniquely identified message, waits for that message's
handler to start, then calls `close()` from the main thread. The configured 10-second drain timeout lets the handler
finish and settle its message before the receiver closes; if it expires, shutdown proceeds anyway. The sample fails
if its handler does not start within 30 seconds or its message is not settled before `close()` returns.
The sample uses `close()` to demonstrate the documented drain and resource-cleanup contract.

### Synchronous sending and receiving
- [Send message batches synchronously][SendMessageBatchSyncSample]
- [Receive messages synchronously][ReceiveMessageSample]

### Reactive processor

Run `ServiceBusProcessorAsyncSample.main` with `AZURE_SERVICEBUS_FULLY_QUALIFIED_NAMESPACE`
(for example, `your-namespace.servicebus.windows.net`) and `AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME`.
Use an empty test queue and an identity with Send and Listen permissions. The sample sends a message before starting
the processor. For a session-enabled queue, pass `session` as the program argument.

The handler returns a `Mono` covering all downstream work and manual settlement. Replace the illustrative delay with
the asynchronous durable write or flush in your application, and complete the Service Bus message only after it succeeds.
Do not return `Mono.empty()` while a detached callback is still working: the processor uses the returned operation's
lifetime to bound concurrency, track processing spans, and drain handlers on shutdown.
Automatic settlement is also reactive; when enabled, successful handler completion triggers message completion.
On failure, the processing span includes the error handler and automatic abandonment. Recovery checks run every ten
seconds so persistent receive failures cannot create a tight restart loop.
The session processor's `maxConcurrentCalls` bounds total concurrent handlers across sessions, not handlers per session.
For ordered work, use sessions and set `maxConcurrentCalls` to one. Higher values allow handlers for the same session
to overlap. Setting concurrency to one on a non-session queue does not guarantee ordering.
Session idle timeout starts when no accepted handlers or settlements remain pending; it does not interrupt a
long-running handler. Configure lock renewal to cover the full processing time.
`stop()` pauses new handler dispatch without cancelling accepted work. Subscribe to `start()` to resume, or call
`close()` from a controlling thread to drain accepted work and release the receivers.

### Message sessions
- [Send messages to a session][SendSessionMessageSample]
- [Process all session messages using processor][ServiceBusSessionProcessorSample]
- [Receive messages from a specific session][ReceiveNamedSessionAsyncSample]
- [Receive messages from the first available session][ReceiveSingleSessionAsyncSample]
- [List session IDs synchronously][ListSessionsSample]
- [List session IDs asynchronously][ListSessionsAsyncSample]

### Synchronous Administration Client operations
- [Update queue properties synchronously][AdministrationClientUpdateQueueSample]
- [Read topic SQL and correlation filter counts][TopicFilterCountsSample]

To run the topic filter-count sample, set `AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING` to a namespace connection
string with Manage permission and `AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME` to an existing topic, then run
`TopicFilterCountsSample.main` from your IDE. The sample reads runtime properties; it does not create or change rules.
The sample uses service API version `2024-05` to read both filter counts.

### Generate Shared Access Signature
- [Generate SAS and receive message][ReceiveMessageAsyncUsingSasSample]

### Advanced Configuration
- [Advanced configuration sample set][AdvancedConfigurationSample]

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
[ServiceBusProcessorAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusProcessorAsyncSample.java
[ListSessionsAsyncSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ListSessionsAsyncSample.java
[ListSessionsSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ListSessionsSample.java
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
[ServiceBusProcessorClientJavaDocCodeSamples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusProcessorClientJavaDocCodeSamples.java
[ServiceBusProcessorPeekLockReceiveSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusProcessorPeekLockReceiveSample.java
[ServiceBusProcessorDrainTimeoutSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusProcessorDrainTimeoutSample.java
[ServiceBusReceiverAsyncClientRetrySample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusReceiverAsyncClientRetrySample.java
[ServiceBusSessionProcessorSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ServiceBusSessionProcessorSample.java
[AdministrationClientUpdateQueueSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/AdministrationClientUpdateQueueSample.java
[TopicFilterCountsSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/TopicFilterCountsSample.java
[ReceiveMessageAsyncUsingSasSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/ReceiveMessageUsingSasSample.java
[AdvancedConfigurationSample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/samples/java/com/azure/messaging/servicebus/AdvancedConfigurationSample.java
