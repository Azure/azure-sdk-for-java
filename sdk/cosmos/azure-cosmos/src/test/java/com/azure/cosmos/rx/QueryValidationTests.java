// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryValidationTests extends TestSuiteBase {
    private static final int NUM_DOCUMENTS = 1000;
    private CosmosAsyncContainer createdContainer;
    private Random random;

    private CosmosAsyncClient client;
    private List<TestObject> createdDocuments = new ArrayList<>();

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public QueryValidationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.getClientBuilder().buildAsyncClient();
        createdContainer = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdContainer);

        List<TestObject> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < NUM_DOCUMENTS; i++) {
            documentsToInsert.add(getDocumentDefinition(UUID.randomUUID().toString()));
        }

        createdDocuments = bulkInsertBlocking(createdContainer, documentsToInsert);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void orderByQuery() {
        /*
        The idea here is to query documents in pages, query all the documents(with pagesize as num_documents and compare
         the results.
         */
        String query = "select * from c order by c.prop ASC";
        List<TestObject> documentsPaged = queryWithContinuationTokens(query, 100, TestObject.class);

        List<TestObject> allDocuments = queryWithContinuationTokens(query, NUM_DOCUMENTS, TestObject.class);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());
        List<String> expectedResourceIds = sortTestObjectsAndCollectIds(createdDocuments,
                                                                        "prop",
                                                                        d -> d.getProp(),
                                                                        validatorComparator);

        List<String> docIds1 = documentsPaged.stream().map(TestObject::getId).collect(Collectors.toList());
        List<String> docIds2 = allDocuments.stream().map(TestObject::getId).collect(Collectors.toList());

        assertThat(docIds2).containsExactlyInAnyOrderElementsOf(expectedResourceIds);
        assertThat(docIds1).containsExactlyElementsOf(docIds2);

    }

    private <T> List<T> queryWithContinuationTokens(String query, int pageSize, Class<T> klass) {
        logger.info("querying: " + query);
        String requestContinuation = null;

        List<T> receivedDocuments = new ArrayList<>();
        QueryRequestOptions options = new QueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);

        do {
            CosmosPagedFlux<T> queryPagedFlux = createdContainer.queryItems(query, options, klass);
            FeedResponse<T> firstPage = queryPagedFlux.byPage(requestContinuation, pageSize).blockFirst();
            assert firstPage != null;
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    private TestObject getDocumentDefinition(String documentId) {
        String uuid = UUID.randomUUID().toString();
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetetions for int value.
        int randInt = random.nextInt(NUM_DOCUMENTS / 2);
        TestObject doc = new TestObject(documentId, "name" + randInt, randInt);
        return doc;
    }

    private <T> List<String> sortTestObjectsAndCollectIds(
        List<TestObject> createdDocuments, String propName,
        Function<TestObject, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                   .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                   .map(d -> d.getId()).collect(Collectors.toList());
    }

    static class TestObject {
        String id;
        String name;
        int prop;

        public TestObject() {
        }

        public TestObject(String id, String name, int prop) {
            this.id = id;
            this.name = name;
            this.prop = prop;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getProp() {
            return prop;
        }

        public void setProp(final int prop) {
            this.prop = prop;
        }
    }
}
