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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

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

        public AggregateConfig (String operator, Object expected, String condition) {
            this.operator = operator;
            this.expected = expected;
            this.condition = condition;
        }
    }

    public final static String DATABASE_ID = getDatabaseId(AggregateQueryTests.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private ArrayList<Document> docs = new ArrayList<Document>();
    private ArrayList<QueryConfig> queryConfigs = new ArrayList<QueryConfig>();

    private String partitionKey = "mypk";
    private String uniquePartitionKey = "uniquePartitionKey";
    private String field = "field";
    private int sum;
    private int numberOfDocuments = 800;
    private int numberOfDocumentsWithNumbericId;
    private int numberOfDocsWithSamePartitionKey = 400;

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public AggregateQueryTests(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithAggregates() throws Exception {

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxDegreeOfParallelism(2);

        for (QueryConfig queryConfig : queryConfigs) {    

            Observable<FeedResponse<Document>> queryObservable = client
                    .queryDocuments(createdCollection.getSelfLink(), queryConfig.query, options);

            FeedResponseListValidator<Document> validator = new FeedResponseListValidator
                    .Builder<Document>()
                    .withAggregateValue(queryConfig.expected)
                    .numberOfPages(1)
                    .build();

            validateQuerySuccess(queryObservable, validator);
        }
    }

    public void bulkInsert(AsyncDocumentClient client) {
        generateTestData();

        ArrayList<Observable<ResourceResponse<Document>>> result = new ArrayList<Observable<ResourceResponse<Document>>>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(client.createDocument("dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(), docs.get(i), null, false));
        }

        Observable.merge(result, 100).toList().toBlocking().single();
    }

    public void generateTestData() {

        Object[] values = new Object[]{null, false, true, "abc", "cdfg", "opqrs", "ttttttt", "xyz", "oo", "ppp"};
        for (int i = 0; i < values.length; i++) {
            Document d = new Document();
            d.set(partitionKey, values[i]);
            docs.add(d);
        }

        for (int i = 0; i < numberOfDocsWithSamePartitionKey; i++) {
            Document d = new Document();
            d.set(partitionKey, uniquePartitionKey);
            d.set("resourceId", Integer.toString(i));
            d.set(field, i + 1);
            docs.add(d);
        }

        numberOfDocumentsWithNumbericId = numberOfDocuments - values.length - numberOfDocsWithSamePartitionKey;
        for (int i = 0; i < numberOfDocumentsWithNumbericId; i++) {
            Document d = new Document();
            d.set(partitionKey, i + 1);
            docs.add(d);
        }

        sum = (int) (numberOfDocumentsWithNumbericId * (numberOfDocumentsWithNumbericId + 1) / 2.0);

    }

    public void generateTestConfigs() {

        String aggregateQueryFormat = "SELECT VALUE %s(r.%s) FROM r WHERE %s";
        AggregateConfig[] aggregateConfigs = new AggregateConfig[] {
                new AggregateConfig("AVG", sum / numberOfDocumentsWithNumbericId, String.format("IS_NUMBER(r.%s)", partitionKey)),
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
            queryConfigs.add(new QueryConfig(testName, query, new Document("{'$1':" + config.expected + "}")));
        }

    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterCLass() {        
        safeDeleteDatabase(client, DATABASE_ID);
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();

        Database d1 = new Database();
        d1.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d1);

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);

        createdCollection = client.createCollection("dbs/" + createdDatabase.getId(),
                getCollectionDefinition(), options).toBlocking().single().getResource();

        bulkInsert(client);
        generateTestConfigs();
    }
}
