// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.implementation.FailureValidator;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
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

        CosmosItemProperties properties = getDocumentDefinition(documentId);
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = container.createItem(properties, new CosmosItemRequestOptions());
        
        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
            .withId(properties.getId())
            .build();

        validateSuccess(createObservable, validator);
    }

    // TODO (DANOBLE) DocumentCrudTest::createLargeDocument fails in some  environments
    //  see https://github.com/Azure/azure-sdk-for-java/issues/6335
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createLargeDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        BridgeInternal.setProperty(docDefinition, "largeString", StringUtils.repeat("x", size));

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentWithVeryLargePartitionKey(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(createObservable, validator);
    }

    // TODO (DANOBLE) DocumentCrudTest::readDocumentWithVeryLargePartitionKey test fails in some environments
    //  see https://github.com/Azure/azure-sdk-for-java/issues/6336
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocumentWithVeryLargePartitionKey(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        createDocument(container, docDefinition);

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(sb.toString()), options,
                                                                                                CosmosItemProperties.class);

        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument_AlreadyExists(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions());
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentTimeout(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createObservable = container.createItem(docDefinition, new CosmosItemRequestOptions()).timeout(Duration.ofMillis(1));
        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument(String documentId) throws InterruptedException {

        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(docDefinition.get("mypk")), 
                                                                          options, CosmosItemProperties.class);

        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void timestamp(String documentId) throws Exception {
        OffsetDateTime before = OffsetDateTime.now();
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        Thread.sleep(1000);
        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        CosmosItemProperties readDocument = container.readItem(docDefinition.getId(),
                                                               new PartitionKey(docDefinition.get("mypk")),
                                                               options,
                                                               CosmosItemProperties.class)
                                                .block()
                                                .getProperties();
        Thread.sleep(1000);
        OffsetDateTime after = OffsetDateTime.now();

        assertThat(readDocument.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(readDocument.getTimestamp()).isBeforeOrEqualTo(after);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument_DoesntExist(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        container.deleteItem(docDefinition.getId(), new PartitionKey(docDefinition.get("mypk"))).block();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.readItem(docDefinition.getId(),
                                                                          new PartitionKey(docDefinition.get("mypk")),
                                                                          options, CosmosItemProperties.class);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(CosmosClientException.class)
                .statusCode(404).build();
        validateFailure(readObservable, validator);
    }

    // TODO (DANOBLE) DocumentCrudTest::deleteDocument test fails in some test environments
    //  see https://github.com/Azure/azure-sdk-for-java/issues/6337
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosAsyncItemResponse> deleteObservable = container.deleteItem(documentId,
                                                                          new PartitionKey(docDefinition.get("mypk")), 
                                                                          options);


        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.readItem(documentId,
                                                                          new PartitionKey(docDefinition.get("mypk")), 
                                                                          options, CosmosItemProperties.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_undefinedPK(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = new CosmosItemProperties();
        docDefinition.setId(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        Mono<CosmosAsyncItemResponse> deleteObservable = container.deleteItem(documentId,
                                                                              PartitionKey.NONE,
                                                                              options);

        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.readItem(documentId,
                                                                          PartitionKey.NONE,
                                                                          options, CosmosItemProperties.class);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_DoesntExist(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        container.deleteItem(documentId,
                             new PartitionKey(docDefinition.get("mypk")),
                             options)
            .block();

        // delete again
        Mono<CosmosAsyncItemResponse> deleteObservable = container.deleteItem(documentId,
                                                                              PartitionKey.NONE,
                                                                              options);;

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument(String documentId) throws InterruptedException {
        // create a document
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        container.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(docDefinition, "newProp", newPropValue);

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        // replace document
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> replaceObservable =
            container.replaceItem(docDefinition,
                                  documentId,
                                  new PartitionKey(docDefinition.get("mypk")),
                                  options);

        // validate
        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(replaceObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_CreateDocument(String documentId) throws Throwable {
        // create a document
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);


        // replace document
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> upsertObservable = container.upsertItem(docDefinition, new CosmosItemRequestOptions());

        // validate
        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(docDefinition.getId()).build();

        validateSuccess(upsertObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_ReplaceDocument(String documentId) throws Throwable {

        CosmosItemProperties properties = getDocumentDefinition(documentId);
        properties = container.createItem(properties, new CosmosItemRequestOptions()).block().getProperties();

        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);

        // Replace document

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readObservable = container.upsertItem(properties, new CosmosItemRequestOptions());
        System.out.println(properties);

        // Validate result

        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
            .withProperty("newProp", newPropValue).build();

        validateSuccess(readObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_DocumentCrudTest() {
        assertThat(this.client).isNull();
        this.client = this.clientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private CosmosItemProperties getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final CosmosItemProperties properties = new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , documentId, uuid));
        return properties;
    }
}
