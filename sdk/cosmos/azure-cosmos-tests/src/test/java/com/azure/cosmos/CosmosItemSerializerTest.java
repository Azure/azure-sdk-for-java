/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
            EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION
        };

        List<Object[]> providers = new ArrayList<>();
        for (CosmosItemSerializer serializer: itemSerializers) {
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
                            true);
                        Object[] injectedProviderParameters = new Object[4];
                        injectedProviderParameters[0] = builder;
                        injectedProviderParameters[1] = isContentResponseOnWriteEnabled;
                        injectedProviderParameters[2] = nonIdempotentWriteRetriesEnabled;
                        injectedProviderParameters[3] = trackingIdUsageForWriteRetriesEnabled;
                        providersCurrentTestCase.add(injectedProviderParameters);

                        for (Object[] wrappedProvider : providersCurrentTestCase) {
                            CosmosClientBuilder clientBuilder = (CosmosClientBuilder) wrappedProvider[0];
                            clientBuilder.setCustomSerializer(serializer);
                            clientBuilder.setNonIdempotentWriteRetryPolicy(
                                nonIdempotentWriteRetriesEnabled,
                                trackingIdUsageForWriteRetriesEnabled);
                        }

                        providers.addAll(providersCurrentTestCase);
                    }
                }
            }
        }

        Object[][] array = new Object[providers.size()][];

        return providers.toArray(array);
    }

    @BeforeClass(groups = {"fast", "emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"fast", "emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
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

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        String prefix = nonIdempotentWriteRetriesEnabled
            ? useTrackingIdForCreateAndReplace ? "WriteRetriesWithTrackingId|" : "WriteRetriesNoTrackingId|"
            : "NoWriteRetries|";

        CosmosItemSerializer requestOptionsSerializer = (CosmosItemSerializer)row[0];
        if (requestOptionsSerializer == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            prefix += "RequestOptions_DEFAULT";
        } else if (requestOptionsSerializer == null) {
            prefix += "RequestOptions_NULL";
        } else {
            prefix += "RequestOptions_" + requestOptionsSerializer.getClass().getSimpleName();
        }

        if (this.getClientBuilder().getCustomSerializer() == null) {
            return prefix + "|Client_NULL";
        } else if (this.getClientBuilder().getCustomSerializer() == CosmosItemSerializer.DEFAULT_SERIALIZER) {
            return prefix + "|Client_DEFAULT";
        }

        return prefix + "|Client_" +  this.getClientBuilder().getCustomSerializer().getClass().getSimpleName();
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void pointOperationsWithPojo(CosmosItemSerializer requestLevelSerializer) {
        String id = UUID.randomUUID().toString();
        TestDocument doc = TestDocument.create(id);
        Consumer<TestDocument> onBeforeReplace = item -> item.someNumber = 999;
        BiFunction<TestDocument,Boolean, CosmosPatchOperations> onBeforePatch = (item, isEnvelopeWrapped) -> {

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

        runPointOperationTestCase(
            doc,
            id,
            onBeforeReplace,
            onBeforePatch,
            requestLevelSerializer,
            TestDocument.class);
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "testConfigs_requestLevelSerializer", timeOut = TIMEOUT * 1000000)
    public void pointOperationsWithObjectNode(CosmosItemSerializer requestLevelSerializer) {
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

        runPointOperationTestCase(
            doc,
            id,
            onBeforeReplace,
            onBeforePatch,
            requestLevelSerializer,
            ObjectNode.class);
    }

    private <T> void runPointOperationTestCase(
        T doc,
        String id,
        Consumer<T> beforeReplace,
        BiFunction<T, Boolean, CosmosPatchOperations> beforePatch,
        CosmosItemSerializer requestLevelSerializer,
        Class<T> classType) {

        boolean useEnvelopeWrapper =
            requestLevelSerializer == EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION
            || (requestLevelSerializer == null
                && this.getClientBuilder()
                       .getCustomSerializer() == EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION);
        if (requestLevelSerializer == EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION
            && isContentOnWriteEnabled
            && nonIdempotentWriteRetriesEnabled
            && useTrackingIdForCreateAndReplace) {

            requestLevelSerializer = EnvelopWrappingItemSerializer.INSTANCE_WITH_TRACKING_ID_VALIDATION;
        }

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setCustomSerializer(requestLevelSerializer);
        CosmosItemResponse<T> pojoResponse = container.createItem(doc, new PartitionKey(id), requestOptions);

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

        CosmosPatchOperations patchOperations = beforePatch.apply(  doc, useEnvelopeWrapper);
        CosmosPatchItemRequestOptions patchRequestOptions = new CosmosPatchItemRequestOptions();
        if (useEnvelopeWrapper) {
            patchRequestOptions.setCustomSerializer(EnvelopWrappingItemSerializer.INSTANCE_FOR_PATCH);
        } else {
            patchRequestOptions.setCustomSerializer(requestLevelSerializer);
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
        CosmosItemRequestOptions upsertRequestOptions = new CosmosItemRequestOptions();
        if (useEnvelopeWrapper) {
            upsertRequestOptions.setCustomSerializer(EnvelopWrappingItemSerializer.INSTANCE_NO_TRACKING_ID_VALIDATION);
        } else {
            upsertRequestOptions.setCustomSerializer(requestOptions.getCustomSerializer());
        }
        pojoResponse = container.upsertItem(doc, new PartitionKey(id), upsertRequestOptions);
        if (this.isContentOnWriteEnabled) {
            assertSameDocument(doc, pojoResponse.getItem());
        } else {
            assertThat(pojoResponse.getItem()).isNull();
        }

        // TODO @fabianm - add missing test cases

        // Batch


        // Bulk

        // Query

        // Query VALUE

        // QUERY SubNode

        // Change feed
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
            TestDocument doc = new TestDocument();
            doc.id = id;
            doc.mypk = id;
            doc.someNumber = 5;
            doc.someStringArray = new String[] {id, "someString2", "someString3"};
            doc.someNumberArray = new Integer[] {1, 3, 5};
            TestChildObject child = new TestChildObject();
            child.childId = "C1_" + id;
            child.someNumber = 9;
            doc.someChildObject = child;
            doc.someChildObjectArray = new TestChildObject[] {child, child};

            return doc;
        }

        public static ObjectNode createAsObjectNode(String id) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", id);
            node.put("mypk", id);
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

    private static void assertSameDocument(Object doc, Object deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

        if (doc instanceof TestDocument) {
            assertSameDocument((TestDocument)doc, (TestDocument)deserializedDoc);
            return;
        }

        assertSameDocument((ObjectNode)doc, (ObjectNode)deserializedDoc);
    }

    private static void assertSameDocument(ObjectNode doc, ObjectNode deserializedDoc) {
        assertThat(doc).isNotNull();
        assertThat(deserializedDoc).isNotNull();

       assertSameDocument(TestDocument.parse(doc), TestDocument.parse(deserializedDoc));
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
                return CosmosItemSerializer.DEFAULT_SERIALIZER.serialize(item);
            }

            Map<String, Object> unwrappedJsonTree = CosmosItemSerializer.DEFAULT_SERIALIZER.serialize(item);

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

            TestDocumentWrappedInEnvelope envelope =
                CosmosItemSerializer.DEFAULT_SERIALIZER.deserialize(jsonNodeMap, TestDocumentWrappedInEnvelope.class);

            if (envelope == null || envelope.wrappedContent == null) {
                return null;
            }

            return CosmosItemSerializer.DEFAULT_SERIALIZER.deserialize(
                (Map<String, Object>)objectMapper.convertValue(envelope.wrappedContent, mapClass),
                classType);
        }
    }
}
