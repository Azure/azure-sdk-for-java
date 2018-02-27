package com.microsoft.azure.cosmosdb.benchmark;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.benchmark.Configuration.Operation;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AsyncQueryBenchmark implements AsyncBenchmark {

    private AsyncDocumentClient client;
    private DocumentCollection collection;
    private Database database;
    private String partitionKey;
    private Configuration cfg;
    private CountDownLatch latch;
    private ArrayList<Document> docsToRead = new ArrayList<Document>();

    public AsyncQueryBenchmark(Configuration cfg) {

        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cfg.getServiceEndpoint())
                .withMasterKey(cfg.getMasterKey())
                .withConnectionPolicy(cfg.getConnectionPolicy())
                .withConsistencyLevel(cfg.getConsistencyLevel())
                .build();

        database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
        collection = DocDBUtils.getCollection(client, database.getSelfLink(),
                cfg.getCollectionId());
        partitionKey = collection.getPartitionKey().getPaths().iterator().next();
        this.cfg = cfg;
        this.latch = new CountDownLatch(cfg.getNumberOfOperations());
        ArrayList<Observable<Document>> k = new ArrayList<Observable<Document>>();

        for (int i = 0; i < 1000; i++) {
            String uuid = UUID.randomUUID().toString();
            Document document = new Document();
            document.setId(uuid);
            document.set(partitionKey, uuid);
            Observable<Document> obs = client.createDocument(collection.getSelfLink(), document, null, false).map(r -> r.getResource());
            k.add(obs);
        }
        docsToRead = (ArrayList<Document>) Observable.merge(k, 100).toList().toBlocking().single();
    }

    @Override
    public void run() throws InterruptedException {
        Semaphore sem = new Semaphore(cfg.getConcurrency());

        long startTime = System.currentTimeMillis();

        for(long i = 0; i < cfg.getNumberOfOperations(); i++) {
            Subscriber<FeedResponse<Document>> subs = new Subscriber<FeedResponse<Document>>() {
                long pageCount = 0;

                @Override
                public void onStart() {
                }

                @Override
                public void onCompleted() {
                    sem.release();
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    System.err.println(Thread.currentThread().getName());
                    System.err.println("sem is " + sem.availablePermits());
                    latch.countDown();
                    sem.release();
                    e.printStackTrace();
                }

                @Override
                public void onNext(FeedResponse<Document> r) {
                    pageCount++;
                    if (pageCount % 10000  == 0) {
                        if (pageCount == 0) {
                            return;
                        }
                        System.out.println("total pages so far: " + pageCount);
                        long now = System.currentTimeMillis();
                        System.out.println("total time (ms) so far: " + (now - startTime) + " rate " + pageCount * 1000 / (now - startTime));
                    }
                }
            };

            if (i % 100000  == 0) {
                if (i == 0) {
                    continue;
                }
                System.out.println("total reads so far: " + i);
                long now = System.currentTimeMillis();
                System.out.println("total time (ms) so far: " + (now - startTime) + " rate " + i * 1000 / (now - startTime));
            }

            sem.acquire();

            Observable<FeedResponse<Document>> obs = null;

            Random r = new Random();
            FeedOptions options = new FeedOptions();

            if (cfg.getOperationType() == Operation.QueryCross) {

                int index = r.nextInt(1000);
                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select * from c where c._rid = \"" + docsToRead.get(index).getResourceId() + "\"";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QuerySingle) {

                int index = r.nextInt(1000);
                String pk = docsToRead.get(index).getString("pk");
                options.setPartitionKey(new PartitionKey(pk));
                String sqlQuery = "Select * from c where c.pk = \"" + pk + "\"";
                obs =  client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QueryParallel) {

                options.setMaxItemCount(10);
                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select * from c";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QueryOrderby) {

                options.setMaxItemCount(10);
                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select * from c order by c._ts";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QueryAggregate) {

                options.setMaxItemCount(10);
                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select value max(c._ts) from c";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QueryAggregateTopOrderby) {

                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select top 1 value count(c) from c order by c._ts";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            } else if (cfg.getOperationType() == Operation.QueryTopOrderby) {

                options.setEnableCrossPartitionQuery(true);
                String sqlQuery = "Select top 1000 * from c order by c._ts";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            }

            obs.subscribeOn(Schedulers.computation()).subscribe(subs);
        }

        latch.await();
        System.out.println(latch.getCount());
        long endTime = System.currentTimeMillis();

        System.out.println(String.format("It took [%d] seconds to perform %s operations [%d] times.", (int) ((endTime - startTime)/1000), cfg.getOperationType(), cfg.getNumberOfOperations()));
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
