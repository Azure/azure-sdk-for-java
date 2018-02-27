package com.microsoft.azure.cosmosdb.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AsyncQuerySinglePartitionMultiple implements AsyncBenchmark {

    private AsyncDocumentClient client;
    private DocumentCollection collection;
    private Database database;
    private Configuration cfg;
    private CountDownLatch latch;

    public AsyncQuerySinglePartitionMultiple(Configuration cfg) {

        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cfg.getServiceEndpoint())
                .withMasterKey(cfg.getMasterKey())
                .withConnectionPolicy(cfg.getConnectionPolicy())
                .withConsistencyLevel(cfg.getConsistencyLevel())
                .build();

        database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
        collection = DocDBUtils.getCollection(client, database.getSelfLink(),
                cfg.getCollectionId());
        this.cfg = cfg;
        this.latch = new CountDownLatch(cfg.getNumberOfOperations());
    }

    @Override
    public void run() throws InterruptedException {

        Semaphore sem = new Semaphore(cfg.getConnectionPolicy().getMaxPoolSize());
        Observable<FeedResponse<Document>> obs = null;
        FeedOptions options = new FeedOptions();
        options.setPartitionKey(new PartitionKey("pk"));
        options.setMaxItemCount(10);
        String sqlQuery = "Select * from c where c.pk = \"pk\"";

        long startTime = System.currentTimeMillis();
        for(long i = 0; i < cfg.getNumberOfOperations(); i++) {
            Subscriber<FeedResponse<Document>> subs = new Subscriber<FeedResponse<Document>>() {

                long start;
                @Override
                public void onStart() {
                    start = System.currentTimeMillis();
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

                    sem.release();
                    e.printStackTrace();
                }

                @Override
                public void onNext(FeedResponse<Document> r) {
                    if(r.getResults().size() > 0)
                        System.out.println(r.getResults().size());
                    System.out.println(r.getRequestCharge());
                    long now = System.currentTimeMillis();
                    System.out.println((now - this.start) + "\n");
                    this.start = now;
                }
            };

            if (i % 100000  == 0) {
                if (i == 0) {
                    continue;
                }
                System.out.println("total single partition queries so far: " + i);
                long now = System.currentTimeMillis();
                System.out.println("total time (ms) so far: " + (now - startTime) + " rate " + i * 1000 / (now - startTime));
            }

            sem.acquire();
            obs =  client.queryDocuments(collection.getSelfLink(), sqlQuery, options);
            obs.subscribeOn(Schedulers.computation()).subscribe(subs);
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println(String.format("It took [%d] seconds to perform %s operations [%d] times.", (int) ((endTime - startTime)/1000), cfg.getOperationType(), cfg.getNumberOfOperations()));
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
