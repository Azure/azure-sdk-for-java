// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.PartitionKeyDefinition;
import com.azure.cosmos.Resource;
import com.azure.cosmos.internal.AsyncDocumentClient;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.Document;
import com.azure.cosmos.internal.DocumentCollection;
import com.azure.cosmos.internal.FeedResponseListValidator;
import com.azure.cosmos.internal.RequestOptions;
import com.azure.cosmos.internal.ResourceResponse;
import com.azure.cosmos.internal.TestSuiteBase;
import com.azure.cosmos.internal.TestUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.azure.cosmos.CommonsBridgeInternal.partitionKeyRangeIdInternal;
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

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/" + PartitionKeyFieldName);
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

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

        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setMaxItemCount(3);
        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));
        changeFeedOption.setStartFromBeginning(true);

        List<FeedResponse<Document>> changeFeedResultList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .collectList().block();

        int count = 0;
        for (int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getContinuationToken()).as("Response continuation should not be null").isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
        }
        assertThat(count).as("the number of changes").isEqualTo(expectedDocuments.size());
    }

    @Test(groups = { "simple" }, timeOut = 5 * TIMEOUT)
    public void changesFromPartitionKeyRangeId_FromBeginning() throws Exception {
        List<String> partitionKeyRangeIds = client.readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Flux.fromIterable(p.getResults()), 1)
                .map(Resource::getId)
                .collectList()
                .block();
        
        assertThat(partitionKeyRangeIds.size()).isGreaterThan(1);

        String pkRangeId = partitionKeyRangeIds.get(0);
        
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setMaxItemCount(3);
        partitionKeyRangeIdInternal(changeFeedOption, pkRangeId);
        changeFeedOption.setStartFromBeginning(true);
        List<FeedResponse<Document>> changeFeedResultList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .collectList().block();
        
        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getContinuationToken()).as("Response continuation should not be null").isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
   
            assertThat(changeFeedPage.getContinuationToken()).as("Response continuation should not be null").isNotNull();
            assertThat(changeFeedPage.getContinuationToken()).as("Response continuation should not be empty").isNotEmpty();
        }
        assertThat(changeFeedResultList.size()).as("has at least one page").isGreaterThanOrEqualTo(1);
        assertThat(count).as("the number of changes").isGreaterThan(0);
        assertThat(count).as("the number of changes").isLessThan(partitionKeyToDocuments.size());
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromNow() throws Exception {
        // READ change feed from current.
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));

        List<FeedResponse<Document>> changeFeedResultsList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .collectList()
                .block();

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>().totalSize(0).build();
        validator.validate(changeFeedResultsList);
        assertThat(changeFeedResultsList.get(changeFeedResultsList.size() -1 ).
            getContinuationToken()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromStartDate() throws Exception {

        //setStartDateTime is not currently supported in multimaster mode. So skipping the test
        if(BridgeInternal.isEnableMultipleWriteLocations(client.getDatabaseAccount().single().block())){
            throw new SkipException("StartTime/IfModifiedSince is not currently supported when EnableMultipleWriteLocations is set");
        }

        // READ change feed from current.
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();

        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));
        OffsetDateTime befTime = OffsetDateTime.now();
        // Waiting for at-least a second to ensure that new document is created after we took the time stamp
        waitAtleastASecond(befTime);

        OffsetDateTime dateTimeBeforeCreatingDoc = OffsetDateTime.now();
        changeFeedOption.setStartDateTime(dateTimeBeforeCreatingDoc);

        // Waiting for at-least a second to ensure that new document is created after we took the time stamp
        waitAtleastASecond(dateTimeBeforeCreatingDoc);
        client.createDocument(getCollectionLink(), getDocumentDefinition(partitionKey), null, true).single().block();

        List<FeedResponse<Document>> changeFeedResultList = client.queryDocumentChangeFeed(getCollectionLink(),
                changeFeedOption).collectList().block();

        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getContinuationToken()).as("Response continuation should not be null").isNotNull();
        }
        assertThat(count).as("Change feed should have one newly created document").isEqualTo(1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changesFromPartitionKey_AfterInsertingNewDocuments() throws Exception {
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setMaxItemCount(3);
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));

        List<FeedResponse<Document>> changeFeedResultsList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .collectList().block();

        assertThat(changeFeedResultsList).as("only one page").hasSize(1);
        assertThat(changeFeedResultsList.get(0).getResults()).as("no recent changes").isEmpty();

        String changeFeedContinuation = changeFeedResultsList.get(changeFeedResultsList.size()-1).getContinuationToken();
        assertThat(changeFeedContinuation).as("continuation token is not null").isNotNull();
        assertThat(changeFeedContinuation).as("continuation token is not empty").isNotEmpty();

        // create some documents
        client.createDocument(getCollectionLink(), getDocumentDefinition(partitionKey), null, true).single().block();
        client.createDocument(getCollectionLink(), getDocumentDefinition(partitionKey), null, true).single().block();

        // READ change feed from continuation
        changeFeedOption.setRequestContinuation(changeFeedContinuation);


        FeedResponse<Document> changeFeedResults2 = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .blockFirst();

        assertThat(changeFeedResults2.getResults()).as("change feed should contain newly inserted docs.").hasSize(2);
        assertThat(changeFeedResults2.getContinuationToken()).as("Response continuation should not be null").isNotNull();
    }

    public void createDocument(AsyncDocumentClient client, String partitionKey) {
        Document docDefinition = getDocumentDefinition(partitionKey);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false).single().block().getResource();
        partitionKeyToDocuments.put(partitionKey, createdDocument);
    }

    public List<Document> bulkInsert(AsyncDocumentClient client, List<Document> docs) {
        ArrayList<Flux<ResourceResponse<Document>>> result = new ArrayList<Flux<ResourceResponse<Document>>>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(client.createDocument("dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(), docs.get(i), null, false));
        }

        return Flux.merge(Flux.fromIterable(result), 100).map(ResourceResponse::getResource).collectList().block();
    }

    @AfterMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void removeCollection() {
        if (createdCollection != null) {
            deleteCollection(client, getCollectionLink());
        }
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void populateDocuments(Method method) {

        partitionKeyToDocuments.clear();

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition(), options);

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

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        // set up the client
        client = clientBuilder().build();
        createdDatabase = SHARED_DATABASE;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
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

    private static void waitAtleastASecond(OffsetDateTime befTime) throws InterruptedException {
        while (befTime.plusSeconds(1).isAfter(OffsetDateTime.now())) {
            Thread.sleep(100);
        }
    }
}
