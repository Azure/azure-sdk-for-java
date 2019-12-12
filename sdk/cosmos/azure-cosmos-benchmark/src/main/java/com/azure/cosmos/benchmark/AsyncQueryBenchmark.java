// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.SqlParameter;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.PartitionKey;
import com.codahale.metrics.Timer;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class AsyncQueryBenchmark extends AsyncBenchmark<FeedResponse<Document>> {

    private int pageCount = 0;

    class LatencySubscriber<T> extends BaseSubscriber<T> {

        Timer.Context context;
        BaseSubscriber<T> baseSubscriber;

        LatencySubscriber(BaseSubscriber<T> baseSubscriber) {
            this.baseSubscriber = baseSubscriber;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            super.hookOnSubscribe(subscription);
        }

        @Override
        protected void hookOnNext(T value) {
        }

        @Override
        protected void hookOnComplete() {
            context.stop();
            baseSubscriber.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            context.stop();
            baseSubscriber.onError(throwable);
        }
    }

    AsyncQueryBenchmark(Configuration cfg) {
        super(cfg);
    }

    @Override
    protected void onSuccess() {
        pageCount++;
        if (pageCount % 10000 == 0) {
            if (pageCount == 0) {
                return;
            }
            logger.info("total pages so far: {}", pageCount);
        }
    }

    @Override
    protected void performWorkload(BaseSubscriber<FeedResponse<Document>> baseSubscriber, long i) throws InterruptedException {
        Flux<FeedResponse<Document>> obs;
        Random r = new Random();
        FeedOptions options = new FeedOptions();

        if (configuration.getOperationType() == Configuration.Operation.QueryCross) {

            int index = r.nextInt(1000);
            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c where c._rid = \"" + docsToRead.get(index).getResourceId() + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QuerySingle) {

            int index = r.nextInt(1000);
            String pk = docsToRead.get(index).getString("pk");
            options.partitionKey(new PartitionKey(pk));
            String sqlQuery = "Select * from c where c.pk = \"" + pk + "\"";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryParallel) {

            options.maxItemCount(10);
            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryOrderby) {

            options.maxItemCount(10);
            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryAggregate) {

            options.maxItemCount(10);
            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select value max(c._ts) from c";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryAggregateTopOrderby) {

            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1 value count(c) from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryTopOrderby) {

            options.setEnableCrossPartitionQuery(true);
            String sqlQuery = "Select top 1000 * from c order by c._ts";
            obs = client.queryDocuments(getCollectionLink(), sqlQuery, options);
        } else if (configuration.getOperationType() == Configuration.Operation.QueryInClauseParallel) {

            ReadMyWriteWorkflow.QueryBuilder queryBuilder = new ReadMyWriteWorkflow.QueryBuilder();
            options.setEnableCrossPartitionQuery(true);
            options.setMaxDegreeOfParallelism(200);
            List<SqlParameter> parameters = new ArrayList<>();
            int j = 0;
            for(Document doc: docsToRead) {
                String partitionKeyValue = doc.getId();
                parameters.add(new SqlParameter("@param" + j, partitionKeyValue));
                j++;
            }

            queryBuilder.whereClause(new ReadMyWriteWorkflow.QueryBuilder.WhereClause.InWhereClause(partitionKey,
                                                                                                    parameters));

            SqlQuerySpec query = queryBuilder.toSqlQuerySpec();
            obs = client.queryDocuments(getCollectionLink(), query, options);

        } else {
            throw new IllegalArgumentException("Unsupported Operation: " + configuration.getOperationType());
        }

        concurrencyControlSemaphore.acquire();
        LatencySubscriber<FeedResponse> latencySubscriber = new LatencySubscriber(baseSubscriber);
        latencySubscriber.context = latency.time();
        obs.subscribeOn(Schedulers.parallel()).subscribe(latencySubscriber);
    }
}
