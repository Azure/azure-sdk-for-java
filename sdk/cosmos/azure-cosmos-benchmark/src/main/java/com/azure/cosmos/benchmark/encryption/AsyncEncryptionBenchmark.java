// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncDatabase;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.microsoft.data.encryption.AzureKeyVaultKeyStoreProvider.AzureKeyVaultKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AsyncEncryptionBenchmark<T> {
    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private ScheduledReporter reporter;

    private volatile Meter successMeter;
    private volatile Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    private CosmosAsyncContainer cosmosAsyncContainer;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private AzureKeyVaultKeyStoreProvider encryptionKeyStoreProvider = null;
    private Properties keyVaultProperties;

    final Logger logger;
    final CosmosAsyncClient cosmosClient;

    final String partitionKey;
    final Configuration configuration;
    final List<PojoizedJson> docsToRead;
    final Semaphore concurrencyControlSemaphore;
    Timer latency;

    private static final String SUCCESS_COUNTER_METER_NAME = "#Successful Operations";
    private static final String FAILURE_COUNTER_METER_NAME = "#Unsuccessful Operations";
    private static final String LATENCY_METER_NAME = "latency";
    private static final String dataEncryptionKeyId = "theDataEncryptionKey";
    static final String ENCRYPTED_STRING_FIELD = "encryptedStringField";
    static final String ENCRYPTED_LONG_FIELD = "encryptedLongField";
    static final String ENCRYPTED_DOUBLE_FIELD = "encryptedDoubleField";

    final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;


    private AtomicBoolean warmupMode = new AtomicBoolean(false);

    AsyncEncryptionBenchmark(Configuration cfg) throws IOException, MicrosoftDataEncryptionException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(cfg.getServiceEndpoint())
            .key(cfg.getMasterKey())
            .consistencyLevel(cfg.getConsistencyLevel())
            .contentResponseOnWriteEnabled(Boolean.parseBoolean(cfg.isContentResponseOnWriteEnabled()));
        if (cfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(cfg.getMaxConnectionPoolSize());
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }
        cosmosClient = cosmosClientBuilder.buildAsyncClient();
        cosmosEncryptionAsyncClient = createEncryptionClientInstance(cosmosClient);
        configuration = cfg;
        logger = LoggerFactory.getLogger(this.getClass());
        createEncryptionDatabaseAndContainer();
        partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];
        concurrencyControlSemaphore = new Semaphore(cfg.getConcurrency());
        ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

        if (configuration.getOperationType() != Configuration.Operation.WriteLatency
            && configuration.getOperationType() != Configuration.Operation.WriteThroughput
            && configuration.getOperationType() != Configuration.Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", cfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());
            for (int i = 0; i < cfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    configuration.getDocumentDataFieldCount());
                for (int j = 1; j <= cfg.getEncryptedStringFieldCount(); j++) {
                    newDoc.setProperty(ENCRYPTED_STRING_FIELD + j, uuid);
                }
                for (int j = 1; j <= cfg.getEncryptedLongFieldCount(); j++) {
                    newDoc.setProperty(ENCRYPTED_LONG_FIELD + j, 1234l);
                }
                for (int j = 1; j <= cfg.getEncryptedDoubleFieldCount(); j++) {
                    newDoc.setProperty(ENCRYPTED_DOUBLE_FIELD + j, 1234.01d);
                }

                Flux<PojoizedJson> obs = cosmosEncryptionAsyncContainer
                    .createItem(newDoc, new PartitionKey(uuid), new CosmosItemRequestOptions())
                    .retryWhen(Retry.max(5).filter((error) -> {
                        if (!(error instanceof CosmosException)) {
                            return false;
                        }
                        final CosmosException cosmosException = (CosmosException) error;
                        if (cosmosException.getStatusCode() == 410 ||
                            cosmosException.getStatusCode() == 408 ||
                            cosmosException.getStatusCode() == 429 ||
                            cosmosException.getStatusCode() == 503) {
                            return true;
                        }

                        return false;
                    }))
                    .onErrorResume(
                        (error) -> {
                            if (!(error instanceof CosmosException)) {
                                return false;
                            }
                            final CosmosException cosmosException = (CosmosException) error;
                            if (cosmosException.getStatusCode() == 409) {
                                return true;
                            }

                            return false;
                        },
                        (conflictException) -> cosmosAsyncContainer.readItem(
                            uuid, new PartitionKey(partitionKey), PojoizedJson.class)
                    )
                    .map(resp -> {
                        PojoizedJson x =
                            resp.getItem();
                        return x;
                    })
                    .flux();
                createDocumentObservables.add(obs);
            }
        }

        docsToRead = Flux.merge(Flux.fromIterable(createDocumentObservables), 100).collectList().block();
        logger.info("Finished pre-populating {} documents", cfg.getNumberOfPreCreatedDocuments());

        init();

        if (configuration.isEnableJvmStats()) {
            metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        if (configuration.getGraphiteEndpoint() != null) {
            final Graphite graphite = new Graphite(new InetSocketAddress(
                configuration.getGraphiteEndpoint(),
                configuration.getGraphiteEndpointPort()));
            reporter = GraphiteReporter.forRegistry(metricsRegistry)
                .prefixedWith(configuration.getOperationType().name())
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        } else if (configuration.getReportingDirectory() != null) {
            reporter = CsvReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(configuration.getReportingDirectory());
        } else {
            reporter = ConsoleReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
        }

        MeterRegistry registry = configuration.getAzureMonitorMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }

        registry = configuration.getGraphiteMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }
    }

    protected void init() {
    }

    public void shutdown() {
        if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", this.configuration.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosAsyncContainer.delete().block();
            logger.info("Deleted temporary collection {} created for this test", this.configuration.getCollectionId());
        }

        cosmosClient.close();
    }

    protected void onSuccess() {
    }

    protected void initializeMetersIfSkippedEnoughOperations(AtomicLong count) {
        if (configuration.getSkipWarmUpOperations() > 0) {
            if (count.get() >= configuration.getSkipWarmUpOperations()) {
                if (warmupMode.get()) {
                    synchronized (this) {
                        if (warmupMode.get()) {
                            logger.info("Warmup phase finished. Starting capturing perf numbers ....");
                            resetMeters();
                            initializeMeter();
                            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
                            warmupMode.set(false);
                        }
                    }
                }
            }
        }
    }

    protected void onError(Throwable throwable) {
    }

    protected abstract void performWorkload(BaseSubscriber<T> baseSubscriber, long i) throws Exception;

    private void resetMeters() {
        metricsRegistry.remove(SUCCESS_COUNTER_METER_NAME);
        metricsRegistry.remove(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            metricsRegistry.remove(LATENCY_METER_NAME);
        }
    }

    private void initializeMeter() {
        successMeter = metricsRegistry.meter(SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(configuration.getOperationType())) {
            latency = metricsRegistry.register(LATENCY_METER_NAME,
                new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        }
    }

    private boolean latencyAwareOperations(Configuration.Operation operation) {
        switch (configuration.getOperationType()) {
            case ReadLatency:
            case WriteLatency:
            case QueryInClauseParallel:
            case QueryCross:
            case QuerySingle:
            case QuerySingleMany:
            case QueryParallel:
            case QueryOrderby:
            case QueryTopOrderby:
            case Mixed:
                return true;
            default:
                return false;
        }
    }

    public void run() throws Exception {
        initializeMeter();
        if (configuration.getSkipWarmUpOperations() > 0) {
            logger.info("Starting warm up phase. Executing {} operations to warm up ...",
                configuration.getSkipWarmUpOperations());
            warmupMode.set(true);
        } else {
            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
        }

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for (i = 0; BenchmarkHelper.shouldContinue(startTime, i, configuration); i++) {

            BaseSubscriber<T> baseSubscriber = new BaseSubscriber<T>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    super.hookOnSubscribe(subscription);
                }

                @Override
                protected void hookOnNext(T value) {
                    logger.debug("hookOnNext: {}, count:{}", value, count.get());
                }

                @Override
                protected void hookOnCancel() {
                    this.hookOnError(new CancellationException());
                }

                @Override
                protected void hookOnComplete() {
                    initializeMetersIfSkippedEnoughOperations(count);
                    successMeter.mark();
                    concurrencyControlSemaphore.release();
                    AsyncEncryptionBenchmark.this.onSuccess();

                    synchronized (count) {
                        count.incrementAndGet();
                        count.notify();
                    }
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    initializeMetersIfSkippedEnoughOperations(count);
                    failureMeter.mark();

                    logger.error("Encountered failure {} on thread {}",
                        throwable.getMessage(), Thread.currentThread().getName(), throwable);
                    concurrencyControlSemaphore.release();
                    AsyncEncryptionBenchmark.this.onError(throwable);

                    synchronized (count) {
                        count.incrementAndGet();
                        count.notify();
                    }
                }
            };

            performWorkload(baseSubscriber, i);
        }

        synchronized (count) {
            while (count.get() < i) {
                count.wait();
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            configuration.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));

        reporter.report();
        reporter.close();
    }

    protected Mono sparsityMono(long i) {
        Duration duration = configuration.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            if (configuration.getSkipWarmUpOperations() > i) {
                // don't wait on the initial warm up time.
                return null;
            }

            return Mono.delay(duration);
        } else return null;
    }

    // load config from resource src/resources/encryption_settings.properties
    private static Properties loadConfig() throws IOException {
        try (InputStream input = AsyncEncryptionBenchmark.class.getClassLoader().getResourceAsStream("encryption_setting.properties");) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    private CosmosEncryptionAsyncClient createEncryptionClientInstance(CosmosAsyncClient cosmosClient) throws MicrosoftDataEncryptionException, IOException {
        keyVaultProperties = loadConfig();
        // Application credentials for authentication with Azure Key Vault.
        // This application must have keys/wrapKey and keys/unwrapKey permissions
        // on the keys that will be used for encryption.
        TokenCredential tokenCredentials = getTokenCredential(keyVaultProperties);
        encryptionKeyStoreProvider = new AzureKeyVaultKeyStoreProvider(tokenCredentials);
        return CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(cosmosClient, encryptionKeyStoreProvider);
    }

    private TokenCredential getTokenCredential(Properties properties) {
        String clientId = getConfiguration("ClientId", properties);
        if (StringUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("Please specify a valid ClientId in the appSettings.json");
        }

        // Get the Tenant ID
        String tenantId = getConfiguration("TenantId", properties);
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalArgumentException("Please specify a valid Tenant Id in the appSettings.json");
        }

        String clientSecret = getConfiguration("ClientSecret", properties);
        if (StringUtils.isEmpty(tenantId)) {
            throw new IllegalArgumentException("Please specify a valid ClientSecret in the appSettings.json");
        }

        ClientSecretCredential credentials = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        return credentials;
    }

    private String getConfiguration(String key, Properties properties) {
        return properties.getProperty(key);
    }

    private void createEncryptionDatabaseAndContainer() {
        try {
            cosmosAsyncDatabase = cosmosClient.getDatabase(this.configuration.getDatabaseId());
            cosmosAsyncDatabase.read().block();
            FeedResponse<CosmosClientEncryptionKeyProperties> keyFeedResponse =
                cosmosAsyncDatabase.readAllClientEncryptionKeys().byPage().blockFirst();
            if (keyFeedResponse.getResults().size() < 1) {
                throw new IllegalArgumentException(String.format("database %s does not have any client encryption key" +
                    " %s" +
                    "key", this.configuration.getDatabaseId(), dataEncryptionKeyId));
            } else {
                boolean containsDataEncryptionKeyId = false;
                for (CosmosClientEncryptionKeyProperties keyProperties : keyFeedResponse.getResults()) {
                    if (keyProperties.getId().equals(dataEncryptionKeyId)) {
                        containsDataEncryptionKeyId = true;
                        break;
                    }
                }
                if (!containsDataEncryptionKeyId) {
                    throw new IllegalArgumentException(String.format("database %s does not have any client encryption" +
                        " key %s" +
                        "key", this.configuration.getDatabaseId(), dataEncryptionKeyId));
                }
            }
            cosmosEncryptionAsyncDatabase =
                cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(this.configuration.getDatabaseId());
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(configuration.getDatabaseId()).block();
                cosmosAsyncDatabase = cosmosClient.getDatabase(configuration.getDatabaseId());
                cosmosEncryptionAsyncDatabase =
                    cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(this.configuration.getDatabaseId());
                String masterKeyUrlFromConfig = getConfiguration("KeyVaultMasterKeyUrl", keyVaultProperties);
                if (StringUtils.isEmpty(masterKeyUrlFromConfig)) {
                    throw new IllegalArgumentException("Please specify a valid MasterKeyUrl in the appSettings.json");
                }

                EncryptionKeyWrapMetadata metadata =
                    new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), dataEncryptionKeyId,
                        masterKeyUrlFromConfig);
                /// Generates an encryption key, wraps it using the key wrap metadata provided
                /// and saves the wrapped encryption key as an asynchronous operation in the Azure Cosmos service.
                CosmosClientEncryptionKeyProperties keyProperties =
                    cosmosEncryptionAsyncDatabase.createClientEncryptionKey(
                        dataEncryptionKeyId,
                        CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata).block().getProperties();

                logger.info("Database {} is created for this test with client encryption key {}",
                    this.configuration.getDatabaseId(), dataEncryptionKeyId);
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId());
        try {
            cosmosAsyncContainer.delete().block();
        } catch (CosmosException ex) {
            //expected if there is no collection to delete
        }

        List<ClientEncryptionIncludedPath> encryptionPaths = new ArrayList<>();
        for (int i = 1; i <= configuration.getEncryptedStringFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_STRING_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);
            encryptionPaths.add(includedPath);
        }
        for (int i = 1; i <= configuration.getEncryptedDoubleFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_LONG_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);
            encryptionPaths.add(includedPath);
        }
        for (int i = 1; i <= configuration.getEncryptedLongFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_DOUBLE_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);
            encryptionPaths.add(includedPath);
        }
        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(encryptionPaths);
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(this.configuration.getCollectionId(),
                Configuration.DEFAULT_PARTITION_KEY_PATH);
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosAsyncDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(this.configuration.getThroughput())
        ).block();

        cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.configuration.getCollectionId());
        logger.info("Collection {} is created for this test with encryption paths",
            this.configuration.getCollectionId());
        collectionCreated = true;
        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(this.configuration.getCollectionId());
    }
}
