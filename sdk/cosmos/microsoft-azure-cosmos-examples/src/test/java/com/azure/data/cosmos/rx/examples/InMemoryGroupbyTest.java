// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx.examples;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.DocumentClientTest;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.SqlParameter;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//FIXME setup method times out occasionally when running against emulator.
@Ignore
public class InMemoryGroupbyTest extends DocumentClientTest {

    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    @BeforeClass(groups = "samples", timeOut = 2 * TIMEOUT)
    public void setUp() throws Exception {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy().connectionMode(ConnectionMode.DIRECT);

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION);

        this.client = this.clientBuilder().build();

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE collection
        createdCollection = client
                .createCollection("dbs/" + createdDatabase.id(), collectionDefinition, null)
                .single().block().getResource();

        int numberOfPayers = 10;
        int numberOfDocumentsPerPayer = 10;

        for (int i = 0; i < numberOfPayers; i++) {

            for (int j = 0; j < numberOfDocumentsPerPayer; j++) {

                LocalDateTime currentTime = LocalDateTime.now();

                Document doc = new Document(String.format("{ "
                        + "'id' : '%s',"
                        + "'site_id': 'ABC', "
                        + "'payer_id': %d, "
                        + " 'created_time' : %d "
                        + "}", UUID.randomUUID().toString(), i, currentTime.getSecond()));
                client.createDocument(getCollectionLink(), doc, null, true).single().block();

                Thread.sleep(100);
            }
        }
        System.out.println("finished inserting documents");
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        client.close();
    }

    /**
     * Queries Documents and performs Group by operation after fetching the Documents.
     * If you want to understand the steps in more details see {@link #groupByInMemory_MoreDetail()}
     * @throws Exception
     */
    @Test(groups = "samples", timeOut = 2 * TIMEOUT)
    public void groupByInMemory() {
        // If you want to understand the steps in more details see groupByInMemoryMoreDetail()
        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Flux<Document> documentsObservable = client
                .queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                                new SqlParameterList(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Flux.fromIterable(page.results()));

        final LocalDateTime now = LocalDateTime.now();

        List<List<Document>> resultsGroupedAsLists = documentsObservable
                .filter(doc -> Math.abs(now.getSecond() - doc.getInt("created_time")) <= 90)
                .groupBy(doc -> doc.getInt("payer_id")).flatMap(Flux::collectList)
                .collectList()
                .block();

        for(List<Document> resultsForEachPayer :resultsGroupedAsLists) {
            System.out.println("documents with payer_id : " + resultsForEachPayer.get(0).getInt("payer_id") + " are " + resultsForEachPayer);
        }
    }

    /**
     * This does the same thing as {@link #groupByInMemory_MoreDetail()} but with pedagogical details
     * @throws Exception
     */
    @Test(groups = "samples", timeOut = 2 * TIMEOUT)
    public void groupByInMemory_MoreDetail() {

        int requestPageSize = 3;
        FeedOptions options = new FeedOptions();
        options.maxItemCount(requestPageSize);
        options.enableCrossPartitionQuery(true);

        Flux<Document> documentsObservable = client
                .queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                                new SqlParameterList(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Flux.fromIterable(page.results()));

        final LocalDateTime now = LocalDateTime.now();

        Flux<GroupedFlux<Integer, Document>> groupedByPayerIdObservable = documentsObservable
                .filter(doc -> Math.abs(now.getSecond() - doc.getInt("created_time")) <= 90)
                .groupBy(doc -> doc.getInt("payer_id"));

        Flux<List<Document>> docsGroupedAsList = groupedByPayerIdObservable.flatMap(grouped -> {
            Flux<List<Document>> list = grouped.collectList().flux();
            return list;
        });

        List<List<Document>> resultsGroupedAsLists = docsGroupedAsList.collectList().single().block();

        for(List<Document> resultsForEachPayer : resultsGroupedAsLists) {
            System.out.println("documents with payer_id : " + resultsForEachPayer.get(0).getInt("payer_id") + " are " + resultsForEachPayer);
        }
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id();
    }
}
