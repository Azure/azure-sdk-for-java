package com.azure.cosmos.avadtest.reconciliation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

/**
 * Writes reconciliation events to a shared Cosmos container.
 * Synchronous — blocks until the write succeeds or is permanently dropped.
 * This ensures CFP does not checkpoint the lease until the reconciliation
 * record is persisted.
 *
 * Container: "reconciliation" in same database
 * Partition key: /correlationId
 */
public final class ReconciliationWriter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationWriter.class);
    private static final String RECONCILIATION_CONTAINER = "reconciliation";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_MS = 500;

    private static final CosmosEndToEndOperationLatencyPolicyConfig E2E_POLICY =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(6)).build();

    private final String source;
    private final CosmosAsyncContainer container;
    private final LongAdder writeCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder retryCount = new LongAdder();

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
     * Record a produced or consumed event for reconciliation.
     * Blocks until the write succeeds or all retries are exhausted.
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

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(E2E_POLICY);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                container.upsertItem(doc, new PartitionKey(eventId), options)
                    .block(Duration.ofSeconds(10));
                writeCount.increment();
                return;
            } catch (Exception e) {
                if (!isRetryable(e) || attempt == MAX_RETRIES) {
                    errorCount.increment();
                    log.error("Reconciliation write failed (attempt {}): id={}, error={}",
                        attempt + 1, doc.get("id").asText(), e.getMessage());
                    return;
                }
                retryCount.increment();
                long backoff = RETRY_BASE_MS * (1L << attempt);
                log.warn("Reconciliation write retry {} for id={}: {}", attempt + 1,
                    doc.get("id").asText(), e.getMessage());
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof CosmosException) {
            int status = ((CosmosException) e).getStatusCode();
            return status != 404 && status != 401 && status != 403;
        }
        return true;
    }

    public long getWriteCount() { return writeCount.sum(); }
    public long getErrorCount() { return errorCount.sum(); }

    @Override
    public void close() {
        log.info("ReconciliationWriter closed: source={}, writes={}, retries={}, errors={}",
            source, writeCount.sum(), retryCount.sum(), errorCount.sum());
    }
}
