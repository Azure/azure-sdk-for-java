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

import java.util.ArrayList;
import java.util.UUID;

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;

import reactor.core.publisher.Flux;

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;

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
    private ArrayList<CosmosItemSettings> docs = new ArrayList<CosmosItemSettings>();
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
        this.clientBuilder = clientBuilder;
    }


    // TODO: DANOBLE: Investigate Direct TCP performance issue
    // Links: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028
    // Notes:
    // I've seen this test time out in my development environment. I test against a debug instance of the public
    // emulator and so what I'm seeing could be the result of a public emulator performance issue. Of course, it
    // might also be the result of a Tcp protocol performance problem.

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsWithAggregates(boolean qmEnabled) throws Exception {

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setPopulateQueryMetrics(qmEnabled);
        options.setMaxDegreeOfParallelism(2);

        for (QueryConfig queryConfig : queryConfigs) {    

            Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(queryConfig.query, options);

            FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .withAggregateValue(queryConfig.expected)
                .numberOfPages(1)
                .hasValidQueryMetrics(qmEnabled)
                .build();

            try {
                validateQuerySuccess(queryObservable, validator);
            } catch (Throwable error) {
                if (this.clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
                    String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                    logger.info(message, error);
                    throw new SkipException(message, error);
                }
                throw error;
            }
        }
    }

    public void bulkInsert() {
        generateTestData();
        bulkInsertBlocking(createdCollection, docs);
    }

    public void generateTestData() {

        Object[] values = new Object[]{null, false, true, "abc", "cdfg", "opqrs", "ttttttt", "xyz", "oo", "ppp"};
        for (int i = 0; i < values.length; i++) {
            CosmosItemSettings d = new CosmosItemSettings();
            d.setId(UUID.randomUUID().toString());
            d.set(partitionKey, values[i]);
            docs.add(d);
        }

        for (int i = 0; i < numberOfDocsWithSamePartitionKey; i++) {
            CosmosItemSettings d = new CosmosItemSettings();
            d.set(partitionKey, uniquePartitionKey);
            d.set("resourceId", Integer.toString(i));
            d.set(field, i + 1);
            d.setId(UUID.randomUUID().toString());
            docs.add(d);
        }

        numberOfDocumentsWithNumericId = numberOfDocuments - values.length - numberOfDocsWithSamePartitionKey;
        for (int i = 0; i < numberOfDocumentsWithNumericId; i++) {
            CosmosItemSettings d = new CosmosItemSettings();
            d.set(partitionKey, i + 1);
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

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT * 100)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        bulkInsert();
        generateTestConfigs();

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }
}
