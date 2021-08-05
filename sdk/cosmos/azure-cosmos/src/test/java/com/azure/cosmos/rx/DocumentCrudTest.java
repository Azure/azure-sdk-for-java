// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCrudTest extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public DocumentCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "documentCrudArgProvider")
    public Object[][] documentCrudArgProvider() {
        return new Object[][] {
            // collection name, is name base
            { UUID.randomUUID().toString() },
            // with special characters in the name.
            { "+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~" },
        };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument(String documentId) throws InterruptedException {

        InternalObjectNode properties = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(properties, new CosmosItemRequestOptions());

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
            .withId(properties.getId())
            .build();

        this.validateItemSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createLargeDocument(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        BridgeInternal.setProperty(docDefinition, "largeString", StringUtils.repeat("x", size));

        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .build();

        this.validateItemSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentWithVeryLargePartitionKey(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();

        this.validateItemSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocumentWithVeryLargePartitionKey(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        createDocument(container, docDefinition);

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(sb.toString()), options,
                                                                                                InternalObjectNode.class);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();
        this.validateItemSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument_AlreadyExists(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());
        FailureValidator validator = new FailureValidator.Builder()
            .resourceAlreadyExists()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateItemFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentTimeout(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions()).timeout(Duration.ofMillis(1));
        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();
        validateItemFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument(String documentId) throws InterruptedException {

        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                container.readItem(docDefinition.getId(),
                        new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                        options, InternalObjectNode.class)
                    .flatMap(item -> {
                        CosmosDiagnostics cosmosDiagnostics = item.getDiagnostics();
                        logger.info("CosmosDiagnostics : {}", cosmosDiagnostics);
                        return Mono.just(item);
                    }).block();
            }).start();
        }
        Thread.sleep(10000);
//        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(docDefinition.getId(),
//                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
//                                                                          options, InternalObjectNode.class);
//
//        CosmosItemResponseValidator validator =
//            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
//                .withId(docDefinition.getId())
//                .build();
//
//        this.validateItemSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void timestamp(String documentId) throws Exception {
        Instant before = Instant.now();
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        Thread.sleep(1000);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        InternalObjectNode readDocument = BridgeInternal.getProperties(container.readItem(docDefinition.getId(),
                                                               new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                               options,
                                                               InternalObjectNode.class)
                                                                                .block());
        Thread.sleep(1000);
        Instant after = Instant.now();

        assertThat(readDocument.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(readDocument.getTimestamp()).isBeforeOrEqualTo(after);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument_DoesntExist(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        container.deleteItem(docDefinition.getId(), new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk"))).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                          options, InternalObjectNode.class);

        FailureValidator validator = new FailureValidator.Builder()
            .instanceOf(CosmosException.class)
            .statusCode(404)
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateItemFailure(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(documentId,
                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                          options);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(documentId,
                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                          options, InternalObjectNode.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder()
            .resourceNotFound()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateItemFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocumentUsingEntity(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        CosmosItemResponse<InternalObjectNode> documentResponse = container.createItem(docDefinition,
            new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(documentResponse.getItem(), options);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(documentId,
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
            options, InternalObjectNode.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder()
            .resourceNotFound()
            .documentClientExceptionToStringExcludesHeader(HttpConstants.HttpHeaders.AUTHORIZATION)
            .build();
        validateItemFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_undefinedPK(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = new InternalObjectNode();
        docDefinition.setId(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(documentId,
                                                                              PartitionKey.NONE,
                                                                              options);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource().build();
        this.validateItemSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(documentId,
                                                                          PartitionKey.NONE,
                                                                          options, InternalObjectNode.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateItemFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_DoesntExist(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        container.deleteItem(documentId,
                             new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                             options)
            .block();

        // delete again
        Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(documentId,
                                                                              PartitionKey.NONE,
                                                                              options);;

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateItemFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument(String documentId) throws InterruptedException {
        // create a document
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(docDefinition, "newProp", newPropValue);

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        // replace document
        Mono<CosmosItemResponse<InternalObjectNode>> replaceObservable =
            container.replaceItem(docDefinition,
                                  documentId,
                                  new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                  options);

        // validate
        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .withProperty("newProp", newPropValue)
                .build();
        this.validateItemSuccess(replaceObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_CreateDocument(String documentId) throws Throwable {
        // create a document
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);


        // replace document
        Mono<CosmosItemResponse<InternalObjectNode>> upsertObservable = container.upsertItem(docDefinition, new CosmosItemRequestOptions());

        // validate
        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
            .withId(docDefinition.getId())
            .build();

        this.validateItemSuccess(upsertObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_ReplaceDocument(String documentId) throws Throwable {

        InternalObjectNode properties = getDocumentDefinition(documentId);
        properties =
            BridgeInternal.getProperties(container.createItem(properties, new CosmosItemRequestOptions()).block());

        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);

        // Replace document

        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.upsertItem(properties, new CosmosItemRequestOptions());
        System.out.println(properties);

        // Validate result
        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withProperty("newProp", newPropValue)
                .build();

        this.validateItemSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertDocument_ReplaceDocumentWithPartitionKey() throws Throwable {
        TestObject item = TestObject.create();
        CosmosItemResponse<TestObject> response = container.createItem(item,  new PartitionKey(item.getMypk()), new CosmosItemRequestOptions()).block();

        item.setStringProp( UUID.randomUUID().toString());

        CosmosItemResponse<TestObject> replaceResponse = container.upsertItem(item,  new CosmosItemRequestOptions()).block();

        // Validate result
        assertThat(replaceResponse.getItem()).isEqualTo(item);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void typedItems() throws Throwable {
        String docId = "1234";
        String partitionKey = UUID.randomUUID().toString();

        List<List<Integer>> sgmts = new ArrayList<>();
        List<Integer> sgmt1 = new ArrayList<>();
        sgmt1.add(6519456);
        sgmt1.add(1471916863);
        sgmts.add(sgmt1);
        List<Integer> sgmt2 = new ArrayList<>();
        sgmt1.add(2498434);
        sgmt1.add(1455671440);
        sgmts.add(sgmt2);

        TestObject newTestObject = new TestObject(docId, partitionKey, sgmts, "test string");

        Mono<CosmosItemResponse<TestObject>> itemResponseMono = container.createItem(newTestObject);
        TestObject resultObject = itemResponseMono.block().getItem();
        compareTestObjs(newTestObject, resultObject);

        Mono<CosmosItemResponse<TestObject>> readResponseMono = container.readItem(newTestObject.getId(),
                                                                                        new PartitionKey(newTestObject
                                                                                                             .getMypk()),
                                                                                        TestObject.class);
        resultObject = readResponseMono.block().getItem();
        compareTestObjs(newTestObject, resultObject);

        newTestObject.setStringProp("another string");
        Mono<CosmosItemResponse<TestObject>> replaceMono = container.replaceItem(newTestObject,
                                                                                      newTestObject.getId(),
                                                                                      new PartitionKey(newTestObject
                                                                                                           .getMypk()));
        resultObject = replaceMono.block().getItem();
        compareTestObjs(newTestObject, resultObject);
    }

    private void compareTestObjs(TestObject newTestObject, TestObject resultObject) {
        assertThat(newTestObject.getId()).isEqualTo(resultObject.getId());
        assertThat(newTestObject.getMypk()).isEqualTo(resultObject.getMypk());
        assertThat(newTestObject.getSgmts().equals(resultObject.getSgmts())).isTrue();
        assertThat(newTestObject.getStringProp()).isEqualTo(resultObject.getStringProp());
    }


    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_DocumentCrudTest() {
        assertThat(this.client).isNull();
        this.client = this.getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , documentId, uuid));
        return properties;
    }
}
