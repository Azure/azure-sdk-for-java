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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class OrderbyDocumentQueryTest extends TestSuiteBase {
    private final static String DATABASE_ID = getDatabaseId(OrderbyDocumentQueryTest.class);
    private final double minQueryRequestChargePerPartition = 2.0;

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private List<Document> createdDocuments = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    private int numberOfPartitions;

    @Factory(dataProvider = "clientBuilders")
    public OrderbyDocumentQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsValidateContent() throws Exception {

        Document expectedDocument = createdDocuments.get(0);

        String query = 
                String.format("SELECT * from root r where r.propStr = '%s'"
                        + " ORDER BY r.propInt", 
                        expectedDocument.getString("propStr"));

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        List<String> expectedResourceIds = new ArrayList<>();
        expectedResourceIds.add(expectedDocument.getResourceId());

        Map<String, ResourceValidator<Document>> resourceIDToValidator = new HashMap<>();

        resourceIDToValidator.put(expectedDocument.getResourceId(), 
                new ResourceValidator.Builder<Document>().areEqual(expectedDocument).build());

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .numberOfPages(1)
                .containsExactly(expectedResourceIds)
                .validateAllResources(resourceIDToValidator)
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {
        String query = "SELECT * from root r where r.id = '2' ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @DataProvider(name = "sortOrder")
    public Object[][] sortOrder() {
        return new Object[][] { { "ASC" }, {"DESC"} };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderBy(String sortOrder) throws Exception {
        String query = String.format("SELECT * FROM r ORDER BY r.propInt %s", sortOrder);
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        if ("DESC".equals(sortOrder)) {
            Collections.reverse(expectedResourceIds);
        }

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByInt() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByString() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propStr";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        Comparator<String> validatorComparator = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propStr", d -> d.getString("propStr"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @DataProvider(name = "topValue")
    public Object[][] topValueParameter() {
        return new Object[][] { { 0 }, { 1 }, { 5 }, { createdDocuments.size() - 1 }, { createdDocuments.size() },
            { createdDocuments.size() + 1 }, { 2 * createdDocuments.size() } };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider =  "topValue")
    public void queryOrderWithTop(int topValue) throws Exception {
        String query = String.format("SELECT TOP %d * FROM r ORDER BY r.propInt", topValue);
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = 
                sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator)
                .stream().limit(topValue).collect(Collectors.toList());

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<Document> validator = new FeedResponseListValidator.Builder<Document>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Document>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * (topValue > 0 ? minQueryRequestChargePerPartition : 1))
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    private <T> List<String> sortDocumentsAndCollectResourceIds(String propName, Function<Document, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                .filter(d -> d.getHashMap().containsKey(propName)) // removes undefined
                .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                .map(d -> d.getResourceId()).collect(Collectors.toList());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void crossPartitionQueryNotEnabled() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(getCollectionLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    public Document createDocument(AsyncDocumentClient client, Map<String, Object> keyValueProps)
            throws DocumentClientException {
        Document docDefinition = getDocumentDefinition(keyValueProps);
        return client.createDocument(getCollectionLink(), docDefinition, null, false)
                .toBlocking().single()
                .getResource();
    }

    public List<Document> bulkInsert(AsyncDocumentClient client, List<Map<String, Object>> keyValuePropsList)
            throws DocumentClientException {

        ArrayList<Observable<ResourceResponse<Document>>> result = new ArrayList<Observable<ResourceResponse<Document>>>();

        for(Map<String, Object> keyValueProps: keyValuePropsList) {
            Document docDefinition = getDocumentDefinition(keyValueProps);
            Observable<ResourceResponse<Document>> obs = client.createDocument(getCollectionLink(), docDefinition, null, false);
            result.add(obs);
        }

        return Observable.merge(result, 100).
                map(resp -> resp.getResource())
                .toList().toBlocking().single();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(10100);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition(), options);

        List<Map<String, Object>> keyValuePropsList = new ArrayList<>();
        Map<String, Object> props = new HashMap<>();

        for(int i = 0; i < 30; i++) {
            props = new HashMap<>();
            props.put("propInt", i);
            props.put("propStr", String.valueOf(i));
            keyValuePropsList.add(props);
        }

        //undefined values
        props = new HashMap<>();
        keyValuePropsList.add(props);

        // null values
        props = new HashMap<>();
        props.put("propInt", null);
        props.put("propStr", null);
        keyValuePropsList.add(props);

        createdDocuments = bulkInsert(client, keyValuePropsList);

        numberOfPartitions = client
                .readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Observable.from(p.getResults())).toList().toBlocking().single().size();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private static Document getDocumentDefinition(Map<String, Object> keyValuePair) {
        String uuid = UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        for(String key: keyValuePair.keySet()) {
            Object val = keyValuePair.get(key);
            sb.append("  ");
            sb.append("\"").append(key).append("\"").append(" :" );
            if (val == null) {
                sb.append("null");
            } else {
                sb.append(JSONObject.valueToString(val));
            }
            sb.append(",\n");
        }

        sb.append(String.format("  \"id\": \"%s\",\n", uuid));
        sb.append(String.format("  \"mypk\": \"%s\"\n", uuid));
        sb.append("}");

        return new Document(sb.toString());
    }

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }
}
