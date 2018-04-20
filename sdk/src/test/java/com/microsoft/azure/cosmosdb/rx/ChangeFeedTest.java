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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class ChangeFeedTest extends TestSuiteBase {

    private static final int SETUP_TIMEOUT = 40000;

    public static final String DATABASE_ID = getDatabaseId(ChangeFeedTest.class);
    private static final String PartitionKeyFieldName = "mypk";
    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private Multimap<String, Document> partitionKeyToDocuments = ArrayListMultimap.create();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(DATABASE_ID, createdCollection.getId());
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
        clientBuilder = createGatewayRxDocumentClient();
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
                .toList().toBlocking().single();

        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getResponseContinuation()).as("Response continuation should not be null").isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
        }
        assertThat(count).as("the number of changes").isEqualTo(expectedDocuments.size());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changesFromParitionKeyRangeId_FromBeginning() throws Exception {
        List<String> partitionKeyRangeIds = client.readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Observable.from(p.getResults()), 1)
                .map(pkr -> pkr.getId())
                .toList()
                .toBlocking()
                .single();
        
        assertThat(partitionKeyRangeIds.size()).isGreaterThan(1);

        String pkRangeId = partitionKeyRangeIds.get(0);
        
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setMaxItemCount(3);
        changeFeedOption.setPartitionKeyRangeId(pkRangeId);
        changeFeedOption.setStartFromBeginning(true);
        List<FeedResponse<Document>> changeFeedResultList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toList().toBlocking().single();
        
        int count = 0;
        for(int i = 0; i < changeFeedResultList.size(); i++) {
            FeedResponse<Document> changeFeedPage = changeFeedResultList.get(i);
            assertThat(changeFeedPage.getResponseContinuation()).as("Response continuation should not be null").isNotNull();

            count += changeFeedPage.getResults().size();
            assertThat(changeFeedPage.getResults().size())
            .as("change feed should contain all the previously created documents")
            .isLessThanOrEqualTo(changeFeedOption.getMaxItemCount());
   
            assertThat(changeFeedPage.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
            assertThat(changeFeedPage.getResponseContinuation()).as("Response continuation should not be empty").isNotEmpty();
        }
        assertThat(changeFeedResultList.size()).as("has at least one page").isGreaterThanOrEqualTo(1);
        assertThat(count).as("the number of changes").isGreaterThan(0);
        assertThat(count).as("the number of changes").isLessThan(partitionKeyToDocuments.size());
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromNow() throws Exception {
        // Read change feed from current.
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));

        FeedResponse<Document> changeFeedResults = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().single();

        assertThat(changeFeedResults.getResults()).as("Change feed response should be empty").isEmpty();
        assertThat(changeFeedResults.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changesFromPartitionKey_AfterInsertingNewDocuments() throws Exception {
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setMaxItemCount(3);
        String partitionKey = partitionKeyToDocuments.keySet().iterator().next();
        changeFeedOption.setPartitionKey(new PartitionKey(partitionKey));

        List<FeedResponse<Document>> changeFeedResultsList = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toList().toBlocking().single();

        assertThat(changeFeedResultsList).as("only one page").hasSize(1);
        assertThat(changeFeedResultsList.get(0).getResults()).as("no recent changes").isEmpty();

        String changeFeedContinuation = changeFeedResultsList.get(changeFeedResultsList.size()-1).getResponseContinuation();
        assertThat(changeFeedContinuation).as("continuation token is not null").isNotNull();
        assertThat(changeFeedContinuation).as("continuation token is not empty").isNotEmpty();

        // create some documents
        client.createDocument(getCollectionLink(), getDocumentDefinition(partitionKey), null, true).toBlocking().single();
        client.createDocument(getCollectionLink(), getDocumentDefinition(partitionKey), null, true).toBlocking().single();

        // Read change feed from continuation
        changeFeedOption.setRequestContinuation(changeFeedContinuation);


        FeedResponse<Document> changeFeedResults2 = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().first();

        assertThat(changeFeedResults2.getResults()).as("change feed should contain newly inserted docs.").hasSize(2);
        assertThat(changeFeedResults2.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
    }

    public void createDocument(AsyncDocumentClient client, String partitionKey) throws DocumentClientException {
        Document docDefinition = getDocumentDefinition(partitionKey);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();
        partitionKeyToDocuments.put(partitionKey, createdDocument);
    }

    public List<Document> bulkInsert(AsyncDocumentClient client, List<Document> docs) {
        ArrayList<Observable<ResourceResponse<Document>>> result = new ArrayList<Observable<ResourceResponse<Document>>>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(client.createDocument("dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(), docs.get(i), null, false));
        }

        return Observable.merge(result, 100).map(r -> r.getResource()).toList().toBlocking().single();
    }

    @AfterMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void removeCollection() {
        if (createdCollection != null) {
            deleteCollection(client, getCollectionLink());
        }
    }
    
    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void populateDocuments() {
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
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, DATABASE_ID);
        safeClose(client);
    }

    private static Document getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document();
        doc.setId(uuid);
        doc.set("mypk", partitionKey);
        doc.set("prop", uuid);
        return doc;
    }
}
