// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReadFeedDocumentsTest extends TestSuiteBase {

    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;
    private List<CosmosItemProperties> createdDocuments;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ReadFeedDocumentsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDocuments() {
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosItemProperties>> feedObservable = createdCollection.readAllItems(options);
        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(createdDocuments.size())
                .numberOfPagesIsGreaterThanOrEqualTo(1)
                .exactlyContainsInAnyOrder(createdDocuments.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0)
                                         .pageSizeIsLessThanOrEqualTo(options.maxItemCount())
                                         .build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readDocuments_withoutEnableCrossPartitionQuery() {
        // With introduction of queryplan, crosspartition need not be enabled anymore.
        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<CosmosItemProperties>> feedObservable = createdCollection.readAllItems(options);
        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                                                                        .totalSize(createdDocuments.size())
                                                                        .numberOfPagesIsGreaterThanOrEqualTo(1)
                                                                        .exactlyContainsInAnyOrder(createdDocuments.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                                                                        .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                                                                                             .requestChargeGreaterThanOrEqualTo(1.0)
                                                                                             .pageSizeIsLessThanOrEqualTo(options.maxItemCount())
                                                                                             .build())
                                                                        .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        List<CosmosItemProperties> docDefList = new ArrayList<>();

        for(int i = 0; i < 100; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(createdCollection, docDefList);
        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
                                                          + "\"id\": \"%s\", "
                                                          + "\"mypk\": \"%s\", "
                                                          + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                          + "}"
                , uuid, uuid));
        return doc;
    }

    public String getCollectionLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId();
    }

    private String getCollectionId() {
        return createdCollection.id();
    }

    private String getDatabaseId() {
        return createdDatabase.id();
    }
}
