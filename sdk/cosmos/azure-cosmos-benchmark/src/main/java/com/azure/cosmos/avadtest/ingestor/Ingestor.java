package com.azure.cosmos.avadtest.ingestor;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.reconciliation.EventLog;
import com.azure.cosmos.avadtest.reconciliation.ReconciliationWriter;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Ingestion workload: creates (40%), replaces (25%), upserts (15%), deletes (20%).
 * Every operation gets a unique eventId for per-event reconciliation.
 * Uses micro-batch rate limiting (batch of N ops every 10ms).
 */
public final class Ingestor implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Ingestor.class);
    private static final int TICK_INTERVAL_MS = 10;
    private static final double FAILURE_ABORT_THRESHOLD = 0.5; // abort if >50% failures

    private final TestConfig config;
    private final CosmosAsyncClient client;
    private final CosmosAsyncContainer container;
    private final EventLog eventLog;
    private final ReconciliationWriter reconWriter;
    private final AtomicLong seqCounter = new AtomicLong(0);
    private final AtomicBoolean running = new AtomicBoolean(true);

    // Failure tracking
    private final LongAdder successCount = new LongAdder();
    private final LongAdder failureCount = new LongAdder();

    // Track recently created doc IDs for replace/upsert/delete operations
    private final String[] recentDocIds;
    private final AtomicLong recentIndex = new AtomicLong(0);

    // Ops per tick = opsPerSec * tickIntervalMs / 1000
    private final int opsPerTick;
    private final String precomputedPayload;

    public Ingestor(TestConfig config) throws Exception {
        this.config = config;
        this.eventLog = new EventLog(config.producedLogFile());
        this.reconWriter = new ReconciliationWriter(config, "ingestor");
        this.recentDocIds = new String[10_000]; // ring buffer
        this.opsPerTick = Math.max(1, config.opsPerSec() * TICK_INTERVAL_MS / 1000);

        // Pre-compute payload once instead of per-operation
        int size = Math.min(Math.max(config.docSizeBytes(), 0), 10_000);
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) { sb.append('x'); }
        this.precomputedPayload = sb.toString();

        this.client = new CosmosClientBuilder()
            .endpoint(config.endpoint())
            .key(config.key())
            .gatewayMode()
            .preferredRegions(config.preferredRegions())
            .buildAsyncClient();

        this.container = client
            .getDatabase(config.database())
            .getContainer(config.feedContainer());

        log.info("Ingestor initialized: endpoint={}, db={}, container={}, ops/sec={}, opsPerTick={}",
            config.endpoint(), config.database(), config.feedContainer(),
            config.opsPerSec(), opsPerTick);
    }

    public void run() throws InterruptedException {
        int durationSec = config.durationSeconds();
        log.info("Starting ingestion at {} ops/sec, duration={}",
            config.opsPerSec(), durationSec > 0 ? durationSec + "s" : "unlimited");

        CountDownLatch latch = new CountDownLatch(1);

        // Rate-limited ingestion: emit opsPerTick operations every TICK_INTERVAL_MS
        int concurrency = Math.min(config.opsPerSec(), 500);
        Flux.interval(Duration.ofMillis(TICK_INTERVAL_MS))
            .takeWhile(tick -> running.get())
            .flatMap(tick -> Flux.range(0, opsPerTick)
                .flatMap(i -> executeOperation()
                    .subscribeOn(Schedulers.boundedElastic()), concurrency),
                concurrency)
            .doOnError(e -> log.error("Ingestion error", e))
            .doOnComplete(latch::countDown)
            .subscribe();

        // Auto-stop after duration
        if (durationSec > 0) {
            Schedulers.single().schedule(() -> {
                log.info("Duration {}s reached, stopping ingestor...", durationSec);
                running.set(false);
            }, durationSec, java.util.concurrent.TimeUnit.SECONDS);
        }

        // Periodic failure rate check
        Flux.interval(Duration.ofSeconds(30))
            .takeWhile(tick -> running.get())
            .subscribe(tick -> {
                long s = successCount.sum();
                long f = failureCount.sum();
                long total = s + f;
                double failRate = total > 0 ? (double) f / total : 0;
                log.info("Progress: success={}, failures={}, failRate={}%, seq={}",
                    s, f, String.format("%.1f", failRate * 100), seqCounter.get());
                if (total > 100 && failRate > FAILURE_ABORT_THRESHOLD) {
                    log.error("Failure rate {}% exceeds threshold, aborting!", String.format("%.1f", failRate * 100));
                    running.set(false);
                }
            });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received, stopping ingestor...");
            running.set(false);
        }));

        latch.await();
    }

    private Mono<Void> executeOperation() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 40) {
            return doCreate();
        } else if (roll < 65) {
            return doReplace();
        } else if (roll < 80) {
            return doUpsert();
        } else {
            return doDelete();
        }
    }

    private Mono<Void> doCreate() {
        String docId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String pk = "tenant-" + ThreadLocalRandom.current().nextInt(config.logicalPartitionCount());
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();

        ObjectNode doc = buildDoc(docId, pk, seq, eventId, "create", ts);

        return container.createItem(doc, new PartitionKey(pk), new CosmosItemRequestOptions())
            .doOnSuccess(resp -> {
                successCount.increment();
                eventLog.logProduced(eventId, seq, "create", pk, ts);
                reconWriter.record(eventId, seq, "create", pk, -1, false, -1);
                trackRecentId(docId + "|" + pk);
            })
            .doOnError(e -> {
                failureCount.increment();
                log.warn("Create failed: docId={}, error={}", docId, e.getMessage());
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    private Mono<Void> doReplace() {
        String recent = getRecentId();
        if (recent == null) return doCreate();

        String[] parts = recent.split("\\|");
        String docId = parts[0];
        String pk = parts[1];
        String eventId = UUID.randomUUID().toString();
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();

        return container.readItem(docId, new PartitionKey(pk), ObjectNode.class)
            .flatMap(readResp -> {
                ObjectNode doc = readResp.getItem();
                doc.put("seqNo", seq);
                doc.put("eventId", eventId);
                doc.put("operationType", "replace");
                doc.put("timestamp", ts);
                doc.put("payload", generatePayload());
                return container.replaceItem(doc, docId, new PartitionKey(pk), new CosmosItemRequestOptions());
            })
            .doOnSuccess(resp -> {
                successCount.increment();
                eventLog.logProduced(eventId, seq, "replace", pk, ts);
                reconWriter.record(eventId, seq, "replace", pk, -1, false, -1);
            })
            .doOnError(e -> {
                failureCount.increment();
                log.warn("Replace failed: docId={}, error={}", docId, e.getMessage());
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    private Mono<Void> doUpsert() {
        String recent = getRecentId();
        String docId;
        String pk;
        if (recent != null && ThreadLocalRandom.current().nextBoolean()) {
            String[] parts = recent.split("\\|");
            docId = parts[0];
            pk = parts[1];
        } else {
            docId = UUID.randomUUID().toString();
            pk = "tenant-" + ThreadLocalRandom.current().nextInt(config.logicalPartitionCount());
        }

        String eventId = UUID.randomUUID().toString();
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();
        ObjectNode doc = buildDoc(docId, pk, seq, eventId, "upsert", ts);

        return container.upsertItem(doc, new PartitionKey(pk), new CosmosItemRequestOptions())
            .doOnSuccess(resp -> {
                successCount.increment();
                eventLog.logProduced(eventId, seq, "upsert", pk, ts);
                reconWriter.record(eventId, seq, "upsert", pk, -1, false, -1);
                trackRecentId(docId + "|" + pk);
            })
            .doOnError(e -> {
                failureCount.increment();
                log.warn("Upsert failed: docId={}, error={}", docId, e.getMessage());
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    private Mono<Void> doDelete() {
        String recent = getRecentId();
        if (recent == null) return doCreate(); // nothing to delete yet

        String[] parts = recent.split("\\|");
        String docId = parts[0];
        String pk = parts[1];
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();

        // Read-before-delete: stamp a delete-specific eventId into the document
        // so AVAD reader's previous image contains the correct correlation key.
        String eventId = UUID.randomUUID().toString();
        return container.readItem(docId, new PartitionKey(pk), ObjectNode.class)
            .flatMap(readResp -> {
                ObjectNode doc = readResp.getItem();
                doc.put("eventId", eventId);
                doc.put("seqNo", seq);
                doc.put("operationType", "delete");
                doc.put("timestamp", ts);
                return container.replaceItem(doc, docId, new PartitionKey(pk), new CosmosItemRequestOptions());
            })
            .flatMap(replaceResp ->
                container.deleteItem(docId, new PartitionKey(pk), new CosmosItemRequestOptions()))
            .doOnSuccess(resp -> {
                successCount.increment();
                eventLog.logProduced(eventId, seq, "delete", pk, ts);
                reconWriter.record(eventId, seq, "delete", pk, -1, false, -1);
                clearRecentId(docId + "|" + pk);
            })
            .doOnError(e -> {
                failureCount.increment();
                // 404 is expected if already deleted
                if (e.getMessage() == null || !e.getMessage().contains("404")) {
                    log.warn("Delete failed: docId={}, error={}", docId, e.getMessage());
                }
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    private ObjectNode buildDoc(String docId, String pk, long seq,
                                String eventId, String opType, String ts) {
        ObjectNode doc = JsonNodeFactory.instance.objectNode();
        doc.put("id", docId);
        doc.put("tenantId", pk);
        doc.put("eventId", eventId);
        doc.put("seqNo", seq);
        doc.put("operationType", opType);
        doc.put("timestamp", ts);
        doc.put("payload", generatePayload());
        return doc;
    }

    private String generatePayload() {
        return precomputedPayload;
    }

    private void trackRecentId(String idAndPk) {
        int idx = (int) (recentIndex.incrementAndGet() % recentDocIds.length);
        recentDocIds[idx] = idAndPk;
    }

    private void clearRecentId(String idAndPk) {
        for (int i = 0; i < recentDocIds.length; i++) {
            if (idAndPk.equals(recentDocIds[i])) {
                recentDocIds[i] = null;
            }
        }
    }

    private String getRecentId() {
        long idx = recentIndex.get();
        if (idx == 0) return null;
        int start = (int) (idx % recentDocIds.length);
        int offset = ThreadLocalRandom.current().nextInt(Math.min((int) idx, recentDocIds.length));
        return recentDocIds[(start - offset + recentDocIds.length) % recentDocIds.length];
    }

    @Override
    public void close() {
        running.set(false);
        try { eventLog.close(); } catch (Exception e) { /* ignore */ }
        reconWriter.close();
        client.close();
        log.info("Ingestor closed. Total ops: {}, success: {}, failures: {}",
            seqCounter.get(), successCount.sum(), failureCount.sum());
    }
}
