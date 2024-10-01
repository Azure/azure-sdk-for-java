/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class OperationPoliciesTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private static final Properties prop = new Properties();
    private static final String E2E_TIMEOUT = "timeout.seconds";
    private static final String CONSISTENCY_LEVEL = "consistency.level";
    private static final String CONTENT_RESPONSE_ON_WRITE = "contentResponseOnWriteEnabled";
    private static final String NON_IDEMPOTENT_WRITE_RETRIES = "nonIdempotentWriteRetriesEnabled";
    private static final String BYPASS_CACHE = "dedicatedGatewayOptions.bypassCache";
    private static final String THROUGHPUT_CONTROL_GROUP_NAME = "throughputControlGroupName";
    private static final String REQUEST_CHARGE_THRESHOLD = "diagnosticThresholds.requestChargeThreshold";
    private static final String SCAN_IN_QUERY = "scanInQueryEnabled";
    private static final String EXCLUDE_REGIONS = "excludeRegions";
    private static final String MAX_DEGREE_OF_PARALLELISM = "maxDegreeOfParallelism";
    private static final String MAX_BUFFERED_ITEM_COUNT = "maxBufferedItemCount";
    private static final String RESPONSE_CONTINUATION_TOKEN_LIMIT_KB = "responseContinuationTokenLimitKb";
    private static final String MAX_ITEM_COUNT = "maxItemCount";
    private static final String QUERY_METRICS = "queryMetricsEnabled";
    private static final String INDEX_METRICS = "indexMetricsEnabled";
    private static final String MAX_PREFETCH_PAGE_COUNT = "maxPrefetchPageCount";
    private static final String QUERY_NAME = "queryName";
    private static final String KEYWORD_IDENTIFIERS = "keywordIdentifiers";
    private static final String[] optionLabels = {E2E_TIMEOUT, CONSISTENCY_LEVEL, CONTENT_RESPONSE_ON_WRITE, NON_IDEMPOTENT_WRITE_RETRIES, BYPASS_CACHE, THROUGHPUT_CONTROL_GROUP_NAME, REQUEST_CHARGE_THRESHOLD, SCAN_IN_QUERY, EXCLUDE_REGIONS, MAX_DEGREE_OF_PARALLELISM, MAX_BUFFERED_ITEM_COUNT, RESPONSE_CONTINUATION_TOKEN_LIMIT_KB, MAX_ITEM_COUNT, QUERY_METRICS, INDEX_METRICS, MAX_PREFETCH_PAGE_COUNT, QUERY_NAME, KEYWORD_IDENTIFIERS};
    private static final String[] initialOptions = {"20", "Session", "true", "false", "false", "default", "2000", "false", "East US 2", "2", "100", "200", "30", "false", "false", "10", "QueryName", "59409493805"};

    @Factory(dataProvider = "clientBuildersWithApplyPolicies")
    public OperationPoliciesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        for (int i = 0; i < optionLabels.length; i++) {
            prop.setProperty(optionLabels[i], initialOptions[i]);
        }
    }

    private static void createReadDeleteBatchEtcOptions(String operationType, String spanName, CosmosRequestOptions cosmosRequestOptions) {
        if (operationType.equals("Create") || operationType.equals("Read") || operationType.equals("Replace")
            || operationType.equals("Delete") || operationType.equals("Patch") || operationType.equals("Upsert")
            || (operationType.equals("Batch") && spanName.contains("transactionalBatch"))) {

            cosmosRequestOptions.setCosmosEndToEndLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfig(true,
                    Duration.ofSeconds(Long.parseLong(prop.getProperty(E2E_TIMEOUT))),
                    new ThresholdBasedAvailabilityStrategy()))
                .setDiagnosticsThresholds(new CosmosDiagnosticsThresholds().setRequestChargeThreshold(Float.parseFloat(prop.getProperty(REQUEST_CHARGE_THRESHOLD))))
                .setConsistencyLevel(ConsistencyLevel.fromServiceSerializedFormat(prop.getProperty(CONSISTENCY_LEVEL)))
                .setContentResponseOnWriteEnabled(Boolean.parseBoolean(prop.getProperty(CONTENT_RESPONSE_ON_WRITE)))
                .setNonIdempotentWriteRetriesEnabled(Boolean.parseBoolean(prop.getProperty(NON_IDEMPOTENT_WRITE_RETRIES)))
                .setDedicatedGatewayRequestOptions(new DedicatedGatewayRequestOptions()
                    .setIntegratedCacheBypassed(Boolean.parseBoolean(prop.getProperty(BYPASS_CACHE))))
                .setThroughputControlGroupName(prop.getProperty(THROUGHPUT_CONTROL_GROUP_NAME))
                .setExcludeRegions(new ArrayList<>(Arrays.asList(prop.getProperty(EXCLUDE_REGIONS).split(","))))
                .setKeywordIdentifiers(new HashSet<>(Arrays.asList(prop.getProperty(KEYWORD_IDENTIFIERS).split(","))));
        }
    }

    private static void createQueryReadAllItemsOptions(String operationType, String spanName, CosmosRequestOptions cosmosRequestOptions) {
        if (operationType.equals("Query") || spanName.contains("readAllItems")) {

            cosmosRequestOptions.setCosmosEndToEndLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfig(true,
                    Duration.ofSeconds(Long.parseLong(prop.getProperty(E2E_TIMEOUT))),
                    new ThresholdBasedAvailabilityStrategy()))
                .setDiagnosticsThresholds(new CosmosDiagnosticsThresholds().setRequestChargeThreshold(Float.parseFloat(prop.getProperty(REQUEST_CHARGE_THRESHOLD))))
                .setThroughputControlGroupName(prop.getProperty(THROUGHPUT_CONTROL_GROUP_NAME))
                .setDedicatedGatewayRequestOptions(new DedicatedGatewayRequestOptions()
                    .setIntegratedCacheBypassed(Boolean.parseBoolean(prop.getProperty(BYPASS_CACHE))))
                .setScanInQueryEnabled(Boolean.parseBoolean(prop.getProperty(SCAN_IN_QUERY)))
                .setExcludeRegions(new ArrayList<>(Arrays.asList(prop.getProperty(EXCLUDE_REGIONS).split(","))))
                .setMaxDegreeOfParallelism(Integer.parseInt(prop.getProperty(MAX_DEGREE_OF_PARALLELISM)))
                .setMaxBufferedItemCount(Integer.parseInt(prop.getProperty(MAX_BUFFERED_ITEM_COUNT)))
                .setResponseContinuationTokenLimitInKb(Integer.parseInt(prop.getProperty(RESPONSE_CONTINUATION_TOKEN_LIMIT_KB)))
                .setMaxItemCount(Integer.parseInt(prop.getProperty(MAX_ITEM_COUNT)))
                .setQueryMetricsEnabled(Boolean.parseBoolean(prop.getProperty(QUERY_METRICS)))
                .setIndexMetricsEnabled(Boolean.parseBoolean(prop.getProperty(INDEX_METRICS)))
                .setMaxPrefetchPageCount(Integer.parseInt(prop.getProperty(MAX_PREFETCH_PAGE_COUNT)))
                .setQueryName(prop.getProperty(QUERY_NAME))
                .setConsistencyLevel(ConsistencyLevel.fromServiceSerializedFormat(prop.getProperty(CONSISTENCY_LEVEL)))
                .setKeywordIdentifiers(new HashSet<>(Arrays.asList(prop.getProperty(KEYWORD_IDENTIFIERS).split(","))));
        }
    }

    private static void createReadManyOptions(String spanName, CosmosRequestOptions cosmosRequestOptions) {
        if (spanName.contains("readMany")) {
                cosmosRequestOptions.setCosmosEndToEndLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfig(true,
                        Duration.ofSeconds(Long.parseLong(prop.getProperty(E2E_TIMEOUT))),
                        new ThresholdBasedAvailabilityStrategy()))
                    .setDiagnosticsThresholds(new CosmosDiagnosticsThresholds().setRequestChargeThreshold(Float.parseFloat(prop.getProperty(REQUEST_CHARGE_THRESHOLD))))
                    .setThroughputControlGroupName(prop.getProperty(THROUGHPUT_CONTROL_GROUP_NAME))
                    .setDedicatedGatewayRequestOptions(new DedicatedGatewayRequestOptions()
                        .setIntegratedCacheBypassed(Boolean.parseBoolean(prop.getProperty(BYPASS_CACHE))))
                    .setExcludeRegions(new ArrayList<>(Arrays.asList(prop.getProperty(EXCLUDE_REGIONS).split(","))))
                    .setResponseContinuationTokenLimitInKb(Integer.parseInt(prop.getProperty(RESPONSE_CONTINUATION_TOKEN_LIMIT_KB)))
                    .setQueryMetricsEnabled(Boolean.parseBoolean(prop.getProperty(QUERY_METRICS)))
                    .setIndexMetricsEnabled(Boolean.parseBoolean(prop.getProperty(INDEX_METRICS)))
                    .setKeywordIdentifiers(new HashSet<>(Arrays.asList(prop.getProperty(KEYWORD_IDENTIFIERS).split(","))))
                    .setConsistencyLevel(ConsistencyLevel.fromServiceSerializedFormat(prop.getProperty(CONSISTENCY_LEVEL)));
        }
    }

    private static void createBulkOptions(String operationType, String spanName, CosmosRequestOptions cosmosRequestOptions) {
        if (operationType.equals("Batch") && spanName.contains("nonTransactionalBatch")) {
                cosmosRequestOptions.setExcludeRegions((new ArrayList<>(Arrays.asList(prop.getProperty(EXCLUDE_REGIONS).split(",")))))
                    .setThroughputControlGroupName(prop.getProperty(THROUGHPUT_CONTROL_GROUP_NAME))
                    .setKeywordIdentifiers(new HashSet<>(Arrays.asList(prop.getProperty(KEYWORD_IDENTIFIERS).split(","))));
        }
    }

    private static void createChangeFeedOptions(String spanName, CosmosRequestOptions cosmosRequestOptions) {
        if (spanName.contains("queryChangeFeed")) {
                cosmosRequestOptions.setExcludeRegions((new ArrayList<>(Arrays.asList(prop.getProperty(EXCLUDE_REGIONS).split(",")))))
                    .setThroughputControlGroupName(prop.getProperty(THROUGHPUT_CONTROL_GROUP_NAME))
                    .setDiagnosticsThresholds(new CosmosDiagnosticsThresholds().setRequestChargeThreshold(Float.parseFloat(prop.getProperty(REQUEST_CHARGE_THRESHOLD))))
                    .setMaxPrefetchPageCount(Integer.parseInt(prop.getProperty(MAX_PREFETCH_PAGE_COUNT)))
                    .setMaxItemCount(Integer.parseInt(prop.getProperty(MAX_ITEM_COUNT)))
                    .setKeywordIdentifiers(new HashSet<>(Arrays.asList(prop.getProperty(KEYWORD_IDENTIFIERS).split(","))));
        }
    }

    @DataProvider
    public static Object[] clientBuildersWithApplyPolicies() {
        CosmosOperationPolicy policy = (cosmosOperationDetails) -> {
            // Figure out Operation
            CosmosDiagnosticsContext cosmosDiagnosticsContext = cosmosOperationDetails.getDiagnosticsContext();
            String operationType = cosmosDiagnosticsContext.getOperationType();
            String spanName = cosmosDiagnosticsContext.getSpanName();
            CosmosRequestOptions cosmosRequestOptions = new CosmosRequestOptions();
            createReadDeleteBatchEtcOptions(operationType, spanName, cosmosRequestOptions);
            createQueryReadAllItemsOptions(operationType, spanName, cosmosRequestOptions);
            createReadManyOptions(spanName, cosmosRequestOptions);
            createBulkOptions(operationType, spanName, cosmosRequestOptions);
            createChangeFeedOptions(spanName, cosmosRequestOptions);
            cosmosOperationDetails.setRequestOptions(cosmosRequestOptions);
        };

        CosmosClientBuilder[] clientBuilders = new CosmosClientBuilder[3];
        clientBuilders[0] =  new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .credential(credential)
            .gatewayMode()
            .addOperationPolicy(policy);
        clientBuilders[1] =  new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .credential(credential)
            .addOperationPolicy(policy);
        clientBuilders[2] = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
            .credential(credential)
            .addOperationPolicy((cosmosOperationDetails) -> {
                CosmosDiagnosticsContext cosmosDiagnosticsContext = cosmosOperationDetails.getDiagnosticsContext();
                String operationType = cosmosDiagnosticsContext.getOperationType();
                String spanName = cosmosDiagnosticsContext.getSpanName();
                CosmosRequestOptions cosmosRequestOptions = new CosmosRequestOptions();
                createReadDeleteBatchEtcOptions(operationType, spanName, cosmosRequestOptions);
                createQueryReadAllItemsOptions(operationType, spanName, cosmosRequestOptions);
                createReadManyOptions(spanName, cosmosRequestOptions);
                cosmosOperationDetails.setRequestOptions(cosmosRequestOptions);
            }).addOperationPolicy((cosmosOperationDetails) -> {
                CosmosDiagnosticsContext cosmosDiagnosticsContext = cosmosOperationDetails.getDiagnosticsContext();
                String operationType = cosmosDiagnosticsContext.getOperationType();
                String spanName = cosmosDiagnosticsContext.getSpanName();
                CosmosRequestOptions cosmosRequestOptions = new CosmosRequestOptions();
                createBulkOptions(operationType, spanName, cosmosRequestOptions);
                createChangeFeedOptions(spanName, cosmosRequestOptions);
                cosmosOperationDetails.setRequestOptions(cosmosRequestOptions);
            });
        return clientBuilders;
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_OperationPoliciesTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        changeProperties(initialOptions);
    }

    @DataProvider(name = "changedOptions")
    private String[][] createChangedOptions() {
        return new String[][] {
            { "8", "ConsistentPrefix", "true", "false", "true", "defaultChanged", "1000", "true", "West US 2", "4", "200", "400", "100", "false", "true", "20", "QueryNameChanged", "112" },
            { "4", "Eventual", "false", "true", "true", "defaultChanged", "1000", "true", "West US 2", "4", "200", "400", "100", "true", "false", "20", "QueryNameChanged", "221" },
            initialOptions
        };
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void createItem(String[] changedOptions) throws Exception {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(item).block();

        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(item, itemResponse);
        validateOptions(initialOptions, itemResponse, false);

        changeProperties(changedOptions);
        item = getDocumentDefinition(UUID.randomUUID().toString());
        itemResponse = container.createItem(item).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        if (changedOptions[2].equals("true")) {
            validateItemResponse(item, itemResponse);
        } else {
            assertThat(BridgeInternal.getProperties(itemResponse)).isNull();
        }
        validateOptions(changedOptions, itemResponse, false);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void deleteItem(String[] changedOptions) throws Exception {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(item).block();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();


        CosmosItemResponse<?> deleteResponse = container.deleteItem(item.getId(),
            new PartitionKey(item.get("mypk")),
            options).block();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        validateOptions(initialOptions, deleteResponse, false);

        changeProperties(changedOptions);

        container.createItem(item).block();
        deleteResponse = container.deleteItem(item.getId(),
            new PartitionKey(item.get("mypk")),
            options).block();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        validateOptions(changedOptions, deleteResponse, false);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void readItem(String[] changedOptions) throws Exception {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(item).block();

        CosmosItemResponse<InternalObjectNode> readResponse = container.readItem(item.getId(),
            new PartitionKey(item.get("mypk")),
            new CosmosItemRequestOptions(),
            InternalObjectNode.class).block();
        validateItemResponse(item, readResponse);
        validateOptions(initialOptions, readResponse, true);

        changeProperties(changedOptions);

        readResponse = container.readItem(item.getId(),
            new PartitionKey(item.get("mypk")),
            new CosmosItemRequestOptions(),
            InternalObjectNode.class).block();
        validateItemResponse(item, readResponse);
        validateOptions(changedOptions, readResponse, true);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void upsertItem(String[] changedOptions) throws Throwable {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());

        CosmosItemResponse<InternalObjectNode> upsertResponse = container.upsertItem(item, new CosmosItemRequestOptions()).block();
        assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(item, upsertResponse);
        validateOptions(initialOptions, upsertResponse, false);

        changeProperties(changedOptions);

        String newPropLabel = "newProp";
        String newPropValue = UUID.randomUUID().toString();
        item.set(newPropLabel, newPropValue, CosmosItemSerializer.DEFAULT_SERIALIZER);
        upsertResponse = container.upsertItem(item, new CosmosItemRequestOptions()).block();

        assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0);
        if (changedOptions[2].equals("true")) {
            assertThat(BridgeInternal.getProperties(upsertResponse).get(newPropLabel)).isEqualTo(newPropValue);
        } else {
            assertThat(BridgeInternal.getProperties(upsertResponse)).isNull();
        }
        validateOptions(changedOptions, upsertResponse, false);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void patchItem(String[] changedOptions) throws Exception {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());

        CosmosItemResponse<InternalObjectNode> createResponse = container.createItem(item, new CosmosItemRequestOptions()).block();
        validateItemResponse(item, createResponse);
        String newPropLabel = "newProp";
        String newPropValue = UUID.randomUUID().toString();
        PartitionKey partitionKey = new PartitionKey(item.get("mypk"));
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
        patchOperations.add("/" + newPropLabel, newPropValue);
        CosmosItemResponse<InternalObjectNode> patchResponse = container.patchItem(
            item.getId(), partitionKey, patchOperations, InternalObjectNode.class).block();
        assertThat(patchResponse.getRequestCharge()).isGreaterThan(0);
        assertThat(BridgeInternal.getProperties(patchResponse).get(newPropLabel)).isEqualTo(newPropValue);
        validateOptions(initialOptions, patchResponse, false);

        changeProperties(changedOptions);

        newPropValue = UUID.randomUUID().toString();
        patchOperations = CosmosPatchOperations.create();
        patchOperations.add("/" + newPropLabel, newPropValue);
        patchResponse = container.patchItem(item.getId(), partitionKey,
            patchOperations, InternalObjectNode.class).block();

        assertThat(patchResponse.getRequestCharge()).isGreaterThan(0);
        if (changedOptions[2].equals("true")) {
            assertThat(BridgeInternal.getProperties(patchResponse).get(newPropLabel)).isEqualTo(newPropValue);
        } else {
            assertThat(BridgeInternal.getProperties(patchResponse)).isNull();
        }

        validateOptions(changedOptions, patchResponse, false);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void replaceItem(String[] changedOptions) throws Exception {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(item).block();

        validateItemResponse(item, itemResponse);
        String newPropLabel = "newProp";
        String newPropValue = UUID.randomUUID().toString();
        item.set(newPropLabel, newPropValue, CosmosItemSerializer.DEFAULT_SERIALIZER);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        PartitionKey pk = new PartitionKey(item.get("mypk"));
        ModelBridgeInternal.setPartitionKey(options, pk);
        // replace document
        CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(item,
            item.getId(),
            pk,
            options).block();
        assertThat(BridgeInternal.getProperties(replace).get(newPropLabel)).isEqualTo(newPropValue);
        validateOptions(initialOptions, replace, false);
        changeProperties(changedOptions);

        newPropValue = UUID.randomUUID().toString();
        item.set(newPropLabel, newPropValue, CosmosItemSerializer.DEFAULT_SERIALIZER);
        // replace document
        replace = container.replaceItem(item,
            item.getId(),
            pk,
            options).block();

        if (changedOptions[2].equals("true")) {
            assertThat(BridgeInternal.getProperties(replace).get(newPropLabel)).isEqualTo(newPropValue);
        } else {
            assertThat(BridgeInternal.getProperties(replace)).isNull();
        }
        validateOptions(changedOptions, replace, false);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void bulk(String[] changedOptions) {

        Flux<CosmosItemOperation> cosmosItemOperationFlux = Flux.range(0, 10).map(i -> {
                InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
                return CosmosBulkOperations.getCreateItemOperation(item, new PartitionKey(item.get("mypk")));
            });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = container
            .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }

                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
                validateOptions(initialOptions, cosmosBulkItemResponse);

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(10);
        changeProperties(changedOptions);

        responseFlux = container
            .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc2 = new AtomicInteger(0);
        responseFlux
            .flatMap((CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc2.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }

                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
                validateOptions(changedOptions, cosmosBulkItemResponse);

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc2.get()).isEqualTo(10);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void batch(String[] changedOptions) {
        InternalObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        InternalObjectNode item2 = getDocumentDefinition(UUID.randomUUID().toString());
        item2.set("mypk", item.get("mypk"), CosmosItemSerializer.DEFAULT_SERIALIZER);
        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(item.get("mypk")));
        batch.createItemOperation(item);
        batch.createItemOperation(item2);

        CosmosBatchResponse batchResponse = container.executeCosmosBatch(batch).block();
        assertThat(batchResponse).isNotNull();
        assertThat(batchResponse.getStatusCode())
            .as("Batch server response had StatusCode {0} instead of {1} expected and had ErrorMessage {2}",
                batchResponse.getStatusCode(), HttpResponseStatus.OK.code())
            .isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.size()).isEqualTo(2);
        assertThat(batchResponse.getRequestCharge()).isPositive();
        assertThat(batchResponse.getDiagnostics().toString()).isNotEmpty();
        validateOptions(initialOptions, batchResponse);

        changeProperties(changedOptions);

        item = getDocumentDefinition(UUID.randomUUID().toString());
        item2 = getDocumentDefinition(UUID.randomUUID().toString());
        item2.set("mypk", item.get("mypk"), CosmosItemSerializer.DEFAULT_SERIALIZER);
        batch = CosmosBatch.createCosmosBatch(new PartitionKey(item.get("mypk")));
        batch.createItemOperation(item);
        batch.createItemOperation(item2);

        batchResponse = container.executeCosmosBatch(batch).block();
        assertThat(batchResponse).isNotNull();
        assertThat(batchResponse.getStatusCode())
            .as("Batch server response had StatusCode {0} instead of {1} expected and had ErrorMessage {2}",
                batchResponse.getStatusCode(), HttpResponseStatus.OK.code())
            .isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.size()).isEqualTo(2);
        assertThat(batchResponse.getRequestCharge()).isPositive();
        assertThat(batchResponse.getDiagnostics().toString()).isNotEmpty();
        validateOptions(changedOptions, batchResponse);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void query(String[] changedOptions) {
        String id = UUID.randomUUID().toString();
        container.createItem(getDocumentDefinition(id)).block();

        String query = String.format("SELECT * from c where c.id = '%s'", id);
        container.queryItems(query, InternalObjectNode.class).byPage()
            .flatMap(feedResponse -> {
                List<InternalObjectNode> results = feedResponse.getResults();
                assertThat(feedResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(results.size()).isEqualTo(1);
                assertThat(results.get(0).getId()).isEqualTo(id);
                validateOptions(initialOptions, feedResponse, false, false);
                return Flux.empty();
            }).blockLast();
        changeProperties(changedOptions);
        container.queryItems(query, InternalObjectNode.class).byPage()
            .flatMap(feedResponse -> {
                List<InternalObjectNode> results = feedResponse.getResults();
                assertThat(feedResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(results.size()).isEqualTo(1);
                assertThat(results.get(0).getId()).isEqualTo(id);
                validateOptions(changedOptions, feedResponse, false, false);
                return Flux.empty();
            }).blockLast();
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void readAllItems(String[] changedOptions) throws Exception {
        String id = UUID.randomUUID().toString();
        container.createItem(getDocumentDefinition(id)).block();
        container.readAllItems(InternalObjectNode.class).byPage()
            .flatMap(feedResponse -> {
                List<InternalObjectNode> results = feedResponse.getResults();
                assertThat(feedResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(results.size()).isGreaterThanOrEqualTo(1);
                validateOptions(initialOptions, feedResponse, false, false);
                return Flux.empty();
            }).blockLast();

        changeProperties(changedOptions);

        container.readAllItems(InternalObjectNode.class).byPage()
            .flatMap(feedResponse -> {
                List<InternalObjectNode> results = feedResponse.getResults();
                assertThat(feedResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(results.size()).isGreaterThanOrEqualTo(1);
                validateOptions(changedOptions, feedResponse, false, false);
                return Flux.empty();
            }).blockLast();
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void readMany(String[] changedOptions) throws Exception {
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        Set<String> idSet = new HashSet<>();
        int numDocuments = 5;
        for (int i = 0; i < numDocuments; i++) {
            InternalObjectNode document = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(document).block();

            PartitionKey partitionKey = new PartitionKey(document.get("mypk"));
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, document.getId());
            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(document.getId());
        }

        FeedResponse<InternalObjectNode> feedResponse = container.readMany(cosmosItemIdentities, InternalObjectNode.class).block();

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);

        for (int i = 0; i < feedResponse.getResults().size(); i++) {
            InternalObjectNode fetchedResult = feedResponse.getResults().get(i);
            assertThat(idSet.contains(fetchedResult.getId())).isTrue();
        }
        validateOptions(initialOptions, feedResponse, false, true);
        changeProperties(changedOptions);
        feedResponse = container.readMany(cosmosItemIdentities, InternalObjectNode.class).block();

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);

        for (int i = 0; i < feedResponse.getResults().size(); i++) {
            InternalObjectNode fetchedResult = feedResponse.getResults().get(i);
            assertThat(idSet.contains(fetchedResult.getId())).isTrue();
        }

        validateOptions(changedOptions, feedResponse, false, true);
    }

    @Test(groups = { "fast" }, dataProvider = "changedOptions", timeOut = TIMEOUT)
    public void queryChangeFeed(String[] changedOptions) {
        int numInserted = 20;
        for (int i = 0; i < numInserted; i++) {
            String id = UUID.randomUUID().toString();
            container.createItem(getDocumentDefinition(id)).block();
        }
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());
        Iterator<FeedResponse<InternalObjectNode>> responseIterator = container.queryChangeFeed(options, InternalObjectNode.class).byPage()
            .toIterable().iterator();
        String continuationToken = "";
        while (responseIterator.hasNext()) {
            FeedResponse<InternalObjectNode> response = responseIterator.next();
            assertThat(response.getRequestCharge()).isGreaterThan(0);
            continuationToken = response.getContinuationToken();

            validateOptions(initialOptions, response, true, false);
        }

        changeProperties(changedOptions);

        for (int i = 0; i < numInserted; i++) {
            String id = UUID.randomUUID().toString();
            container.createItem(getDocumentDefinition(id)).block();
        }

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuationToken);
        responseIterator = container.queryChangeFeed(options, InternalObjectNode.class).byPage()
            .toIterable().iterator();
        int totalResults = 0;
        while (responseIterator.hasNext()) {
            FeedResponse<InternalObjectNode> response = responseIterator.next();
            assertThat(response.getRequestCharge()).isGreaterThan(0);
            totalResults += response.getResults().size();
            validateOptions(changedOptions, response, true, false);
        }
        assertThat(totalResults).isEqualTo(numInserted);
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> response) {
        assertThat(BridgeInternal.getProperties(response).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(response).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateOptions(String[] options, CosmosItemResponse<?> response, boolean doesRequestLevelConsistencyOverrideMatter) {
       OverridableRequestOptions requestOptions = ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor().getRequestOptions(
           response.getDiagnostics().getDiagnosticsContext());
       assertThat(requestOptions.getCosmosEndToEndLatencyPolicyConfig().getEndToEndOperationTimeout().getSeconds()) // 8
           .isEqualTo(Long.parseLong(options[0]));

       if (doesRequestLevelConsistencyOverrideMatter) {
           assertThat(requestOptions.getConsistencyLevel().toString().toUpperCase(Locale.ROOT)).isEqualTo(options[1].toUpperCase(Locale.ROOT));
       }

       assertThat(requestOptions.isContentResponseOnWriteEnabled()).isEqualTo(Boolean.parseBoolean(options[2]));
       assertThat(requestOptions.getNonIdempotentWriteRetriesEnabled()).isEqualTo(Boolean.parseBoolean(options[3]));
       assertThat(requestOptions.getDedicatedGatewayRequestOptions().isIntegratedCacheBypassed()).isEqualTo(Boolean.parseBoolean(options[4]));
       assertThat(requestOptions.getThroughputControlGroupName()).isEqualTo(options[5]);
       assertThat(requestOptions.getDiagnosticsThresholds().getRequestChargeThreshold()).isEqualTo(Float.parseFloat(options[6]));
       assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(options[8].split(","))));
       assertThat(requestOptions.getKeywordIdentifiers()).isEqualTo(new HashSet<>(Arrays.asList(options[17].split(","))));
    }

    private void validateOptions(String[] options, CosmosBatchResponse response) {
        OverridableRequestOptions requestOptions = ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor().getRequestOptions(
            response.getDiagnostics().getDiagnosticsContext());
        assertThat(requestOptions.getConsistencyLevel().toString().toUpperCase(Locale.ROOT)).isEqualTo(options[1].toUpperCase(Locale.ROOT));
        assertThat(requestOptions.getDiagnosticsThresholds().getRequestChargeThreshold()).isEqualTo(Float.parseFloat(options[6]));
        assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(options[8].split(","))));
        assertThat(requestOptions.getKeywordIdentifiers()).isEqualTo(new HashSet<>(Arrays.asList(options[17].split(","))));
    }

    private void validateOptions(String[] options, CosmosBulkItemResponse response) {
        OverridableRequestOptions requestOptions = ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor().getRequestOptions(
            response.getCosmosDiagnostics().getDiagnosticsContext());
        assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(options[8].split(","))));
        assertThat(requestOptions.getThroughputControlGroupName()).isEqualTo(options[5]);
        assertThat(requestOptions.getKeywordIdentifiers()).isEqualTo(new HashSet<>(Arrays.asList(options[17].split(","))));
    }

    private void validateOptions(String[] changedOptions, FeedResponse<InternalObjectNode> response, boolean isChangeFeed, boolean isReadMany) {
        OverridableRequestOptions requestOptions = ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor().getRequestOptions(
            response.getCosmosDiagnostics().getDiagnosticsContext());
        if (isChangeFeed) {
            assertThat(requestOptions.getThroughputControlGroupName()).isEqualTo(changedOptions[5]);
            assertThat(requestOptions.getDiagnosticsThresholds().getRequestChargeThreshold()).isEqualTo(Float.parseFloat(changedOptions[6]));
            assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(changedOptions[8].split(","))));
            assertThat(requestOptions.getMaxItemCount()).isEqualTo(Integer.parseInt(changedOptions[12]));
            assertThat(requestOptions.getMaxPrefetchPageCount()).isEqualTo(Integer.parseInt(changedOptions[15]));
        } else if (isReadMany) {
            assertThat(requestOptions.getCosmosEndToEndLatencyPolicyConfig().getEndToEndOperationTimeout().getSeconds())
                .isEqualTo(Long.parseLong(changedOptions[0]));
            assertThat(requestOptions.getConsistencyLevel().toString().toUpperCase(Locale.ROOT)).isEqualTo(changedOptions[1].toUpperCase(Locale.ROOT));
            assertThat(requestOptions.getDedicatedGatewayRequestOptions().isIntegratedCacheBypassed()).isEqualTo(Boolean.parseBoolean(changedOptions[4]));
            assertThat(requestOptions.getThroughputControlGroupName()).isEqualTo(changedOptions[5]);
            assertThat(requestOptions.getDiagnosticsThresholds().getRequestChargeThreshold()).isEqualTo(Float.parseFloat(changedOptions[6]));
            assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(changedOptions[8].split(","))));
            assertThat(requestOptions.getResponseContinuationTokenLimitInKb()).isEqualTo(Integer.parseInt(changedOptions[11]));
            assertThat(requestOptions.getMaxItemCount()).isEqualTo(Integer.parseInt(changedOptions[12]));
            assertThat(requestOptions.isQueryMetricsEnabled()).isEqualTo(Boolean.parseBoolean(changedOptions[13]));
            assertThat(requestOptions.isIndexMetricsEnabled()).isEqualTo(Boolean.parseBoolean(changedOptions[14]));
        } else {
            assertThat(requestOptions.getCosmosEndToEndLatencyPolicyConfig().getEndToEndOperationTimeout().getSeconds())
                .isEqualTo(Long.parseLong(changedOptions[0]));
            assertThat(requestOptions.getConsistencyLevel().toString().toUpperCase(Locale.ROOT)).isEqualTo(changedOptions[1].toUpperCase(Locale.ROOT));
            assertThat(requestOptions.getDedicatedGatewayRequestOptions().isIntegratedCacheBypassed()).isEqualTo(Boolean.parseBoolean(changedOptions[4]));
            assertThat(requestOptions.getThroughputControlGroupName()).isEqualTo(changedOptions[5]);
            assertThat(requestOptions.getDiagnosticsThresholds().getRequestChargeThreshold()).isEqualTo(Float.parseFloat(changedOptions[6]));
            assertThat(requestOptions.isScanInQueryEnabled()).isEqualTo(Boolean.parseBoolean(changedOptions[7]));
            assertThat(requestOptions.getExcludedRegions()).isEqualTo(new ArrayList<>(Arrays.asList(changedOptions[8].split(","))));
            assertThat(requestOptions.getMaxDegreeOfParallelism()).isEqualTo(Integer.parseInt(changedOptions[9]));
            assertThat(requestOptions.getMaxBufferedItemCount()).isEqualTo(Integer.parseInt(changedOptions[10]));
            assertThat(requestOptions.getResponseContinuationTokenLimitInKb()).isEqualTo(Integer.parseInt(changedOptions[11]));
            assertThat(requestOptions.getMaxItemCount()).isEqualTo(Integer.parseInt(changedOptions[12]));
            assertThat(requestOptions.isQueryMetricsEnabled()).isEqualTo(Boolean.parseBoolean(changedOptions[13]));
            assertThat(requestOptions.isIndexMetricsEnabled()).isEqualTo(Boolean.parseBoolean(changedOptions[14]));
            assertThat(requestOptions.getQueryNameOrDefault("")).isEqualTo(changedOptions[16]);
        }
        assertThat(requestOptions.getKeywordIdentifiers()).isEqualTo(new HashSet<>(Arrays.asList(changedOptions[17].split(","))));
    }

    private void changeProperties(String[] values) {
       for (int i = 0; i < values.length; i++) {
           prop.setProperty(optionLabels[i], values[i]);
       }
    }
}
