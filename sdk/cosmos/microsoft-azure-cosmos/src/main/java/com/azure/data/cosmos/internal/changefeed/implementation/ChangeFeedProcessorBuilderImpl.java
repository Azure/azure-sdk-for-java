// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.ChangeFeedProcessor;
import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.changefeed.Bootstrapper;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverFactory;
import com.azure.data.cosmos.internal.changefeed.CheckpointFrequency;
import com.azure.data.cosmos.internal.changefeed.HealthMonitor;
import com.azure.data.cosmos.internal.changefeed.LeaseStoreManager;
import com.azure.data.cosmos.internal.changefeed.PartitionController;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancer;
import com.azure.data.cosmos.internal.changefeed.PartitionLoadBalancingStrategy;
import com.azure.data.cosmos.internal.changefeed.PartitionManager;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessorFactory;
import com.azure.data.cosmos.internal.changefeed.PartitionSupervisorFactory;
import com.azure.data.cosmos.internal.changefeed.RequestOptionsFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Helper class to build {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 * <pre>
 * {@code
 *  ChangeFeedProcessor.Builder()
 *     .hostName(hostName)
 *     .feedContainer(feedContainer)
 *     .leaseContainer(leaseContainer)
 *     .handleChanges(docs -> {
 *         // Implementation for handling and processing CosmosItemProperties list goes here
 *      })
 *     .observer(SampleObserverImpl.class)
 *     .build();
 * }
 * </pre>
 */
public class ChangeFeedProcessorBuilderImpl implements ChangeFeedProcessor.BuilderDefinition, ChangeFeedProcessor, AutoCloseable {
    private static final long DefaultUnhealthinessDuration = Duration.ofMinutes(15).toMillis();
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);
    private static final int DefaultQueryPartitionsMaxBatchSize = 100;

    private int queryPartitionsMaxBatchSize = DefaultQueryPartitionsMaxBatchSize;
    private int degreeOfParallelism = 25; // default


    private String hostName;
    private ChangeFeedContextClient feedContextClient;
    private ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private ChangeFeedObserverFactory observerFactory;
    private String databaseResourceId;
    private String collectionResourceId;
    private ChangeFeedContextClient leaseContextClient;
    private PartitionLoadBalancingStrategy loadBalancingStrategy;
    private PartitionProcessorFactory partitionProcessorFactory;
    private LeaseStoreManager leaseStoreManager;
    private HealthMonitor healthMonitor;
    private PartitionManager partitionManager;

    private ExecutorService executorService;

    /**
     * Start listening for changes asynchronously.
     *
     *  @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> start() {
        return partitionManager.start();
    }

    /**
     * Stops listening for changes asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> stop() {
        return partitionManager.stop();
    }

    /**
     * Sets the host name.
     *
     * @param hostName the name to be used for the host. When using multiple hosts, each host must have a unique name.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl hostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Sets and existing {@link CosmosContainer} to be used to read from the monitored collection.
     *
     * @param feedDocumentClient the instance of {@link CosmosContainer} to be used.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl feedContainer(CosmosContainer feedDocumentClient) {
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
    @Override
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

    /**
     * Sets an existing {@link ChangeFeedObserver} type to be used by a {@link ChangeFeedObserverFactory} to process changes.
     * @param type the type of {@link ChangeFeedObserver} to be used.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl observer(Class<? extends ChangeFeedObserver> type) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }

        this.observerFactory = new ChangeFeedObserverFactoryImpl(type);

        return this;
    }

    @Override
    public ChangeFeedProcessorBuilderImpl handleChanges(Consumer<List<CosmosItemProperties>> consumer) {
        return this.observerFactory(new DefaultObserverFactory(consumer));
    }

    /**
     * Sets the database resource ID of the monitored collection.
     *
     * @param databaseResourceId the database resource ID of the monitored collection.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withDatabaseResourceId(String databaseResourceId) {
        this.databaseResourceId = databaseResourceId;
        return this;
    }

    /**
     * Sets the collection resource ID of the monitored collection.
     * @param collectionResourceId the collection resource ID of the monitored collection.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withCollectionResourceId(String collectionResourceId) {
        this.collectionResourceId = collectionResourceId;
        return this;
    }

    /**
     * Sets an existing {@link CosmosContainer} to be used to read from the leases collection.
     *
     * @param leaseDocumentClient the instance of {@link CosmosContainer} to use.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl leaseContainer(CosmosContainer leaseDocumentClient) {
        if (leaseDocumentClient == null) {
            throw new IllegalArgumentException("leaseContextClient");
        }

        this.leaseContextClient = new ChangeFeedContextClientImpl(leaseDocumentClient);
        return this;
    }

    /**
     * Sets the {@link PartitionLoadBalancingStrategy} to be used for partition load balancing.
     *
     * @param loadBalancingStrategy the {@link PartitionLoadBalancingStrategy} to be used for partition load balancing.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withPartitionLoadBalancingStrategy(PartitionLoadBalancingStrategy loadBalancingStrategy) {
        if (loadBalancingStrategy == null) {
            throw new IllegalArgumentException("loadBalancingStrategy");
        }

        this.loadBalancingStrategy = loadBalancingStrategy;
        return this;
    }

    /**
     * Sets the {@link PartitionProcessorFactory} to be used to create {@link PartitionProcessor} for partition processing.
     *
     * @param partitionProcessorFactory the instance of {@link PartitionProcessorFactory} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withPartitionProcessorFactory(PartitionProcessorFactory partitionProcessorFactory) {
        if (partitionProcessorFactory == null) {
            throw new IllegalArgumentException("partitionProcessorFactory");
        }

        this.partitionProcessorFactory = partitionProcessorFactory;
        return this;
    }

    /**
     * Sets the {@link LeaseStoreManager} to be used to manage leases.
     *
     * @param leaseStoreManager the instance of {@link LeaseStoreManager} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withLeaseStoreManager(LeaseStoreManager leaseStoreManager) {
        if (leaseStoreManager == null) {
            throw new IllegalArgumentException("leaseStoreManager");
        }

        this.leaseStoreManager = leaseStoreManager;
        return this;
    }

    /**
     * Sets the {@link HealthMonitor} to be used to monitor unhealthiness situation.
     *
     * @param healthMonitor The instance of {@link HealthMonitor} to use.
     * @return current Builder.
     */
    public ChangeFeedProcessorBuilderImpl withHealthMonitor(HealthMonitor healthMonitor) {
        if (healthMonitor == null) {
            throw new IllegalArgumentException("healthMonitor");
        }

        this.healthMonitor = healthMonitor;
        return this;
    }

    /**
     * Builds a new instance of the {@link ChangeFeedProcessor} with the specified configuration asynchronously.
     *
     * @return an instance of {@link ChangeFeedProcessor}.
     */
    @Override
    public ChangeFeedProcessor build() {
        ChangeFeedProcessorBuilderImpl self = this;

        if (this.hostName == null)
        {
            throw new IllegalArgumentException("Host name was not specified");
        }

        if (this.observerFactory == null)
        {
            throw new IllegalArgumentException("Observer was not specified");
        }

        if (this.executorService == null) {
            this.executorService = Executors.newCachedThreadPool();
        }

        // TBD: Move this initialization code as part of the start() call.
        return this.initializeCollectionPropertiesForBuild()
            .then(self.getLeaseStoreManager().flatMap(leaseStoreManager -> self.buildPartitionManager(leaseStoreManager)))
            .map(partitionManager1 -> {
                self.partitionManager = partitionManager1;
                return self;
            }).block();
    }

    public ChangeFeedProcessorBuilderImpl() {
        this.queryPartitionsMaxBatchSize = DefaultQueryPartitionsMaxBatchSize;
        this.degreeOfParallelism = 25; // default
    }

    public ChangeFeedProcessorBuilderImpl(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    private Mono<Void> initializeCollectionPropertiesForBuild() {
        ChangeFeedProcessorBuilderImpl self = this;

        if (this.changeFeedProcessorOptions == null) {
            this.changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }

        return this.feedContextClient
            .readDatabase(this.feedContextClient.getDatabaseClient(), null)
            .map( databaseResourceResponse -> {
                self.databaseResourceId = databaseResourceResponse.database().id();
                return self.databaseResourceId;
            })
            .flatMap( id -> self.feedContextClient
                .readContainer(self.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
                    self.collectionResourceId = documentCollectionResourceResponse.container().id();
                    return self.collectionResourceId;
                }))
            .then();
    }

    private Mono<LeaseStoreManager> getLeaseStoreManager() {
        ChangeFeedProcessorBuilderImpl self = this;

        if (this.leaseStoreManager == null) {

            return this.leaseContextClient.readContainerSettings(this.leaseContextClient.getContainerClient(), null)
                .flatMap( collectionSettings -> {
                    boolean isPartitioned =
                        collectionSettings.partitionKeyDefinition() != null &&
                            collectionSettings.partitionKeyDefinition().paths() != null &&
                            collectionSettings.partitionKeyDefinition().paths().size() > 0;
                    if (!isPartitioned || (collectionSettings.partitionKeyDefinition().paths().size() != 1 || !collectionSettings.partitionKeyDefinition().paths().get(0).equals("/id"))) {
//                        throw new IllegalArgumentException("The lease collection, if partitioned, must have partition key equal to id.");
                        return Mono.error(new IllegalArgumentException("The lease collection must have partition key equal to id."));
                    }

                    RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();

                    String leasePrefix = self.getLeasePrefix();

                    return LeaseStoreManager.Builder()
                        .leasePrefix(leasePrefix)
                        .leaseCollectionLink(self.leaseContextClient.getContainerClient())
                        .leaseContextClient(self.leaseContextClient)
                        .requestOptionsFactory(requestOptionsFactory)
                        .hostName(self.hostName)
                        .build()
                        .map(manager -> {
                            self.leaseStoreManager = manager;
                            return self.leaseStoreManager;
                        });
                });
        }

        return Mono.just(this.leaseStoreManager);
    }

    private String getLeasePrefix() {
        String optionsPrefix = this.changeFeedProcessorOptions.leasePrefix();

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
        ChangeFeedProcessorBuilderImpl self = this;

        CheckpointerObserverFactory factory = new CheckpointerObserverFactory(this.observerFactory, new CheckpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            this.degreeOfParallelism,
            this.queryPartitionsMaxBatchSize
        );

        Bootstrapper bootstrapper = new BootstrapperImpl(synchronizer, leaseStoreManager, this.lockTime, this.sleepTime);
        PartitionSupervisorFactory partitionSupervisorFactory = new PartitionSupervisorFactoryImpl(
            factory,
            leaseStoreManager,
            this.partitionProcessorFactory != null ? this.partitionProcessorFactory : new PartitionProcessorFactoryImpl(
                this.feedContextClient,
                this.changeFeedProcessorOptions,
                leaseStoreManager,
                this.feedContextClient.getContainerClient()),
            this.changeFeedProcessorOptions,
            executorService
        );

        if (this.loadBalancingStrategy == null) {
            this.loadBalancingStrategy = new EqualPartitionsBalancingStrategy(
                this.hostName,
                this.changeFeedProcessorOptions.minScaleCount(),
                this.changeFeedProcessorOptions.maxScaleCount(),
                this.changeFeedProcessorOptions.leaseExpirationInterval());
        }

        PartitionController partitionController = new PartitionControllerImpl(leaseStoreManager, leaseStoreManager, partitionSupervisorFactory, synchronizer, executorService);

        if (this.healthMonitor == null) {
            this.healthMonitor = new TraceHealthMonitor();
        }

        PartitionController partitionController2 = new HealthMonitoringPartitionControllerDecorator(partitionController, this.healthMonitor);

        PartitionLoadBalancer partitionLoadBalancer = new PartitionLoadBalancerImpl(
            partitionController2,
            leaseStoreManager,
            this.loadBalancingStrategy,
            this.changeFeedProcessorOptions.leaseAcquireInterval(),
            this.executorService
        );

        PartitionManager partitionManager = new PartitionManagerImpl(bootstrapper, partitionController, partitionLoadBalancer);

        return Mono.just(partitionManager);
    }

    @Override
    public void close() {
        this.stop().subscribeOn(Schedulers.elastic()).subscribe();
    }
}
