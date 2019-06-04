// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.util.function.Consumer;

/**
 * BatchOptions is used to create {@link EventDataBatch}es.
 * <p>If you're creating {@link EventDataBatch}es with {@link EventHubClient}, then you can set a partitionKey and maxMessageSize
 * using the .with() method. Alternatively, if you'd like the default settings, simply construct BatchOptions with the void constructor.
 * Default settings:
 * - partitionKey is null
 * - maxMessageSize is the maximum allowed size
 * <p>If you're creating {@link EventDataBatch}es with {@link PartitionSender}, then you can only set a maxMessageSize
 * using the .with() method. Alternatively, if you'd like the default settings, simply construct BatchOptions with the void constructor.
 * Default settings:
 * - maxMessageSize is the maximum allowed size
 * - Note: if you set a partition key, an {@link IllegalArgumentException} will be thrown.
 * <p>To construct either type of batch, create a {@link BatchOptions} object and pass it into the appropriate
 * createBatch method. If using {@link PartitionSender}, then use ({@link PartitionSender#createBatch(BatchOptions)}.
 * If using {@link EventHubClient}, then use {@link EventHubClient#createBatch(BatchOptions)}.
 * <pre>
 *     {@code
 *     // Note: For all examples, 'client' is an instance of EventHubClient. The usage is the same for PartitionSender,
 *     however, you can NOT set a partition key when using PartitionSender
 *
 *     // Create EventDataBatch with defaults
 *     EventDataBatch edb1 = client.createBatch();
 *
 *     // Create EventDataBatch with custom partitionKey
 *     BatchOptions options = new BatchOptions().with( options -> options.partitionKey = "foo");
 *     EventDataBatch edb2 = client.createBatch(options);
 *
 *     // Create EventDataBatch with custom partitionKey and maxMessageSize
 *     BatchOptions options = new BatchOptions().with ( options -> {
 *         options.partitionKey = "foo";
 *         options.maxMessageSize = 100 * 1024;
 *     };
 *     EventDataBatch edb3 = client.createBatch(options);
 *     }
 * </pre>
 */
public final class BatchOptions {

    /**
     * The partitionKey to use for all {@link EventData}s sent in the current {@link EventDataBatch}.
     * Setting a PartitionKey will deliver the {@link EventData} to a specific Event Hubs partition.
     */
    public String partitionKey;

    /**
     * The maximum size in bytes of {@link EventDataBatch} being constructed.
     * This value cannot exceed the maximum size supported by Event Hubs service.
     * {@link EventDataBatch#tryAdd(EventData)} API will use this value as the upper limit.
     */
    public Integer maxMessageSize;

    public BatchOptions with(Consumer<BatchOptions> builderFunction) {
        builderFunction.accept(this);
        return this;
    }
}
