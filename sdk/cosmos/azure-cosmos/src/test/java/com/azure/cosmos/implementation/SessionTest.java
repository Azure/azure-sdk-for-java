// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.batch.SinglePartitionKeyServerBatchRequest;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest extends TestSuiteBase {
    protected static final int TIMEOUT = 20000;

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private String collectionId = "+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~";
    private SpyClientUnderTestFactory.SpyBaseClass<HttpRequest> spyClient;
    private AsyncDocumentClient houseKeepingClient;
    private ConnectionMode connectionMode;
    private RequestOptions options;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public SessionTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "sessionTestArgProvider")
    public Object[] sessionTestArgProvider() {
        return new Object[] {
                // boolean indicating whether requests should be name based or not
                true,
                false
        };
    }

    @BeforeClass(groups = { "simple", "multi-master" }, timeOut = SETUP_TIMEOUT)
    public void before_SessionTest() {
        createdDatabase = SHARED_DATABASE;

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collection = new DocumentCollection();
        collection.setId(collectionId);
        collection.setPartitionKey(partitionKeyDef);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setOfferThroughput(20000); //Making sure we have 4 physical partitions

        createdCollection = createCollection(createGatewayHouseKeepingDocumentClient().build(), createdDatabase.getId(),
                collection, requestOptions);
        houseKeepingClient = clientBuilder().build();
        connectionMode = houseKeepingClient.getConnectionPolicy().getConnectionMode();

        if (connectionMode == ConnectionMode.DIRECT) {
            spyClient = SpyClientUnderTestFactory.createDirectHttpsClientUnderTest(clientBuilder());
        } else {
            // Gateway builder has multipleWriteRegionsEnabled false by default, enabling it for multi master test
            ConnectionPolicy connectionPolicy = clientBuilder().connectionPolicy;
            connectionPolicy.setMultipleWriteRegionsEnabled(true);
            spyClient = SpyClientUnderTestFactory.createClientUnderTest(clientBuilder().withConnectionPolicy(connectionPolicy));
        }
        options = new RequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
    }

    @AfterClass(groups = { "simple", "multi-master" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(houseKeepingClient, createdCollection);
        safeClose(houseKeepingClient);
        safeClose(spyClient);
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeTest(Method method) {
        spyClient.clearCapturedRequests();
    }

    private List<String> getSessionTokensInRequests() {
        return spyClient.getCapturedRequests().stream()
            .map(r -> r.headers()
                .value(HttpConstants.HttpHeaders.SESSION_TOKEN))
            .filter(s -> s != null)
            .distinct()
            .collect(Collectors.toList());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionConsistency_ReadYourWrites(boolean isNameBased) {
        spyClient.readCollection(getCollectionLink(isNameBased), null).block();
        spyClient.createDocument(getCollectionLink(isNameBased), newDocument(), null, false).block();

        spyClient.clearCapturedRequests();

        for (int i = 0; i < 10; i++) {
            Document documentCreated = spyClient.createDocument(getCollectionLink(isNameBased), newDocument(), null, false)
                    .block().getResource();

            spyClient.clearCapturedRequests();

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).block();

            assertThat(getSessionTokensInRequests()).hasSize(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).block();

            // same session token expected - because we collect
            // distinct session tokens only one of them should be kept
            assertThat(getSessionTokensInRequests()).hasSize(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void partitionedSessionToken(boolean isNameBased) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        spyClient.readCollection(getCollectionLink(isNameBased), null).block();
        spyClient.createDocument(getCollectionLink(isNameBased), newDocument(), null, false).block();

        spyClient.clearCapturedRequests();
        Document documentCreated = null;
        RequestOptions requestOptions = new RequestOptions();
        for (int i = 0; i < 10; i++) {
            Document document = newDocument();
            document.set("mypk", document.getId());
            requestOptions.setPartitionKey(new PartitionKey(document.getId()));
            documentCreated = spyClient.createDocument(getCollectionLink(isNameBased), document, requestOptions, false)
                .block().getResource();
        }
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(spyClient);
        if(!globalEndpointManager.getLatestDatabaseAccount().getEnableMultipleWriteLocations()) {
            // No session token set for write call
            assertThat(getSessionTokensInRequests()).hasSize(0);
        } else {
            assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
            assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token
        }
        spyClient.clearCapturedRequests();

        // Session token set for default session consistency
        spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token set for request session consistency
        spyClient.clearCapturedRequests();
        requestOptions.setConsistencyLevel(ConsistencyLevel.SESSION);
        spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
        assertThat(getSessionTokensInRequests()).hasSize(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for single partition query
        spyClient.clearCapturedRequests();
        String query = "select * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(new PartitionKey(documentCreated.getId()));
        spyClient.queryDocuments(getCollectionLink(isNameBased), query, queryRequestOptions, Document.class).blockFirst();
        assertThat(getSessionTokensInRequests()).hasSize(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for cross partition query
        spyClient.clearCapturedRequests();
        queryRequestOptions = new CosmosQueryRequestOptions();
        spyClient.queryDocuments(getCollectionLink(isNameBased), query, queryRequestOptions, Document.class).blockFirst();
        assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for feed ranges query
        spyClient.clearCapturedRequests();
        List<FeedRange> feedRanges = spyClient.getFeedRanges(getCollectionLink(isNameBased)).block();
        queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setFeedRange(feedRanges.get(0));
        spyClient.queryDocuments(getCollectionLink(isNameBased), query, queryRequestOptions, Document.class).blockFirst();
        assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for readAll with partition query
        spyClient.clearCapturedRequests();
        queryRequestOptions = new CosmosQueryRequestOptions();
        spyClient.readAllDocuments(
            getCollectionLink(isNameBased),
            new PartitionKey(documentCreated.getId()),
            queryRequestOptions,
            Document.class).blockFirst();
        assertThat(getSessionTokensInRequests().size()).isEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for readAll with cross partition
        spyClient.clearCapturedRequests();
        queryRequestOptions = new CosmosQueryRequestOptions();
        spyClient.readDocuments(getCollectionLink(isNameBased), queryRequestOptions, Document.class).blockFirst();
        assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token

        // Session token validation for readMany with cross partition
        spyClient.clearCapturedRequests();
        queryRequestOptions = new CosmosQueryRequestOptions();
        CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(new PartitionKey(documentCreated.getId()), documentCreated.getId());
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        cosmosItemIdentities.add(cosmosItemIdentity);
        spyClient.readMany(cosmosItemIdentities, getCollectionLink(isNameBased), queryRequestOptions, Document.class).block();
        assertThat(getSessionTokensInRequests().size()).isEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token
        // session token

        // Session token validation for create in Batch
        if(isNameBased) { // Batch only work with name based url
            spyClient.clearCapturedRequests();
            Document document = newDocument();
            document.set("mypk", document.getId());
            ItemBatchOperation<Document> itemBatchOperation = new ItemBatchOperation<Document>(CosmosItemOperationType.CREATE,
                documentCreated.getId(), new PartitionKey(documentCreated.getId()), new RequestOptions(), document);
            List<ItemBatchOperation<Document>> itemBatchOperations = new ArrayList<>();
            itemBatchOperations.add(itemBatchOperation);

            Method method = SinglePartitionKeyServerBatchRequest.class.getDeclaredMethod("createBatchRequest",
                PartitionKey.class,
                List.class);
            method.setAccessible(true);
            SinglePartitionKeyServerBatchRequest serverBatchRequest =
                (SinglePartitionKeyServerBatchRequest) method.invoke(SinglePartitionKeyServerBatchRequest.class, new PartitionKey(document.getId()),
                    itemBatchOperations);
            spyClient.executeBatchRequest(getCollectionLink(isNameBased), serverBatchRequest,
                new RequestOptions(), false).block();
            assertThat(getSessionTokensInRequests().size()).isEqualTo(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
            assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenNotRequired(boolean isNameBased) {
        spyClient.readCollection(getCollectionLink(isNameBased), null).block();
        // No session token set for the master resource related request
        assertThat(getSessionTokensInRequests()).hasSize(0);
        spyClient.createDocument(getCollectionLink(isNameBased), newDocument(), null, false).block();

        spyClient.clearCapturedRequests();
        Document documentCreated = null;
        RequestOptions requestOptions = new RequestOptions();
        for (int i = 0; i < 10; i++) {
            Document document = newDocument();
            document.set("mypk", document.getId());
            requestOptions.setPartitionKey(new PartitionKey(document.getId()));
            documentCreated = spyClient.createDocument(getCollectionLink(isNameBased), document, requestOptions, false)
                .block().getResource();
        }
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(spyClient);
        if(!globalEndpointManager.getLatestDatabaseAccount().getEnableMultipleWriteLocations()) {
            // No session token set for write call
            assertThat(getSessionTokensInRequests()).hasSize(0);
        } else {
            assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
            assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token
        }
        spyClient.clearCapturedRequests();

        // No session token set for EVENTUAL consistency
        spyClient.clearCapturedRequests();
        requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
        spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
        assertThat(getSessionTokensInRequests()).hasSize(0);

        // No session token set for CONSISTENT_PREFIX consistency
        spyClient.clearCapturedRequests();
        requestOptions.setConsistencyLevel(ConsistencyLevel.CONSISTENT_PREFIX);
        spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
        assertThat(getSessionTokensInRequests()).hasSize(0);

        if (globalEndpointManager.getLatestDatabaseAccount().getConsistencyPolicy().getDefaultConsistencyLevel().equals(ConsistencyLevel.STRONG) ||
            globalEndpointManager.getLatestDatabaseAccount().getConsistencyPolicy().getDefaultConsistencyLevel().equals(ConsistencyLevel.BOUNDED_STALENESS)) {
            // No session token set for BOUNDED_STALENESS consistency
            spyClient.clearCapturedRequests();
            requestOptions.setConsistencyLevel(ConsistencyLevel.BOUNDED_STALENESS);
            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
            assertThat(getSessionTokensInRequests()).hasSize(0);
        }

        if (globalEndpointManager.getLatestDatabaseAccount().getConsistencyPolicy().getDefaultConsistencyLevel().equals(ConsistencyLevel.STRONG)) {
            // No session token set for STRONG consistency
            spyClient.clearCapturedRequests();
            requestOptions.setConsistencyLevel(ConsistencyLevel.STRONG);
            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), requestOptions).block();
            assertThat(getSessionTokensInRequests()).hasSize(0);
        }
    }

    @Test(groups = { "multi-master" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenForCreateOnMultiMaster(boolean isNameBased) {
        spyClient.readCollection(getCollectionLink(isNameBased), null).block();
        spyClient.createDocument(getCollectionLink(isNameBased), newDocument(), null, false).block();

        spyClient.clearCapturedRequests();
        RequestOptions requestOptions = new RequestOptions();
        for (int i = 0; i < 10; i++) {
            Document document = newDocument();
            document.set("mypk", document.getId());
            requestOptions.setPartitionKey(new PartitionKey(document.getId()));
            spyClient.createDocument(getCollectionLink(isNameBased), document, requestOptions, false)
                .block().getResource();
        }
        // Session token validation set for create request
        assertThat(getSessionTokensInRequests().size()).isGreaterThanOrEqualTo(1);
        assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();
        assertThat(getSessionTokensInRequests().get(0)).doesNotContain(","); // making sure we have only one scope session token
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenInDocumentRead(boolean isNameBased) throws UnsupportedEncodingException {
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        BridgeInternal.setProperty(document, "pk", "pk");
        document = spyClient.createDocument(getCollectionLink(isNameBased), document, null, false)
                .block()
                .getResource();

        final String documentLink = getDocumentLink(document, isNameBased);
        spyClient.readDocument(documentLink, options).block()
                .getResource();

        List<HttpRequest> documentReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.httpMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.uri().toString().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(documentLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                }).collect(Collectors.toList());

        // DIRECT mode may make more than one call (multiple replicas)
        assertThat(documentReadHttpRequests.size() >= 1).isTrue();
        assertThat(documentReadHttpRequests.get(0).headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNotEmpty();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenRemovedForMasterResource(boolean isNameBased) throws UnsupportedEncodingException {
        if (connectionMode == ConnectionMode.DIRECT) {
            throw new SkipException("Master resource access is only through gateway");
        }
        String collectionLink = getCollectionLink(isNameBased);
        spyClient.readCollection(collectionLink, null).block();

        List<HttpRequest> collectionReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.httpMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.uri().toString().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(collectionLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        assertThat(collectionReadHttpRequests).hasSize(1);
        assertThat(collectionReadHttpRequests.get(0).headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
    }

    private String getCollectionLink(boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId():
            createdCollection.getSelfLink();
    }

    private String getDocumentLink(Document doc, boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/docs/" + doc.getId() :
            "dbs/" + createdDatabase.getResourceId() + "/colls/" + createdCollection.getResourceId() + "/docs/" + doc.getResourceId() + "/";
    }

    private Document newDocument() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID().toString());

        return doc;
    }
}
