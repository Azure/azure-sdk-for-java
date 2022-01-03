// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.DocumentServiceRequestValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceResponseValidator;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SpyClientUnderTestFactory;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return new Object[][] { { createDCBuilder(Protocol.TCP) } };
    }

    static Builder createDCBuilder(Protocol protocol) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();

        return new Builder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withConfigs(configs)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY);
    }

    @Factory(dataProvider = "directClientBuilder")
    public DCDocumentCrudTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void executeStoredProc() {
        StoredProcedure storedProcedure = new StoredProcedure();
        storedProcedure.setId(UUID.randomUUID().toString());
        storedProcedure.setBody("function() {var x = 10;}");

        Mono<ResourceResponse<StoredProcedure>> createObservable = client
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
                .executeStoredProcedure(storedProcLink, options, null).block();

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

        Mono<ResourceResponse<Document>> createObservable = client.createDocument(
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
        Document document = client.createDocument(getCollectionLink(), docDefinition, null, false).block().getResource();

        // give times to replicas to catch up after a write
        waitIfNeededForReplicasToCatchUp(clientBuilder());

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

        ResourceResponseValidator<Document> validator = ResourceResponseValidator.<Document>builder()
            .withProperty(propName, propValue)
            .build();
        validateSuccess(client.upsertDocument(getCollectionLink(), document, options, false), validator, TIMEOUT);

        validateNoDocumentOperationThroughGateway();
    }

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

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(-1);
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 100);

        Flux<FeedResponse<Document>> results = client.queryDocuments(getCollectionLink(), "SELECT * FROM r", options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .totalSize(documentList.size())
                .exactlyContainsInAnyOrder(documentList.stream().map(Document::getResourceId).collect(Collectors.toList())).build();

        validateQuerySuccess(results, validator, QUERY_TIMEOUT);
        validateNoDocumentQueryOperationThroughGateway();
        // validates only the first query for fetching query plan goes to gateway.
        assertThat(client.getCapturedRequests().stream().filter(r -> r.getResourceType() == ResourceType.Document)).hasSize(1);
    }

    private void validateNoStoredProcExecutionOperationThroughGateway() {
        // this validates that Document related requests don't go through gateway
        DocumentServiceRequestValidator<RxDocumentServiceRequest> validateResourceTypesSentToGateway
            = DocumentServiceRequestValidator.<RxDocumentServiceRequest>builder()
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
        DocumentServiceRequestValidator<RxDocumentServiceRequest> validateResourceTypesSentToGateway
            = DocumentServiceRequestValidator.<RxDocumentServiceRequest>builder()
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
        DocumentServiceRequestValidator<RxDocumentServiceRequest> validateResourceTypesSentToGateway
            = DocumentServiceRequestValidator.builder()
            .resourceTypeIn(ResourceType.DatabaseAccount,
                ResourceType.Database,
                ResourceType.DocumentCollection,
                ResourceType.PartitionKeyRange)
                .build();

        // validate that all gateway captured requests are non document resources
        for(RxDocumentServiceRequest request: client.getCapturedRequests()) {
            if (request.getOperationType() == OperationType.Query
                    || request.getOperationType() == OperationType.QueryPlan) {
                assertThat(request.getPartitionKeyRangeIdentity()).isNull();
            } else {
                validateResourceTypesSentToGateway.validate(request);
            }
        }
    }

    @BeforeClass(groups = { "direct" }, timeOut = SETUP_TIMEOUT)
    public void before_DCDocumentCrudTest() {

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        createdDatabase = SHARED_DATABASE;
        createdCollection = createCollection(createdDatabase.getId(), getCollectionDefinition(), options);
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
        return String.format("/dbs/%s/colls/%s", createdDatabase.getId(), createdCollection.getId());
    }

    private Document getDocumentDefinition() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, PARTITION_KEY_FIELD_NAME, UUID.randomUUID().toString());
        BridgeInternal.setProperty(doc, "name", "Hafez");
        return doc;
    }

}
