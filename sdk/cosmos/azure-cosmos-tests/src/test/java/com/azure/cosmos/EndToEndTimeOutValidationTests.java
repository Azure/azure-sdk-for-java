// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationCancelledException;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import com.azure.cosmos.util.CosmosPagedFlux;
import net.bytebuddy.implementation.bytecode.Throw;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EndToEndTimeOutValidationTests extends TestSuiteBase {
    private static final int DEFAULT_NUM_DOCUMENTS = 100;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private CosmosAsyncContainer createdContainer;
    private final Random random;
    private final List<TestObject> createdDocuments = new ArrayList<>();
    private final CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public EndToEndTimeOutValidationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
        endToEndOperationLatencyPolicyConfig = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
            .build();
    }

    public CosmosAsyncClient initializeClient(CosmosEndToEndOperationLatencyPolicyConfig e2eDefaultConfig) {
        CosmosAsyncClient client = this
            .getClientBuilder()
            .endToEndOperationLatencyPolicyConfig(e2eDefaultConfig)
            .buildAsyncClient();

        try {
            createdContainer = getSharedMultiPartitionCosmosContainer(client);
            truncateCollection(createdContainer);

            createdDocuments.addAll(this.insertDocuments(DEFAULT_NUM_DOCUMENTS, null, createdContainer));

            return client;
        } catch (Throwable t) {
            safeClose(client);

            throw t;
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItemWithEndToEndTimeoutPolicyInOptionsShouldTimeoutWithClientConfig() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule rule = null;
        try {

            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            TestObject itemToRead = createdDocuments.get(random.nextInt(createdDocuments.size()));
            rule = injectFailure(createdContainer, FaultInjectionOperationType.READ_ITEM, null);

            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                createdContainer.readItem(itemToRead.id, new PartitionKey(itemToRead.mypk), options, TestObject.class);

            verifyExpectError(cosmosItemResponseMono);
        } finally {
            if (rule != null) {
                rule.disable();
            }
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItemWithEndToEndTimeoutPolicyInOptionsShouldTimeoutEvenWhenDisabledForNonPointOperations() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        System.setProperty(
            Configs.DEFAULT_E2E_FOR_NON_POINT_DISABLED,
            "true");

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);

        FaultInjectionRule rule = null;
        try {

            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            TestObject itemToRead = createdDocuments.get(random.nextInt(createdDocuments.size()));
            rule = injectFailure(createdContainer, FaultInjectionOperationType.READ_ITEM, null);

            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                createdContainer.readItem(itemToRead.id, new PartitionKey(itemToRead.mypk), options, TestObject.class);

            verifyExpectError(cosmosItemResponseMono);
        } finally {
            if (rule != null) {
                rule.disable();
            }

            System.clearProperty(Configs.DEFAULT_E2E_FOR_NON_POINT_DISABLED);
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void createItemWithEndToEndTimeoutPolicyInOptionsShouldTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule faultInjectionRule = null;
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);

            faultInjectionRule = injectFailure(createdContainer, FaultInjectionOperationType.CREATE_ITEM, null);
            TestObject inputObject = new TestObject(UUID.randomUUID().toString(), "name123", 1, UUID.randomUUID().toString());
            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                createdContainer.createItem(inputObject, new PartitionKey(inputObject.mypk), options);

            verifyExpectError(cosmosItemResponseMono);
        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void replaceItemWithEndToEndTimeoutPolicyInOptionsShouldTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule rule = null;
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);

            TestObject inputObject = new TestObject(UUID.randomUUID().toString(), "name123", 1, UUID.randomUUID().toString());
            createdContainer.createItem(inputObject, new PartitionKey(inputObject.mypk), options).block();
            rule = injectFailure(createdContainer, FaultInjectionOperationType.REPLACE_ITEM, null);
            inputObject.setName("replaceName");
            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                createdContainer.replaceItem(inputObject, inputObject.id, new PartitionKey(inputObject.mypk), options);

            verifyExpectError(cosmosItemResponseMono);
        } finally {
            if (rule != null) {
                rule.disable();
            }
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void upsertItemWithEndToEndTimeoutPolicyInOptionsShouldTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule rule = null;
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);

            rule = injectFailure(createdContainer, FaultInjectionOperationType.UPSERT_ITEM, null);
            TestObject inputObject = new TestObject(UUID.randomUUID().toString(), "name123", 1, UUID.randomUUID().toString());
            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                createdContainer.upsertItem(inputObject, new PartitionKey(inputObject.mypk), options);

            verifyExpectError(cosmosItemResponseMono);
        } finally {
            if (rule != null) {
                rule.disable();
            }

            safeClose(cosmosClient);
        }
    }

    static void verifyExpectError(Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono) {
        StepVerifier.create(cosmosItemResponseMono)
            .expectErrorMatches(throwable -> throwable instanceof OperationCancelledException)
            .verify();
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void queryItemWithEndToEndTimeoutPolicyInOptionsShouldTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule faultInjectionRule = null;
        try {
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
                    .build();

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);

            createdDocuments.get(random.nextInt(createdDocuments.size()));

            String queryText = "select top 1 * from c";
            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText);

            faultInjectionRule = injectFailure(createdContainer, FaultInjectionOperationType.QUERY_ITEM, null);
            CosmosPagedFlux<TestObject> queryPagedFlux = createdContainer.queryItems(sqlQuerySpec, options, TestObject.class);

            StepVerifier.create(queryPagedFlux)
                        .expectErrorMatches(throwable -> throwable instanceof OperationCancelledException
                            && ((OperationCancelledException) throwable).getSubStatusCode()
                            == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT)
                        .verify();
        } finally {
            if (faultInjectionRule !=  null) {
                faultInjectionRule.disable();
            }

            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void queryItemWithEndToEndTimeoutPolicyInOptionsShouldTimeoutWithClientConfig() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
                .build();

        CosmosAsyncClient cosmosClient = initializeClient(endToEndOperationLatencyPolicyConfig);
        FaultInjectionRule faultInjectionRule = null;
        try {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            createdDocuments.get(random.nextInt(createdDocuments.size()));

            String queryText = "select top 1 * from c";
            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText);

            faultInjectionRule = injectFailure(createdContainer, FaultInjectionOperationType.QUERY_ITEM, null);
            CosmosPagedFlux<TestObject> queryPagedFlux = createdContainer.queryItems(sqlQuerySpec, options, TestObject.class);

            StepVerifier.create(queryPagedFlux)
                        .expectErrorMatches(throwable -> throwable instanceof OperationCancelledException
                            && ((OperationCancelledException) throwable).getSubStatusCode()
                            == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT)
                        .verify();
        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void queryItemWithEndToEndTimeoutPolicyInOptionsShouldNotTimeoutWhenSuppressed() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        System.setProperty(
            Configs.DEFAULT_E2E_FOR_NON_POINT_DISABLED,
            "true");

        logger.info(
            "isDefaultE2ETimeoutDisabledForNonPointOperations() after setting system property {}",
            Configs.isDefaultE2ETimeoutDisabledForNonPointOperations());

        CosmosAsyncClient cosmosClient = initializeClient(
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
                .build()
        );

        FaultInjectionRule faultInjectionRule = null;
        try {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            createdDocuments.get(random.nextInt(createdDocuments.size()));

            String queryText = "select top 1 * from c";
            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText);

            faultInjectionRule = injectFailure(createdContainer, FaultInjectionOperationType.QUERY_ITEM, null);
            CosmosPagedFlux<TestObject> queryPagedFlux = createdContainer.queryItems(sqlQuerySpec, options, TestObject.class);

            StepVerifier.create(queryPagedFlux)
                .expectNextCount(1L)
                .verifyComplete();
        } finally {
            if (faultInjectionRule != null) {
                faultInjectionRule.disable();
            }

            System.clearProperty(Configs.DEFAULT_E2E_FOR_NON_POINT_DISABLED);
            safeClose(cosmosClient);
        }
    }

    @Test(groups = {"fast"}, timeOut = 10000L, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void clientLevelEndToEndTimeoutPolicyInOptionsShouldTimeout() {
        if (getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure injection only supported for DIRECT mode");
        }
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .endToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig)
            .credential(credential);

        try (CosmosAsyncClient cosmosAsyncClient = builder.buildAsyncClient()) {
            String dbname = "db_" + UUID.randomUUID();
            String containerName = "container_" + UUID.randomUUID();
            CosmosContainerProperties properties = new CosmosContainerProperties(containerName, "/mypk");
            cosmosAsyncClient.createDatabaseIfNotExists(dbname).block();
            cosmosAsyncClient.getDatabase(dbname)
                .createContainerIfNotExists(properties).block();
            CosmosAsyncContainer container = cosmosAsyncClient.getDatabase(dbname)
                .getContainer(containerName);

            TestObject obj = new TestObject(UUID.randomUUID().toString(),
                "name123",
                2,
                UUID.randomUUID().toString());
            container.createItem(obj).block();

            Mono<CosmosItemResponse<TestObject>> cosmosItemResponseMono =
                container.readItem(obj.id, new PartitionKey(obj.mypk), TestObject.class);

            // Should read item properly before injecting failure
            StepVerifier.create(cosmosItemResponseMono)
                .expectNextCount(1)
                .expectComplete()
                .verify();

            injectFailure(container, FaultInjectionOperationType.READ_ITEM, null);

            // Should timeout after injected delay
            verifyExpectError(cosmosItemResponseMono);

            String queryText = "select top 1 * from c";
            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(queryText);

            CosmosPagedFlux<TestObject> queryPagedFlux = container.queryItems(sqlQuerySpec, TestObject.class);

            // Should query item properly before injecting failure
            StepVerifier.create(queryPagedFlux)
                .expectNextCount(1)
                .expectComplete()
                .verify();

            FaultInjectionRule faultInjectionRule = injectFailure(container, FaultInjectionOperationType.QUERY_ITEM, null);

            // Should timeout after injected delay
            StepVerifier.create(queryPagedFlux)
                .expectErrorMatches(throwable -> throwable instanceof OperationCancelledException)
                .verify();

            // Enabling at client level and disabling at the read item operation level should not fail the request even
            // with injected delay
            CosmosItemRequestOptions options = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
                        .enable(false)
                        .build());
            cosmosItemResponseMono =
                container.readItem(obj.id, new PartitionKey(obj.mypk), options, TestObject.class);
            StepVerifier.create(cosmosItemResponseMono)
                .expectNextCount(1)
                .expectComplete()
                .verify();

            // Enabling at client level and disabling at the query item operation level should not fail the request even
            // with injected delay
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(1))
                        .enable(false)
                        .build());
            queryPagedFlux = container.queryItems(sqlQuerySpec, queryRequestOptions, TestObject.class);
            StepVerifier.create(queryPagedFlux)
                .expectNextCount(1)
                .expectComplete()
                .verify();

            faultInjectionRule.disable();
            // delete the database
            cosmosAsyncClient.getDatabase(dbname).delete().block();
        }

    }


    private FaultInjectionRule injectFailure(
        CosmosAsyncContainer container,
        FaultInjectionOperationType operationType,
        Boolean suppressServiceRequests) {

        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(1500))
            .times(1);

        if (suppressServiceRequests != null) {
            faultInjectionResultBuilder.suppressServiceRequests(suppressServiceRequests);
        }

        IFaultInjectionResult result = faultInjectionResultBuilder.build();

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("InjectedResponseDelay")
            .condition(condition)
            .result(result)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) container
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(container));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        return rule;
    }

    private TestObject getDocumentDefinition(String documentId, String partitionKey) {
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetetions for int value.
        int randInt = random.nextInt(DEFAULT_NUM_DOCUMENTS / 2);

        TestObject doc = new TestObject(documentId, "name" + randInt, randInt, partitionKey);
        return doc;
    }

    private List<TestObject> insertDocuments(int documentCount, List<String> partitionKeys, CosmosAsyncContainer container) {
        List<TestObject> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            documentsToInsert.add(
                getDocumentDefinition(
                    UUID.randomUUID().toString(),
                    partitionKeys == null ? UUID.randomUUID().toString() : partitionKeys.get(random.nextInt(partitionKeys.size()))));
        }

        List<TestObject> documentInserted = bulkInsertBlocking(container, documentsToInsert);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        return documentInserted;
    }

    static class TestObject {
        String id;
        String name;
        int prop;
        String mypk;
        String constantProp = "constantProp";

        public TestObject() {
        }

        public TestObject(String id, String name, int prop, String mypk) {
            this.id = id;
            this.name = name;
            this.prop = prop;
            this.mypk = mypk;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getProp() {
            return prop;
        }

        public void setProp(final int prop) {
            this.prop = prop;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getConstantProp() {
            return constantProp;
        }
    }
}
