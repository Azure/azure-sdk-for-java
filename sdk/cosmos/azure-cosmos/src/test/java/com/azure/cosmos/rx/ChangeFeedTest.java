// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

//TODO: change to use external TestSuiteBase
public class ChangeFeedTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;
    private static final int TIMEOUT = 30000;
    private static final String PartitionKeyFieldName = "mypk";
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private Multimap<String, Document> partitionKeyToDocuments = ArrayListMultimap.create();

    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition(boolean enableFullFidelity) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/" + PartitionKeyFieldName);
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        if (enableFullFidelity) {
            collectionDefinition.setChangeFeedPolicy(
                ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(10)));
        }

        return collectionDefinition;
    }

    public ChangeFeedTest() {
        super(createGatewayRxDocumentClient());
        subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning() throws Exception {
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        Collection<Document> expectedDocuments = partitionKeyToDocuments.get(partitionKey);

        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);

        List<FeedResponse<Document>> changeFeedResultList = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .collectList().block();

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

    @Test(groups = { "simple" }, timeOut = 5 * TIMEOUT)
    public void changesFromPartitionKeyRangeId_FromBeginning() {
        List<String> partitionKeyRangeIds = client.readPartitionKeyRanges(getCollectionLink(), null)
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
        List<FeedResponse<Document>> changeFeedResultList = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .collectList().block();

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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromNow() throws Exception {
        // READ change feed from current.
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);

        List<FeedResponse<Document>> changeFeedResultsList = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .collectList()
            .block();

        FeedResponseListValidator<Document> validator =
            new FeedResponseListValidator.Builder<Document>().totalSize(0).build();
        validator.validate(changeFeedResultsList);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();
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
            changeFeedOption = changeFeedOption.fullFidelity();
        }

        List<FeedResponse<Document>> changeFeedResultsList = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .collectList()
            .block();

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
        deleteDocument(client, docToBeDeleted.getSelfLink(), new PartitionKey(partitionKey));

        CosmosChangeFeedRequestOptions changeFeedOptionForContinuationAfterDeletes =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);

        List<FeedResponse<Document>> changeFeedResultsListAfterDeletes = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOptionForContinuationAfterDeletes)
            .collectList()
            .block();

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
        updateDocument(client, docToBeUpdated);
        updateDocument(client, docToBeUpdated);
        updateDocument(client, docToBeUpdated);

        CosmosChangeFeedRequestOptions changeFeedOptionForContinuationAfterUpdates =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuationToken);

        List<FeedResponse<Document>> changeFeedResultsListAfterUpdates = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOptionForContinuationAfterUpdates)
            .collectList()
            .block();

        FeedResponseListValidator<Document> validatorAfterUpdates =
            new FeedResponseListValidator
                .Builder<Document>()
                .totalSize(enableFullFidelityChangeFeedMode ? 3: 1)
                .build();

        validatorAfterUpdates.validate(changeFeedResultsListAfterUpdates);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "emulator" }, enabled = false, timeOut = TIMEOUT)
    @Tag(name = "EnableFullFidelity")
    public void changeFeed_fullFidelity_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(true);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    @Tag(name = "EnableFullFidelity")
    @Ignore("TODO fabianm - re-enable when bug in emulator always using FF change feed on conatiners with retention is fixed")
    public void changeFeed_incrementalOnFullFidelityContainer_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(false);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    @Tag(name = "DisableFullFidelity")
    public void changeFeed_incremental_fromNow() throws Exception {
        changeFeed_withUpdatesAndDelete(false);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromStartDate() throws Exception {

        //setStartDateTime is not currently supported in multimaster mode. So skipping the test
        if(BridgeInternal.isEnableMultipleWriteLocations(client.getDatabaseAccount().single().block())){
            throw new SkipException(
                "StartTime/IfModifiedSince is not currently supported when EnableMultipleWriteLocations is set");
        }

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
        client
            .createDocument(
                getCollectionLink(),
                getDocumentDefinition(partitionKey),
                null,
                true)
            .block();

        List<FeedResponse<Document>> changeFeedResultList = client.queryDocumentChangeFeed(createdCollection,
                changeFeedOption).collectList().block();

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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changesFromPartitionKey_AfterInsertingNewDocuments() throws Exception {
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);
        changeFeedOption.setMaxItemCount(3);

        List<FeedResponse<Document>> changeFeedResultsList = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .collectList()
            .block();

        assertThat(changeFeedResultsList).as("only one page").hasSize(1);
        assertThat(changeFeedResultsList.get(0).getResults())
            .as("no recent changes")
            .isEmpty();

        String changeFeedContinuation = changeFeedResultsList.get(changeFeedResultsList.size()-1).getContinuationToken();
        assertThat(changeFeedContinuation).as("continuation token is not null").isNotNull();
        assertThat(changeFeedContinuation).as("continuation token is not empty").isNotEmpty();

        // create some documents
        client
            .createDocument(
                getCollectionLink(),
                getDocumentDefinition(partitionKey),
                null,
                true)
            .block();
        client
            .createDocument(
                getCollectionLink(),
                getDocumentDefinition(partitionKey),
                null,
                true)
            .block();

        // READ change feed from continuation
        changeFeedOption = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(changeFeedContinuation);

        FeedResponse<Document> changeFeedResults2 = client
            .queryDocumentChangeFeed(createdCollection, changeFeedOption)
            .blockFirst();

        assertThat(changeFeedResults2.getResults())
            .as("change feed should contain newly inserted docs.")
            .hasSize(2);
        assertThat(changeFeedResults2.getContinuationToken())
            .as("Response continuation should not be null")
            .isNotNull();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, enabled = false)
    public void changeFeed_fromBeginning_withFeedRangeFiltering() throws Exception {

        ArrayList<Range<String>> ranges = new ArrayList<>();

        for (String partitionKey : partitionKeyToDocuments.keySet().stream().collect(Collectors.toList())) {
            Collection<Document> expectedDocuments = partitionKeyToDocuments.get(partitionKey);

            FeedRangePartitionKeyImpl feedRangeForLogicalPartition= new FeedRangePartitionKeyImpl(
                ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(partitionKey)));

            Range<String> effectiveRange =
                feedRangeForLogicalPartition
                    .getNormalizedEffectiveRange(
                        client.getPartitionKeyRangeCache(),
                        null,
                        Mono.just(new Utils.ValueHolder<>(this.createdCollection)))
                    .block();

            assertThat(effectiveRange).isNotNull();

            FeedRange feedRange = new FeedRangeEpkImpl(effectiveRange);

            CosmosChangeFeedRequestOptions changeFeedOption =
                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);

            List<FeedResponse<Document>> changeFeedResultListForEPK = client
                .queryDocumentChangeFeed(createdCollection, changeFeedOption)
                .collectList()
                .block();

            int count = 0;
            for (int i = 0; i < changeFeedResultListForEPK.size(); i++) {
                FeedResponse<Document> changeFeedPage = changeFeedResultListForEPK.get(i);
                assertThat(changeFeedPage.getContinuationToken())
                    .as("Response continuation should not be null")
                    .isNotNull();

                count += changeFeedPage.getResults().size();
                assertThat(changeFeedPage.getResults().size())
                    .as("change feed should contain all the previously created documents")
                    .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
            }
            assertThat(count)
                .as("the number of changes")
                .isEqualTo(expectedDocuments.size());
        }
    }

    public void createDocument(AsyncDocumentClient client, String partitionKey) {
        Document docDefinition = getDocumentDefinition(partitionKey);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false)
                .block()
                .getResource();
        partitionKeyToDocuments.put(partitionKey, createdDocument);
    }

    public Document updateDocument(AsyncDocumentClient client, Document originalDocument) {
        String uuid = UUID.randomUUID().toString();
        BridgeInternal.setProperty(originalDocument, "prop", uuid);

        return client
            .replaceDocument(originalDocument.getSelfLink(), originalDocument, null)
            .block()
            .getResource();
    }

    public List<Document> bulkInsert(AsyncDocumentClient client, List<Document> docs) {
        ArrayList<Mono<ResourceResponse<Document>>> result = new ArrayList<Mono<ResourceResponse<Document>>>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(client
                .createDocument(
                    "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(),
                    docs.get(i),
                    null,
                    false));
        }

        return Flux.merge(
            Flux.fromIterable(result),
            100)
                   .map(ResourceResponse::getResource).collectList().block();
    }

    @AfterMethod(groups = { "simple", "emulator" }, timeOut = SETUP_TIMEOUT)
    public void removeCollection() {
        if (createdCollection != null) {
            deleteCollection(client, getCollectionLink());
        }
    }

    @BeforeMethod(groups = { "simple", "emulator" }, timeOut = SETUP_TIMEOUT)
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

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        createdCollection = createCollection(
            client,
            createdDatabase.getId(),
            getCollectionDefinition(enableFullFidelity),
            options);

        List<Document> docs = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            String partitionKey = UUID.randomUUID().toString();
            for(int j = 0; j < 7; j++) {
                docs.add(getDocumentDefinition(partitionKey));
            }
        }

        List<Document> insertedDocs = bulkInsert(client, docs);
        for(Document doc: insertedDocs) {
            partitionKeyToDocuments.put(doc.getString(PartitionKeyFieldName), doc);
        }
    }

    @BeforeClass(groups = { "simple", "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ChangeFeedTest() throws Exception {
        // set up the client
        client = clientBuilder().build();
        createdDatabase = SHARED_DATABASE;
    }

    @AfterClass(groups = { "simple", "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static Document getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.setId(uuid);
        BridgeInternal.setProperty(doc, "mypk", partitionKey);
        BridgeInternal.setProperty(doc, "prop", uuid);
        return doc;
    }

    private static void waitAtleastASecond(Instant befTime) throws InterruptedException {
        while (befTime.plusSeconds(1).isAfter(Instant.now())) {
            Thread.sleep(100);
        }
    }

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target({METHOD})
    @interface Tag {
        String name();
    }
}
