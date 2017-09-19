package com.microsoft.azure.documentdb.benchmark;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.ResourceResponse;

/**
 * BulkInsert parses the command line configuration.
 * It creates a DocumentClient using the provided configuration.
 * And will insert the documents concurrently.
 * 
 * Each few seconds it will print out the rate at which documents were inserted.
 */
public abstract class AbstractBulkInsertBenchmark {

    private final MetricRegistry metricsRegistry = new MetricRegistry();
    private final ScheduledReporter reporter;
    protected final Configuration cfg;
    protected final Database database;
    protected final DocumentCollection collection;
    protected final Logger logger;

    private Meter insertMeter;
    private Meter insertFailureMeter;
    private CountDownLatch latch;

    public AbstractBulkInsertBenchmark(Configuration cfg, Database database, DocumentCollection collection) throws DocumentClientException {
        this.cfg = cfg;
        this.database = database;
        this.collection = collection;
        this.reporter = ConsoleReporter.forRegistry(metricsRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public interface CallBack<T> {
        void invoke(T arg);
    }

    protected abstract void onInit() throws Exception;
    protected abstract void onShutdown() throws Exception;

    public void init() throws Exception {
        onInit();
        this.latch = new CountDownLatch(cfg.getNumberOfDocumentsToInsert());
    }

    public void shutdown() throws Exception {
        onShutdown();
    }

    protected abstract void createDocument(Document docToInsert, CallBack<ResourceResponse<Document>> onSuccess, CallBack<Throwable> onFailure);

    public void run() throws Exception {

        String uuid = UUID.randomUUID().toString();
        
        String docToInsertTemplate = "{ "
                + "\"id\": \"%s\", "
                + "\"%s\": \"%s\","
                + "\"dataField\": \"" +  RandomStringUtils.randomAlphabetic(cfg.getDocumentDataFieldSize()) + "\" }";        

        this.insertMeter =  metricsRegistry.meter("DocInsert(Success)");
        this.insertFailureMeter = metricsRegistry.meter("DocInsert(Failure)");

        this.reporter.start(10, TimeUnit.SECONDS);        

        long startTime = System.currentTimeMillis();

        CallBack<ResourceResponse<Document>> onSuccess = new CallBack<ResourceResponse<Document>>() {
            @Override
            public void invoke(ResourceResponse<Document> arg) {

                insertMeter.mark();
                logger.debug("Creating document succeeded");
                latch.countDown();
            }
        };

        CallBack<Throwable> onFailure = new CallBack<Throwable>() {

            @Override
            public void invoke(Throwable t) {

                insertFailureMeter.mark();
                if (t instanceof DocumentClientException ) {
                    DocumentClientException e = (DocumentClientException) t;
                    if (e.getError() != null) {
                        logger.error("Inserting document failed {}", e.getError().getMessage());
                    } else {
                        logger.error("Inserting document failed", e);
                    }
                } else {
                    logger.error("Inserting document failed", t);
                }
                latch.countDown();
            }
        };

        for (int i = 0; i < cfg.getNumberOfDocumentsToInsert(); i++ ) {
            String id = uuid + i;
            String pk = uuid + i;
            Document docToInsert = new Document(String.format(docToInsertTemplate, id , cfg.getPartitionKey(), pk));

            createDocument(docToInsert, onSuccess, onFailure);
        }
        // wait till all inserts finish (either successfully or failure)
        logger.info("All Inserts Jobs are submitted. Waiting for result ...");

        latch.await();

        long endTime = System.currentTimeMillis();

        reporter.report();
        reporter.close();

        System.out.println(String.format("It took [%d] seconds to insert [%d] documents.", (int) ((endTime - startTime)/1000), cfg.getNumberOfDocumentsToInsert()));
    }
}
