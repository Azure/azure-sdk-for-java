// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.benchmark.Benchmark;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.Operation;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.benchmark.PojoizedJson;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncDatabase;
import com.azure.cosmos.encryption.CosmosEncryptionClientBuilder;
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
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.RandomStringUtils;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.io.InputStream;
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

public abstract class AsyncEncryptionBenchmark<T> implements Benchmark {
    private final MetricRegistry metricsRegistry;

    private volatile Meter successMeter;
    private volatile Meter failureMeter;
    private boolean databaseCreated;
    private boolean collectionCreated;

    private CosmosAsyncContainer cosmosAsyncContainer;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private Properties keyVaultProperties;

    final Logger logger;
    final CosmosAsyncClient cosmosClient;

    final String partitionKey;
    final TenantWorkloadConfig workloadConfig;
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

    AsyncEncryptionBenchmark(TenantWorkloadConfig workloadCfg, MetricRegistry sharedRegistry) throws IOException {

        workloadConfig = workloadCfg;

        final TokenCredential credential = workloadCfg.isManagedIdentityRequired()
            ? workloadCfg.buildTokenCredential()
            : null;

        CosmosClientBuilder cosmosClientBuilder = workloadCfg.isManagedIdentityRequired() ?
                new CosmosClientBuilder().credential(credential) :
                new CosmosClientBuilder().key(workloadCfg.getMasterKey());

        cosmosClientBuilder
                .preferredRegions(workloadCfg.getPreferredRegionsList())
                .endpoint(workloadCfg.getServiceEndpoint())
                .consistencyLevel(workloadCfg.getConsistencyLevel())
                .contentResponseOnWriteEnabled(workloadCfg.isContentResponseOnWriteEnabled());

        if (workloadCfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(workloadCfg.getMaxConnectionPoolSize());
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }
        cosmosClient = cosmosClientBuilder.buildAsyncClient();
        cosmosEncryptionAsyncClient = createEncryptionClientInstance(cosmosClient);
        metricsRegistry = sharedRegistry;
        logger = LoggerFactory.getLogger(this.getClass());
        createEncryptionDatabaseAndContainer();
        partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];
        concurrencyControlSemaphore = new Semaphore(workloadCfg.getConcurrency());
        ArrayList<Flux<PojoizedJson>> createDocumentObservables = new ArrayList<>();

        if (workloadConfig.getOperationType() != Operation.WriteLatency
            && workloadConfig.getOperationType() != Operation.WriteThroughput
            && workloadConfig.getOperationType() != Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", workloadCfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(workloadCfg.getDocumentDataFieldSize());
            for (int i = 0; i < workloadCfg.getNumberOfPreCreatedDocuments(); i++) {
                String uuid = UUID.randomUUID().toString();
                PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                    dataFieldValue,
                    partitionKey,
                    workloadConfig.getDocumentDataFieldCount());
                for (int j = 1; j <= workloadCfg.getEncryptedStringFieldCount(); j++) {
                    newDoc.setProperty(ENCRYPTED_STRING_FIELD + j, uuid);
                }
                for (int j = 1; j <= workloadCfg.getEncryptedLongFieldCount(); j++) {
                    newDoc.setProperty(ENCRYPTED_LONG_FIELD + j, 1234l);
                }
                for (int j = 1; j <= workloadCfg.getEncryptedDoubleFieldCount(); j++) {
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
        logger.info("Finished pre-populating {} documents", workloadCfg.getNumberOfPreCreatedDocuments());

        init();
    }

    protected void init() {
    }

    public void shutdown() {
        if (this.databaseCreated) {
            cosmosAsyncDatabase.delete().block();
            logger.info("Deleted temporary database {} created for this test", this.workloadConfig.getDatabaseId());
        } else if (this.collectionCreated) {
            cosmosAsyncContainer.delete().block();
            logger.info("Deleted temporary collection {} created for this test", this.workloadConfig.getContainerId());
        }

        cosmosClient.close();
    }

    protected void onSuccess() {
    }

    protected void initializeMetersIfSkippedEnoughOperations(AtomicLong count) {
        if (workloadConfig.getSkipWarmUpOperations() > 0) {
            if (count.get() >= workloadConfig.getSkipWarmUpOperations()) {
                if (warmupMode.get()) {
                    synchronized (this) {
                        if (warmupMode.get()) {
                            logger.info("Warmup phase finished. Starting capturing perf numbers ....");
                            resetMeters();
                            initializeMeter();
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
        if (latencyAwareOperations(workloadConfig.getOperationType())) {
            metricsRegistry.remove(LATENCY_METER_NAME);
        }
    }

    private void initializeMeter() {
        successMeter = metricsRegistry.meter(SUCCESS_COUNTER_METER_NAME);
        failureMeter = metricsRegistry.meter(FAILURE_COUNTER_METER_NAME);
        if (latencyAwareOperations(workloadConfig.getOperationType())) {
            latency = metricsRegistry.register(LATENCY_METER_NAME,
                new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        }
    }

    private boolean latencyAwareOperations(Operation operation) {
        switch (workloadConfig.getOperationType()) {
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
        if (workloadConfig.getSkipWarmUpOperations() > 0) {
            logger.info("Starting warm up phase. Executing {} operations to warm up ...",
                workloadConfig.getSkipWarmUpOperations());
            warmupMode.set(true);
        }

        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong(0);
        long i;

        for (i = 0; BenchmarkHelper.shouldContinue(startTime, i, workloadConfig); i++) {

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
            workloadConfig.getNumberOfOperations(), (int) ((endTime - startTime) / 1000));
    }

    protected Mono sparsityMono(long i) {
        Duration duration = workloadConfig.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            if (workloadConfig.getSkipWarmUpOperations() > i) {
                // don't wait on the initial warm up time.
                return null;
            }

            return Mono.delay(duration);
        } else return null;
    }

    // load config from resource src/resources/encryption_settings.properties
    private Properties loadConfig() throws IOException {
        try (InputStream input = AsyncEncryptionBenchmark.class.getClassLoader().getResourceAsStream("encryption_setting.properties");) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    private CosmosEncryptionAsyncClient createEncryptionClientInstance(CosmosAsyncClient cosmosClient) throws IOException {
        keyVaultProperties = loadConfig();
        // Application credentials for authentication with Azure Key Vault.
        // This application must have keys/wrapKey and keys/unwrapKey permissions
        // on the keys that will be used for encryption.
        TokenCredential tokenCredentials = getTokenCredential(keyVaultProperties);
        KeyEncryptionKeyClientBuilder keyEncryptionKeyClientBuilder = new KeyEncryptionKeyClientBuilder();
        keyEncryptionKeyClientBuilder.credential(tokenCredentials);
        return new CosmosEncryptionClientBuilder().cosmosAsyncClient(cosmosClient).keyEncryptionKeyResolver(keyEncryptionKeyClientBuilder).keyEncryptionKeyResolverName(CosmosEncryptionClientBuilder.KEY_RESOLVER_NAME_AZURE_KEY_VAULT).buildAsyncClient();
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
            cosmosAsyncDatabase = cosmosClient.getDatabase(this.workloadConfig.getDatabaseId());
            cosmosAsyncDatabase.read().block();
            FeedResponse<CosmosClientEncryptionKeyProperties> keyFeedResponse =
                cosmosAsyncDatabase.readAllClientEncryptionKeys().byPage().blockFirst();
            if (keyFeedResponse.getResults().size() < 1) {
                throw new IllegalArgumentException(String.format("database %s does not have any client encryption key" +
                    " %s" +
                    "key", this.workloadConfig.getDatabaseId(), dataEncryptionKeyId));
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
                        "key", this.workloadConfig.getDatabaseId(), dataEncryptionKeyId));
                }
            }
            cosmosEncryptionAsyncDatabase =
                cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(this.workloadConfig.getDatabaseId());
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(workloadConfig.getDatabaseId()).block();
                cosmosAsyncDatabase = cosmosClient.getDatabase(workloadConfig.getDatabaseId());
                cosmosEncryptionAsyncDatabase =
                    cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(this.workloadConfig.getDatabaseId());
                String masterKeyUrlFromConfig = getConfiguration("KeyVaultMasterKeyUrl", keyVaultProperties);
                if (StringUtils.isEmpty(masterKeyUrlFromConfig)) {
                    throw new IllegalArgumentException("Please specify a valid MasterKeyUrl in the appSettings.json");
                }

                EncryptionKeyWrapMetadata metadata =
                    new EncryptionKeyWrapMetadata(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolverName(), dataEncryptionKeyId,
                        masterKeyUrlFromConfig, EncryptionAlgorithm.RSA_OAEP.toString());
                /// Generates an encryption key, wraps it using the key wrap metadata provided
                /// and saves the wrapped encryption key as an asynchronous operation in the Azure Cosmos service.
                CosmosClientEncryptionKeyProperties keyProperties =
                    cosmosEncryptionAsyncDatabase.createClientEncryptionKey(
                        dataEncryptionKeyId,
                        CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata).block().getProperties();

                logger.info("Database {} is created for this test with client encryption key {}",
                    this.workloadConfig.getDatabaseId(), dataEncryptionKeyId);
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.workloadConfig.getContainerId());
        try {
            cosmosAsyncContainer.delete().block();
        } catch (CosmosException ex) {
            //expected if there is no collection to delete
        }

        List<ClientEncryptionIncludedPath> encryptionPaths = new ArrayList<>();
        for (int i = 1; i <= workloadConfig.getEncryptedStringFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_STRING_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.toString());
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
            encryptionPaths.add(includedPath);
        }
        for (int i = 1; i <= workloadConfig.getEncryptedDoubleFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_LONG_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.toString());
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
            encryptionPaths.add(includedPath);
        }
        for (int i = 1; i <= workloadConfig.getEncryptedLongFieldCount(); i++) {
            ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
            includedPath.setClientEncryptionKeyId(dataEncryptionKeyId);
            includedPath.setPath("/" + ENCRYPTED_DOUBLE_FIELD + i);
            includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.toString());
            includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());
            encryptionPaths.add(includedPath);
        }
        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(encryptionPaths);
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(this.workloadConfig.getContainerId(),
                TenantWorkloadConfig.DEFAULT_PARTITION_KEY_PATH);
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosAsyncDatabase.createContainer(containerProperties,
            ThroughputProperties.createManualThroughput(this.workloadConfig.getThroughput())
        ).block();

        cosmosAsyncContainer = cosmosAsyncDatabase.getContainer(this.workloadConfig.getContainerId());
        logger.info("Collection {} is created for this test with encryption paths",
            this.workloadConfig.getContainerId());
        collectionCreated = true;
        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(this.workloadConfig.getContainerId());
    }
}
