### The Sync receiveMessages API and implicit Prefetch:

The documentation [here][prefetchgeneral] describes the Service Bus SDK prefetch feature in general. It also states that setting the prefetch-count property in the builder to zero disables prefetch.

Even after the application disables prefetch in the builder, the `receiveMessages` API in `ServiceBusReceiverClient` can re-enable prefetch implicitly, which may not be obvious. This document outlines how this API impacts prefetch (covers minimal internals to ease the understanding), helping developers to learn about the API behavior and account for it in the application design.

#### The receiveMessages API signature:

```java
IterableStream<ServiceBusReceivedMessage> receiveMessages(int maxMessages, Duration maxWaitTime)
```

where `maxMessages` is the maximum number of messages to receive and `maxWaitTime` is the overall duration the SDK should wait for the entire `maxMessages` to arrive.

*Note*: the overload `receiveMessages(int maxMessages)` is based on the above method, with `maxWaitTime` computed from `RetryConfig` set in builder.

#### The receiveMessages API interaction with prefetch-queue:

Once the application obtains the `ServiceBusReceiverClient` object, an unbounded prefetch-queue object is assigned to this client object (though in reality, the creation of prefetch-queue is lazy).

<img width="428" alt="EmptyPrefetchQueue" src="https://user-images.githubusercontent.com/1471612/144946242-ccdde0e4-236d-4cd3-81b8-090b06cd6c5f.png">

```java
IterableStream<ServiceBusReceivedMessage> messagesIterable = client.receiveMessages(10, Duration.ofSeconds(5));
```

Once the `receiveMessages` method is called, the Client immediately requests the service bus to return `maxMessages` messages, which asynchronously starts buffering the messages to the prefetch-queue.

<img width="715" alt="PrefetchQueueBuffering" src="https://user-images.githubusercontent.com/1471612/144946373-9e773d46-df30-45c4-8981-b34f4e5993b0.png">

This is an important point to note, i.e., the prefetch-queue can already have some messages before the application starts iterating the `messagesIterable`. It also means the delivery count on the server side will be incremented due to the API invocation.

Once the application starts iterating `messagesIterable`, the iterator will read and deliver messages from the prefetch-queue.

```java
int msgCnt = 0;
for (ServiceBusReceivedMessage message : messagesIterable) {
    msgCnt++;
    System.out.println("Received Message #" + msgCnt);
}
```

Here is the state of the prefetch-queue after two passes of iteration: 

<img width="889" alt="PrefetchQueueReading" src="https://user-images.githubusercontent.com/1471612/144946540-41845294-1c0b-4037-82c0-44482327c8f3.png">

#### The shared nature of prefetch-queue:

The prefetch-queue is scoped to the Client and shared across all `receiveMessages` calls.

Given the prefetch-queue is shared, the messages will get asynchronously buffered irrespective of:
1. the application ignores the Iterable returned from `receiveMessages` calls.
2. the application stops iteration in the middle.
3. the `maxTimeout` elapses before receiving `maxMessages`.

> On #3, the current iterator will be completed if the `maxTimeout` elapses, but the buffering continues in the background.

Any messages from a previous `receiveMessages` call that its Iterable couldn't read from the buffer (prefetch-queue) will be read and delivered by the Iterable from the next `receiveMessages` call on the same Client. 

Let's take the previous example, say within `maxTimeout` of 5 sec, the service returned only 7 messages out of 10; in this case, the `messagesIterable` will complete after returning those 7 messages and exit the for loop. The remaining 3 messages can get buffered in the background. If the application again calls `receiveMessages(5, ..)`, then SDK initiates a request for 5 messages. The new Iterable read and deliver 5 messages from the buffer (starting with the first three messages buffered by initial `revceiveMessages` call).

<img width="897" alt="PrefetchQueueSharing" src="https://user-images.githubusercontent.com/1471612/144946661-17155943-60a1-47e4-b772-2dafe53d379a.png">

If the application decide not to do anything with the messages in the buffer, those are eventually GC-ed. If such messages were requested with PEEK_LOCK mode, then, after the server-side lock duration elapses, a later `receiveMessages` call on another `ServiceBusReceiverClient` object returns the same messages.

#### Timers in receiveMessages API:

An invocation of `receiveMessages(int maxMessages, Duration maxWaitTime)` users two timers. 

The first one enables `maxWaitTime` support, controlling the maximum duration client should wait for the messages. If `maxWaitTime` elapses before `maxMessages` messages arrive, Iterable returns whatever messages it received and completes.

The second timer controls the timeout between messages, i.e., the maximum duration the client should wait for the next message since the arrival of the last message. The Iterable completes if no message arrives within this duration. Currently, this duration is set to 1 second and cannot be changed.

#### Messages in the prefetch-queue can expire:

Another important point is that messages may expire while in the prefetch-queue, which means the iterator may return expired messages.

Refer to [this][prefetchtradeoff] document for the tradeoff when enabling prefetch (or maxMessages > 1).



<!-- Links --->
[prefetchgeneral]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-prefetch?tabs=java
[prefetchtradeoff]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-prefetch?tabs=java#why-is-prefetch-not-the-default-option