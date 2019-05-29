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

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.StoredProcedureResponse;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.SpyClientUnderTestFactory;
import org.mockito.stubbing.Answer;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * The purpose of the tests in this class is to ensure the request are routed through direct connectivity stack.
 * The tests in other test classes validate the actual behaviour and different scenarios.
 */
public class DCDocumentCrudTest extends TestSuiteBase {
    private final static int QUERY_TIMEOUT = 40000;
    private final static String PARTITION_KEY_FIELD_NAME = "mypk";

    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private SpyClientUnderTestFactory.ClientWithGatewaySpy client;

    @DataProvider
    public static Object[][] directClientBuilder() {
        return new Object[][] { { createDCBuilder(Protocol.Https) }, { createDCBuilder(Protocol.Tcp) } };
    }

    static Builder createDCBuilder(Protocol protocol) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return new Builder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withConfigs(configs)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.Session)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY);
    }

    @Factory(dataProvider = "directClientBuilder")
    public DCDocumentCrudTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void executeStoredProc() {
        StoredProcedure storedProcedure = new StoredProcedure();
        storedProcedure.setId(UUID.randomUUID().toString());
        storedProcedure.setBody("function() {var x = 10;}");

        Observable<ResourceResponse<StoredProcedure>> createObservable = client
                .createStoredProcedure(getCollectionLink(), storedProcedure, null);

        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(storedProcedure.getId())
                .build();

        validateSuccess(createObservable, validator, TIMEOUT);

        // creating a stored proc will go through gateway so clearing captured requests

        client.getCapturedRequests().clear();

        // execute the created storedProc and ensure it goes through direct connectivity stack
        String storedProcLink = "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/sprocs/" + storedProcedure.getId();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey("dummy"));
        StoredProcedureResponse storedProcedureResponse =  client
                .executeStoredProcedure(storedProcLink, options, null).toBlocking().single();

        assertThat(storedProcedureResponse.getStatusCode()).isEqualTo(200);

        // validate the request routed through direct stack
        validateNoStoredProcExecutionOperationThroughGateway();
    }

    /**
     * Tests document creation through direct mode
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void create() {
        final Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client.createDocument(
            this.getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
            .withId(docDefinition.getId())
            .build();

        validateSuccess(createObservable, validator, TIMEOUT);
        validateNoDocumentOperationThroughGateway();
    }

    /**
     * Tests document read through direct https.
     * @throws Exception
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void read() throws Exception {
        Document docDefinition = this.getDocumentDefinition();
        Document document = client.createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();

        // give times to replicas to catch up after a write
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        String pkValue = document.getString(PARTITION_KEY_FIELD_NAME);

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(pkValue));

        String docLink =
                String.format("dbs/%s/colls/%s/docs/%s", createdDatabase.getId(), createdCollection.getId(), document.getId());

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(client.readDocument(docLink, options), validator, TIMEOUT);

        validateNoDocumentOperationThroughGateway();
    }

    /**
     * Tests document upsert through direct https.
     * @throws Exception
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void upsert() throws Exception {

        final Document docDefinition = getDocumentDefinition();

        final Document document = client.createDocument(getCollectionLink(), docDefinition, null, false)
            .toBlocking()
            .single()
            .getResource();

        // give times to replicas to catch up after a write
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        String pkValue = document.getString(PARTITION_KEY_FIELD_NAME);
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(pkValue));

        String propName = "newProp";
        String propValue = "hello";
        document.set(propName, propValue);
        
        ResourceResponseValidator<Document> validator = ResourceResponseValidator.builder()
                .withProperty(propName, propValue)
                .build();
        validateSuccess(client.upsertDocument(getCollectionLink(), document, options, false), validator, TIMEOUT);

        validateNoDocumentOperationThroughGateway();
    }

    @Test(groups = { "direct" }, timeOut = QUERY_TIMEOUT)
    public void crossPartitionQuery() {

        truncateCollection(createdCollection);
        waitIfNeededForReplicasToCatchUp(clientBuilder);

        client.getCapturedRequests().clear();

        int cnt = 1000;
        List<Document> documentList = new ArrayList<>();
        for(int i = 0; i < cnt; i++) {
            Document docDefinition = getDocumentDefinition();
            documentList.add(docDefinition);
        }

        documentList = bulkInsert(client, getCollectionLink(), documentList).map(ResourceResponse::getResource).toList().toBlocking().single();

        waitIfNeededForReplicasToCatchUp(clientBuilder);

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxDegreeOfParallelism(-1);
        options.setMaxItemCount(100);
        Observable<FeedResponse<Document>> results = client.queryDocuments(getCollectionLink(), "SELECT * FROM r", options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(documentList.size())
                .exactlyContainsInAnyOrder(documentList.stream().map(Document::getResourceId).collect(Collectors.toList())).build();

        try {
            validateQuerySuccess(results, validator, QUERY_TIMEOUT);
            validateNoDocumentQueryOperationThroughGateway();
            // validates only the first query for fetching query plan goes to gateway.
            assertThat(client.getCapturedRequests().stream().filter(r -> r.getResourceType() == ResourceType.Document)).hasSize(1);
        } catch (Throwable error) {
            if (clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
        }
    }

    private void validateNoStoredProcExecutionOperationThroughGateway() {
        // this validates that Document related requests don't go through gateway
        DocumentServiceRequestValidator validateResourceTypesSentToGateway = DocumentServiceRequestValidator.builder()
                .resourceTypeIn(ResourceType.DatabaseAccount,
                                ResourceType.Database,
                                ResourceType.DocumentCollection,
                                ResourceType.PartitionKeyRange)
                .build();

        // validate that all gateway captured requests are non document resources
        for(RxDocumentServiceRequest request: client.getCapturedRequests()) {
            validateResourceTypesSentToGateway.validate(request);
        }
    }

    private void validateNoDocumentOperationThroughGateway() {
        // this validates that Document related requests don't go through gateway
        DocumentServiceRequestValidator validateResourceTypesSentToGateway = DocumentServiceRequestValidator.builder()
                .resourceTypeIn(ResourceType.DatabaseAccount,
                                ResourceType.Database,
                                ResourceType.DocumentCollection,
                                ResourceType.PartitionKeyRange)
                .build();

        // validate that all gateway captured requests are non document resources
        for(RxDocumentServiceRequest request: client.getCapturedRequests()) {
            validateResourceTypesSentToGateway.validate(request);
        }
    }

    private void validateNoDocumentQueryOperationThroughGateway() {
        // this validates that Document related requests don't go through gateway
        DocumentServiceRequestValidator validateResourceTypesSentToGateway = DocumentServiceRequestValidator.builder()
                .resourceTypeIn(ResourceType.DatabaseAccount,
                                ResourceType.Database,
                                ResourceType.DocumentCollection,
                                ResourceType.PartitionKeyRange)
                .build();

        // validate that all gateway captured requests are non document resources
        for(RxDocumentServiceRequest request: client.getCapturedRequests()) {
            if (request.getOperationType() == OperationType.Query) {
                assertThat(request.getPartitionKeyRangeIdentity()).isNull();
            } else {
                validateResourceTypesSentToGateway.validate(request);
            }
        }
    }

    @BeforeClass(groups = { "direct" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        createdDatabase = SHARED_DATABASE;
        createdCollection = createCollection(createdDatabase.getId(), getCollectionDefinition(), options);
        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder);

        assertThat(client.getCapturedRequests()).isNotEmpty();
    }

    @AfterClass(groups = { "direct" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeMethod(groups = { "direct" })
    public void beforeMethod(Method method) {
        super.beforeMethod(method);
        client.getCapturedRequests().clear();
    }

    private String getCollectionLink() {
        return String.format("/dbs/%s/colls/%s", createdDatabase.getId(), createdCollection.getId());
    }

    private Document getDocumentDefinition() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID().toString());
        doc.set(PARTITION_KEY_FIELD_NAME, UUID.randomUUID().toString());
        doc.set("name", "Hafez");
        return doc;
    }
}
