/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.DefaultCosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class CosmosItemSerializerTest extends TestSuiteBase {
    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
        .configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);

    private CosmosClient client;
    private CosmosContainer container;
    private final boolean isContentOnWriteEnabled;
    private final boolean nonIdempotentWriteRetriesEnabled;
    private final boolean useTrackingIdForCreateAndReplace;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGatewayAndDifferentItemSerializers")
    public CosmosItemSerializerTest(
        CosmosClientBuilder clientBuilder,
        boolean inContentOnWriteEnabled,
        boolean nonIdempotentWriteRetriesEnabled,
        boolean useTrackingIdForCreateAndReplace) {
        super(clientBuilder);

        this.isContentOnWriteEnabled = inContentOnWriteEnabled;
        this.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;
        this.useTrackingIdForCreateAndReplace = useTrackingIdForCreateAndReplace;
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectSessionIncludeComputeGatewayAndDifferentItemSerializers() {
        boolean[] contentResponseOnWriteValues = new boolean[] { true, false };
        boolean[] nonIdempotentWriteRetriesEnabledValues = new boolean[] { true, false };
        boolean[] trackingIdUsageForWriteRetriesEnabledValues = new boolean[] { true, false };

        CosmosItemSerializer[] itemSerializers = new CosmosItemSerializer[] {
            null,
            CosmosItemSerializer.DEFAULT_SERIALIZER,
            EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION,
            BasicCustomItemSerializer.INSTANCE
        };

        List<Object[]> providers = new ArrayList<>();
        for (CosmosItemSerializer serializer : itemSerializers) {
            for (boolean isContentResponseOnWriteEnabled : contentResponseOnWriteValues) {
                for (boolean nonIdempotentWriteRetriesEnabled : nonIdempotentWriteRetriesEnabledValues) {
                    for (boolean trackingIdUsageForWriteRetriesEnabled : trackingIdUsageForWriteRetriesEnabledValues) {
                        if (!nonIdempotentWriteRetriesEnabled && trackingIdUsageForWriteRetriesEnabled) {
                            continue;
                        }

                        Object[][] originalProviders = clientBuildersWithDirectSession(
                            isContentResponseOnWriteEnabled,
                            true,
                            toArray(protocols));
                        List<Object[]> providersCurrentTestCase = new ArrayList<>();

                        for (Object[] current : originalProviders) {
                            Object[] injectedProviderParameters = new Object[4];
                            injectedProviderParameters[0] = current[0];

                            injectedProviderParameters[1] = isContentResponseOnWriteEnabled;
                            injectedProviderParameters[2] = nonIdempotentWriteRetriesEnabled;
                            injectedProviderParameters[3] = trackingIdUsageForWriteRetriesEnabled;
                            providersCurrentTestCase.add(injectedProviderParameters);
                        }

                        CosmosClientBuilder builder = createGatewayRxDocumentClient(
                            TestConfigurations.HOST.replace(ROUTING_GATEWAY_EMULATOR_PORT, COMPUTE_GATEWAY_EMULATOR_PORT),
                            ConsistencyLevel.SESSION,
                            false,
                            null,
                            isContentResponseOnWriteEnabled,
                            true,
                            false);
                        Object[] injectedProviderParameters = new Object[4];
                        injectedProviderParameters[0] = builder;
                        injectedProviderParameters[1] = isContentResponseOnWriteEnabled;
                        injectedProviderParameters[2] = nonIdempotentWriteRetriesEnabled;
                        injectedProviderParameters[3] = trackingIdUsageForWriteRetriesEnabled;
                        providersCurrentTestCase.add(injectedProviderParameters);

                        for (Object[] wrappedProvider : providersCurrentTestCase) {
                            CosmosClientBuilder clientBuilder = (CosmosClientBuilder) wrappedProvider[0];
                            clientBuilder.customItemSerializer(serializer);
                            clientBuilder.nonIdempotentWriteRetryOptions(
                                new NonIdempotentWriteRetryOptions()
                                    .setEnabled(nonIdempotentWriteRetriesEnabled)
                                    .setTrackingIdUsed(trackingIdUsageForWriteRetriesEnabled));
                        }

                        providers.addAll(providersCurrentTestCase);
                    }
                }
            }
        }

        Object[][] array = new Object[providers.size()][];

        return providers.toArray(array);
    }

    @BeforeClass(groups = { "fast", "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = { "fast", "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseSyncClient(this.client);
    }

    @DataProvider(name = "testConfigs_requestLevelSerializer")
    public Object[][] testConfigs_requestLevelSerializer() {
        return new Object[][] {
            new Object[] {
                CosmosItemSerializer.DEFAULT_SERIALIZER
            },

            new Object[] {
                EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION
            },

            new Object[] {
                null
            }
        };
    }

    @DataProvider(name = "testConfigs_onlyCustomSerializer")
    public Object[][] testConfigs_onlyCustomSerializer() {
        return new Object[][] {
            new Object[] {
                EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION
            },
        };
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        String prefix = nonIdempotentWriteRetriesEnabled
            ? useTrackingIdForCreateAndReplace ? "WriteRetriesWithTrackingId|" : "WriteRetriesNoTrackingId|"
            : "NoWriteRetries|";

        if (row != null && row.length > 0) {
            CosmosItemSerializer requestOptionsSerializer = (CosmosItemSerializer) row[0];
            if (requestOptionsSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
                prefix += "RequestOptions_DEFAULT";
            } else if (requestOptionsSerializer == null) {
                prefix += "RequestOptions_NULL";
            } else {
                prefix += "RequestOptions_" + requestOptionsSerializer.getClass().getSimpleName();
            }
        }

        if (this.getClientBuilder().getCustomItemSerializer() == null) {
            return prefix + "|Client_NULL";
        } else if (this.getClientBuilder().getCustomItemSerializer() == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return prefix + "|Client_DEFAULT";
        }

        return prefix + "|Client_" + this.getClientBuilder().getCustomItemSerializer().getClass().getSimpleName();
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void pointOperationsAndQueryWithPojo(CosmosItemSerializer requestLevelSerializer) {
        String id = UUID.randomUUID().toString();
        TestDocument doc = TestDocument.create(id);
        Consumer<TestDocument> onBeforeReplace = item -> item.someNumber = 999;
        BiFunction<TestDocument, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            doc.someNumber = 555;
            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        runPointOperationAndQueryTestCase(
            doc,
            id,
            onBeforeReplace,
            onBeforePatch,
            requestLevelSerializer,
            TestDocument.class);
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void pointOperationsAndQueryWithObjectNode(CosmosItemSerializer requestLevelSerializer) {
        String id = UUID.randomUUID().toString();
        ObjectNode doc = TestDocument.createAsObjectNode(id);
        Consumer<ObjectNode> onBeforeReplace = item -> item.put("someNumber", 999);
        BiFunction<ObjectNode, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            item.put("someNumber", 555);

            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        runPointOperationAndQueryTestCase(
            doc,
            id,
            onBeforeReplace,
            onBeforePatch,
            requestLevelSerializer,
            ObjectNode.class);
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void replaceItemRespectsCustomObjectMapper(CosmosItemSerializer ignored) {
        ObjectMapper customMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CosmosItemSerializer customSerializer = new DefaultCosmosItemSerializer(customMapper);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions()
            .setCustomItemSerializer(customSerializer);

        String id = UUID.randomUUID().toString();
        TestDocumentWithNullableField doc = new TestDocumentWithNullableField();
        doc.id = id;
        doc.mypk = id;
        doc.someValue = "hello";
        doc.nullableField = null;

        container.createItem(doc, new PartitionKey(id), options);

        ObjectNode rawAfterCreate = container
            .readItem(id, new PartitionKey(id), options, ObjectNode.class)
            .getItem();
        assertThat(rawAfterCreate.has("nullableField"))
            .as("createItem should respect NON_NULL and omit null fields")
            .isFalse();

        doc.someValue = "updated";
        container.replaceItem(doc, id, new PartitionKey(id), options);

        ObjectNode rawAfterReplace = container
            .readItem(id, new PartitionKey(id), options, ObjectNode.class)
            .getItem();
        assertThat(rawAfterReplace.get("someValue").asText()).isEqualTo("updated");
        assertThat(rawAfterReplace.has("nullableField"))
            .as("replaceItem should respect NON_NULL and omit null fields")
            .isFalse();
    }

    private <T> void runPointOperationAndQueryTestCase(
        T doc,
        String id,
        Consumer<T> beforeReplace,
        BiFunction<T, Boolean, CosmosPatchOperations> beforePatch,
        CosmosItemSerializer requestLevelSerializer,
        Class<T> classType) {

        boolean useEnvelopeWrapper =
            requestLevelSerializer instanceof EnvelopWrappingItemSerializer
                || (requestLevelSerializer == null
                && this.getClientBuilder()
                       .getCustomItemSerializer() instanceof EnvelopWrappingItemSerializer);
        if (requestLevelSerializer instanceof EnvelopWrappingItemSerializer
            && isContentOnWriteEnabled
            && nonIdempotentWriteRetriesEnabled
            && useTrackingIdForCreateAndReplace) {

            requestLevelSerializer = EnvelopWrappingItemSerializer.INSTANCE_WITH_TRACKING_ID_VALIDATION;
        }

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setCustomItemSerializer(requestLevelSerializer);
        CosmosItemResponse<T> pojoResponse = container.createItem(doc, new PartitionKey(id), requestOptions);

        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        container.deleteItem(doc, requestOptions);

        pojoResponse = container.createItem(doc, requestOptions);

        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        pojoResponse = container.readItem(id, new PartitionKey(id), requestOptions, classType);
        assertSameDocument(doc, pojoResponse.getItem());

        beforeReplace.accept(doc);
        pojoResponse = container.replaceItem(doc, id, new PartitionKey(id), requestOptions);
        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        CosmosPatchOperations patchOperations = beforePatch.apply(doc, useEnvelopeWrapper);
        CosmosPatchItemRequestOptions patchRequestOptions = new CosmosPatchItemRequestOptions();
        if (useEnvelopeWrapper) {
            patchRequestOptions.setCustomItemSerializer(EnvelopWrappingItemSerializer.INSTANCE_FOR_PATCH);
        } else {
            patchRequestOptions.setCustomItemSerializer(requestLevelSerializer);
        }

        pojoResponse = container.patchItem(id, new PartitionKey(id), patchOperations, patchRequestOptions, classType);
        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        pojoResponse = container.readItem(id, new PartitionKey(id), requestOptions, classType);
        assertSameDocument(doc, pojoResponse.getItem());

        beforeReplace.accept(doc);
        if (requestLevelSerializer == EnvelopWrappingItemSerializer.INSTANCE_WITH_TRACKING_ID_VALIDATION) {
            requestLevelSerializer = EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION;
        }
        CosmosItemRequestOptions upsertRequestOptions = new CosmosItemRequestOptions();
        if (useEnvelopeWrapper) {
            upsertRequestOptions.setCustomItemSerializer(EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION);
        } else {
            upsertRequestOptions.setCustomItemSerializer(requestOptions.getCustomItemSerializer());
        }
        pojoResponse = container.upsertItem(doc, new PartitionKey(id), upsertRequestOptions);
        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        if (useEnvelopeWrapper) {
            queryRequestOptions.setCustomItemSerializer(EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION);
        } else {
            queryRequestOptions.setCustomItemSerializer(requestOptions.getCustomItemSerializer());
        }
        List<T> results = container
            .queryItems("SELECT * FROM c where c.id = '" + id + "'", queryRequestOptions, classType)
            .stream().collect(Collectors.toList());
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertSameDocument(doc, results.get(0));

        results = container.readAllItems(new PartitionKey(id), queryRequestOptions, classType)
                           .stream().collect(Collectors.toList());
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertSameDocument(doc, results.get(0));
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void bulkAndReadManyWithObjectNode(CosmosItemSerializer requestLevelSerializer) {

        runBulkAndReadManyTestCase(
            id -> TestDocument.createAsObjectNode(id),
            requestLevelSerializer,
            ObjectNode.class
        );
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void bulkAndReadManyWithPojo(CosmosItemSerializer requestLevelSerializer) {

        runBulkAndReadManyTestCase(
            id -> TestDocument.create(id),
            requestLevelSerializer,
            TestDocument.class
        );
    }


    private <T> void runBulkAndReadManyTestCase(
        Function<String, T> docGenerator,
        CosmosItemSerializer requestLevelSerializer,
        Class<T> classType) {

        CosmosBulkExecutionOptions bulkExecOptions = new CosmosBulkExecutionOptions()
            .setCustomItemSerializer(requestLevelSerializer);

        List<CosmosItemOperation> bulkOperations = new ArrayList<>();
        Map<String, T> inputItems = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String id = UUID.randomUUID().toString();
            T doc = docGenerator.apply(id);
            inputItems.put(id, doc);

            bulkOperations.add(CosmosBulkOperations.getCreateItemOperation(
                doc,
                new PartitionKey(id),
                id));
        }

        Iterable<CosmosBulkOperationResponse<Object>> responseFlux =
            container.executeBulkOperations(bulkOperations, bulkExecOptions);
        for(CosmosBulkOperationResponse<Object> response: responseFlux) {
            assertThat(response.getException()).isNull();
            assertThat(response.getResponse()).isNotNull();
            assertThat(response.getResponse().getStatusCode()).isBetween(200, 201);
            assertThat(response.getOperation().<String>getContext()).isNotNull();
            String id = response.getOperation().getContext();
            assertThat(inputItems.containsKey(id)).isTrue();
            T responseItem = response.getResponse().getItem(classType);

            if (isContentOnWriteEnabled) {
                assertSameDocument(inputItems.get(id), responseItem);
            } else {
                assertThat(responseItem).isNull();
            }
        }

        List<CosmosItemIdentity> readManyTuples = new ArrayList<>();
        for (String id: inputItems.keySet().stream().limit(3).toArray(count -> new String[count])) {
            readManyTuples.add(new CosmosItemIdentity(new PartitionKey(id), id));
        }

        CosmosReadManyRequestOptions readManyRequestOptions = new CosmosReadManyRequestOptions()
            .setCustomItemSerializer(requestLevelSerializer);

        FeedResponse<T> response = container.readMany(readManyTuples, readManyRequestOptions, classType);
        assertThat(response).isNotNull();
        Object[] items = response.getElements().stream().toArray();
        assertThat(items).isNotNull();
        assertThat(items).hasSize(3);
        for(Object responseItem: items) {
            assertThat(responseItem).isNotNull();
            if (responseItem instanceof TestDocument) {
                TestDocument doc = (TestDocument) responseItem;
                assertThat(inputItems.containsKey(doc.id)).isTrue();
                assertSameDocument(inputItems.get(doc.id), doc);
            } else if (responseItem instanceof ObjectNode) {
                ObjectNode doc = (ObjectNode) responseItem;
                String id = doc.get("id").asText();
                assertThat(inputItems.containsKey(id)).isTrue();
                assertSameDocument(inputItems.get(id), doc);
            } else {
                fail("Unexpected response item type '" + responseItem.getClass().getSimpleName() + "'.");
            }
        }
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void batchAndChangeFeedWithObjectNode(CosmosItemSerializer requestLevelSerializer) {

        runBatchAndChangeFeedTestCase(
            pk -> TestDocument.createAsObjectNode(UUID.randomUUID().toString(), pk),
            requestLevelSerializer,
            ObjectNode.class
        );
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void batchAndChangeFeedWithPojo(CosmosItemSerializer requestLevelSerializer) {

        runBatchAndChangeFeedTestCase(
            pk -> TestDocument.create(UUID.randomUUID().toString(), pk),
            requestLevelSerializer,
            TestDocument.class
        );
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_onlyCustomSerializer", timeOut = TIMEOUT * 1000000)
    public void handleCustomSerializationExceptionPojo(CosmosItemSerializer requestLevelSerializer) {
        String id = "serializationFailure" + UUID.randomUUID();
        TestDocument doc = TestDocument.create(id);
        Consumer<TestDocument> onBeforeReplace = item -> item.someNumber = 999;
        BiFunction<TestDocument, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            doc.someNumber = 555;
            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        try {
            runPointOperationAndQueryTestCase(
                doc,
                id,
                onBeforeReplace,
                onBeforePatch,
                requestLevelSerializer,
                TestDocument.class);

            fail("A custom serialization exception should have been thrown.");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(400);
            assertThat(cosmosException.getCause()).isNotNull();
            assertThat(cosmosException.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(cosmosException.getCause().getCause()).isNotNull();
            assertThat(cosmosException.getCause().getCause()).isInstanceOf(OutOfMemoryError.class);
            assertThat(cosmosException.getCause().getCause().getMessage())
                .isEqualTo("Some dummy Error thrown in custom serializer during serialization.");
        }
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_onlyCustomSerializer", timeOut = TIMEOUT * 1000000)
    public void handleCustomSerializationExceptionObjectNode(CosmosItemSerializer requestLevelSerializer) {
        String id = "serializationFailure" + UUID.randomUUID();
        ObjectNode doc = TestDocument.createAsObjectNode(id);
        Consumer<ObjectNode> onBeforeReplace = item -> item.put("someNumber", 999);
        BiFunction<ObjectNode, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            item.put("someNumber", 555);

            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        try {
            runPointOperationAndQueryTestCase(
                doc,
                id,
                onBeforeReplace,
                onBeforePatch,
                requestLevelSerializer,
                ObjectNode.class);

            fail("A custom serialization exception should have been thrown.");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(400);
            assertThat(cosmosException.getCause()).isNotNull();
            assertThat(cosmosException.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(cosmosException.getCause().getCause()).isNotNull();
            assertThat(cosmosException.getCause().getCause()).isInstanceOf(OutOfMemoryError.class);
            assertThat(cosmosException.getCause().getCause().getMessage())
                .isEqualTo("Some dummy Error thrown in custom serializer during serialization.");
        }
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_onlyCustomSerializer", timeOut = TIMEOUT * 1000000)
    public void handleCustomDeserializationExceptionPojo(CosmosItemSerializer requestLevelSerializer) {
        String id = "deserializationFailure" + UUID.randomUUID();
        TestDocument doc = TestDocument.create(id);
        Consumer<TestDocument> onBeforeReplace = item -> item.someNumber = 999;
        BiFunction<TestDocument, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            doc.someNumber = 555;
            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        try {
            runPointOperationAndQueryTestCase(
                doc,
                id,
                onBeforeReplace,
                onBeforePatch,
                requestLevelSerializer,
                TestDocument.class);

            fail("A custom deserialization exception should have been thrown.");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(400);
            assertThat(cosmosException.getCause()).isNotNull();
            assertThat(cosmosException.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(cosmosException.getCause().getCause()).isNotNull();
            assertThat(cosmosException.getCause().getCause()).isInstanceOf(OutOfMemoryError.class);
            assertThat(cosmosException.getCause().getCause().getMessage())
                .isEqualTo("Some dummy Error thrown in custom serializer during deserialization.");
        }
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_onlyCustomSerializer", timeOut = TIMEOUT * 1000000)
    public void handleCustomDeserializationExceptionObjectNode(CosmosItemSerializer requestLevelSerializer) {
        String id = "deserializationFailure" + UUID.randomUUID();
        ObjectNode doc = TestDocument.createAsObjectNode(id);
        Consumer<ObjectNode> onBeforeReplace = item -> item.put("someNumber", 999);
        BiFunction<ObjectNode, Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

            item.put("someNumber", 555);

            if (!isEnvelopeWrapped) {
                return CosmosPatchOperations
                    .create()
                    .add("/someNumber", 555);
            } else {
                return CosmosPatchOperations
                    .create()
                    .add("/wrappedContent/someNumber", 555);
            }
        };

        try {
            runPointOperationAndQueryTestCase(
                doc,
                id,
                onBeforeReplace,
                onBeforePatch,
                requestLevelSerializer,
                ObjectNode.class);

            fail("A custom deserialization exception should have been thrown.");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(400);
            assertThat(cosmosException.getCause()).isNotNull();
            assertThat(cosmosException.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(cosmosException.getCause().getCause()).isNotNull();
            assertThat(cosmosException.getCause().getCause()).isInstanceOf(OutOfMemoryError.class);
            assertThat(cosmosException.getCause().getCause().getMessage())
                .isEqualTo("Some dummy Error thrown in custom serializer during deserialization.");
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithOrderByAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        String pkValue = UUID.randomUUID().toString();
        List<String> createdIds = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                String id = String.format("%s_%03d", pkValue, i);
                TestDocument doc = TestDocument.create(id, pkValue);
                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                    .setCustomItemSerializer(clientSerializer);
                container.createItem(doc, new PartitionKey(pkValue), requestOptions);
                createdIds.add(id);
            }

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            List<TestDocument> results = container
                .queryItems(
                    "SELECT * FROM c WHERE c.mypk = '" + pkValue + "' ORDER BY c.id ASC",
                    queryRequestOptions,
                    TestDocument.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(3);
            for (int i = 0; i < results.size(); i++) {
                assertThat(results.get(i).id).isEqualTo(createdIds.get(i));
                assertThat(results.get(i).mypk).isEqualTo(pkValue);
            }
        } finally {
            for (String id : createdIds) {
                try {
                    container.deleteItem(id, new PartitionKey(pkValue), new CosmosItemRequestOptions());
                } catch (Exception ignored) { }
            }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithAggregateAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        boolean isEnvelopeWrapper = clientSerializer instanceof EnvelopWrappingItemSerializer;
        if (isEnvelopeWrapper) {
            return;
        }

        String pkValue = UUID.randomUUID().toString();
        List<String> createdIds = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                String id = UUID.randomUUID().toString();
                TestDocument doc = TestDocument.create(id, pkValue);
                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                    .setCustomItemSerializer(clientSerializer);
                container.createItem(doc, new PartitionKey(pkValue), requestOptions);
                createdIds.add(id);
            }

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            // SELECT COUNT(1) returns an object (e.g. {"$1": 3}) rather than a scalar.
            // Custom serializers work with Map<String, Object> which is object-level
            // deserialization - they cannot handle scalar VALUE queries like
            // SELECT VALUE COUNT(1) because the aggregate pipeline wraps the scalar
            // in a {"_value": N} Document that can't be deserialized as Integer.
            List<ObjectNode> results = container
                .queryItems(
                    "SELECT COUNT(1) FROM c WHERE c.mypk = '" + pkValue + "'",
                    queryRequestOptions,
                    ObjectNode.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).get("$1").asInt()).isEqualTo(3);
        } finally {
            for (String id : createdIds) {
                try {
                    container.deleteItem(id, new PartitionKey(pkValue), new CosmosItemRequestOptions());
                } catch (Exception ignored) { }
            }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithDistinctAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        boolean isEnvelopeWrapper = clientSerializer instanceof EnvelopWrappingItemSerializer;
        if (isEnvelopeWrapper) {
            return;
        }

        String pkValue = UUID.randomUUID().toString();
        List<String> createdIds = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                String id = UUID.randomUUID().toString();
                TestDocument doc = TestDocument.create(id, pkValue);
                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                    .setCustomItemSerializer(clientSerializer);
                container.createItem(doc, new PartitionKey(pkValue), requestOptions);
                createdIds.add(id);
            }

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            List<ObjectNode> results = container
                .queryItems(
                    "SELECT DISTINCT c.mypk FROM c WHERE c.mypk = '" + pkValue + "'",
                    queryRequestOptions,
                    ObjectNode.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).get("mypk").asText()).isEqualTo(pkValue);
        } finally {
            for (String id : createdIds) {
                try {
                    container.deleteItem(id, new PartitionKey(pkValue), new CosmosItemRequestOptions());
                } catch (Exception ignored) { }
            }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithGroupByAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        boolean isEnvelopeWrapper = clientSerializer instanceof EnvelopWrappingItemSerializer;
        if (isEnvelopeWrapper) {
            return;
        }

        String pkValue = UUID.randomUUID().toString();
        List<String> createdIds = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                String id = UUID.randomUUID().toString();
                TestDocument doc = TestDocument.create(id, pkValue);
                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                    .setCustomItemSerializer(clientSerializer);
                container.createItem(doc, new PartitionKey(pkValue), requestOptions);
                createdIds.add(id);
            }

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            List<ObjectNode> results = container
                .queryItems(
                    "SELECT c.mypk, COUNT(1) as cnt FROM c WHERE c.mypk = '" + pkValue + "' GROUP BY c.mypk",
                    queryRequestOptions,
                    ObjectNode.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).get("mypk").asText()).isEqualTo(pkValue);
            assertThat(results.get(0).get("cnt").asInt()).isEqualTo(3);
        } finally {
            for (String id : createdIds) {
                try {
                    container.deleteItem(id, new PartitionKey(pkValue), new CosmosItemRequestOptions());
                } catch (Exception ignored) { }
            }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithSqlParameterAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        String id = UUID.randomUUID().toString();
        TestDocument doc = TestDocument.create(id);
        try {
            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                .setCustomItemSerializer(clientSerializer);
            container.createItem(doc, new PartitionKey(id), requestOptions);

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE c.id = @id",
                new SqlParameter("@id", id));

            List<TestDocument> results = container
                .queryItems(querySpec, queryRequestOptions, TestDocument.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertSameDocument(doc, results.get(0));
        } finally {
            try {
                container.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
            } catch (Exception ignored) { }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithSqlParameterDateTimeAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        boolean isEnvelopeWrapper = clientSerializer instanceof EnvelopWrappingItemSerializer;
        if (isEnvelopeWrapper) {
            return;
        }

        // This test validates that when a custom serializer changes how values are
        // stored (e.g. Instant as ISO-8601 string instead of a timestamp number),
        // SqlParameter correctly applies the same serializer so that query filters
        // match the stored representation.
        String id = UUID.randomUUID().toString();
        String pkValue = id;
        Instant createdAt = Instant.parse("2026-03-15T10:30:00Z");
        TestDocumentWithTimestamp doc = new TestDocumentWithTimestamp();
        doc.id = id;
        doc.mypk = pkValue;
        doc.createdAt = createdAt;
        doc.description = "test-datetime-serialization";

        try {
            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                .setCustomItemSerializer(clientSerializer);
            container.createItem(doc, new PartitionKey(pkValue), requestOptions);

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            // Query using SqlParameter with the same Instant value — the custom serializer
            // must serialize the parameter the same way as the document field.
            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE c.createdAt = @createdAt AND c.id = @id",
                new SqlParameter("@createdAt", createdAt),
                new SqlParameter("@id", id));

            List<TestDocumentWithTimestamp> results = container
                .queryItems(querySpec, queryRequestOptions, TestDocumentWithTimestamp.class)
                .stream().collect(Collectors.toList());

            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).id).isEqualTo(id);
            assertThat(results.get(0).createdAt).isEqualTo(createdAt);
            assertThat(results.get(0).description).isEqualTo("test-datetime-serialization");
        } finally {
            try {
                container.deleteItem(id, new PartitionKey(pkValue), new CosmosItemRequestOptions());
            } catch (Exception ignored) { }
        }
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT * 1000000)
    public void queryWithConcurrentSqlQuerySpecReuseAndCustomSerializer() {
        CosmosItemSerializer clientSerializer = this.getClientBuilder().getCustomItemSerializer();
        if (clientSerializer == null || clientSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return;
        }

        boolean isEnvelopeWrapper = clientSerializer instanceof EnvelopWrappingItemSerializer;
        if (isEnvelopeWrapper) {
            return;
        }

        CosmosAsyncClient asyncClient = client.asyncClient();
        CosmosAsyncContainer asyncContainer = asyncClient
            .getDatabase(container.asyncContainer.getDatabase().getId())
            .getContainer(container.asyncContainer.getId());

        // Create multiple documents with Instant fields
        String pkValue = UUID.randomUUID().toString();
        int docCount = 5;
        List<TestDocumentWithTimestamp> createdDocs = new ArrayList<>();
        Instant createdAt = Instant.parse("2026-03-15T10:30:00Z");

        try {
            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            for (int i = 0; i < docCount; i++) {
                TestDocumentWithTimestamp doc = new TestDocumentWithTimestamp();
                doc.id = UUID.randomUUID().toString();
                doc.mypk = pkValue;
                doc.createdAt = createdAt;
                doc.description = "concurrent-test-" + i;
                asyncContainer.createItem(doc, new PartitionKey(pkValue), requestOptions).block();
                createdDocs.add(doc);
            }

            // Build a single shared SqlQuerySpec — this is the scenario under test:
            // the same instance is reused across concurrent queries.
            SqlQuerySpec sharedQuerySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE c.createdAt = @createdAt AND c.mypk = @pk",
                new SqlParameter("@createdAt", createdAt),
                new SqlParameter("@pk", pkValue));

            // Capture original parameter values before concurrent execution
            List<SqlParameter> originalParams = sharedQuerySpec.getParameters();
            Object originalCreatedAtValue = originalParams.get(0).getValue(Object.class);
            Object originalPkValue = originalParams.get(1).getValue(Object.class);

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions()
                .setCustomItemSerializer(clientSerializer);

            int concurrentQueries = 10;

            List<List<TestDocumentWithTimestamp>> allResults =
                Flux.range(0, concurrentQueries)
                    .flatMap(i ->
                        asyncContainer
                            .queryItems(sharedQuerySpec, queryRequestOptions, TestDocumentWithTimestamp.class)
                            .byPage()
                            .flatMapIterable(FeedResponse::getResults)
                            .collectList(),
                        concurrentQueries)
                    .collectList()
                    .block();

            assertThat(allResults).isNotNull();
            assertThat(allResults).hasSize(concurrentQueries);
            for (List<TestDocumentWithTimestamp> results : allResults) {
                assertThat(results).hasSize(docCount);
                for (TestDocumentWithTimestamp result : results) {
                    assertThat(result.createdAt).isEqualTo(createdAt);
                    assertThat(result.mypk).isEqualTo(pkValue);
                }
            }

            // Verify the original SqlQuerySpec parameters are unmodified
            assertThat(sharedQuerySpec.getParameters()).hasSize(2);
            assertThat(sharedQuerySpec.getParameters().get(0).getValue(Object.class))
                .isEqualTo(originalCreatedAtValue);
            assertThat(sharedQuerySpec.getParameters().get(1).getValue(Object.class))
                .isEqualTo(originalPkValue);
        } finally {
            for (TestDocumentWithTimestamp doc : createdDocs) {
                try {
                    asyncContainer.deleteItem(doc.id, new PartitionKey(pkValue), new CosmosItemRequestOptions()).block();
                } catch (Exception ignored) { }
            }
        }
    }

    private <T> void runBatchAndChangeFeedTestCase(
        Function<String, T> docGenerator,
        CosmosItemSerializer requestLevelSerializer,
        Class<T> classType) {

        String pkValue = UUID.randomUUID().toString();
        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(pkValue));
        List<T> inputItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            T doc = docGenerator.apply(pkValue);
            inputItems.add(doc);
            batch.createItemOperation(doc);
        }

        CosmosBatchRequestOptions batchRequestOptions = new CosmosBatchRequestOptions()
            .setCustomItemSerializer(requestLevelSerializer);

        CosmosBatchResponse response = container.executeCosmosBatch(batch, batchRequestOptions);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getResults()).isNotNull();
        assertThat(response.getResults()).hasSize(10);
        for (CosmosBatchOperationResult result: response.getResults()) {
            T responseItem = result.getItem(classType);
            if (isContentOnWriteEnabled) {
                boolean found = false;
                for (T inputItem: inputItems) {
                    if (hasSameId(inputItem, responseItem)) {
                        assertSameDocument(inputItem, responseItem);
                        found = true;
                        break;
                    }
                }

                assertThat(found).isTrue();

            } else {
                assertThat(responseItem).isNull();
            }
        }

        CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(
                FeedRange.forLogicalPartition(new PartitionKey(pkValue))
            )
            .setCustomItemSerializer(requestLevelSerializer);

        List<T> results = container
            .queryChangeFeed(changeFeedRequestOptions, classType)
            .stream().collect(Collectors.toList());
        assertThat(results).isNotNull();
        assertThat(results).hasSize(10);
        for (T responseItem: results) {
            boolean found = false;
            for (T inputItem: inputItems) {
                if (hasSameId(inputItem, responseItem)) {
                    assertSameDocument(inputItem, responseItem);
                    found = true;
                    break;
                }
            }

            assertThat(found).isTrue();
        }
    }

    private static class TestChildObject {
        public String childId;

        public Integer someNumber;
    }

    private static class TestDocument {
        public String id;

        public String mypk;

        public Integer someNumber;

        public String[] someStringArray;

        public Integer[] someNumberArray;

        public TestChildObject someChildObject;

        public TestChildObject[] someChildObjectArray;

        public static TestDocument create(String id) {
            return create(id, id);
        }

        public static TestDocument create(String id, String pk) {
            TestDocument doc = new TestDocument();
            doc.id = id;
            doc.mypk = pk;
            doc.someNumber = 5;
            doc.someStringArray = new String[] { id, "someString2", "someString3" };
            doc.someNumberArray = new Integer[] { 1, 3, 5 };
            TestChildObject child = new TestChildObject();
            child.childId = "C1_" + id;
            child.someNumber = 9;
            doc.someChildObject = child;
            doc.someChildObjectArray = new TestChildObject[] { child, child };

            return doc;
        }

        public static ObjectNode createAsObjectNode(String id) {
            return createAsObjectNode(id, id);
        }

        public static ObjectNode createAsObjectNode(String id, String pk) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", id);
            node.put("mypk", pk);
            node.put("someNumber", 5);
            node.put("someStringArray", objectMapper.createArrayNode().add(id).add("someString2").add("someString3"));
            node.put("someNumberArray", objectMapper.createArrayNode().add(1).add(3).add(5));
            ObjectNode child = objectMapper.createObjectNode();
            child.put("childId", "C1_" + id);
            child.put("someNumber", 9);
            node.put("someChildObject", child);
            node.put("someChildObjectArray", objectMapper.createArrayNode().add(child).add(child));

            return node;
        }

        public static TestDocument parse(ObjectNode node) {
            try {
                return objectMapper.treeToValue(node, TestDocument.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean hasSameId(Object doc, Object deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

        if (doc instanceof TestDocument) {
            return hasSameId((TestDocument) doc, (TestDocument) deserializedDoc);
        }

        return hasSameId((ObjectNode) doc, (ObjectNode) deserializedDoc);
    }

    private static void assertSameDocument(Object doc, Object deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

        if (doc instanceof TestDocument) {
            assertSameDocument((TestDocument) doc, (TestDocument) deserializedDoc);
            return;
        }

        assertSameDocument((ObjectNode) doc, (ObjectNode) deserializedDoc);
    }

    private static boolean hasSameId(ObjectNode doc, ObjectNode deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

        return hasSameId(TestDocument.parse(doc), TestDocument.parse(deserializedDoc));
    }

    private static void assertSameDocument(ObjectNode doc, ObjectNode deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

        assertSameDocument(TestDocument.parse(doc), TestDocument.parse(deserializedDoc));
    }

    private static boolean hasSameId(TestDocument doc, TestDocument deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();
        return doc.id.equals(deserializedDoc.id);
    }

    private static void assertSameDocument(TestDocument doc, TestDocument deserializedDoc) {

        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();
        assertThat(deserializedDoc.id).isEqualTo(doc.id);
        assertThat(deserializedDoc.mypk).isEqualTo(doc.mypk);
        assertThat(deserializedDoc.someNumber).isEqualTo(doc.someNumber);
        if (doc.someStringArray == null) {
            assertThat(deserializedDoc.someStringArray).isNull();
        } else {
            assertThat(deserializedDoc.someStringArray).isNotNull();
            assertThat(deserializedDoc.someStringArray.length).isEqualTo(doc.someStringArray.length);
            assertThat(deserializedDoc.someStringArray).containsAll(Arrays.stream(doc.someStringArray).collect(Collectors.toList()));
        }

        if (doc.someNumberArray == null) {
            assertThat(deserializedDoc.someNumberArray).isNull();
        } else {
            assertThat(deserializedDoc.someNumberArray).isNotNull();
            assertThat(deserializedDoc.someNumberArray.length).isEqualTo(doc.someNumberArray.length);
            assertThat(deserializedDoc.someNumberArray).containsAll(Arrays.stream(doc.someNumberArray).collect(Collectors.toList()));
        }

        if (doc.someChildObject == null) {
            assertThat(deserializedDoc.someChildObject).isNull();
        } else {
            assertThat(deserializedDoc.someChildObject).isNotNull();
            assertThat(deserializedDoc.someChildObject.childId).isEqualTo(doc.someChildObject.childId);
            assertThat(deserializedDoc.someChildObject.someNumber).isEqualTo(doc.someChildObject.someNumber);
        }

        if (doc.someChildObjectArray == null) {
            assertThat(deserializedDoc.someChildObjectArray).isNull();
        } else {
            assertThat(deserializedDoc.someChildObjectArray).isNotNull();
            assertThat(deserializedDoc.someChildObjectArray.length).isEqualTo(doc.someChildObjectArray.length);
            for (int i = 0; i < doc.someChildObjectArray.length; i++) {
                assertThat(deserializedDoc.someChildObjectArray[i].childId).isEqualTo(doc.someChildObjectArray[i].childId);
                assertThat(deserializedDoc.someChildObjectArray[i].someNumber).isEqualTo(doc.someChildObjectArray[i].someNumber);
            }
        }
    }

    private static class TestDocumentWithNullableField {
        public String id;
        public String mypk;
        public String someValue;
        public String nullableField;
    }

    private static class TestDocumentWithTimestamp {
        public String id;
        public String mypk;
        public Instant createdAt;
        public String description;
    }

    private static class TestDocumentWrappedInEnvelope {
        public String id;

        public String mypk;

        public ObjectNode wrappedContent;
    }

    private static class EnvelopWrappingItemSerializer extends CosmosItemSerializer {
        public static final CosmosItemSerializer INSTANCE_NO_TRACKING_ID_VALIDATION = new EnvelopWrappingItemSerializer(false, false);
        public static final CosmosItemSerializer INSTANCE_WITH_TRACKING_ID_VALIDATION = new EnvelopWrappingItemSerializer(true, false);
        public static final CosmosItemSerializer INSTANCE_FOR_PATCH = new EnvelopWrappingItemSerializer(false, true);

        private final static Class<?> mapClass = new ConcurrentHashMap<String, Object>().getClass();

        private final boolean shouldValidateTrackingId;
        private final boolean passThroughOnSerialize;

        public EnvelopWrappingItemSerializer(boolean enabledTrackingIdValidation, boolean passThroughOnSerialize) {
            this.shouldValidateTrackingId = enabledTrackingIdValidation;
            this.passThroughOnSerialize = passThroughOnSerialize;
        }

        @Override
        public <T> Map<String, Object> serialize(T item) {
            if (item == null) {
                return null;
            }

            if (passThroughOnSerialize) {
                return ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor().serializeSafe(
                    CosmosItemSerializer.DEFAULT_SERIALIZER,
                    item);
            }

            Map<String, Object> unwrappedJsonTree = CosmosItemSerializer.DEFAULT_SERIALIZER.serialize(item);

            // Non-document values (e.g., SQL parameter values like strings or numbers)
            // don't have "id" — pass them through without wrapping.
            if (!unwrappedJsonTree.containsKey("id")) {
                return unwrappedJsonTree;
            }

            if (unwrappedJsonTree.containsKey("wrappedContent")) {
                throw new IllegalStateException("Double wrapping");
            }

            if (unwrappedJsonTree.get("id") != null && unwrappedJsonTree.get("id").toString().startsWith("serializationFailure")) {
                throw new OutOfMemoryError("Some dummy Error thrown in custom serializer during serialization.");
            }

            Map<String, Object> wrappedJsonTree = new ConcurrentHashMap<>();
            wrappedJsonTree.put("id", unwrappedJsonTree.get("id"));
            wrappedJsonTree.put("mypk", unwrappedJsonTree.get("mypk"));
            wrappedJsonTree.put("wrappedContent", unwrappedJsonTree);

            return wrappedJsonTree;
        }

        @Override
        public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
            if (jsonNodeMap == null) {
                return null;
            }

            if (shouldValidateTrackingId) {
                assertThat(jsonNodeMap.containsKey("_trackingId")).isEqualTo(true);
                assertThat(jsonNodeMap.get("_trackingId")).isNotNull();
            }

            TestDocumentWrappedInEnvelope envelope = ImplementationBridgeHelpers
                .CosmosItemSerializerHelper
                .getCosmosItemSerializerAccessor()
                .deserializeSafe(
                    CosmosItemSerializer.DEFAULT_SERIALIZER,
                    jsonNodeMap,
                    TestDocumentWrappedInEnvelope.class);

            if (envelope == null || envelope.wrappedContent == null) {
                return null;
            }

            Map<String, Object> unwrappedContent =
                (Map<String, Object>) objectMapper.convertValue(envelope.wrappedContent, mapClass);

            if (unwrappedContent.containsKey("wrappedContent")) {
                throw new IllegalStateException("Double wrapped");
            }

            if (unwrappedContent.get("id") != null && unwrappedContent.get("id").toString().startsWith("deserializationFailure")) {
                throw new OutOfMemoryError("Some dummy Error thrown in custom serializer during deserialization.");
            }

            return ImplementationBridgeHelpers
                .CosmosItemSerializerHelper
                .getCosmosItemSerializerAccessor()
                .deserializeSafe(
                    CosmosItemSerializer.DEFAULT_SERIALIZER,
                    unwrappedContent,
                    classType);
        }
    }

    /**
     * A simple custom item serializer that mirrors the real-world use case from
     * <a href="https://github.com/Azure/azure-sdk-for-java/issues/45521">issue #45521</a>.
     * Uses a custom ObjectMapper with different settings (e.g., dates as ISO strings)
     * without transforming the document structure (no wrapping/unwrapping).
     */
    @SuppressWarnings("unchecked")
    private static class BasicCustomItemSerializer extends CosmosItemSerializer {
        public static final BasicCustomItemSerializer INSTANCE = new BasicCustomItemSerializer();

        private static final ObjectMapper customMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        private BasicCustomItemSerializer() {
        }

        @Override
        public <T> Map<String, Object> serialize(T item) {
            if (item == null) {
                return null;
            }

            JsonNode jsonNode = customMapper.convertValue(item, JsonNode.class);
            if (jsonNode == null) {
                return null;
            }

            if (jsonNode.isObject()) {
                return customMapper.convertValue(jsonNode, Map.class);
            }

            // For scalar values (e.g. Instant, String, numbers), return a single-entry
            // map using the well-known primitive value key so the framework correctly
            // sets it as a scalar in the JSON property bag.
            Map<String, Object> primitiveMap = new HashMap<>();
            primitiveMap.put("__primitive_json-node_value__", customMapper.convertValue(jsonNode, Object.class));
            return primitiveMap;
        }

        @Override
        public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
            if (jsonNodeMap == null) {
                return null;
            }
            return customMapper.convertValue(jsonNodeMap, classType);
        }
    }
}
