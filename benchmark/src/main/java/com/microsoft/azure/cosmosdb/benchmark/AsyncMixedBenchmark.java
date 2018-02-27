package com.microsoft.azure.cosmosdb.benchmark;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang3.RandomStringUtils;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AsyncMixedBenchmark implements AsyncBenchmark {

    private AsyncDocumentClient client;
    private DocumentCollection collection;
    private Database database;
    private String partitionKey;
    private Configuration cfg;
    private CountDownLatch latch;
    private ArrayList<Document> docsToRead = new ArrayList<Document>();

    public AsyncMixedBenchmark(Configuration cfg) {

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

        String uuid = UUID.randomUUID().toString();

        String docToInsertTemplate = "{ "
                + "\"id\": \"%s\", "
                + "\"%s\": \"%s\","
                + "\"dataField\": \"" +  RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize()) + "\" }";  
        long startTime = System.currentTimeMillis();

        for(long i = 0; i < cfg.getNumberOfOperations(); i++) {
            Subscriber<Document> subs = new Subscriber<Document>() {

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

                    sem.release();
                    latch.countDown();
                    e.printStackTrace();
                }

                @Override
                public void onNext(Document r) {
                }
            };

            if (i % 100000  == 0) {
                if (i == 0) {
                    continue;
                }
                System.out.println("total operations so far: " + i);
                long now = System.currentTimeMillis();
                System.out.println("total time (ms) so far: " + (now - startTime) + " rate " + i * 1000 / (now - startTime));
            }

            Observable<Document> obs = null;
            if (i % 10 == 0 && i % 100 != 0) {

                String id = uuid + i;
                String pk = uuid + i;
                Document docToInsert = new Document(String.format(docToInsertTemplate, id , partitionKey, pk));
                obs = client.createDocument(collection.getSelfLink(), docToInsert, null, false)
                        .map(rr -> rr.getResource());

            } else if (i % 100 == 0) {

                FeedOptions options = new FeedOptions();
                options.setMaxItemCount(10);
                options.setEnableCrossPartitionQuery(true);

                String sqlQuery = "Select top 100 * from c order by c._ts";
                obs = client.queryDocuments(collection.getSelfLink(), sqlQuery, options)
                        .map( frp -> frp.getResults().get(0));
            } else {

                Random r = new Random();
                int index = r.nextInt(1000);

                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

                obs =  client.readDocument(docsToRead.get(index).getSelfLink(), options)
                        .map(rr -> rr.getResource());
            }

            sem.acquire();
            obs.subscribeOn(Schedulers.computation()).subscribe(subs);
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println(String.format("It took [%d] seconds to read/insert/query [%d] documents.", (int) ((endTime - startTime)/1000), cfg.getNumberOfOperations()));
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
