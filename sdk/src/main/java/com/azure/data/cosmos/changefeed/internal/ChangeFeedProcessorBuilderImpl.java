/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.ChangeFeedObserver;
import com.azure.data.cosmos.ChangeFeedObserverFactory;
import com.azure.data.cosmos.ChangeFeedProcessor;
import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.changefeed.Bootstrapper;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.changefeed.HealthMonitor;
import com.azure.data.cosmos.changefeed.LeaseStoreManager;
import com.azure.data.cosmos.changefeed.PartitionController;
import com.azure.data.cosmos.changefeed.PartitionLoadBalancer;
import com.azure.data.cosmos.changefeed.PartitionLoadBalancingStrategy;
import com.azure.data.cosmos.changefeed.PartitionManager;
import com.azure.data.cosmos.changefeed.PartitionProcessor;
import com.azure.data.cosmos.changefeed.PartitionProcessorFactory;
import com.azure.data.cosmos.changefeed.PartitionSupervisorFactory;
import com.azure.data.cosmos.changefeed.RequestOptionsFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class to build {@link ChangeFeedProcessor} instances
 * as logical representation of the Azure Cosmos DB database service.
 *
 * <pre>
 * {@code
 *  ChangeFeedProcessor.Builder()
 *     .withHostName(hostName)
 *     .withFeedContainerClient(feedContainer)
 *     .withLeaseContainerClient(leaseContainer)
 *     .withChangeFeedObserver(SampleObserverImpl.class)
 *     .build();
 * }
 * </pre>
 */
public class ChangeFeedProcessorBuilderImpl implements ChangeFeedProcessor.BuilderDefinition, ChangeFeedProcessor, AutoCloseable {
    private static final long DefaultUnhealthinessDuration = Duration.ofMinutes(15).toMillis();
    private final Duration sleepTime = Duration.ofSeconds(15);
    private final Duration lockTime = Duration.ofSeconds(30);

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
    public ChangeFeedProcessorBuilderImpl withHostName(String hostName) {
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
    public ChangeFeedProcessorBuilderImpl withFeedContainerClient(CosmosContainer feedDocumentClient) {
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
    public ChangeFeedProcessorBuilderImpl withProcessorOptions(ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        if (changeFeedProcessorOptions == null) {
            throw new IllegalArgumentException("changeFeedProcessorOptions");
        }

        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.executorService = changeFeedProcessorOptions.executorService();

        return this;
    }

    /**
     * Sets the {@link ChangeFeedObserverFactory} to be used to generate {@link ChangeFeedObserver}
     *
     * @param observerFactory The instance of {@link ChangeFeedObserverFactory} to use.
     * @return current Builder.
     */
    @Override
    public ChangeFeedProcessorBuilderImpl withChangeFeedObserverFactory(ChangeFeedObserverFactory observerFactory) {
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
    @Override
    public ChangeFeedProcessorBuilderImpl withChangeFeedObserver(Class<? extends ChangeFeedObserver> type) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }

        this.observerFactory = new ChangeFeedObserverFactoryImpl(type);

        return this;
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
    public ChangeFeedProcessorBuilderImpl withLeaseContainerClient(CosmosContainer leaseDocumentClient) {
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
    public Mono<ChangeFeedProcessor> build() {
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

        this.initializeCollectionPropertiesForBuild().block();
        LeaseStoreManager leaseStoreManager = this.getLeaseStoreManager().block();
        this.partitionManager = this.buildPartitionManager(leaseStoreManager).block();

        return Mono.just(this);
    }

    public ChangeFeedProcessorBuilderImpl() {
    }

    public ChangeFeedProcessorBuilderImpl(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    private Mono<Void> initializeCollectionPropertiesForBuild() {
        ChangeFeedProcessorBuilderImpl self = this;

        if (this.changeFeedProcessorOptions == null) {
            this.changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        }

        if (this.databaseResourceId == null) {
            this.feedContextClient
                .readDatabase(this.feedContextClient.getDatabaseClient(), null)
                .map( databaseResourceResponse -> {
                    self.databaseResourceId = databaseResourceResponse.database().id();
                    return self.databaseResourceId;
                })
                .subscribeOn(Schedulers.elastic())
                .then()
                .block();
        }

        if (this.collectionResourceId == null) {
            self.feedContextClient
                .readContainer(self.feedContextClient.getContainerClient(), null)
                .map(documentCollectionResourceResponse -> {
                    self.collectionResourceId = documentCollectionResourceResponse.container().id();
                    return self.collectionResourceId;
                })
                .subscribeOn(Schedulers.elastic())
                .then()
                .block();
        }

        return Mono.empty();
    }

    private Mono<LeaseStoreManager> getLeaseStoreManager() {
        ChangeFeedProcessorBuilderImpl self = this;

        if (this.leaseStoreManager == null) {

            return this.leaseContextClient.readContainerSettings(this.leaseContextClient.getContainerClient(), null)
                .map( collectionSettings -> {
                    boolean isPartitioned =
                        collectionSettings.partitionKey() != null &&
                            collectionSettings.partitionKey().paths() != null &&
                            collectionSettings.partitionKey().paths().size() > 0;
                    if (!isPartitioned || (collectionSettings.partitionKey().paths().size() != 1 || !collectionSettings.partitionKey().paths().get(0).equals("/id"))) {
//                        throw new IllegalArgumentException("The lease collection, if partitioned, must have partition key equal to id.");
                        Mono.error(new IllegalArgumentException("The lease collection must have partition key equal to id."));
                    }

                    RequestOptionsFactory requestOptionsFactory = new PartitionedByIdCollectionRequestOptionsFactory();

                    String leasePrefix = self.getLeasePrefix();

                    self.leaseStoreManager = LeaseStoreManager.Builder()
                        .withLeasePrefix(leasePrefix)
                        .withLeaseContextClient(self.leaseContextClient)
                        .withRequestOptionsFactory(requestOptionsFactory)
                        .withHostName(self.hostName)
                        .build()
                        .block();

                    return self.leaseStoreManager;
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

        CheckpointerObserverFactory factory = new CheckpointerObserverFactory(this.observerFactory, this.changeFeedProcessorOptions.checkpointFrequency());

        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(
            this.feedContextClient,
            this.feedContextClient.getContainerClient(),
            leaseStoreManager,
            leaseStoreManager,
            this.changeFeedProcessorOptions.degreeOfParallelism(),
            this.changeFeedProcessorOptions.queryPartitionsMaxBatchSize()
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
                this.changeFeedProcessorOptions.minPartitionCount(),
                this.changeFeedProcessorOptions.maxPartitionCount(),
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
