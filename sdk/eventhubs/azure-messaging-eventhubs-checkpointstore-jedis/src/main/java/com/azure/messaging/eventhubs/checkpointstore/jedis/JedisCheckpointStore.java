// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.exception.AzureException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.azure.messaging.eventhubs.checkpointstore.jedis.ClientConstants.CONSUMER_GROUP_KEY;
import static com.azure.messaging.eventhubs.checkpointstore.jedis.ClientConstants.ENTITY_NAME_KEY;
import static com.azure.messaging.eventhubs.checkpointstore.jedis.ClientConstants.HOSTNAME_KEY;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 *
 * <p><strong>Instantiate checkpoint store</strong></p>
 * Demonstrates one way to instantiate the checkpoint store. {@link JedisPool} has multiple ways to create an instance.
 *
 * <!-- src_embed com.azure.messaging.eventhubs.jedischeckpointstore.instantiation -->
 * <pre>
 * JedisClientConfig clientConfig = DefaultJedisClientConfig.builder&#40;&#41;
 *     .password&#40;&quot;&lt;YOUR_REDIS_PRIMARY_ACCESS_KEY&gt;&quot;&#41;
 *     .ssl&#40;true&#41;
 *     .build&#40;&#41;;
 *
 * String redisHostName = &quot;&lt;YOUR_REDIS_HOST_NAME&gt;.redis.cache.windows.net&quot;;
 * HostAndPort hostAndPort = new HostAndPort&#40;redisHostName, 6380&#41;;
 * JedisPool jedisPool = new JedisPool&#40;hostAndPort, clientConfig&#41;;
 *
 * CheckpointStore checkpointStore = new JedisCheckpointStore&#40;jedisPool&#41;;
 *
 * &#47;&#47; DefaultAzureCredential tries to resolve the best credential to use based on your environment.
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * EventProcessorClient processorClient = new EventProcessorClientBuilder&#40;&#41;
 *     .checkpointStore&#40;checkpointStore&#41;
 *     .fullyQualifiedNamespace&#40;&quot;&lt;YOUR_EVENT_HUB_FULLY_QUALIFIED_NAMESPACE&gt;&quot;&#41;
 *     .eventHubName&#40;&quot;&lt;YOUR_EVENT_HUB_NAME&gt;&quot;&#41;
 *     .credential&#40;credential&#41;
 *     .consumerGroup&#40;&quot;&lt;YOUR_CONSUMER_GROUP_NAME&gt;&quot;&#41;
 *     .processEvent&#40;eventContext -&gt; &#123;
 *         System.out.println&#40;&quot;Partition id = &quot; + eventContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41; + &quot; and &quot;
 *             + &quot;sequence number of event = &quot; + eventContext.getEventData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .processError&#40;context -&gt; &#123;
 *         System.out.println&#40;&quot;Error occurred while processing events &quot; + context.getThrowable&#40;&#41;.getMessage&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .buildEventProcessorClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.jedischeckpointstore.instantiation -->
 *
 * @see EventProcessorClient
 * @see EventProcessorClientBuilder
 */
public final class JedisCheckpointStore implements CheckpointStore {
    private static final String PARTITION_ID_KEY = "partitionId";

    private static final ClientLogger LOGGER = new ClientLogger(JedisCheckpointStore.class);
    static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);
    static final byte[] CHECKPOINT = "checkpoint".getBytes(StandardCharsets.UTF_8);
    static final byte[] PARTITION_OWNERSHIP = "partitionOwnership".getBytes(StandardCharsets.UTF_8);
    private final JedisPool jedisPool;

    /**
     * Constructor for JedisRedisCheckpointStore
     *
     * @param jedisPool a JedisPool object that creates a pool connected to the Azure Redis Cache
     *
     * @throws IllegalArgumentException thrown when JedisPool object supplied is null
     */
    public JedisCheckpointStore(JedisPool jedisPool) {
        if (jedisPool == null) {
            throw LOGGER.logExceptionAsError(Exceptions
                .propagate(new IllegalArgumentException(
                    "JedisPool object supplied to constructor is null.")));
        }
        this.jedisPool = jedisPool;
    }

    /**
     * This method returns the list of partitions that were owned successfully.
     *
     * @param requestedPartitionOwnerships List of partition ownerships from the current instance
     *
     * @return Flux of PartitionOwnership objects
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {
        return Flux.fromIterable(requestedPartitionOwnerships).handle((partitionOwnership, sink) -> {
            String partitionId = partitionOwnership.getPartitionId();
            String fullyQualifiedNamespace = partitionOwnership.getFullyQualifiedNamespace();
            String eventHubName = partitionOwnership.getEventHubName();
            String consumerGroup = partitionOwnership.getConsumerGroup();
            byte[] prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
            byte[] key = keyBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup, partitionId);

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.sadd(prefix, key);

                // Start watching for any updates.
                jedis.watch(key);

                long lastModifiedTimeSeconds = Long.parseLong(jedis.time().get(0));
                partitionOwnership.setLastModifiedTime(lastModifiedTimeSeconds * 1000);
                partitionOwnership.setETag("");
                byte[] serializedOwnership = DEFAULT_SERIALIZER.serializeToBytes(partitionOwnership);

                try {
                    Transaction transaction = jedis.multi();
                    transaction.hset(key, PARTITION_OWNERSHIP, serializedOwnership);

                    // If at least one watched key is modified before the EXEC command, the whole transaction aborts, and
                    // EXEC returns a Null reply to notify that the transaction failed.
                    List<Object> executionResponse = transaction.exec();

                    if (executionResponse == null) {
                        // This means that the transaction did not execute, which implies that another client has
                        // changed the ownership during this transaction
                        sink.error(createClaimPartitionException(fullyQualifiedNamespace, eventHubName, consumerGroup,
                            partitionId, "Transaction was aborted."));
                    } else if (executionResponse.isEmpty()) {
                        sink.error(createClaimPartitionException(fullyQualifiedNamespace, eventHubName, consumerGroup,
                            partitionId, "No command results in transaction result."));
                    } else if (executionResponse.get(0) == null) {
                        sink.error(createClaimPartitionException(fullyQualifiedNamespace, eventHubName, consumerGroup,
                            partitionId, "Executing update command resulted in null."));
                    } else {
                        addEventHubInformation(LOGGER.atVerbose(), fullyQualifiedNamespace, eventHubName, consumerGroup)
                            .addKeyValue(PARTITION_ID_KEY, partitionId)
                            .log("Claimed partition.");

                        sink.next(partitionOwnership);
                    }
                } catch (Exception e) {
                    sink.error(e);
                } finally {
                    jedis.unwatch();
                }
            }
        });
    }

    /**
     * This method returns the list of checkpoints from the underlying data store, and if no checkpoints are available,
     * then it returns empty results.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the current Event Hub instance
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     *
     * @return Flux of Checkpoint objects
     */
    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return Flux.create(sink -> {
            try (Jedis jedis = jedisPool.getResource()) {

                byte[] prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
                Set<byte[]> members = jedis.smembers(prefix);
                if (members == null) {
                    sink.complete();
                    return;
                }

                for (byte[] member : members) {
                    //get the associated JSON representation for each for the members
                    List<byte[]> checkpointJsonList = jedis.hmget(member, CHECKPOINT);

                    if (CoreUtils.isNullOrEmpty(checkpointJsonList) || checkpointJsonList.get(0) == null) {
                        addEventHubInformation(LOGGER.atVerbose(), fullyQualifiedNamespace, eventHubName, consumerGroup)
                            .log("Checkpoint does not exist.");
                    } else {
                        Checkpoint checkpoint = DEFAULT_SERIALIZER.deserializeFromBytes(checkpointJsonList.get(0),
                            TypeReference.createInstance(Checkpoint.class));
                        sink.next(checkpoint);
                    }
                }
                sink.complete();
            } catch (Throwable e) {
                sink.error(e);
            }
        });
    }

    /**
     * This method returns the list of ownership records from the underlying data store, and if no ownership records are available, then it returns empty results.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance of Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     *
     * @return Flux of PartitionOwnership objects
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return Flux.create(sink -> {
            try (Jedis jedis = jedisPool.getResource()) {
                byte[] prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
                Set<byte[]> members = jedis.smembers(prefix);
                if (members == null) {
                    sink.complete();
                    return;
                }

                for (byte[] member : members) {
                    //get the associated JSON representation for each for the members
                    List<byte[]> partitionOwnershipJsonList = jedis.hmget(member, PARTITION_OWNERSHIP);

                    // if PARTITION_OWNERSHIP field exists but has no records than the list will be empty
                    if (!partitionOwnershipJsonList.isEmpty()) {
                        byte[] partitionOwnershipJson = partitionOwnershipJsonList.get(0);
                        // if PARTITION_OWNERSHIP field does not exist for member we will get a null
                        if (partitionOwnershipJson == null) {
                            addEventHubInformation(LOGGER.atVerbose(), fullyQualifiedNamespace, eventHubName, consumerGroup)
                                .log("No partition ownership records exist for this checkpoint yet.");

                            continue;
                        }
                        PartitionOwnership partitionOwnership = DEFAULT_SERIALIZER.deserializeFromBytes(partitionOwnershipJson, TypeReference.createInstance(PartitionOwnership.class));
                        sink.next(partitionOwnership);
                    }
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });

    }

    /**
     * This method updates the checkpoint in the Jedis resource for a given partition.
     *
     * @param checkpoint Checkpoint information for this partition
     *
     * @return Mono that completes if no errors take place
     * @throws NullPointerException if {@code checkpoint} is null.
     * @throws IllegalArgumentException if {@code checkpoint} does not have a sequenceNumber or offset.
     */
    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        if (Objects.isNull(checkpoint)) {
            return Mono.error(new NullPointerException("'checkpoint' cannot be null."));
        }

        if (!isCheckpointValid(checkpoint)) {
            return FluxUtil.monoError(addEventHubInformation(LOGGER.atError(), checkpoint.getFullyQualifiedNamespace(),
                    checkpoint.getEventHubName(), checkpoint.getConsumerGroup())
                    .addKeyValue(PARTITION_ID_KEY, checkpoint.getPartitionId()),
                new IllegalArgumentException("Checkpoint is either null, or both the offset and the sequence number are null."));
        }

        return Mono.fromRunnable(() -> {
            byte[] key = keyBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(),
                checkpoint.getConsumerGroup(), checkpoint.getPartitionId());

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.hset(key, CHECKPOINT, DEFAULT_SERIALIZER.serializeToBytes(checkpoint));
            }
        });
    }

    static byte[] prefixBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return (fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup).getBytes(StandardCharsets.UTF_8);
    }

    static byte[] keyBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup, String partitionId) {
        return (fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup + "/" + partitionId).getBytes(StandardCharsets.UTF_8);
    }

    private static Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null);
    }

    private static LoggingEventBuilder addEventHubInformation(LoggingEventBuilder builder,
        String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {

        return builder.addKeyValue(HOSTNAME_KEY, fullyQualifiedNamespace)
            .addKeyValue(ENTITY_NAME_KEY, eventHubName)
            .addKeyValue(CONSUMER_GROUP_KEY, consumerGroup);
    }

    private static AzureException createClaimPartitionException(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup, String partitionId, String message) {

        AzureException exception = new AzureException("Unable to claim partition: " + partitionId +  ". " + message);
        addEventHubInformation(LOGGER.atInfo(), fullyQualifiedNamespace, eventHubName, consumerGroup)
            .addKeyValue(PARTITION_ID_KEY, partitionId)
            .log("Unable to claim partition.", exception);

        return exception;
    }
}
