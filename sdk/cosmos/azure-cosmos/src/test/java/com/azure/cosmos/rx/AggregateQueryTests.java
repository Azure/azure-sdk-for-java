// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.Exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

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

    public static class MultiAggregateConfig {
        String operator1;
        String operator2;
        Object expected1;
        Object expected2;

        public MultiAggregateConfig(String operator1, String operator2, Object expected1, Object expected2) {
            this.operator1 = operator1;
            this.operator2 = operator2;
            this.expected1 = expected1;
            this.expected2 = expected2;
        }
    }

    private CosmosAsyncContainer createdCollection;
    private ArrayList<InternalObjectNode> docs = new ArrayList<InternalObjectNode>();
    private ArrayList<QueryConfig> queryConfigs = new ArrayList<QueryConfig>();
    private ArrayList<QueryConfig> multiAggregateQueryConfigs = new ArrayList<QueryConfig>();

    private String partitionKey = "mypk";
    private String uniquePartitionKey = "uniquePartitionKey";
    private String field = "field";
    private int sum;
    private int numberOfDocuments = 800;
    private int numberOfDocumentsWithNumericId;
    private int numberOfDocsWithSamePartitionKey = 400;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public AggregateQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsWithAggregates(Boolean qmEnabled) throws Exception {

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }
        options.setMaxDegreeOfParallelism(2);

        for (QueryConfig queryConfig : queryConfigs) {

            CosmosPagedFlux<JsonNode> queryObservable = createdCollection.queryItems(queryConfig.query, options,
                                                                                     JsonNode.class);

            FeedResponseListValidator<JsonNode> validator = new FeedResponseListValidator.Builder<JsonNode>()
                .withAggregateValue(queryConfig.expected)
                .numberOfPages(1)
                .hasValidQueryMetrics(qmEnabled)
                .build();

            validateQuerySuccess(queryObservable.byPage(), validator);
        }
    }

    public void bulkInsert() {
        generateTestData();
        voidBulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData() {

        Object[] values = new Object[]{null, false, true, "abc", "cdfg", "opqrs", "ttttttt", "xyz", "oo", "ppp"};
        for (int i = 0; i < values.length; i++) {
            InternalObjectNode d = new InternalObjectNode();
            d.setId(UUID.randomUUID().toString());
            BridgeInternal.setProperty(d, partitionKey, values[i]);
            docs.add(d);
        }

        for (int i = 0; i < numberOfDocsWithSamePartitionKey; i++) {
            InternalObjectNode d = new InternalObjectNode();
            BridgeInternal.setProperty(d, partitionKey, uniquePartitionKey);
            BridgeInternal.setProperty(d, "getResourceId", Integer.toString(i));
            BridgeInternal.setProperty(d, field, i + 1);
            d.setId(UUID.randomUUID().toString());
            docs.add(d);
        }

        numberOfDocumentsWithNumericId = numberOfDocuments - values.length - numberOfDocsWithSamePartitionKey;
        for (int i = 0; i < numberOfDocumentsWithNumericId; i++) {
            InternalObjectNode d = new InternalObjectNode();
            BridgeInternal.setProperty(d, partitionKey, i + 1);
            d.setId(UUID.randomUUID().toString());
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

            query = String.format(aggregateSinglePartitionQueryFormatSelect, config.operator, field, partitionKey,
            uniquePartitionKey);
            testName = String.format("%s SinglePartition %s", config.operator, "SELECT");
            queryConfigs.add(new QueryConfig(testName, query, new Document("{'$1':" + config.expected + "}")));
        }
    }

    public void generateMultiAggregateQueryConfig(){
        List<MultiAggregateConfig> configList = new ArrayList<>();

        List<Integer> integers = docs.stream()
                                     .filter(cosmosItemProperties -> cosmosItemProperties.has(field))
                                     .map(cosmosItemProperties -> cosmosItemProperties.getInt(field))
                                     .collect(Collectors.toList());

        int min = Collections.min(integers);
        int max = Collections.max(integers);
        double sum = integers.stream().mapToInt(Integer::intValue).sum();
        int count = integers.size();
        double avg = sum / count;

        String multiAggregateQueryFormat = "SELECT %s(r.field), %s(r.field) FROM r";
        configList.add(new MultiAggregateConfig("MIN", "MAX", min, max));
        configList.add(new MultiAggregateConfig("Avg", "Sum", avg, sum));
        configList.add(new MultiAggregateConfig("Count", "Min", count, min));

        for (MultiAggregateConfig config : configList) {
            String query = String.format(multiAggregateQueryFormat, config.operator1, config.operator2);
            String testName = String.format("%s, %s multi aggregates", config.operator1, config.operator2);
            multiAggregateQueryConfigs.add(new QueryConfig(testName,
                                                           query,
                                                           new Document("{'$1':" + config.expected1
                                                                            + ",'$2':" + config.expected2 + "}")));
        }

        // Adding a multi aggregate query with aliases
        String query = "SELECT Max(r.field) as max_field, Min(r.field) as min_field FROM r";
        multiAggregateQueryConfigs.add(new QueryConfig("max, min with alias",
                                                       query,
                                                       new Document("{'max_field':" + max
                                                                        + ",'min_field':" + min + "}")));
    }

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT)
    public void queryDocumentsWithMultipleAggregates() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setQueryMetricsEnabled(false);
        options.setMaxDegreeOfParallelism(2);

        for (QueryConfig queryConfig : multiAggregateQueryConfigs) {
            CosmosPagedFlux<JsonNode> queryFlux = createdCollection
                                                                        .queryItems(queryConfig.query, options,
                                                                                    JsonNode.class);
            FeedResponseListValidator<JsonNode> validator = new FeedResponseListValidator.Builder<JsonNode>()
                                                                            .withAggregateValue(queryConfig.expected)
                                                                            .numberOfPages(1)
                                                                            .build();

            validateQuerySuccess(queryFlux.byPage(), validator);
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

    @BeforeClass(groups = { "simple" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_AggregateQueryTests() throws Throwable {
        client = this.getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        try {
            truncateCollection(createdCollection);
        } catch (Throwable throwable) {
            throwable = Exceptions.unwrap(throwable);
            if (!(throwable instanceof CosmosException)) {
                throw new AssertionError(lenientFormat("stopping test due to collection %s truncation failure: ",
                    createdCollection,
                    throwable));
            }
            logger.error("ignored: collection {} truncation failure: ", createdCollection, throwable);
        }
        bulkInsert();
        generateTestConfigs();
        generateMultiAggregateQueryConfig();
        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());
    }
}
