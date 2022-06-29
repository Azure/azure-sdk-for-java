// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;


import com.azure.core.annotation.ServiceClient;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * A client responsible for publishing instances of {@link EventData} to a specific Event Hub.  Depending on the options
 * specified when events are enqueued, they may be automatically assigned to a partition, grouped according to the
 * specified partition key, or assigned a specifically requested partition.
 *
 * <p>
 * The {@link EventHubBufferedProducerClient} does not publish immediately, instead using a deferred model where
 * events are collected into a buffer so that they may be efficiently batched and published when the batch is full or
 * the {@link EventHubBufferedProducerClientBuilder#maxWaitTime(Duration) maxWaitTime} has elapsed with no new events
 * enqueued.
 * </p>
 * <p>
 * This model is intended to shift the burden of batch management from callers, at the cost of non-deterministic timing,
 * for when events will be published. There are additional trade-offs to consider, as well:
 * </p>
 * <ul>
 * <li>If the application crashes, events in the buffer will not have been published.  To
 * prevent data loss, callers are encouraged to track publishing progress using
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchFailed(Consumer) onSendBatchFailed} and
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchSucceeded(Consumer) onSendBatchSucceeded}.</li>
 * <li>Events specifying a partition key may be assigned a different partition than those
 * using the same key with other producers.</li>
 * <li>In the unlikely event that a partition becomes temporarily unavailable,
 * the {@link EventHubBufferedProducerClient} may take longer to recover than other producers.</li>
 * </ul>
 * <p>
 * In scenarios where it is important to have events published immediately with a deterministic outcome, ensure that
 * partition keys are assigned to a partition consistent with other publishers, or where maximizing availability is a
 * requirement, using {@link EventHubProducerAsyncClient} or {@link EventHubProducerClient} is recommended.
 * </p>
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class, isAsync = false)
public final class EventHubBufferedProducerClient {
}
