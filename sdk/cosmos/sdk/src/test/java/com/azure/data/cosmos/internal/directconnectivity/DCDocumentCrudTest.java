// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.AsyncDocumentClient.Builder;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.StoredProcedure;
import com.azure.data.cosmos.internal.StoredProcedureResponse;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.SpyClientUnderTestFactory;
import com.azure.data.cosmos.internal.TestSuiteBase;
import com.azure.data.cosmos.internal.DocumentServiceRequestValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.ResourceResponseValidator;
import com.azure.data.cosmos.internal.TestConfigurations;
import org.mockito.stubbing.Answer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import reactor.core.publisher.Flux;

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
        return new Object[][] { { createDCBuilder(Protocol.HTTPS) }, { createDCBuilder(Protocol.TCP) } };
    }

    static Builder createDCBuilder(Protocol protocol) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return new Builder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withConfigs(configs)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY);
    }

    @Factory(dataProvider = "directClientBuilder")
    public DCDocumentCrudTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void executeStoredProc() {
        StoredProcedure storedProcedure = new StoredProcedure();
        storedProcedure.id(UUID.randomUUID().toString());
        storedProcedure.setBody("function() {var x = 10;}");

        Flux<ResourceResponse<StoredProcedure>> createObservable = client
                .createStoredProcedure(getCollectionLink(), storedProcedure, null);

        ResourceResponseValidator<StoredProcedure> validator = new ResourceResponseValidator.Builder<StoredProcedure>()
                .withId(storedProcedure.id())
                .build();

        validateSuccess(createObservable, validator, TIMEOUT);

        // creating a stored proc will go through gateway so clearing captured requests

        client.getCapturedRequests().clear();

        // execute the created storedProc and ensure it goes through direct connectivity stack
        String storedProcLink = "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id() + "/sprocs/" + storedProcedure.id();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey("dummy"));
        StoredProcedureResponse storedProcedureResponse =  client
                .executeStoredProcedure(storedProcLink, options, null).single().block();

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

        Flux<ResourceResponse<Document>> createObservable = client.createDocument(
            this.getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
            .withId(docDefinition.id())
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
        Document document = client.createDocument(getCollectionLink(), docDefinition, null, false).single().block().getResource();

        // give times to replicas to catch up after a write
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        String pkValue = document.getString(PARTITION_KEY_FIELD_NAME);

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(pkValue));

        String docLink =
                String.format("dbs/%s/colls/%s/docs/%s", createdDatabase.id(), createdCollection.id(), document.id());

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.id())
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
            .single()
            .block()
            .getResource();

        // give times to replicas to catch up after a write
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        String pkValue = document.getString(PARTITION_KEY_FIELD_NAME);
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(pkValue));

        String propName = "newProp";
        String propValue = "hello";
        BridgeInternal.setProperty(document, propName, propValue);
        
        ResourceResponseValidator<Document> validator = ResourceResponseValidator.builder()
                .withProperty(propName, propValue)
                .build();
        validateSuccess(client.upsertDocument(getCollectionLink(), document, options, false), validator, TIMEOUT);

        validateNoDocumentOperationThroughGateway();
    }

    //FIXME: Times out
    @Ignore
    @Test(groups = { "direct" }, timeOut = QUERY_TIMEOUT)
    public void crossPartitionQuery() {

        truncateCollection(createdCollection);
        waitIfNeededForReplicasToCatchUp(clientBuilder());

        client.getCapturedRequests().clear();

        int cnt = 1000;
        List<Document> documentList = new ArrayList<>();
        for(int i = 0; i < cnt; i++) {
            Document docDefinition = getDocumentDefinition();
            documentList.add(docDefinition);
        }

        documentList = bulkInsert(client, getCollectionLink(), documentList).map(ResourceResponse::getResource).collectList().single().block();

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxDegreeOfParallelism(-1);
        options.maxItemCount(100);
        Flux<FeedResponse<Document>> results = client.queryDocuments(getCollectionLink(), "SELECT * FROM r", options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(documentList.size())
                .exactlyContainsInAnyOrder(documentList.stream().map(Document::resourceId).collect(Collectors.toList())).build();

        validateQuerySuccess(results, validator, QUERY_TIMEOUT);
        validateNoDocumentQueryOperationThroughGateway();
        // validates only the first query for fetching query plan goes to gateway.
        assertThat(client.getCapturedRequests().stream().filter(r -> r.getResourceType() == ResourceType.Document)).hasSize(1);
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
        createdCollection = createCollection(createdDatabase.id(), getCollectionDefinition(), options);
        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder());

        assertThat(client.getCapturedRequests()).isNotEmpty();
    }

    @AfterClass(groups = { "direct" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeMethod(groups = { "direct" })
    public void beforeMethod(Method method) {
        client.getCapturedRequests().clear();
    }

    private String getCollectionLink() {
        return String.format("/dbs/%s/colls/%s", createdDatabase.id(), createdCollection.id());
    }

    private Document getDocumentDefinition() {
        Document doc = new Document();
        doc.id(UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, PARTITION_KEY_FIELD_NAME, UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, "name", "Hafez");
        return doc;
    }
    
}
