// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.examples;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.TestConfigurations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InMemoryGroupbyTest extends DocumentClientTest {

    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    @BeforeClass(groups = "samples", timeOut = 2 * TIMEOUT)
    public void before_InMemoryGroupbyTest() throws Exception {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        // CREATE collection
        createdCollection = client
                .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .single().block().getResource();

        int numberOfPayers = 10;
        int numberOfDocumentsPerPayer = 10;
        List<Mono<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfPayers; i++) {

            for (int j = 0; j < numberOfDocumentsPerPayer; j++) {

                LocalDateTime currentTime = LocalDateTime.now();

                Document doc = new Document(String.format("{ "
                        + "'id' : '%s',"
                        + "'site_id': 'ABC', "
                        + "'payer_id': %d, "
                        + " 'created_time' : %d "
                        + "}", UUID.randomUUID().toString(), i, currentTime.getSecond()));
                tasks.add(client.createDocument(getCollectionLink(), doc, null, true).then());

                Thread.sleep(100);
            }
        }
        Flux.merge(tasks).then().block();
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
        ModelBridgeInternal.setFeedOptionsMaxItemCount(options, requestPageSize);

        Flux<Document> documentsObservable = client
                .<Document>queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                            Collections.singletonList(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Flux.fromIterable(page.getResults()));

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
        ModelBridgeInternal.setFeedOptionsMaxItemCount(options, requestPageSize);

        Flux<Document> documentsObservable = client
                .<Document>queryDocuments(getCollectionLink(),
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.site_id=@site_id",
                                Collections.singletonList(new SqlParameter("@site_id", "ABC"))),
                        options)
                .flatMap(page -> Flux.fromIterable(page.getResults()));

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
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }
}
