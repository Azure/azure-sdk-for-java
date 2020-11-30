// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Utils;
import org.apache.commons.lang3.RandomUtils;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This workflow is intended for session and above consistency levels.
 * <p>
 * This workflow first will create some documents in cosmosdb and will store them all in its local cache.
 * Then at each step will randomly will try to do a write, read its own write, or query for its own write.
 */
class ReadMyWriteWorkflow extends AsyncBenchmark<Document> {
    private final static String QUERY_FIELD_NAME = "prop";
    private final static String ORDER_BY_FIELD_NAME = "_ts";
    private final static int MAX_TOP_QUERY_COUNT = 2000;
    private AsyncDocumentClient client;
    private DocumentCollection collection;
    private String nameCollectionLink;

    private ConcurrentHashMap<Integer, Document> cache;
    private int cacheSize;

    ReadMyWriteWorkflow(Configuration cfg) {
        super(cfg);
    }

    @Override
    protected void init() {
        // TODO: move read my writes to use v4 APIs
        this.client =  CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);
        Database database = DocDBUtils.getDatabase(client, configuration.getDatabaseId());
        this.collection = DocDBUtils.getCollection(client, database.getSelfLink(), configuration.getCollectionId());
        this.nameCollectionLink = String.format("dbs/%s/colls/%s", database.getId(), collection.getId());

        this.cacheSize = configuration.getNumberOfPreCreatedDocuments();
        this.cache = new ConcurrentHashMap<>();
        this.populateCache();
    }

    @Override
    protected void performWorkload(BaseSubscriber<Document> baseSubscriber, long i) throws Exception {

        Flux<Document> obs;
        boolean readyMyWrite = RandomUtils.nextBoolean();

        if (readyMyWrite) {

            // will do a write and immediately upon success will either
            // do a point read
            // or single partition query
            // or cross partition query to find the write.

            int j = Math.toIntExact(Math.floorMod(i, 3));

            switch (j) {
                case 0:
                    // write a random document to cosmodb and update the cache.
                    // then try to read the document which just was written
                    obs = writeDocument()
                            .flatMap(this::readDocument);
                    break;
                case 1:
                    // write a random document to cosmodb and update the cache.
                    // then try to query for the document which just was written
                    obs = writeDocument()
                            .flatMap(d -> singlePartitionQuery(d)
                                    .switchIfEmpty(Flux.error(new NotFoundException(
                                            "couldn't find my write in a single partition query!"))));
                    break;
                case 2:
                    // write a random document to cosmosdb and update the cache.
                    // then try to query for the document which just was written
                    obs = writeDocument()
                            .flatMap(d -> xPartitionQuery(generateQuery(d))
                                    .switchIfEmpty(Flux.error(new NotFoundException(
                                            "couldn't find my write in a cross partition query!"))));
                    break;
                default:
                    assert false;
                    throw new IllegalStateException();
            }
        } else {

            // will either do
            // a write
            // a point read for a in memory cached document4
            // or single partition query for a in memory cached document
            // or cross partition query for a in memory cached document

            int j = Math.toIntExact(Math.floorMod(i, 4));

            switch (j) {
                case 0:
                    // write a random document to cosmosdb and update the cache
                    obs = writeDocument();
                    break;
                case 1:
                    // randomly choose a document from the cache and do a single point read
                    obs = readDocument(cache.get(cacheKey()));
                    break;
                case 2:
                    // randomly choose a document from the cache and do a single partition query
                    obs = singlePartitionQuery(cache.get(cacheKey()))
                            .switchIfEmpty(Flux.error(new NotFoundException(
                                    "couldn't find my cached write in a single partition query!")));
                    break;
                case 3:
                    // randomly choose a document from the cache and do a cross partition query
                    obs = xPartitionQuery(generateRandomQuery())
                            .switchIfEmpty(Flux.error(new NotFoundException(
                                    "couldn't find my cached write in a cross partition query!")));
                    break;
                default:
                    assert false;
                    throw new IllegalStateException();
            }
        }

        concurrencyControlSemaphore.acquire();
        logger.debug("concurrencyControlSemaphore: {}", concurrencyControlSemaphore);

        try {
            obs.subscribeOn(Schedulers.parallel()).subscribe(baseSubscriber);
        } catch (Throwable error) {
            concurrencyControlSemaphore.release();
            logger.error("subscription failed due to ", error);
        }
    }

    private void populateCache() {
        ArrayList<Flux<Document>> list = new ArrayList<>();
        for (int i = 0; i < cacheSize; i++) {
            Flux<Document> observable = writeDocument(i);
            list.add(observable);
        }

        logger.info("PRE-populating {} documents ....", cacheSize);
        Flux.merge(Flux.fromIterable(list), configuration.getConcurrency()).then().block();
        logger.info("Finished pre-populating {} documents", cacheSize);
    }

    /**
     * Writes a random document to cosmosdb and store it in a random location in the cache.
     *
     * @return Observable of document
     */
    private Flux<Document> writeDocument() {
        return writeDocument(null);
    }

    /**
     * Writes a random document to cosmosdb and store it in the slot i-th in the cache.
     *
     * @return Observable of document
     */
    private Flux<Document> writeDocument(Integer i) {
        String idString = Utils.randomUUID().toString();
        String randomVal = Utils.randomUUID().toString();
        Document document = new Document();
        document.setId(idString);
        BridgeInternal.setProperty(document, partitionKey, idString);
        BridgeInternal.setProperty(document, QUERY_FIELD_NAME, randomVal);
        BridgeInternal.setProperty(document, "dataField1", randomVal);
        BridgeInternal.setProperty(document, "dataField2", randomVal);
        BridgeInternal.setProperty(document, "dataField3", randomVal);
        BridgeInternal.setProperty(document, "dataField4", randomVal);

        Integer key = i == null ? cacheKey() : i;
        return client.createDocument(getCollectionLink(), document, null, false)
                     .retryWhen(Retry.max(5).filter((error) -> {
                         if (!(error instanceof CosmosException)) {
                             return false;
                         }
                         final CosmosException cosmosException = (CosmosException)error;
                         if (cosmosException.getStatusCode() == 410 ||
                             cosmosException.getStatusCode() == 408 ||
                             cosmosException.getStatusCode() == 429 ||
                             cosmosException.getStatusCode() == 503) {
                             return true;
                         }

                         return false;
                     }))
                     .onErrorResume(
                         (error) -> {
                             if (!(error instanceof CosmosException)) {
                                 return false;
                             }
                             final CosmosException cosmosException = (CosmosException)error;
                             if (cosmosException.getStatusCode() == 409) {
                                 return true;
                             }

                             return false;
                         },
                         (conflictException) -> client.readDocument(getDocumentLink(document), null)
                     )
                    .doOnNext(r -> cache.put(key, r.getResource()))
                    .map(ResourceResponse::getResource).flux();
    }

    /**
     * given a document tries to read it from cosmosdb
     *
     * @param d document to be read
     * @return Observable of document
     */
    private Flux<Document> readDocument(Document d) {
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(d.getString(partitionKey)));

        return client.readDocument(getDocumentLink(d), options)
                .map(ResourceResponse::getResource).flux();
    }

    /**
     * Generates a random query
     *
     * @return a randomly generated query
     */
    private SqlQuerySpec generateRandomQuery() {
        int docCount = RandomUtils.nextInt(1, 2);
        Set<Integer> keys = new HashSet<>();
        for (int i = 0; i < docCount; i++) {
            int key = RandomUtils.nextInt(0, cacheSize);
            keys.add(key);
        }
        List<Document> documentList = null;
        if (RandomUtils.nextBoolean()) {
            documentList = keys.stream().map(cache::get).collect(Collectors.toList());
        }

        int top = RandomUtils.nextInt(0, MAX_TOP_QUERY_COUNT);
        boolean useOrderBy = RandomUtils.nextBoolean();

        return generateQuery(documentList, top > 1000 ? top : null, useOrderBy);
    }

    /**
     * given a query returns the corresponding observable result
     *
     * @param query to find document
     * @return Observable document
     */
    private Flux<Document> xPartitionQuery(SqlQuerySpec query) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(-1);

        return client.<Document>queryDocuments(getCollectionLink(), query, options)
                .flatMap(p -> Flux.fromIterable(p.getResults()));
    }

    /**
     * given a document returns the corresponding observable result of issuing a single partition query
     * for the document.
     *
     * @param d document to be queried for.
     * @return Observable document
     */
    private Flux<Document> singlePartitionQuery(Document d) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(d.get(partitionKey)));

        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(String.format("Select top 100 * from c where c.%s = '%s'",
                                                                   QUERY_FIELD_NAME,
                                                                   d.getString(QUERY_FIELD_NAME)));
        return client.<Document>queryDocuments(getCollectionLink(), sqlQuerySpec, options)
                .flatMap(p -> Flux.fromIterable(p.getResults()));
    }

    /**
     * Given a document list generates a randomly generated sql query which can find only and only the documents
     * <p>
     * The generated query may have a top, orderby, top and orderby.
     *
     * @param documentList list of documents to be queried for
     * @return SqlQuerySpec
     */
    private SqlQuerySpec generateQuery(Document... documentList) {
        return generateQuery(Arrays.asList(documentList));
    }

    /**
     * Given a document list generates a randomly generated sql query which can find only and only the documents
     * <p>
     * The generated query may have a top, orderby, top and orderby.
     *
     * @param documentList list of documents to be queried for
     * @return SqlQuerySpec
     */
    private SqlQuerySpec generateQuery(List<Document> documentList) {
        int top = RandomUtils.nextInt(0, MAX_TOP_QUERY_COUNT);
        boolean useOrderBy = RandomUtils.nextBoolean();

        return generateQuery(documentList, top >= documentList.size() ? top : null, useOrderBy);
    }

    /**
     * Given a document list generates sql query which can find only and only the documents
     *
     * @param documentList lists of documents to find
     * @param topCount     if a valid top count, the query will have a top count
     * @param withOrderBy  if not null, the query will have an orderby clause
     * @return SqlQuerySpec
     */
    private SqlQuerySpec generateQuery(List<Document> documentList, Integer topCount, boolean withOrderBy) {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (withOrderBy) {
            queryBuilder.orderBy(ORDER_BY_FIELD_NAME);
        }
        if (documentList != null && !documentList.isEmpty()) {
            if (topCount != null) {
                topCount = Math.max(topCount, documentList.size());
            }

            queryBuilder.whereClause(QueryBuilder.WhereClause.InWhereClause.asInWhereClause(QUERY_FIELD_NAME, documentList));
        }

        if ((documentList == null || documentList.isEmpty()) && (topCount == null || topCount <= 0)) {
            topCount = 100;
        }

        if (topCount != null) {
            queryBuilder.top(topCount);
        }

        return queryBuilder.toSqlQuerySpec();
    }

    private int cacheKey() {
        return RandomUtils.nextInt(0, cacheSize);
    }

    /**
     * This is used for making random query generation with different terms (top, orderby) easier.
     */
    static class QueryBuilder {
        private String orderByFieldName;
        private Integer topCount;
        private WhereClause whereClause;

        QueryBuilder top(int top) {
            this.topCount = top;
            return this;
        }

        QueryBuilder orderBy(String fieldName) {
            this.orderByFieldName = fieldName;
            return this;
        }

        QueryBuilder whereClause(WhereClause whereClause) {
            this.whereClause = whereClause;
            return this;
        }

        static abstract class WhereClause {
            static class InWhereClause extends WhereClause {
                private final List<SqlParameter> parameters;
                private final String whereCondition;

                static InWhereClause asInWhereClause(String fieldName, List<Document> documentList) {
                    List<SqlParameter> parameters = new ArrayList<>(documentList.size());
                    for (int i = 0; i < documentList.size(); i++) {
                        Object value = documentList.get(i).get(fieldName);
                        SqlParameter sqlParameter = new SqlParameter("@param" + i, value);
                        parameters.add(sqlParameter);
                    }

                    return new InWhereClause(fieldName, parameters);
                }

                InWhereClause(String fieldName, List<SqlParameter> parameters) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(fieldName);
                    stringBuilder.append(" IN (");
                    List<String> params = parameters.stream().map(SqlParameter::getName).collect(Collectors.toList());
                    stringBuilder.append(String.join(", ", params));
                    stringBuilder.append(")");

                    this.whereCondition = stringBuilder.toString();
                    this.parameters = parameters;
                }

                @Override
                String getWhereCondition(String rootName) {
                    return rootName + "." + this.whereCondition;
                }

                @Override
                List<SqlParameter> getSqlParameterCollection() {
                    return this.parameters;
                }
            }

            abstract String getWhereCondition(String rootName);

            abstract List<SqlParameter> getSqlParameterCollection();
        }

        SqlQuerySpec toSqlQuerySpec() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT");

            if (topCount != null) {
                stringBuilder.append(" TOP ").append(topCount);
            }

            stringBuilder.append(" * FROM root");
            if (whereClause != null) {
                stringBuilder.append(" WHERE ");
                stringBuilder.append(whereClause.getWhereCondition("root"));

            }

            if (orderByFieldName != null) {
                stringBuilder.append(" ORDER BY ").append("root.").append(orderByFieldName);
            }

            return whereClause == null ?
                    new SqlQuerySpec(stringBuilder.toString()) :
                    new SqlQuerySpec(stringBuilder.toString(), whereClause.getSqlParameterCollection());
        }
    }

    protected String getCollectionLink() {
        if (configuration.isUseNameLink()) {
            return this.nameCollectionLink;
        } else {
            return collection.getSelfLink();
        }
    }

    protected String getDocumentLink(Document doc) {
        if (configuration.isUseNameLink()) {
            return this.nameCollectionLink + "/docs/" + doc.getId();
        } else {
            return doc.getSelfLink();
        }
    }
}
