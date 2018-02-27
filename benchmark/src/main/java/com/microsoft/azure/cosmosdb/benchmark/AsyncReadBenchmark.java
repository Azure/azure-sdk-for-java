package com.microsoft.azure.cosmosdb.benchmark;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang3.RandomStringUtils;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AsyncReadBenchmark implements AsyncBenchmark {

    private AsyncDocumentClient client;
    private DocumentCollection collection;
    private Database database;
    private String partitionKey;
    private Configuration cfg;
    private ArrayList<Document> docsToRead = new ArrayList<Document>();

    public AsyncReadBenchmark(Configuration cfg) {

        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cfg.getServiceEndpoint())
                .withMasterKey(cfg.getMasterKey())
                .withConnectionPolicy(cfg.getConnectionPolicy())
                .withConsistencyLevel(cfg.getConsistencyLevel())
                .build();

        database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
        collection = DocDBUtils.getCollection(client, database.getSelfLink(),
                cfg.getCollectionId());
        partitionKey = collection.getPartitionKey().getPaths().iterator().next().split("/")[1];
        this.cfg = cfg;
        ArrayList<Observable<Document>> k = new ArrayList<Observable<Document>>();
        String dataFieldValue = RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize());

        for (int i = 0; i < 1000; i++) {
            String uuid = UUID.randomUUID().toString();
            Document newDoc = new Document();
            newDoc.setId(uuid);
            newDoc.set(partitionKey, uuid);
            newDoc.set("dataField1", dataFieldValue);
            newDoc.set("dataField2", dataFieldValue);
            newDoc.set("dataField3", dataFieldValue);
            newDoc.set("dataField4", dataFieldValue);
            newDoc.set("dataField5", dataFieldValue);
            Observable<Document> obs = client.createDocument(collection.getSelfLink(), newDoc, null, false).map(r -> r.getResource());
            k.add(obs);
        }
        docsToRead = (ArrayList<Document>) Observable.merge(k, 100).toList().toBlocking().single();
    }

    @Override
    public void run() throws InterruptedException {
        Semaphore sem = new Semaphore(cfg.getConcurrency());

        long startTime = System.currentTimeMillis();
        for(long i = 0; i < cfg.getNumberOfOperations(); i++) {
            Subscriber<ResourceResponse<Document>> subs = new Subscriber<ResourceResponse<Document>>() {

                @Override
                public void onStart() {
                }

                @Override
                public void onCompleted() {
                    sem.release();
                }

                @Override
                public void onError(Throwable e) {
                    System.err.println(Thread.currentThread().getName());
                    System.err.println("sem is " + sem.availablePermits());

                    sem.release();
                    e.printStackTrace();
                }

                @Override
                public void onNext(ResourceResponse<Document> r) {
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

            int index = (int) (i % docsToRead.size());
            RequestOptions options = new RequestOptions();
            options.setPartitionKey(new PartitionKey(docsToRead.get(index).getId()));

            sem.acquire();
            Observable<ResourceResponse<Document>> obs = client.readDocument(docsToRead.get(index).getSelfLink(), options);
            obs.subscribeOn(Schedulers.computation()).subscribe(subs);
        }

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("It took [%d] seconds to read [%d] documents.", (int) ((endTime - startTime)/1000), cfg.getNumberOfOperations()));
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
