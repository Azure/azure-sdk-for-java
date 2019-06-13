/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.directconnectivity.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    private CosmosContainer createdCollection;

    private CosmosClient client;
    
    @Factory(dataProvider = "clientBuildersWithDirect")
    public DocumentCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "documentCrudArgProvider")
    public Object[][] documentCrudArgProvider() {
        return new Object[][] {
                // collection name, is name base
                {UUID.randomUUID().toString()} ,

                // with special characters in the name.
                {"+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~"} ,
        };
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createLargeDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        docDefinition.set("largeString", StringUtils.repeat("x", size));

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id())
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
        docDefinition.set("mypk", sb.toString());

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocumentWithVeryLargePartitionKey(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        docDefinition.set("mypk", sb.toString());

        CosmosItem createdDocument = TestSuiteBase.createDocument(createdCollection, docDefinition);

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(sb.toString()));
        Mono<CosmosItemResponse> readObservable = createdDocument.read(options);

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument_AlreadyExists(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block();

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentTimeout(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions())
                .timeout(Duration.ofMillis(1));

        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();

        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosItemResponse> readObservable = document.read(options);

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(document.id())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void timestamp(String documentId) throws Exception {
        OffsetDateTime before = OffsetDateTime.now();
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);
        Thread.sleep(1000);
        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        CosmosItemProperties readDocument = document.read(options).block().properties();
        Thread.sleep(1000);
        OffsetDateTime after = OffsetDateTime.now();

        assertThat(readDocument.timestamp()).isAfterOrEqualTo(before);
        assertThat(readDocument.timestamp()).isBeforeOrEqualTo(after);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument_DoesntExist(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        document.delete(options).block();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        options.partitionKey(new PartitionKey("looloo"));
        Mono<CosmosItemResponse> readObservable = document.read(options);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(CosmosClientException.class)
                .statusCode(404).build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        Mono<CosmosItemResponse> deleteObservable = document.delete(options);


        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosItemResponse> readObservable = document.read(options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_undefinedPK(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = new CosmosItemProperties();
        docDefinition.id(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(PartitionKey.None);
        Mono<CosmosItemResponse> deleteObservable = document.delete(options);

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        Mono<CosmosItemResponse> readObservable = document.read(options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_DoesntExist(String documentId) throws InterruptedException {
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        document.delete(options).block();

        // delete again
        Mono<CosmosItemResponse> deleteObservable = document.delete(options);

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument(String documentId) throws InterruptedException {
        // create a document
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        CosmosItem document = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().item();

        String newPropValue = UUID.randomUUID().toString();
        docDefinition.set("newProp", newPropValue);

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(docDefinition.get("mypk")));
        // replace document
        Mono<CosmosItemResponse> replaceObservable = document.replace(docDefinition, options);
        
        // validate
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(replaceObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_CreateDocument(String documentId) throws Throwable {
        // create a document
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);


        // replace document
        Mono<CosmosItemResponse> upsertObservable = createdCollection.upsertItem(docDefinition, new CosmosItemRequestOptions());

        // validate
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id()).build();

        validateSuccess(upsertObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 100, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_ReplaceDocument(String documentId) throws Throwable {
        // create a document
        CosmosItemProperties docDefinition = getDocumentDefinition(documentId);

        docDefinition = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions()).block().properties();

        String newPropValue = UUID.randomUUID().toString();
        docDefinition.set("newProp", newPropValue);

        // replace document
        Mono<CosmosItemResponse> readObservable = createdCollection.upsertItem(docDefinition, new CosmosItemRequestOptions());
        System.out.println(docDefinition);

        // validate
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeMethod() {
        safeClose(client);
        client = clientBuilder().build();
    }
    
    private CosmosItemProperties getDocumentDefinition(String documentId) {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , documentId, uuid));
        return doc;
    }
}