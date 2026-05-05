package com.azure.cosmos.avadtest.reconciliation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * Writes reconciliation events to a shared Cosmos container using bulk upsert.
 * Collects events via {@link #add}, then flushes them as a single bulk batch
 * via {@link #flush}. The caller must flush after processing each CFP batch
 * to ensure all recon records are persisted before the lease checkpoints.
 *
 * Container: "reconciliation" in same database
 * Partition key: /correlationId
 */
public final class ReconciliationWriter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationWriter.class);
    private static final String RECONCILIATION_CONTAINER = "reconciliation";

    private final String source;
    private final CosmosAsyncContainer container;
    private final CosmosBulkExecutionOptions bulkOptions = new CosmosBulkExecutionOptions();
    private final LongAdder writeCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();

    // Batch buffer — not thread-safe; callers (handleChanges) are single-threaded per partition
    private final List<CosmosItemOperation> pending = new ArrayList<>();

    /**
     * @param client shared CosmosAsyncClient — caller owns lifecycle
     * @param database database name
     * @param source source identifier for reconciliation docs
     */
    public ReconciliationWriter(CosmosAsyncClient client, String database, String source) {
        this.source = source;

        this.container = client
            .getDatabase(database)
            .getContainer(RECONCILIATION_CONTAINER);

        log.info("ReconciliationWriter initialized: source={}, container={}",
            source, RECONCILIATION_CONTAINER);
    }

    /**
     * Buffer a reconciliation event for the next bulk flush.
     * Does not write to Cosmos — call {@link #flush} after the batch.
     */
    public void record(String eventId, long seqNo, String opType,
                       String partitionKey, long lsn, boolean hasPreviousImage, long crts) {
        ObjectNode doc = JsonNodeFactory.instance.objectNode();
        doc.put("id", source + "-" + eventId);
        doc.put("correlationId", eventId);
        doc.put("source", source);
        doc.put("seqNo", seqNo);
        doc.put("opType", opType);
        doc.put("partitionKey", partitionKey);
        doc.put("lsn", lsn);
        doc.put("hasPreviousImage", hasPreviousImage);
        doc.put("crts", crts);
        doc.put("timestamp", Instant.now().toString());

        pending.add(CosmosBulkOperations.getUpsertItemOperation(doc, new PartitionKey(eventId)));
    }

    /**
     * Flush all buffered events to the reconciliation container via bulk upsert.
     * Blocks until all writes complete. Throws on any permanent failure so
     * CFP's handleChanges fails and the lease does not advance.
     */
    public void flush() {
        if (pending.isEmpty()) return;

        List<CosmosItemOperation> batch = new ArrayList<>(pending);
        pending.clear();

        List<String> failures = new ArrayList<>();

        container.executeBulkOperations(Flux.fromIterable(batch), bulkOptions)
            .toStream()
            .forEach(response -> {
                if (response.getResponse() != null && response.getResponse().isSuccessStatusCode()) {
                    writeCount.increment();
                } else {
                    errorCount.increment();
                    int status = response.getResponse() != null ? response.getResponse().getStatusCode() : -1;
                    String id = response.getOperation().getId();
                    failures.add("id=" + id + " status=" + status);
                }
            });

        if (!failures.isEmpty()) {
            throw new RuntimeException(
                "Reconciliation bulk flush failed: " + failures.size() + " errors. First: " + failures.get(0));
        }
    }

    public long getWriteCount() { return writeCount.sum(); }
    public long getErrorCount() { return errorCount.sum(); }

    @Override
    public void close() {
        // Flush any remaining buffered events
        try { flush(); } catch (Exception e) { log.warn("Final flush failed: {}", e.getMessage()); }
        log.info("ReconciliationWriter closed: source={}, writes={}, errors={}",
            source, writeCount.sum(), errorCount.sum());
    }
}
