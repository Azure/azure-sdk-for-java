# The Sync receiveMessages API and implicit Prefetch

The documentation [here][prefetchgeneral] describes the Service Bus SDK prefetch feature in general. It also states that setting the prefetch-count property in the builder to zero disables prefetch.

Even after the application disables prefetch in the builder, the `receiveMessages` API in `ServiceBusReceiverClient` can re-enable prefetch implicitly, which may not be obvious. This document outlines how this API impacts prefetch (covers minimal internals to ease the understanding), helping developers to learn about the API behaviors and account for it in the application design.

## The receiveMessages API signature

```java
IterableStream<ServiceBusReceivedMessage> receiveMessages(int maxMessages, Duration maxWaitTime)
```

where `maxMessages` is the maximum number of messages to receive and `maxWaitTime` is the maximum duration client should wait for the entire batch of `maxMessages` messages to arrive.

*Note*: the overload `receiveMessages(int maxMessages)` is based on the above method, with `maxWaitTime` computed from `RetryConfig` set in builder.

## The receiveMessages API interaction with prefetch-queue

Once the application obtains the `ServiceBusReceiverClient` object, an unbounded prefetch-queue object is assigned to this client object (though in reality, the creation of prefetch-queue is lazy).

<img src="./EmptyPrefetchQueue.png" alt="EmptyPrefetchQueue" width="528">

```java
IterableStream<ServiceBusReceivedMessage> messagesIterable = client.receiveMessages(10, Duration.ofSeconds(5));
```

Once the `receiveMessages` method is called, the Client immediately requests the service bus to return `maxMessages` messages, which asynchronously starts buffering the messages to the prefetch-queue.

<img src="./PrefetchQueueBuffering.png" alt="PrefetchQueueBuffering" width="528">

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

<img src="./PrefetchQueueReading.png" alt="PrefetchQueueReading" width="528">

## The shared nature of prefetch-queue

The prefetch-queue is scoped to the Client and shared across all `receiveMessages` calls.

Given the prefetch-queue is shared, the messages will get asynchronously buffered even if:
1. the application ignores the Iterable returned from `receiveMessages` calls.
2. the application stops iteration in the middle.
3. the `maxTimeout` elapses before receiving `maxMessages`.

> On #3, the current iterator will be completed if the `maxTimeout` elapses, but the buffering continues in the background.

Any messages from a previous `receiveMessages` call that its Iterable couldn't read from the buffer (prefetch-queue) will be read and delivered by the Iterable from the next `receiveMessages` call on the same Client. 

Let's take the previous example, say within `maxTimeout` of 5 sec, the service returned only 7 messages out of 10; the `messagesIterable` will complete after returning those 7 messages and exits the for-loop. The remaining 3 messages can get buffered in the background. If the application then calls `receiveMessages(5, ..)`, then SDK initiates a request for 5 messages. The new Iterable read and deliver 5 messages from the buffer (starting with the first three messages buffered by the initial `receiveMessages` call).

<img src="./PrefetchQueueSharing.png" alt="PrefetchQueueSharing" width="528">

If the application decide not to do anything with the messages in the buffer, those are eventually GC-ed. If such messages were requested with PEEK_LOCK mode, then, after the server-side lock duration elapses, a later `receiveMessages` call on another `ServiceBusReceiverClient` object returns the same messages.

## Only one Iterable can be "active"

It is possible to have more than one Iterable to co-exist. For example, 3 Iterable will be allocated and co-exists if the application simply calls `receiveMessages` 3 times in a row. 

Regardless of the number of co-existing Iterable, there can be only one "active" Iterable, i.e., the application can iterate only one Iterable at a time. The currently "active" Iterable needs to complete ("terminated") for another Iterable to be "active". The Iterable transitions from "active" to "terminated" in the FIFO order they were allocated.

Hence it is recommended to complete the iteration on Iterable from a `receiveMessages` call before invoking `receiveMessages` again to obtain another Iterable. There is no actual use of many "in-active" Iterable to co-exists; it just consumes Heap, possibly making it hard to reason the application code.  

## Timers in receiveMessages API

An invocation of `receiveMessages(int maxMessages, Duration maxWaitTime)` uses two timers. 

The first timer enables `maxWaitTime` support, controlling the maximum duration client should wait for the entire batch of `maxMessages` messages to arrive. If `maxWaitTime` elapses before `maxMessages` messages arrive, Iterable completes immediately after returning the messages received within this duration.

The second timer controls the timeout between messages, i.e., the maximum duration the client should wait for the next message since the arrival of the last message. The Iterable completes if no message arrives within this duration. Currently, this duration is set to 1 second and cannot be changed.

## Messages in the prefetch-queue can expire

Another important point is that messages may expire while in the prefetch-queue, which means the iterator may return expired messages.

The lock timeout configured on the service bus entity and `maxMessages` needs to be balanced such that the lock timeout  is at least exceeds the cumulative expected message processing time for the `maxMessages`, plus one message. At the same time, the lock timeout shouldn't be so long that messages can exceed their maximum time to live when they're accidentally dropped, and so requiring their lock to expire before being redelivered.

Refer to [this][prefetchtradeoff] document for the tradeoff when enabling prefetch (or maxMessages > 1).

## The exception/faulted state in ServiceBusReceiverClient

_The exception topic is not directly related to prefetching, but adding this section given exception/faulted state is also scoped in client object-level (like prefetch-queue) and impacts the behavior of Iterables from `receiveMessages` calls._

The Client types in Service Bus SDK have built-in retry to recover from retriable errors. A `ServiceBusReceiverClient` object can reach a faulted terminal state if it exhausts the maximum retry or encounters a non-retriable error.

Once a `ServiceBusReceiverClient` object is in a faulted state, the SDK will throw an Exception if the application attempt to use any future Iterable.

If the application finds that a `ServiceBusReceiverClient` object is in a faulted state while iterating, the application should create a new client object and dispose the current one.

<!-- Links --->
[prefetchgeneral]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-prefetch?tabs=java
[prefetchtradeoff]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-prefetch?tabs=java#why-is-prefetch-not-the-default-option