/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.microsoft.azure.documentdb.ChangeFeedOptions;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient.Builder;

public class ChangeFeedTest extends TestSuiteBase {

    public static final String DATABASE_ID = getDatabaseId(ChangeFeedTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    public String getCollectionLink() {
        return createdCollection.getSelfLink();
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    public ChangeFeedTest() {
        clientBuilder = createGatewayRxDocumentClient();
    }

    @Test
    public void changeFeed_fromBeginning() throws Exception {

        String pkRangeId = client.readPartitionKeyRanges(getCollectionLink(), null)
                .toBlocking()
                .first().getResults().get(0)
                .getId();

        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setPageSize(30);
        changeFeedOption.setPartitionKeyRangeId(pkRangeId);
        changeFeedOption.setStartFromBeginning(true);

        FeedResponsePage<Document> changeFeedResults = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().single();

        assertThat(changeFeedResults.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
        assertThat(changeFeedResults.getResults().size()).as("change feed should contain all the previously created documents").isGreaterThanOrEqualTo(createdDocuments.size());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_fromNow() throws Exception {

        String pkRangeId = client.readPartitionKeyRanges(getCollectionLink(), null)
                .toBlocking()
                .first().getResults().get(0)
                .getId();

        // Read change feed from current.
        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setPartitionKeyRangeId(pkRangeId);

        FeedResponsePage<Document> changeFeedResults = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().single();

        assertThat(changeFeedResults.getResults()).as("Change feed response should be empty").isEmpty();
        assertThat(changeFeedResults.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void changeFeed_AfterInsertingNewDocuments() throws Exception {

        String pkRangeId = client.readPartitionKeyRanges(getCollectionLink(), null)
                .toBlocking()
                .first().getResults().get(0)
                .getId();

        ChangeFeedOptions changeFeedOption = new ChangeFeedOptions();
        changeFeedOption.setPageSize(3);
        changeFeedOption.setPartitionKeyRangeId(pkRangeId);
        FeedResponsePage<Document> changeFeedResults = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().single();
        assertThat(changeFeedResults.getResponseContinuation()).as("Response continuation should not be null").isNotNull();

        String changeFeedContinuation = changeFeedResults.getResponseContinuation();

        // create some documents
        client.createDocument(getCollectionLink(), new Document("{ 'id': 'doc2' }"), null, true).toBlocking().single();
        client.createDocument(getCollectionLink(), new Document("{ 'id': 'doc3' }"), null, true).toBlocking().single();

        // Read change feed from continuation
        changeFeedOption.setRequestContinuation(changeFeedContinuation);


        FeedResponsePage<Document> changeFeedResults2 = client.queryDocumentChangeFeed(getCollectionLink(), changeFeedOption)
                .toBlocking().first();

        assertThat(changeFeedResults2.getResults()).as("change feed should contain newly inserted docs.").hasSize(2);
        assertThat(changeFeedResults2.getResponseContinuation()).as("Response continuation should not be null").isNotNull();
    }

    public void createDocument(AsyncDocumentClient client, int cnt) throws DocumentClientException {
        Document docDefinition = getDocumentDefinition(cnt);

        Document createdDocument = client
                .createDocument(getCollectionLink(), docDefinition, null, false).toBlocking().single().getResource();
        createdDocuments.add(createdDocument);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        // set up the client        
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);

        createdCollection = safeCreateCollection(client, createdDatabase.getSelfLink(), getCollectionDefinition());
        for(int i = 0; i < 5; i++) {
            createDocument(client, i);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        deleteDatabase(client, DATABASE_ID);
        client.close();
    }

    private static Document getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, cnt, uuid));
        return doc;
    }
}
