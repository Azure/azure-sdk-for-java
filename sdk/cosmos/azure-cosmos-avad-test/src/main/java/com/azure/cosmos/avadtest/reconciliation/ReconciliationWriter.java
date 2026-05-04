package com.azure.cosmos.avadtest.reconciliation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

/**
 * Writes reconciliation events to a shared Cosmos container.
 * All consumers (CFP LV, CFP AVAD, Spark LV, Spark AVAD, Ingestor) write here
 * with a common schema, enabling a single reconciliation query across all of them.
 *
 * Container: "reconciliation" in same database
 * Partition key: /correlationId
 *
 * Document schema:
 * {
 *   "id": "{source}-{correlationId}",      // unique per source+event
 *   "correlationId": "corr-uuid",
 *   "source": "ingestor|cfp-lv|cfp-avad|spark-lv|spark-avad",
 *   "seqNo": 12345,
 *   "opType": "create|replace|upsert|delete",
 *   "partitionKey": "tenant-42",
 *   "lsn": 999,                            // -1 for ingestor
 *   "hasPreviousImage": true,              // only for AVAD sources
 *   "crts": 1714300800000,                 // conflict resolution timestamp (epoch ms), -1 if N/A
 *   "timestamp": "2026-04-28T12:00:00Z",
 *   "recordedAt": "2026-04-28T12:00:01Z"
 * }
 */
public final class ReconciliationWriter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationWriter.class);
    private static final String RECONCILIATION_CONTAINER = "reconciliation";
    private static final int MAX_RETRIES = 3;
    private static final int MAX_REQUEUES = 2; // max times a doc can be requeued after all retries fail
    private static final String REQUEUE_COUNT_FIELD = "_requeueCount";

    private static final CosmosEndToEndOperationLatencyPolicyConfig E2E_POLICY =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(6)).build();

    private final String source;
    private final CosmosAsyncContainer container;
    private final CosmosAsyncClient client;
    private final LongAdder writeCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder retryCount = new LongAdder();
    private final LongAdder dropCount = new LongAdder();

    private final Sinks.Many<ObjectNode> sink;
    private final reactor.core.Disposable subscription;

    public ReconciliationWriter(TestConfig config, String source) {
        this.source = source;

        this.client = new CosmosClientBuilder()
            .endpoint(config.endpoint())
            .key(config.key())
            .gatewayMode()
            .preferredRegions(config.preferredRegions())
            .buildAsyncClient();

        this.container = client
            .getDatabase(config.database())
            .getContainer(RECONCILIATION_CONTAINER);

        this.sink = Sinks.many().multicast().onBackpressureBuffer(100_000);

        this.subscription = sink.asFlux()
            .flatMap(this::writeDoc, 50)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        log.info("ReconciliationWriter initialized: source={}, container={}",
            source, RECONCILIATION_CONTAINER);
    }

    /**
     * Record a produced or consumed event for reconciliation.
     * Non-blocking — buffers internally and writes async.
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
        doc.put(REQUEUE_COUNT_FIELD, 0);

        Sinks.EmitResult result = sink.tryEmitNext(doc);
        if (result.isFailure()) {
            dropCount.increment();
            log.warn("Reconciliation sink full/closed, dropping event: eventId={}", eventId);
        }
    }

    private Mono<Void> writeDoc(ObjectNode doc) {
        String eventId = doc.get("correlationId").asText();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setCosmosEndToEndOperationLatencyPolicyConfig(E2E_POLICY);

        return container.upsertItem(doc, new PartitionKey(eventId), options)
            .doOnSuccess(r -> writeCount.increment())
            .retryWhen(reactor.util.retry.Retry.backoff(MAX_RETRIES, Duration.ofMillis(500))
                .maxBackoff(Duration.ofSeconds(2))
                .filter(this::isRetryable)
                .doBeforeRetry(signal -> {
                    retryCount.increment();
                    log.warn("Reconciliation write retry #{} for id={}: {}",
                        signal.totalRetries() + 1,
                        doc.get("id").asText(),
                        signal.failure().getMessage());
                }))
            .doOnError(e -> {
                int requeueCount = doc.has(REQUEUE_COUNT_FIELD) ? doc.get(REQUEUE_COUNT_FIELD).asInt() : 0;
                if (requeueCount < MAX_REQUEUES && isRetryable(e)) {
                    doc.put(REQUEUE_COUNT_FIELD, requeueCount + 1);
                    Sinks.EmitResult result = sink.tryEmitNext(doc);
                    if (result.isSuccess()) {
                        log.warn("Requeueing (attempt {}): id={}", requeueCount + 1, doc.get("id").asText());
                    } else {
                        errorCount.increment();
                        log.error("Requeue failed (sink full/closed): id={}", doc.get("id").asText());
                    }
                } else {
                    errorCount.increment();
                    dropCount.increment();
                    log.error("Permanently dropped: id={}, requeues={}, error={}",
                        doc.get("id").asText(), requeueCount, e.getMessage());
                }
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    private boolean isRetryable(Throwable e) {
        String msg = e.getMessage();
        if (msg == null) return true;
        // Don't retry permanent failures
        return !msg.contains("404") && !msg.contains("Unauthorized") && !msg.contains("403");
    }

    public long getWriteCount() { return writeCount.sum(); }
    public long getErrorCount() { return errorCount.sum(); }
    public long getDropCount() { return dropCount.sum(); }

    @Override
    public void close() {
        sink.tryEmitComplete();
        // Wait for the subscriber to drain all buffered writes
        try {
            subscription.dispose();
            Thread.sleep(10_000); // allow in-flight writes to complete
        } catch (InterruptedException ignored) {}
        client.close();
        log.info("ReconciliationWriter closed: source={}, writes={}, retries={}, errors={}, drops={}",
            source, writeCount.sum(), retryCount.sum(), errorCount.sum(), dropCount.sum());
    }
}
