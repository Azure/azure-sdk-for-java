# Release History
## 7.0.0-beta.6 (Unreleased)


## 7.0.0-beta.5 (2020-08-11)
- Remove public constructor for QueueDescription, TopicDescription, SubscriptionDescription.
- Expose CreateQueueOptions, CreateTopicOptions, CreateSubscriptionOptions to create entities.
- Flatten and remove MessageCountDetails in QueueRuntimeInfo, TopicRuntimeInfo, and SubscriptionRuntimeInfo.
- Limiting visibility of properties on QueueDescription, TopicDescription, SubscriptionDescription to only those that 
  can be updated.
- Added a short timeout of 1 second in between messages for sync receive only.

## 7.0.0-beta.4 (2020-07-10)
- Add support for send messages via another entity.
- Add support for management operations on a topic, subscription, or namespace.
- Add support for receiving messages from the dead letter queue.
- Change suffixes for receive methods by adding `Message` or `Messages`.
- Remove `MessageLockToken` interface in favour of passing a lock token string.

## 7.0.0-beta.3 (2020-06-08)
- Add support for transaction feature in all the clients.
- Add support for management operations on a Queue.
 
## 7.0.0-beta.2 (2020-05-07)

- Add support for receiving messages from specific sessions
- Add support for receiving messages from multiple sessions
- Add missing schedule and cancel APIs in ServiceBusSenderClient
- Add support to send a collection of messages at once without needing to create a `ServiceBusMessageBatch` first. This
  will throw an error/exception will the messages cannot fit as per batch size restrictions
- Change return type from `ServiceBusReceivedMessage` to `ServiceBusReceivedMessageContext` when calling `receive()` so
  users can distinguish between transient failure scenarios where receiving continues and an actual terminal signal that
  is signaled through the downstream `onError`.
- Fix message settlement to occur on receive link
- Fix issue where backpressure is not properly supported

## 7.0.0-beta.1 (2020-04-06)

Version 7.0.0-beta.1 is a beta of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for
.Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

### Features

- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Send messages to an Azure Service Bus Topic or Queue.
- Receive messages from an Azure Service Bus Queue or Subscriber.

### Known issues

- Following features are not implemented Transactions, Sessions, Managing filter rules on Subscription.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FCHANGELOG.png)
