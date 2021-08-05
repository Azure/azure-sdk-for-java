// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.implementation.changefeed.HealthMonitor;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancer;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancingStrategy;
import com.azure.cosmos.implementation.changefeed.PartitionManager;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;

/**
 * Helper class to buildAsyncClient {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 * <pre>
 * {@code
 * ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
 *     .hostName(hostName)
 *     .feedContainer(feedContainer)
 *     .leaseContainer(leaseContainer)
 *     .handleChanges(docs -> {
 *         for (JsonNode item : docs) {
 *             // Implementation for handling and processing of each JsonNode item goes here
 *         }
 *     })
 *     .buildChangeFeedProcessor();
 * }
 * </pre>
 */
public class ChangeFeedProcessorBuilderImpl implements ChangeFeedProcessor, AutoCloseable {
    private static final String PK_RANGE_ID_SEPARATOR = ":";
    private static final String SEGMENT_SEPARATOR = "#";
    private static final String PROPERTY_NAME_LSN = "_lsn";

    private final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorBuilderImpl.class);
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE = 100;

    private final static int DEFAULT_DEGREE_OF_PARALLELISM = 25; // default


    private String hostName;
    private ChangeFeedContextClient feedContextClient;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private ChangeFeedObserverFactory observerFactory;
    private volatile String databaseResourceId;
    private volatile String collectionResourceId;
    private ChangeFeedContextClient leaseContextClient;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private LeaseStoreManager leaseStoreManager;
    private HealthMonitor healthMonitor;
    private volatile PartitionManager partitionManager;

    private Scheduler scheduler;

    /**
     * Start listening for changes asynchronously.
     *
     *  @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> start() {
        if (this.partitionManager == null) {
            return this.initializeCollectionPropertiesForBuild()
                .flatMap( value -> this.getLeaseStoreManager()
                    .flatMap(this::buildPartitionManager))
                .flatMap(partitionManager1 -> {
                    this.partitionManager = partitionManager1;
                    return this.partitionManager.start();
                });

        } else {
            return partitionManager.start();
        }
    }

    /**
     * Stops listening for changes asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> stop() {
        if (this.partitionManager == null || !this.partitionManager.isRunning()) {
            throw new IllegalStateException("The ChangeFeedProcessor instance has not fully started");
        }
        return this.partitionManager.stop();
    }

    /**
     * Returns the state of the change feed processor.
     *
     * @return true if the change feed processor is currently active and running.
     */
    @Override
    public boolean isStarted() {
        return this.partitionManager != null && this.partitionManager.isRunning();
    }

    /**
     * Returns the current owner (host) and an approximation of the difference between the last processed item (defined
     *   by the state of the feed container) and the latest change in the container for each partition (lease
     *   document).
     * <p>
     * An empty map will be returned if the processor was not started or no lease documents matching the current
     *   {@link ChangeFeedProcessor} instance's lease prefix could be found.
     *
     * @return a map representing the current owner and lease token, the current LSN and latest LSN, and the estimated
     *         lag, asynchronously.
     */
    @Override
    public Mono<Map<String, Integer>> getEstimatedLag() {
        Map<String, Integer> earlyResult = new ConcurrentHashMap<>();

        if (this.leaseStoreManager == null || this.feedContextClient == null) {
            return Mono.just(earlyResult);
        }

        return this.leaseStoreManager.getAllLeases()
            .flatMap(lease -> {
                final FeedRangeInternal feedRange = new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken());
                final CosmosChangeFeedRequestOptions options =
                    ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(
                        lease.getContinuationState(
                            this.collectionResourceId,
                            feedRange));
                options.setMaxItemCount(1);

                return this.feedContextClient.createDocumentChangeFeedQuery(
                        this.feedContextClient.getContainerClient(),
                        options)
                    .take(1)
                    .map(feedResponse -> {
                        String ownerValue = lease.getOwner();
                        String sessionTokenLsn = feedResponse.getSessionToken();
                        String parsedSessionToken = sessionTokenLsn.substring(
                            sessionTokenLsn.indexOf(PK_RANGE_ID_SEPARATOR));
                        String[] segments = StringUtils.split(parsedSessionToken, SEGMENT_SEPARATOR);
                        String latestLsn = segments[0];

                        if (segments.length >= 2) {
                            // default to Global LSN
                            latestLsn = segments[1];
                        }

                        if (ownerValue == null) {
                            ownerValue = "";
                        }

                        // An empty list of documents returned means that we are current (zero lag)
                        if (feedResponse.getResults() == null || feedResponse.getResults().size() == 0) {
                            return Pair.of(ownerValue + "_" + lease.getLeaseToken(), 0);
                        }

                        int currentLsn = 0;
                        int estimatedLag;
                        try {
                            currentLsn = Integer.parseInt(feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText("0"));
                            estimatedLag = Integer.parseInt(latestLsn);
                            estimatedLag = estimatedLag - currentLsn + 1;
                        } catch (NumberFormatException ex) {
                            logger.warn("Unexpected Cosmos LSN found", ex);
                            estimatedLag = -1;
                        }

                        return Pair.of(
                            ownerValue + "_" + lease.getLeaseToken() + "_" + currentLsn + "_" + latestLsn,
                            estimatedLag);
                    });
            })
            .collectList()
            .map(valueList -> {
                Map<String, Integer> result = new ConcurrentHashMap<>();
                for (Pair<String, Integer> pair : valueList) {
                    result.put(pair.getKey(), pair.getValue());
                }
                return result;
            });
    }

    /**
     * Returns a list of states each representing one scoped worker item.
     * <p>
     * An empty list will be returned if the processor was not started or no lease items matching the current
     *   {@link ChangeFeedProcessor} instance's lease prefix could be found.
     *
     * @return a list of states each representing one scoped worker item.
     */
    @Override
    public Mono<List<ChangeFeedProcessorState>> getCurrentState() {

        if (this.leaseStoreManager == null || this.feedContextClient == null) {
            return Mono.just(Collections.unmodifiableList(new ArrayList<>()));
        }

        return this.leaseStoreManager.getAllLeases()
            .flatMap(lease -> {
                final FeedRangeInternal feedRange = new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken());
                final CosmosChangeFeedRequestOptions options =
                    ModelBridgeInternal.createChangeFeedRequestOptionsForChangeFeedState(
                        lease.getContinuationState(
                            this.collectionResourceId,
                            feedRange));
                options.setMaxItemCount(1);

                return this.feedContextClient.createDocumentChangeFeedQuery(
                        this.feedContextClient.getContainerClient(),
                        options)
                    .take(1)
                    .map(feedResponse -> {
                        String sessionTokenLsn = feedResponse.getSessionToken();
                        String parsedSessionToken = sessionTokenLsn.substring(
                            sessionTokenLsn.indexOf(PK_RANGE_ID_SEPARATOR));
                        String[] segments = StringUtils.split(parsedSessionToken, SEGMENT_SEPARATOR);
                        String latestLsn = segments[0];

                        if (segments.length >= 2) {
                            // default to Global LSN
                            latestLsn = segments[1];
                        }

                        // lease.getId() - the ID of the lease item representing the persistent state of a
                        // change feed processor worker.
                        // latestLsn - a marker representing the latest item that will be processed.
                        ChangeFeedProcessorState changeFeedProcessorState = new ChangeFeedProcessorState()
                            .setHostName(lease.getOwner())
                            .setLeaseToken(lease.getLeaseToken());

                        // An empty list of documents returned means that we are current (zero lag)
                        if (feedResponse.getResults() == null || feedResponse.getResults().size() == 0) {
                            changeFeedProcessorState.setEstimatedLag(0)
                                .setContinuationToken(latestLsn);

                            return changeFeedProcessorState;
                        }

                        changeFeedProcessorState.setContinuationToken(
                            feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText(null));

                        int currentLsn;
                        int estimatedLag;
                        try {
                            currentLsn = Integer.parseInt(feedResponse.getResults().get(0).get(PROPERTY_NAME_LSN).asText("0"));
                            estimatedLag = Integer.parseInt(latestLsn);
                            estimatedLag = estimatedLag - currentLsn + 1;
                            changeFeedProcessorState.setEstimatedLag(estimatedLag);
                        } catch (NumberFormatException ex) {
                            logger.warn("Unexpected Cosmos LSN found", ex);
                            changeFeedProcessorState.setEstimatedLag(-1);
                        }

                        return changeFeedProcessorState;
                    });
            })
            .collectList()
            .map(Collections::unmodifiableList);
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique name.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosAsyncContainer} to be used to read from the monitored collection.
     *
     * @param feedDocumentClient the instance of {@link CosmosAsyncContainer} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl feedContainer(CosmosAsyncContainer feedDocumentClient) {
        if (feedDocumentClient == null) {
            throw new IllegalArgumentException("feedContextClient");
        }

        this.feedContextClient = new ChangeFeedContextClientImpl(feedDocumentClient);
        return this;
    }

    /**
     * Sets the {@link ChangeFeedProcessorOptions} to be used.
     *
     * @param changeFeedProcessorOptions the change feed processor options to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl options(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        if (changeFeedProcessorOptions == null) {
            throw new IllegalArgumentException("changeFeedProcessorOptions");
        }

        this.changeFeedProcessorOptions = changeFeedProcessorOptions;

        return this;
    }

    /**
     * Sets the {@link ChangeFeedObserverFactory} to be used to generate {@link ChangeFeedObserver}
     *
     * @param observerFactory The instance of {@link ChangeFeedObserverFactory} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl observerFactory(ChangeFeedObserverFactory observerFactory) {
        if (observerFactory == null) {
            throw new IllegalArgumentException("observerFactory");
        }

        this.observerFactory = observerFactory;
        return this;
    }

    public ChangeFeedProcessorBuilderImpl handleChanges(Consumer<List<JsonNode>> consumer) {
        return this.observerFactory(new DefaultObserverFactory(consumer));
    }

    /**
     * Sets an existing {@link CosmosAsyncContainer} to be used to read from the leases collection.
     *
     * @param leaseClient the instance of {@link CosmosAsyncContainer} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl leaseContainer(CosmosAsyncContainer leaseClient) {
        if (leaseClient == null) {
            throw new IllegalArgumentException("leaseClient");
        }

        if (!getContextClient(leaseClient).isContentResponseOnWriteEnabled()) {
            throw new IllegalArgumentException("leaseClient: content response on write setting must be enabled");
        }

        ConsistencyLevel consistencyLevel = getContextClient(leaseClient).getConsistencyLevel();
        if (consistencyLevel == ConsistencyLevel.CONSISTENT_PREFIX || consistencyLevel == ConsistencyLevel.EVENTUAL) {
            logger.warn("leaseClient consistency level setting are less then expected which is SESSION");
        }

        this.leaseContextClient = new ChangeFeedContextClientImpl(leaseClient);

        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    public ChangeFeedProcessor build() {
        if (this.hostName == null) {
            throw new IllegalArgumentException("Host name was not specified");
        }

        if (this.observerFactory == null) {
            throw new IllegalArgumentException("Observer was not specified");
        }

        if (this.changeFeedProcessorOptions != null && this.changeFeedProcessorOptions.getLeaseAcquireInterval().compareTo(ChangeFeedProcessorOptions.DEFAULT_ACQUIRE_INTERVAL) < 0) {
            logger.warn("Found lower than expected setting for leaseAcquireInterval");
        }

        if (this.scheduler == null) {
            this.scheduler = Schedulers.boundedElastic();
        }

        return this;
    }

    public ChangeFeedProcessorBuilderImpl() {
    }

    private Mono<ChangeFeedProcessor> initializeCollectionPropertiesForBuild() {
        if (this.changeFeedProcessorOptions == null) {
            this.changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }

        return this.feedContextClient
            .readDatabase(this.feedContextClient.getDatabaseClient(), null)
            .map( databaseResourceResponse -> {
                this.databaseResourceId = databaseResourceResponse.getProperties().getId();
                return this.databaseResourceId;
            })
            .flatMap( id -> this.feedContextClient
                .readContainer(this.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
                    this.collectionResourceId = documentCollectionResourceResponse.getProperties().getId();
                    return this;
                }));
    }

    private Mono<LeaseStoreManager> getLeaseStoreManager() {
        if (this.leaseStoreManager == null) {

            return this.leaseContextClient.readContainerSettings(this.leaseContextClient.getContainerClient(), null)
                .flatMap( collectionSettings -> {
                    boolean isPartitioned =
                        collectionSettings.getPartitionKeyDefinition() != null &&
                            collectionSettings.getPartitionKeyDefinition().getPaths() != null &&
                            collectionSettings.getPartitionKeyDefinition().getPaths().size() > 0;
                    if (!isPartitioned || (collectionSettings.getPartitionKeyDefinition().getPaths().size() != 1 || !collectionSettings.getPartitionKeyDefinition().getPaths().get(0).equals("/id"))) {
//                        throw new IllegalArgumentException("The lease collection, if partitioned, must have partition key equal to id.");
                        return Mono.error(new IllegalArgumentException("The lease collection must have partition key equal to id."));
                    }

                    RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();

                    String leasePrefix = this.getLeasePrefix();

                    return LeaseStoreManager.builder()
                        .leasePrefix(leasePrefix)
                        .leaseCollectionLink(this.leaseContextClient.getContainerClient())
                        .leaseContextClient(this.leaseContextClient)
                        .requestOptionsFactory(requestOptionsFactory)
                        .hostName(this.hostName)
                        .build()
                        .map(manager -> {
                            this.leaseStoreManager = manager;
                            return this.leaseStoreManager;
                        });
                });
        }

        return Mono.just(this.leaseStoreManager);
    }

    private String getLeasePrefix() {
        String optionsPrefix = this.changeFeedProcessorOptions.getLeasePrefix();

        if (optionsPrefix == null) {
            optionsPrefix = "";
        }

        URI uri = this.feedContextClient.getServiceEndpoint();

        return String.format(
            "%s%s_%s_%s",
            optionsPrefix,
            uri.getHost(),
            this.databaseResourceId,
            this.collectionResourceId);
    }

    private Mono<PartitionManager> buildPartitionManager(LeaseStoreManager leaseStoreManager) {
        CheckpointerObserverFactory factory = new CheckpointerObserverFactory(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            DEFAULT_DEGREE_OF_PARALLELISM,
            DEFAULT_QUERY_PARTITIONS_MAX_BATCH_SIZE,
            this.collectionResourceId
        );

        Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, leaseStoreManager, this.lockTime, this.sleepTime);
        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl(
            factory,
            leaseStoreManager,
            new PartitionProcessorFactoryImpl(
                this.feedContextClient,
                this.changeFeedProcessorOptions,
                leaseStoreManager,
                this.feedContextClient.getContainerClient(),
                this.collectionResourceId),
            this.changeFeedProcessorOptions,
            this.scheduler
        );

        if (this.loadBalancingStrategy == null) {
            this.loadBalancingStrategy = new EqualPartitionsBalancingStrategy(
                this.hostName,
                this.changeFeedProcessorOptions.getMinScaleCount(),
                this.changeFeedProcessorOptions.getMaxScaleCount(),
                this.changeFeedProcessorOptions.getLeaseExpirationInterval());
        }

        PartitionController partitionController = new PartitionControllerImpl(leaseStoreManager, leaseStoreManager, partitionSupervisorFactory, synchronizer, scheduler);

        if (this.healthMonitor == null) {
            this.healthMonitor = new TraceHealthMonitor();
        }

        PartitionController partitionController2 = new HealthMonitoringPartitionControllerDecorator(partitionController, this.healthMonitor);

        PartitionLoadBalancer partitionLoadBalancer = new PartitionLoadBalancerImpl(
            partitionController2,
            leaseStoreManager,
            this.loadBalancingStrategy,
            this.changeFeedProcessorOptions.getLeaseAcquireInterval(),
            this.scheduler
        );

        PartitionManager partitionManager = new PartitionManagerImpl(bootstrapper, partitionController, partitionLoadBalancer);

        return Mono.just(partitionManager);
    }

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
