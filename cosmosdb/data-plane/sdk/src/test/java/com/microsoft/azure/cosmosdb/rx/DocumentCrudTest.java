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

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCrudTest extends TestSuiteBase {

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;
    
    @Factory(dataProvider = "clientBuildersWithDirect")
    public DocumentCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @DataProvider(name = "documentCrudArgProvider")
    public Object[][] documentCrudArgProvider() {
        return new Object[][] {
                // collection name, is name base
                {UUID.randomUUID().toString(), false } ,
                {UUID.randomUUID().toString(), true  } ,

                // with special characters in the name.
                {"+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~", true  } ,
        };
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createLargeDocument(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        docDefinition.set("largeString", StringUtils.repeat("x", size));

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentWithVeryLargePartitionKey(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        docDefinition.set("mypk", sb.toString());

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocumentWithVeryLargePartitionKey(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        docDefinition.set("mypk", sb.toString());

        Document createdDocument = TestSuiteBase.createDocument(client, createdDatabase.getId(), createdCollection.getId(), docDefinition);

        waitIfNeededForReplicasToCatchUp(clientBuilder);

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(sb.toString()));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(createdDocument, isNameBased), options);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .withProperty("mypk", sb.toString())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocument_AlreadyExists(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        client.createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false);

        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void createDocumentTimeout(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false)
                .timeout(1, TimeUnit.MILLISECONDS);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(TimeoutException.class).build();

        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        waitIfNeededForReplicasToCatchUp(clientBuilder);

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(document, isNameBased), options);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(document.getId())
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void timestamp(String documentId, boolean isNameBased) throws Exception {
        Date before = new Date();
        Document docDefinition = getDocumentDefinition(documentId);
        Thread.sleep(1000);
        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        waitIfNeededForReplicasToCatchUp(clientBuilder);

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(document, isNameBased), options);
        Document readDocument = readObservable.toBlocking().single().getResource();
        Thread.sleep(1000);
        Date after = new Date();

        assertThat(readDocument.getTimestamp()).isAfterOrEqualsTo(before);
        assertThat(readDocument.getTimestamp()).isBeforeOrEqualsTo(after);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void readDocument_DoesntExist(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        client.deleteDocument(getDocumentLink(document, isNameBased), options).toBlocking().first();

        waitIfNeededForReplicasToCatchUp(clientBuilder);

        options.setPartitionKey(new PartitionKey("looloo"));
        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(document, isNameBased), options);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(DocumentClientException.class)
                .statusCode(404).build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        Observable<ResourceResponse<Document>> deleteObservable = client.deleteDocument(getDocumentLink(document, isNameBased), options);


        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(document, isNameBased), options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_undefinedPK(String documentId, boolean isNameBased) {
        Document docDefinition = new Document();
        docDefinition.setId(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(Undefined.Value()));
        Observable<ResourceResponse<Document>> deleteObservable = client.deleteDocument(getDocumentLink(document, isNameBased), options);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);

        // attempt to read document which was deleted
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        Observable<ResourceResponse<Document>> readObservable = client.readDocument(getDocumentLink(document, isNameBased), options);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void deleteDocument_DoesntExist(String documentId, boolean isNameBased) {
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(document.get("mypk")));
        client.deleteDocument(getDocumentLink(document, isNameBased), options).toBlocking().single();

        // delete again
        Observable<ResourceResponse<Document>> deleteObservable = client.deleteDocument(getDocumentLink(document, isNameBased), options);

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument(String documentId, boolean isNameBased) {
        // create a document
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.replaceDocument(document, null);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void replaceDocument_UsingDocumentLink(String documentId, boolean isNameBased) {
        // create a document
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.replaceDocument(getDocumentLink(document, isNameBased), document, null);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_CreateDocument(String documentId, boolean isNameBased) {
        // create a document
        Document docDefinition = getDocumentDefinition(documentId);


        // replace document
        Observable<ResourceResponse<Document>> upsertObservable = client.upsertDocument(getCollectionLink(isNameBased),
                docDefinition, null, false);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId()).build();
        try {
            validateSuccess(upsertObservable, validator);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "documentCrudArgProvider")
    public void upsertDocument_ReplaceDocument(String documentId, boolean isNameBased) {
        // create a document
        Document docDefinition = getDocumentDefinition(documentId);

        Document document = client
                .createDocument(getCollectionLink(isNameBased), docDefinition, null, false).toBlocking().single().getResource();

        String newPropValue = UUID.randomUUID().toString();
        document.set("newProp", newPropValue);

        // replace document
        Observable<ResourceResponse<Document>> readObservable = client.upsertDocument
                (getCollectionLink(isNameBased), document, null, true);

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withProperty("newProp", newPropValue).build();
        try {
            validateSuccess(readObservable, validator);
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeMethod() {
        safeClose(client);
        client = clientBuilder.build();
    }
    
    private String getCollectionLink(boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() : createdCollection.getSelfLink();
    }

    private String getDocumentLink(Document doc, boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/docs/" + doc.getId() :
            "dbs/" + createdDatabase.getResourceId() + "/colls/" + createdCollection.getResourceId() + "/docs/" + doc.getResourceId();
    }

    private Document getDocumentDefinition(String documentId) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , documentId, uuid));
        return doc;
    }
}
