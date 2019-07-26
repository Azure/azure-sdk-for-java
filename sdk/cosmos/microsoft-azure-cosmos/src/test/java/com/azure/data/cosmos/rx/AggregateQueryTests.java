// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.UUID;

public class AggregateQueryTests extends TestSuiteBase {

    public static class QueryConfig {
        String testName;
        String query;
        Object expected;

        public QueryConfig (String testName, String query, Object expected) {
            this.testName = testName;
            this.query = query;
            this.expected = expected;
        }
    }

    public static class AggregateConfig {
        String operator;
        Object expected;
        String condition;

        public AggregateConfig(String operator, Object expected, String condition) {
            this.operator = operator;
            this.expected = expected;
            this.condition = condition;
        }
    }

    private CosmosContainer createdCollection;
    private ArrayList<CosmosItemProperties> docs = new ArrayList<CosmosItemProperties>();
    private ArrayList<QueryConfig> queryConfigs = new ArrayList<QueryConfig>();

    private String partitionKey = "mypk";
    private String uniquePartitionKey = "uniquePartitionKey";
    private String field = "field";
    private int sum;
    private int numberOfDocuments = 800;
    private int numberOfDocumentsWithNumericId;
    private int numberOfDocsWithSamePartitionKey = 400;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public AggregateQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsWithAggregates(boolean qmEnabled) throws Exception {

        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(qmEnabled);
        options.maxDegreeOfParallelism(2);

        for (QueryConfig queryConfig : queryConfigs) {

            Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(queryConfig.query, options);

            FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .withAggregateValue(queryConfig.expected)
                .numberOfPages(1)
                .hasValidQueryMetrics(qmEnabled)
                .build();

            validateQuerySuccess(queryObservable, validator);
        }
    }

    public void bulkInsert() {
        generateTestData();
        voidBulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData() {

        Object[] values = new Object[]{null, false, true, "abc", "cdfg", "opqrs", "ttttttt", "xyz", "oo", "ppp"};
        for (int i = 0; i < values.length; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            d.id(UUID.randomUUID().toString());
            BridgeInternal.setProperty(d, partitionKey, values[i]);
            docs.add(d);
        }

        for (int i = 0; i < numberOfDocsWithSamePartitionKey; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            BridgeInternal.setProperty(d, partitionKey, uniquePartitionKey);
            BridgeInternal.setProperty(d, "resourceId", Integer.toString(i));
            BridgeInternal.setProperty(d, field, i + 1);
            d.id(UUID.randomUUID().toString());
            docs.add(d);
        }

        numberOfDocumentsWithNumericId = numberOfDocuments - values.length - numberOfDocsWithSamePartitionKey;
        for (int i = 0; i < numberOfDocumentsWithNumericId; i++) {
            CosmosItemProperties d = new CosmosItemProperties();
            BridgeInternal.setProperty(d, partitionKey, i + 1);
            d.id(UUID.randomUUID().toString());
            docs.add(d);
        }

        sum = (int) (numberOfDocumentsWithNumericId * (numberOfDocumentsWithNumericId + 1) / 2.0);

    }

    public void generateTestConfigs() {

        String aggregateQueryFormat = "SELECT VALUE %s(r.%s) FROM r WHERE %s";
        AggregateConfig[] aggregateConfigs = new AggregateConfig[] {
                new AggregateConfig("AVG", sum / numberOfDocumentsWithNumericId, String.format("IS_NUMBER(r.%s)", partitionKey)),
                new AggregateConfig("AVG", null, "true"),
                new AggregateConfig("COUNT", numberOfDocuments, "true"),
                new AggregateConfig("MAX","xyz","true"),
                new AggregateConfig("MIN", null, "true"),
                new AggregateConfig("SUM", sum, String.format("IS_NUMBER(r.%s)", partitionKey)),
                new AggregateConfig("SUM", null, "true")
        };

        for (AggregateConfig config: aggregateConfigs) {
            String query = String.format(aggregateQueryFormat, config.operator, partitionKey, config.condition);
            String testName = String.format("%s %s", config.operator, config.condition);
            queryConfigs.add(new QueryConfig(testName, query, config.expected));
        }

        String aggregateSinglePartitionQueryFormat = "SELECT VALUE %s(r.%s) FROM r WHERE r.%s = '%s'";
        String aggregateSinglePartitionQueryFormatSelect = "SELECT %s(r.%s) FROM r WHERE r.%s = '%s'";
        double samePartitionSum = numberOfDocsWithSamePartitionKey * (numberOfDocsWithSamePartitionKey + 1) / 2.0;

        AggregateConfig[] aggregateSinglePartitionConfigs = new AggregateConfig[] {
                new AggregateConfig("AVG", samePartitionSum / numberOfDocsWithSamePartitionKey, null),
                new AggregateConfig("COUNT", numberOfDocsWithSamePartitionKey, null),
                new AggregateConfig("MAX", numberOfDocsWithSamePartitionKey, null),
                new AggregateConfig("MIN", 1, null),
                new AggregateConfig("SUM", samePartitionSum, null)
        };

        for (AggregateConfig config: aggregateSinglePartitionConfigs) {
            String query = String.format(aggregateSinglePartitionQueryFormat, config.operator, field, partitionKey, uniquePartitionKey);
            String testName = String.format("%s SinglePartition %s", config.operator, "SELECT VALUE");
            queryConfigs.add(new QueryConfig(testName, query, config.expected));

            query = String.format(aggregateSinglePartitionQueryFormatSelect, config.operator, field, partitionKey, uniquePartitionKey);
            testName = String.format("%s SinglePartition %s", config.operator, "SELECT");
            queryConfigs.add(new QueryConfig(testName, query, new Document("{'$1':" + removeTrailingZerosIfInteger(config.expected) + "}")));
        }
    }

    private Object removeTrailingZerosIfInteger(Object obj) {
        if (obj instanceof Number) {
            Number num = (Number) obj;
            if (num.doubleValue() == num.intValue()) {
                return num.intValue();
            }
        }
        return obj;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT * 2)
    public void beforeClass() throws Exception {
        client = this.clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert();
        generateTestConfigs();

        waitIfNeededForReplicasToCatchUp(this.clientBuilder());
    }
}
