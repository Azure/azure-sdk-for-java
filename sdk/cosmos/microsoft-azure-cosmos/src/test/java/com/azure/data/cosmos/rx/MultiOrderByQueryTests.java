// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CompositePath;
import com.azure.data.cosmos.CompositePathSortOrder;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.ComparatorUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MultiOrderByQueryTests extends TestSuiteBase {

    private static final int TIMEOUT = 35000;
    private static final String NUMBER_FIELD = "numberField";
    private static final String STRING_FIELD = "stringField";
    private static final String NUMBER_FIELD_2 = "numberField2";
    private static final String STRING_FIELD_2 = "stringField2";
    private static final String BOOL_FIELD = "boolField";
    private static final String NULL_FIELD = "nullField";
    private static final String OBJECT_FIELD = "objectField";
    private static final String ARRAY_FIELD = "arrayField";
    private static final String SHORT_STRING_FIELD = "shortStringField";
    private static final String MEDIUM_STRING_FIELD = "mediumStringField";
    private static final String LONG_STRING_FIELD = "longStringField";
    private static final String PARTITION_KEY = "pk";
    private List<CosmosItemProperties> documents = new ArrayList<CosmosItemProperties>();
    private CosmosContainer documentCollection;
    private CosmosClient client;

    class CustomComparator implements Comparator<CosmosItemProperties> {
        String path;
        CompositePathSortOrder order;
        boolean isNumericPath = false;
        boolean isStringPath = false;
        boolean isBooleanPath = false;
        boolean isNullPath = false;
        
        public CustomComparator(String path, CompositePathSortOrder order) {
            this.path = path;
            this.order = order;
            if (this.path.contains("number")) {
                isNumericPath = true;
            } else if (this.path.toLowerCase().contains("string")) {
                isStringPath = true;
            } else if (this.path.contains("bool")) {
                isBooleanPath = true;
            } else if (this.path.contains("null")) {
                isNullPath = true;
            }
        }

        @Override
        public int compare(CosmosItemProperties doc1, CosmosItemProperties doc2) {
            boolean isAsc = order == CompositePathSortOrder.ASCENDING;
            if (isNumericPath) {
                if (doc1.getInt(path) < doc2.getInt(path))
                    return isAsc ? -1 : 1;
                else if (doc1.getInt(path) > doc2.getInt(path))
                    return isAsc ? 1 : -1;
                else
                    return 0;
            } else if (isStringPath) {
                if (!isAsc) {
                    CosmosItemProperties temp = doc1;
                    doc1 = doc2;
                    doc2 = temp;
                }
                return doc1.getString(path).compareTo(doc2.getString(path));
            } else if (isBooleanPath) {
                if (doc1.getBoolean(path) == false && doc2.getBoolean(path) == true)
                    return isAsc ? -1 : 1;
                else if (doc1.getBoolean(path) == true && doc2.getBoolean(path) == false)
                    return isAsc ? 1 : -1;
                else
                    return 0;
            } else if (isNullPath) {
                // all nulls are equal
                return 0; 
            } else {
                throw new IllegalStateException("data type not handled by comparator!");
            }
        }
    }

    @Factory(dataProvider = "clientBuilders")
    public MultiOrderByQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();
        documentCollection = getSharedMultiPartitionCosmosContainerWithCompositeAndSpatialIndexes(client);
        truncateCollection(documentCollection);

        int numberOfDocuments = 4;

        Random random = new Random();
        for (int i = 0; i < numberOfDocuments; ++i) {
            CosmosItemProperties multiOrderByDocument = generateMultiOrderByDocument();
            String multiOrderByDocumentString = multiOrderByDocument.toJson();
            int numberOfDuplicates = 5;

            for (int j = 0; j < numberOfDuplicates; j++) {
                // Add the document itself for exact duplicates
                CosmosItemProperties initialDocument = new CosmosItemProperties(multiOrderByDocumentString);
                initialDocument.id(UUID.randomUUID().toString());
                this.documents.add(initialDocument);

                // Permute all the fields so that there are duplicates with tie breaks
                CosmosItemProperties numberClone = new CosmosItemProperties(multiOrderByDocumentString);
                BridgeInternal.setProperty(numberClone, NUMBER_FIELD, random.nextInt(5));
                numberClone.id(UUID.randomUUID().toString());
                this.documents.add(numberClone);

                CosmosItemProperties stringClone = new CosmosItemProperties(multiOrderByDocumentString);
                BridgeInternal.setProperty(stringClone, STRING_FIELD, Integer.toString(random.nextInt(5)));
                stringClone.id(UUID.randomUUID().toString());
                this.documents.add(stringClone);

                CosmosItemProperties boolClone = new CosmosItemProperties(multiOrderByDocumentString);
                BridgeInternal.setProperty(boolClone, BOOL_FIELD, random.nextInt(2) % 2 == 0);
                boolClone.id(UUID.randomUUID().toString());
                this.documents.add(boolClone);

                // Also fuzz what partition it goes to
                CosmosItemProperties partitionClone = new CosmosItemProperties(multiOrderByDocumentString);
                BridgeInternal.setProperty(partitionClone, PARTITION_KEY, random.nextInt(5));
                partitionClone.id(UUID.randomUUID().toString());
                this.documents.add(partitionClone);
            }
        }

        voidBulkInsertBlocking(documentCollection, documents);

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    private CosmosItemProperties generateMultiOrderByDocument() {
        Random random = new Random();
        CosmosItemProperties document = new CosmosItemProperties();
        document.id(UUID.randomUUID().toString());
        BridgeInternal.setProperty(document, NUMBER_FIELD, random.nextInt(5));
        BridgeInternal.setProperty(document, NUMBER_FIELD_2, random.nextInt(5));
        BridgeInternal.setProperty(document, BOOL_FIELD, (random.nextInt() % 2) == 0);
        BridgeInternal.setProperty(document, STRING_FIELD, Integer.toString(random.nextInt(5)));
        BridgeInternal.setProperty(document, STRING_FIELD_2, Integer.toString(random.nextInt(5)));
        BridgeInternal.setProperty(document, NULL_FIELD, null);
        BridgeInternal.setProperty(document, OBJECT_FIELD, "");
        BridgeInternal.setProperty(document, ARRAY_FIELD, (new ObjectMapper()).createArrayNode());
        BridgeInternal.setProperty(document, SHORT_STRING_FIELD, "a" + random.nextInt(100));
        BridgeInternal.setProperty(document, MEDIUM_STRING_FIELD, "a" + random.nextInt(128) + 100);
        BridgeInternal.setProperty(document, LONG_STRING_FIELD, "a" + random.nextInt(255) + 128);
        BridgeInternal.setProperty(document, PARTITION_KEY, random.nextInt(5));
        return document;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocumentsWithMultiOrder() throws CosmosClientException, InterruptedException {
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);

        boolean[] booleanValues = new boolean[] {true, false};
        CosmosContainerProperties containerSettings = documentCollection.read().block().properties();
        Iterator<List<CompositePath>> compositeIndexesIterator = containerSettings.indexingPolicy().compositeIndexes().iterator();
        while (compositeIndexesIterator.hasNext()) {
        List<CompositePath> compositeIndex = compositeIndexesIterator.next();
            // for every order
            for (boolean invert : booleanValues) {
                // for normal and inverted order
                for (boolean hasTop : booleanValues) {
                    // with and without top
                    for (boolean hasFilter : booleanValues) {
                        // with and without filter
                        // Generate a multi order by from that index
                        List<String> orderByItems = new ArrayList<String>();
                        List<String> selectItems = new ArrayList<String>();
                        boolean isDesc;
                        Iterator<CompositePath> compositeIndexiterator = compositeIndex.iterator();
                        while (compositeIndexiterator.hasNext()) {
                            CompositePath compositePath = compositeIndexiterator.next();
                            isDesc = compositePath.order() == CompositePathSortOrder.DESCENDING ? true : false;
                            if (invert) {
                                isDesc = !isDesc;
                            }

                            String isDescString = isDesc ? "DESC" : "ASC";
                            String compositePathName = compositePath.path().replaceAll("/", "");
                            String orderByItemsString = "root." + compositePathName + " " + isDescString;
                            String selectItemsString = "root." + compositePathName;
                            orderByItems.add(orderByItemsString);
                            selectItems.add(selectItemsString);
                        }

                        int topCount = 10;
                        StringBuilder selectItemStringBuilder = new StringBuilder();
                        for (String selectItem: selectItems) {
                            selectItemStringBuilder.append(selectItem);
                            selectItemStringBuilder.append(",");
                        }
                        selectItemStringBuilder.deleteCharAt(selectItemStringBuilder.length() - 1);
                        StringBuilder orderByItemStringBuilder = new StringBuilder();
                        for (String orderByItem : orderByItems) {
                            orderByItemStringBuilder.append(orderByItem);
                            orderByItemStringBuilder.append(",");
                        }
                        orderByItemStringBuilder.deleteCharAt(orderByItemStringBuilder.length() - 1);
                        
                        String topString = hasTop ? "TOP " + topCount : "";
                        String whereString = hasFilter ? "WHERE root." + NUMBER_FIELD  + " % 2 = 0" : "";
                        String query = "SELECT " + topString + " [" + selectItemStringBuilder.toString() + "] " + 
                                "FROM root " + whereString + " " +
                                "ORDER BY " + orderByItemStringBuilder.toString();
                        
                        List<CosmosItemProperties> expectedOrderedList = top(sort(filter(this.documents, hasFilter), compositeIndex, invert), hasTop, topCount) ;
                        
                        Flux<FeedResponse<CosmosItemProperties>> queryObservable = documentCollection.queryItems(query, feedOptions);

                        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator
                                .Builder<CosmosItemProperties>()
                                .withOrderedResults(expectedOrderedList, compositeIndex)
                                .build();

                        validateQuerySuccess(queryObservable, validator);
                    }
                }
            }
        }
        
        // CREATE document with numberField not set.
        // This query would then be invalid.
        CosmosItemProperties documentWithEmptyField = generateMultiOrderByDocument();
        BridgeInternal.remove(documentWithEmptyField, NUMBER_FIELD);
        documentCollection.createItem(documentWithEmptyField, new CosmosItemRequestOptions()).block();
        String query = "SELECT [root." + NUMBER_FIELD + ",root." + STRING_FIELD + "] FROM root ORDER BY root." + NUMBER_FIELD + " ASC ,root." + STRING_FIELD + " DESC";
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = documentCollection.queryItems(query, feedOptions);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(UnsupportedOperationException.class)
                .build();

        validateQueryFailure(queryObservable, validator);
    }

    private List<CosmosItemProperties> top(List<CosmosItemProperties> arrayList, boolean hasTop, int topCount) {
        List<CosmosItemProperties> result = new ArrayList<CosmosItemProperties>();
        int counter = 0;
        if (hasTop) {
            while (counter < topCount && counter < arrayList.size()) {
                result.add(arrayList.get(counter));
                counter++;
            }
        } else {
            result.addAll(arrayList);
        }
        return result;
    }

    private List<CosmosItemProperties> sort(List<CosmosItemProperties> arrayList, List<CompositePath> compositeIndex,
                                                 boolean invert) {
        Collection<Comparator<CosmosItemProperties>> comparators = new ArrayList<Comparator<CosmosItemProperties>>();
        Iterator<CompositePath> compositeIndexIterator = compositeIndex.iterator();
        while (compositeIndexIterator.hasNext()) {
            CompositePath compositePath = compositeIndexIterator.next();
            CompositePathSortOrder order = compositePath.order();
            if (invert) {
                if (order == CompositePathSortOrder.DESCENDING) {
                    order = CompositePathSortOrder.ASCENDING;
                } else {
                    order = CompositePathSortOrder.DESCENDING;
                }
            }
            String path = compositePath.path().replace("/", "");
            comparators.add(new CustomComparator(path, order));
        }
        Collections.sort(arrayList, ComparatorUtils.chainedComparator(comparators));
        return arrayList;
    }

    private List<CosmosItemProperties> filter(List<CosmosItemProperties> cosmosItemSettings, boolean hasFilter) {
        List<CosmosItemProperties> result = new ArrayList<CosmosItemProperties>();
        if (hasFilter) {
            for (CosmosItemProperties document : cosmosItemSettings) {
                if (document.getInt(NUMBER_FIELD) % 2 == 0) {
                    result.add(document);
                }
            }
        } else {
            result.addAll(cosmosItemSettings);
        }
        return result;
    }
}
