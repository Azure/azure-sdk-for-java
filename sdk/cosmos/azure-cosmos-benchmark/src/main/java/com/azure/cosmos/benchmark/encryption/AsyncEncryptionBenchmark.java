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
import com.azure.cosmos.Http2ConnectionConfig;
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
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AsyncEncryptionBenchmark<T> implements Benchmark {

    // Dedicated scheduler for encryption benchmark workload dispatch.
    // Owned and disposed by the orchestrator (or test harness) that creates the benchmark.
    final Scheduler benchmarkScheduler;

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

    private static final String dataEncryptionKeyId = "theDataEncryptionKey";
    static final String ENCRYPTED_STRING_FIELD = "encryptedStringField";
    static final String ENCRYPTED_LONG_FIELD = "encryptedLongField";
    static final String ENCRYPTED_DOUBLE_FIELD = "encryptedDoubleField";

    final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;

    AsyncEncryptionBenchmark(TenantWorkloadConfig workloadCfg, Scheduler scheduler) throws IOException {

        workloadConfig = workloadCfg;
        this.benchmarkScheduler = scheduler;
        logger = LoggerFactory.getLogger(this.getClass());

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
            if (workloadCfg.isHttp2Enabled()) {
                Http2ConnectionConfig http2Config = gatewayConnectionConfig.getHttp2ConnectionConfig();
                http2Config.setEnabled(true);
                if (workloadCfg.getHttp2MaxConcurrentStreams() != null) {
                    http2Config.setMaxConcurrentStreams(workloadCfg.getHttp2MaxConcurrentStreams());
                }
            }
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }
        cosmosClient = cosmosClientBuilder.buildAsyncClient();
        cosmosEncryptionAsyncClient = createEncryptionClientInstance(cosmosClient);
        createEncryptionDatabaseAndContainer();
        partitionKey = cosmosAsyncContainer.read().block().getProperties().getPartitionKeyDefinition()
            .getPaths().iterator().next().split("/")[1];
        if (workloadConfig.getOperationType() != Operation.WriteThroughput
            && workloadConfig.getOperationType() != Operation.ReadMyWrites) {
            logger.info("PRE-populating {} documents ....", workloadCfg.getNumberOfPreCreatedDocuments());
            String dataFieldValue = RandomStringUtils.randomAlphabetic(workloadCfg.getDocumentDataFieldSize());
            List<PojoizedJson> generatedDocs = new ArrayList<>();

            Flux<CosmosItemOperation> bulkOperationFlux = Flux.range(0, workloadCfg.getNumberOfPreCreatedDocuments())
                .map(i -> {
                    String uuid = UUID.randomUUID().toString();
                    PojoizedJson newDoc = BenchmarkHelper.generateDocument(uuid,
                        dataFieldValue,
                        partitionKey,
                        workloadConfig.getDocumentDataFieldCount());
                    for (int j = 1; j <= workloadCfg.getEncryptedStringFieldCount(); j++) {
                        newDoc.setProperty(ENCRYPTED_STRING_FIELD + j, uuid);
                    }
                    for (int j = 1; j <= workloadCfg.getEncryptedLongFieldCount(); j++) {
                        newDoc.setProperty(ENCRYPTED_LONG_FIELD + j, 1234L);
                    }
                    for (int j = 1; j <= workloadCfg.getEncryptedDoubleFieldCount(); j++) {
                        newDoc.setProperty(ENCRYPTED_DOUBLE_FIELD + j, 1234.01d);
                    }
                    generatedDocs.add(newDoc);
                    return CosmosBulkOperations.getCreateItemOperation(newDoc, new PartitionKey(uuid));
                });

            CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
            List<CosmosBulkOperationResponse<Object>> failedResponses = Collections.synchronizedList(new ArrayList<>());
            cosmosEncryptionAsyncContainer
                .executeBulkOperations(bulkOperationFlux, bulkExecutionOptions)
                .doOnNext(response -> {
                    if (response.getResponse() == null || !response.getResponse().isSuccessStatusCode()) {
                        failedResponses.add(response);
                    }
                })
                .blockLast(Duration.ofMinutes(10));

            BenchmarkHelper.retryFailedBulkOperations(failedResponses,
                (item, pk) -> cosmosEncryptionAsyncContainer.createItem(item, pk, null).then());

            docsToRead = generatedDocs;
        } else {
            docsToRead = new ArrayList<>();
        }
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

    protected void onError(Throwable throwable) {
    }

    protected abstract Mono<T> performWorkload(long i);

    @SuppressWarnings("unchecked")
    public void run() throws Exception {

        long startTime = System.currentTimeMillis();
        int concurrency = workloadConfig.getConcurrency();

        Flux<Long> source;
        Duration maxDuration = workloadConfig.getMaxRunningTimeDuration();
        if (maxDuration != null) {
            final long deadline = startTime + maxDuration.toMillis();
            source = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    if (System.currentTimeMillis() < deadline) {
                        sink.next(state.getAndIncrement());
                    } else {
                        sink.complete();
                    }
                    return state;
                });
        } else {
            // Count-based termination using Flux.generate to avoid long-to-int truncation
            long numberOfOps = workloadConfig.getNumberOfOperations();
            source = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long current = state.getAndIncrement();
                    if (current < numberOfOps) {
                        sink.next(current);
                    } else {
                        sink.complete();
                    }
                    return state;
                });
        }

        AtomicLong completedCount = new AtomicLong(0);

        source
            .flatMap(i -> {
                Mono<T> workload = performWorkload(i);
                Mono<T> delayed = sparsityMono(i);
                if (delayed != null) {
                    workload = delayed.then(workload);
                }
                return workload
                    .subscribeOn(benchmarkScheduler)
                    .doOnSuccess(v -> {
                        completedCount.incrementAndGet();
                        AsyncEncryptionBenchmark.this.onSuccess();
                    })
                    .doOnError(e -> {
                        completedCount.incrementAndGet();
                        logger.error("Encountered failure {} on thread {}",
                            e.getMessage(), Thread.currentThread().getName(), e);
                        AsyncEncryptionBenchmark.this.onError(e);
                    })
                    .onErrorResume(e -> Mono.empty());
            }, concurrency)
            .blockLast();

        long endTime = System.currentTimeMillis();
        logger.info("[{}] operations performed in [{}] seconds.",
            completedCount.get(), (int) ((endTime - startTime) / 1000));
    }

    protected Mono sparsityMono(long i) {
        Duration duration = workloadConfig.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
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
