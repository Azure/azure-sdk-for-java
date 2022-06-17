// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.accesshelpers.CosmosItemResponseHelper;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
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

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCrudTest extends TestSuiteBase {

    private String databaseIdForTest = DatabaseForTest.generateId();

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private CosmosAsyncDatabase database;

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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument(String documentId) throws InterruptedException {

        InternalObjectNode properties = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(properties, new CosmosItemRequestOptions());

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
            .withId(properties.getId())
            .build();

        this.validateItemSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentTimeout(String documentId) throws InterruptedException {
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        Mono<CosmosItemResponse<InternalObjectNode>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions()).timeout(Duration.ofNanos(10));
        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();
        validateItemFailure(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument(String documentId) throws InterruptedException {

        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                                          options, InternalObjectNode.class);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(docDefinition.getId())
                .build();

        this.validateItemSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void timestamp(String documentId) throws Exception {
        Instant before = Instant.now();
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);
        Thread.sleep(1000);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        InternalObjectNode readDocument = CosmosItemResponseHelper.getInternalObjectNode(container.readItem(docDefinition.getId(),
                                                               new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                                                               options,
                                                               InternalObjectNode.class)
                                                                                .block());
        Thread.sleep(1000);
        Instant after = Instant.now();

        assertThat(readDocument.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(readDocument.getTimestamp()).isBeforeOrEqualTo(after);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument(String documentId) throws InterruptedException {
        // create a document
        InternalObjectNode docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        String newPropValue = UUID.randomUUID().toString();
        docDefinition.set("newProp", newPropValue);

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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_ReplaceDocument(String documentId) throws Throwable {

        InternalObjectNode properties = getDocumentDefinition(documentId);
        properties =
            CosmosItemResponseHelper.getInternalObjectNode(container.createItem(properties, new CosmosItemRequestOptions()).block());

        String newPropValue = UUID.randomUUID().toString();
        properties.set("newProp", newPropValue);

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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void upsertDocument_ReplaceDocumentWithPartitionKey() throws Throwable {
        TestObject item = TestObject.create();
        CosmosItemResponse<TestObject> response = container.createItem(item,  new PartitionKey(item.getMypk()), new CosmosItemRequestOptions()).block();

        item.setStringProp( UUID.randomUUID().toString());

        CosmosItemResponse<TestObject> replaceResponse = container.upsertItem(item,  new CosmosItemRequestOptions()).block();

        // Validate result
        assertThat(replaceResponse.getItem()).isEqualTo(item);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
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

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteAllItemsByPartitionKey() throws Exception {
        String pkValue1 = UUID.randomUUID().toString();
        String pkValue2 = UUID.randomUUID().toString();

        // item 1
        TestObject properties1 = TestObject.create(pkValue1);
        container.createItem(properties1).block();

        // item 2
        TestObject properties2 = TestObject.create(pkValue1);
        container.createItem(properties2).block();


        // item 3
        TestObject properties3 = TestObject.create(pkValue2);
        container.createItem(properties3).block();

        // delete the items with partition key pk1
        CosmosItemResponse<?> deleteResponse = container.deleteAllItemsByPartitionKey(
            new PartitionKey(pkValue1), new CosmosItemRequestOptions()).block();

        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);

        // verify that the items with partition key pkValue1 are deleted
        CosmosPagedFlux<TestObject> feedResponseFlux1 =
            container.readAllItems(
                new PartitionKey(pkValue1),
                new CosmosQueryRequestOptions(),
                TestObject.class);

        FeedResponseListValidator<TestObject> validator =
            new FeedResponseListValidator.Builder<TestObject>().totalSize(0).build();
        validateQuerySuccess(feedResponseFlux1.byPage(), validator, TIMEOUT);

        CosmosPagedFlux<TestObject> feedResponseFlux2 =
            container.readAllItems(
                new PartitionKey(pkValue2),
                new CosmosQueryRequestOptions(),
                TestObject.class);

        //verify that the item with the other partition Key pkValue2 is not deleted
        validator =
            new FeedResponseListValidator.Builder<TestObject>().totalSize(1).build();
        validateQuerySuccess(feedResponseFlux2.byPage(), validator, TIMEOUT);
    }

    private void compareTestObjs(TestObject newTestObject, TestObject resultObject) {
        assertThat(newTestObject.getId()).isEqualTo(resultObject.getId());
        assertThat(newTestObject.getMypk()).isEqualTo(resultObject.getMypk());
        assertThat(newTestObject.getSgmts().equals(resultObject.getSgmts())).isTrue();
        assertThat(newTestObject.getStringProp()).isEqualTo(resultObject.getStringProp());
    }


    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_DocumentCrudTest() {
        assertThat(this.client).isNull();
        this.client = this.getClientBuilder().buildAsyncClient();
        this.database = createDatabase(this.client, databaseIdForTest);
        this.container = createCollection(this.client, databaseIdForTest, getCollectionDefinition());
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(this.database);
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
