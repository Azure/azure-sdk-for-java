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
package com.microsoft.azure.cosmosdb.rx;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class DocumentCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(DocumentCrudTest.class);

    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private Builder clientBuilder;
    private AsyncDocumentClient client;
    
    @Factory(dataProvider = "clientBuilders")
    public DocumentCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createLargeDocument() throws Exception {
        Document docDefinition = getDocumentDefinition();

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        docDefinition.set("largeString", StringUtils.repeat("x", size));

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument_AlreadyExists() throws Exception {
        Document docDefinition = getDocumentDefinition();

        client.createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(), docDefinition, null, false);

        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentTimeout() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(), docDefinition, null, false)
                .timeout(1, TimeUnit.MILLISECONDS);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();

        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocument() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();


        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(document.getSelfLink(), options);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(document.getId())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readDocument_DoesntExist() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        client.deleteDocument(document.getSelfLink(), options).toBlocking().first();

        options.setPartitionKey(new PartitionKey("looloo"));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(document.getSelfLink(), options);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(DocumentClientException.class)
                .statusCode(404).build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteDocument() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        Observable<ResourceResponse<Document>> deleteObservable = client.deleteDocument(document.getSelfLink(), options);


        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteDocument_DoesntExist() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        client.deleteDocument(document.getSelfLink(), options).toBlocking().single();

        // delete again
        Observable<ResourceResponse<Document>> deleteObservable = client.deleteDocument(document.getSelfLink(), options);

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceDocument() throws Exception {
        // create a document
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.replaceDocument(document, null);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceDocument_UsingDocumentLink() throws Exception {
        // create a document
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.replaceDocument(document.getSelfLink(), document, null);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertDocument_CreateDocument() throws Exception {
        // create a document
        Document docDefinition = getDocumentDefinition();


        // replace document
        Observable<ResourceResponse<Document>> upsertObservable = client.upsertDocument(getCollectionLink(), 
                docDefinition, null, false);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId()).build();
        validateSuccess(upsertObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertDocument_ReplaceDocument() throws Exception {
        // create a document
        Document docDefinition = getDocumentDefinition();

        Document document = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.upsertDocument
                (getCollectionLink(), document, null, true);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }
}
