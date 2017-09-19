package com.microsoft.azure.documentdb.benchmark;

import java.util.concurrent.Semaphore;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DatabaseAccount;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient.Builder;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import rx.Observable;

public class RxAsyncBulkInsertBenchmark extends AbstractBulkInsertBenchmark {

    private AsyncDocumentClient rxDocumentClient;
    private final Semaphore throttle;
    
    RxAsyncBulkInsertBenchmark(Configuration cfg, Database database, DocumentCollection collection,
            DatabaseAccount databaseAccount) throws DocumentClientException {
        super(cfg, database, collection);

        // the throttle semaphore is used to throttle concurrent async creates
        // if we don't throttle when the pool exhausted the subsequent async
        // create will immediately fail
        this.throttle = new Semaphore(cfg.getConnectionPolicy().getMaxPoolSize());
    }

    @Override
    protected void onInit() throws Exception {
        
        if (cfg.isRxEnableNativeLinuxEpoll()) {
            System.out.println("Enabling Native Linux Transport...");
            RxNetty.useNativeTransportIfApplicable();
        }
        
        RxNetty.useMetricListenersFactory(new ServoEventsListenerFactory());
        Builder builder = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(cfg.getServiceEndpoint())
                .withMasterKey(cfg.getMasterKey())
                .withConnectionPolicy(cfg.getConnectionPolicy())
                .withConsistencyLevel(cfg.getConsistencyLevel());
        
        this.rxDocumentClient = builder.build();
    }

    @Override
    protected void onShutdown() throws Exception {
        rxDocumentClient.close();
    }

    @Override
    protected void createDocument(Document docToInsert, CallBack<ResourceResponse<Document>> onSuccess,
            CallBack<Throwable> onFailure) {
        try {
            throttle.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        try {
            // create the cold observable
            Observable<ResourceResponse<Document>> documentCreateObservable = rxDocumentClient
                    .createDocument(collection.getSelfLink(), docToInsert, null, false);
            
            // subscribing to the semaphore will start the async io document create http request
            documentCreateObservable.subscribe(result -> onSuccess.invoke(result), error -> {
                // if error happens release the throttle semaphore
                throttle.release();
                onFailure.invoke(error);
            }, () ->
            // when completes release the throttle semaphore
            throttle.release());
        } catch (Throwable t) {
            
            // if any failure in creating observable release the throttle semaphore
            throttle.release();
            onFailure.invoke(t);
        }
    }

    public static RxAsyncBulkInsertBenchmark fromConfiguration(Configuration cfg) throws DocumentClientException {
        try (DocumentClient client = new DocumentClient(cfg.getServiceEndpoint(), cfg.getMasterKey(),
                cfg.getConnectionPolicy(), cfg.getConsistencyLevel())) {

            Database database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
            DocumentCollection collection = DocDBUtils.getCollection(client, database.getSelfLink(),
                    cfg.getCollectionId());
            DatabaseAccount databaseAccount = client.getDatabaseAccount();

            return new RxAsyncBulkInsertBenchmark(cfg, database, collection, databaseAccount);
        }
    }
}
