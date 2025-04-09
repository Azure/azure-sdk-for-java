// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.FabricTestSuiteBase;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.METHOD;
import static org.assertj.core.api.Assertions.assertThat;

public class FabricChangeFeedTest extends FabricTestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;
    private static final String PartitionKeyFieldName = "mypk";
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdContainer;
    private Multimap<String, Document> partitionKeyToDocuments = ArrayListMultimap.create();
    private static final String CONTAINER_ID = "fabric-java-test-container-" + UUID.randomUUID();

    private CosmosClient client;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdContainer.getId());
    }

    static protected CosmosContainerProperties getCollectionDefinition(boolean enableFullFidelity) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/" + PartitionKeyFieldName);
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(),
            partitionKeyDef);

        if (enableFullFidelity) {
            cosmosContainerProperties.setChangeFeedPolicy(
                ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofMinutes(10)));
        }

        return cosmosContainerProperties;
    }

    public FabricChangeFeedTest() {
        super(createGatewayForFabric());
        subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "startFromParamProvider")
    public Object[] startFromParamProvider() {
        return new Object[]{
            false,
            true
        };
    }

    @BeforeClass(groups = { "fabric-test" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        this.createdDatabase = this.client.getDatabase(DATABASE_ID);
        this.createdDatabase.createContainerIfNotExists(CONTAINER_ID, "/mypk",
            ThroughputProperties.createManualThroughput(16000));
        this.createdContainer = this.createdDatabase.getContainer(CONTAINER_ID);
    }

    @AfterClass(groups = { "fabric-test" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        deleteCollection(this.createdContainer);
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fabric-test" })
    public void testChangeFeed_fromBeginning() {
        CosmosPagedIterable<Document> documents = createdContainer.queryChangeFeed(CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange()), Document.class);
        List<FeedResponse<Document>> collect = documents.streamByPage().collect(Collectors.toList());
        assertThat(collect).hasSize(1);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning() {
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        Collection<Document> expectedDocuments = partitionKeyToDocuments.get(partitionKey);

        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);

        List<FeedResponse<Document>> changeFeedResultList = createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        int count = 0;
        for (int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
        }
        assertThat(count).as("the number of changes").isEqualTo(expectedDocuments.size());
    }

    @Test(groups = { "fabric-test" }, timeOut = 5 * TIMEOUT)
    public void changesFromPartitionKeyRangeId_FromBeginning() {
        List<String> partitionKeyRangeIds = CosmosBridgeInternal
            .getAsyncDocumentClient(client)
            .readPartitionKeyRanges(getCollectionLink(), (CosmosQueryRequestOptions) null)
            .flatMap(p -> Flux.fromIterable(p.getResults()), 1)
            .map(Resource::getId)
            .collectList()
            .block();

        assertThat(partitionKeyRangeIds.size()).isGreaterThan(1);

        String pkRangeId = partitionKeyRangeIds.get(0);

        FeedRange feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);
        List<FeedResponse<Document>> changeFeedResultList = this.createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());

            assertThat(changeFeedPage.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
            assertThat(changeFeedPage.getContinuationToken())
                .as("Response continuation should not be empty")
                .isNotEmpty();
        }
        assertThat(changeFeedResultList.size()).as("has at least one page").isGreaterThanOrEqualTo(1);
        assertThat(count).as("the number of changes").isGreaterThan(0);
        assertThat(count).as("the number of changes").isLessThan(partitionKeyToDocuments.size());
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void changeFeed_fromNow() throws Exception {
        // READ change feed from current.
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);

        List<FeedResponse<Document>> changeFeedResultsList = this.createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        FeedResponseListValidator<Document> validator =
            new FeedResponseListValidator.Builder<Document>().totalSize(0).build();
        validator.validate(changeFeedResultsList);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "fabric-test" }, dataProvider = "startFromParamProvider", timeOut = TIMEOUT)
    public void changeFeed_cosmosDiagnostics(boolean startFromBeginning) {
        // READ change feed from a partitionKey.
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            startFromBeginning ?
                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange) :
                CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);

        List<FeedResponse<Document>> changeFeedResultsList = this.createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        // since the changeFeed is targeting a specific partitionKey,
        // for each feedResponse,
        // we should only expect one store result in stable situation(no error happens)
        assertThat(changeFeedResultsList.size()).isGreaterThanOrEqualTo(1);
        for (FeedResponse<Document> changeFeedResponse : changeFeedResultsList) {
            validateStoreResultInDiagnostics(
                changeFeedResponse.getCosmosDiagnostics(),
                1,
                this.getConnectionPolicy().getConnectionMode());
        }
    }

    private void changeFeed_withUpdatesAndDelete(boolean enableFullFidelityChangeFeedMode) {

        // READ change feed from current.
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(feedRange);

        if (enableFullFidelityChangeFeedMode) {
            changeFeedOption = changeFeedOption.allVersionsAndDeletes();
        }

        List<FeedResponse<Document>> changeFeedResultsList = this.createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        FeedResponseListValidator<Document> validator =
            new FeedResponseListValidator
                .Builder<Document>()
                .totalSize(0)
                .build();
        validator.validate(changeFeedResultsList);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();

        String continuationToken = changeFeedResultsList
            .get(changeFeedResultsList.size() - 1)
            .getContinuationToken();

        Document docToBeDeleted = partitionKeyToDocuments.get(partitionKey).stream().findFirst().get();
        deleteDocument(this.createdContainer, docToBeDeleted.getId());

        CosmosChangeFeedRequestOptions changeFeedOptionForContinuationAfterDeletes =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);

        List<FeedResponse<Document>> changeFeedResultsListAfterDeletes = this.createdContainer
            .queryChangeFeed(changeFeedOptionForContinuationAfterDeletes, Document.class)
            .streamByPage().collect(Collectors.toList());

        FeedResponseListValidator<Document> validatorAfterDeletes =
            new FeedResponseListValidator
                .Builder<Document>()
                .totalSize(enableFullFidelityChangeFeedMode ? 1: 0)
                .build();
        validatorAfterDeletes.validate(changeFeedResultsListAfterDeletes);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();

        continuationToken = changeFeedResultsListAfterDeletes
            .get(changeFeedResultsList.size() - 1)
            .getContinuationToken();

        Document docToBeUpdated = partitionKeyToDocuments.get(partitionKey).stream().skip(1).findFirst().get();
        updateDocument(this.createdContainer, docToBeUpdated);
        updateDocument(this.createdContainer, docToBeUpdated);
        updateDocument(this.createdContainer, docToBeUpdated);

        CosmosChangeFeedRequestOptions changeFeedOptionForContinuationAfterUpdates =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);

        List<FeedResponse<Document>> changeFeedResultsListAfterUpdates = this.createdContainer
            .queryChangeFeed(changeFeedOptionForContinuationAfterUpdates, Document.class)
            .streamByPage().collect(Collectors.toList());

        FeedResponseListValidator<Document> validatorAfterUpdates =
            new FeedResponseListValidator
                .Builder<Document>()
                .totalSize(enableFullFidelityChangeFeedMode ? 3: 1)
                .build();

        validatorAfterUpdates.validate(changeFeedResultsListAfterUpdates);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "fabric-test" }, enabled = false, timeOut = TIMEOUT)
    @Tag(name = "EnableFullFidelity")
    public void changeFeed_fullFidelity_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(true);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    @Tag(name = "EnableFullFidelity")
    @Ignore("TODO fabianm - re-enable when bug in emulator always using FF change feed on conatiners with retention is fixed")
    public void changeFeed_incrementalOnFullFidelityContainer_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(false);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    @Tag(name = "DisableFullFidelity")
    public void changeFeed_incremental_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(false);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void changeFeed_fromStartDate() throws Exception {

        // READ change feed from current.
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);

        Instant befTime = Instant.now();
        // Waiting for at-least a second to ensure that new document is created after we took the time stamp
        waitAtleastASecond(befTime);

        Instant dateTimeBeforeCreatingDoc = Instant.now();
        changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromPointInTime(dateTimeBeforeCreatingDoc, feedRange);

        // Waiting for at-least a second to ensure that new document is created after we took the time stamp
        waitAtleastASecond(dateTimeBeforeCreatingDoc);
        createdContainer.createItem(getDocumentDefinition(partitionKey), null);

        List<FeedResponse<Document>> changeFeedResultList = createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
        }
        assertThat(count).as("Change feed should have one newly created document").isEqualTo(1);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void changesFromPartitionKey_AfterInsertingNewDocuments() throws Exception {
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);
        changeFeedOption.setMaxItemCount(3);

        List<FeedResponse<Document>> changeFeedResultsList = createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .streamByPage().collect(Collectors.toList());

        assertThat(changeFeedResultsList).as("only one page").hasSize(1);
        assertThat(changeFeedResultsList.get(0).getResults())
            .as("no recent changes")
            .isEmpty();

        String changeFeedContinuation = changeFeedResultsList.get(changeFeedResultsList.size()-1).getContinuationToken();
        assertThat(changeFeedContinuation).as("continuation token is not null").isNotNull();
        assertThat(changeFeedContinuation).as("continuation token is not empty").isNotEmpty();

        // create some documents
        createdContainer.createItem(getDocumentDefinition(partitionKey), null);
        createdContainer.createItem(getDocumentDefinition(partitionKey), null);

        // READ change feed from continuation
        changeFeedOption = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(changeFeedContinuation);

        FeedResponse<Document> changeFeedResults2 = createdContainer
            .queryChangeFeed(changeFeedOption, Document.class)
            .iterableByPage().iterator().next();

        assertThat(changeFeedResults2.getResults())
            .as("change feed should contain newly inserted docs.")
            .hasSize(2);
        assertThat(changeFeedResults2.getContinuationToken())
            .as("Response continuation should not be null")
            .isNotNull();
    }

    public void createDocument(AsyncDocumentClient client, String partitionKey) {
        Document docDefinition = getDocumentDefinition(partitionKey);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false)
                .block()
                .getResource();
        partitionKeyToDocuments.put(partitionKey, createdDocument);
    }

    public Document updateDocument(CosmosContainer cosmosContainer, Document originalDocument) {
        String uuid = UUID.randomUUID().toString();
        originalDocument.set("prop", uuid);

        CosmosItemResponse<Document> documentCosmosItemResponse = cosmosContainer
            .replaceItem(originalDocument, originalDocument.getId(), null, null);
        return documentCosmosItemResponse.getItem();
    }

    public List<Document> bulkInsert(AsyncDocumentClient client, List<Document> docs) {
        ArrayList<Mono<ResourceResponse<Document>>> result = new ArrayList<Mono<ResourceResponse<Document>>>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(client
                .createDocument(
                    "dbs/" + createdDatabase.getId() + "/colls/" + createdContainer.getId(),
                    docs.get(i),
                    null,
                    false));
        }

        return Flux.merge(
                       Flux.fromIterable(result),
                       100)
                   .map(ResourceResponse::getResource).collectList().block();
    }

    @BeforeMethod(groups = { "fabric-test" }, timeOut = SETUP_TIMEOUT)
    public void populateDocuments(Method method) {

        checkNotNull(method, "Argument method must not be null.");

        Tag tag = method.getDeclaredAnnotation(Tag.class);
        if (tag != null && "EnableFullFidelity".equalsIgnoreCase(tag.name())) {
            populateDocumentsInternal(method, true);
        } else {
            populateDocumentsInternal(method, false);
        }
    }

    void populateDocumentsInternal(Method method, boolean enableFullFidelity) {

        partitionKeyToDocuments.clear();

        CosmosContainerResponse containerResponse = this.createdDatabase.createContainer(getCollectionDefinition(enableFullFidelity),
            ThroughputProperties.createManualThroughput(10100));
        createdContainer = createdDatabase.getContainer(containerResponse.getProperties().getId());

        List<Document> docs = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            String partitionKey = UUID.randomUUID().toString();
            for(int j = 0; j < 7; j++) {
                docs.add(getDocumentDefinition(partitionKey));
            }
        }

        List<Document> insertedDocs = bulkInsert(CosmosBridgeInternal.getAsyncDocumentClient(this.client), docs);
        for(Document doc: insertedDocs) {
            partitionKeyToDocuments.put(doc.getString(PartitionKeyFieldName), doc);
        }
    }

    private static Document getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.setId(uuid);
        doc.set("mypk", partitionKey);
        doc.set("prop", uuid);
        return doc;
    }

    private static void waitAtleastASecond(Instant befTime) throws InterruptedException {
        while (befTime.plusSeconds(1).isAfter(Instant.now())) {
            Thread.sleep(100);
        }
    }

    private void validateStoreResultInDiagnostics(
        CosmosDiagnostics cosmosDiagnostics,
        int expectedResponseCount,
        ConnectionMode connectionMode) {
        Collection<ClientSideRequestStatistics> clientSideRequestStatistics = ImplementationBridgeHelpers
            .CosmosDiagnosticsHelper
            .getCosmosDiagnosticsAccessor()
            .getClientSideRequestStatistics(cosmosDiagnostics);
        assertThat(clientSideRequestStatistics.size()).isEqualTo(1);

        if (connectionMode == ConnectionMode.DIRECT) {
            Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics =
                clientSideRequestStatistics
                    .iterator()
                    .next()
                    .getResponseStatisticsList();
            assertThat(storeResponseStatistics.size()).isEqualTo(expectedResponseCount);
        } else {
            List<ClientSideRequestStatistics.GatewayStatistics> gatewayStatistics =
                clientSideRequestStatistics.iterator().next().getGatewayStatisticsList();
            assertThat(gatewayStatistics.size()).isEqualTo(expectedResponseCount);
        }
    }

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target({METHOD})
    @interface Tag {
        String name();
    }
}
