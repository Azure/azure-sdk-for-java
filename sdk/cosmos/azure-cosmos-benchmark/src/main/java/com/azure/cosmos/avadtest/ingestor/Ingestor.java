package com.azure.cosmos.avadtest.ingestor;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.reconciliation.EventLog;
import com.azure.cosmos.avadtest.reconciliation.ReconciliationWriter;
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
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Ingestion workload using the Cosmos DB bulk API.
 * Operation mix: creates (40%), upserts (40% — replaces + upserts), deletes (20%).
 * Every operation gets a unique eventId for per-event reconciliation.
 * Batch of N operations submitted via executeBulkOperations every tick.
 */
public final class Ingestor implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Ingestor.class);
    private static final int TICK_INTERVAL_MS = 100;
    private static final double FAILURE_ABORT_THRESHOLD = 0.5;

    private final TestConfig config;
    private final CosmosAsyncClient client;
    private final CosmosAsyncContainer container;
    private final EventLog eventLog;
    private final ReconciliationWriter reconWriter;
    private final AtomicLong seqCounter = new AtomicLong(0);
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final LongAdder successCount = new LongAdder();
    private final LongAdder failureCount = new LongAdder();

    // Track recently created doc IDs for upsert/delete operations
    private final String[] recentDocIds;
    private final AtomicLong recentIndex = new AtomicLong(0);

    private final int opsPerTick;
    private final String precomputedPayload;

    // Reactor subscriptions — disposed on close to prevent leaks
    private volatile reactor.core.Disposable mainSubscription;
    private volatile reactor.core.Disposable progressSubscription;
    private final CosmosBulkExecutionOptions bulkOptions;

    public Ingestor(TestConfig config) throws Exception {
        this.config = config;
        this.eventLog = new EventLog(config.producedLogFile());
        this.recentDocIds = new String[10_000];
        this.opsPerTick = Math.max(1, config.opsPerSec() * TICK_INTERVAL_MS / 1000);
        this.bulkOptions = new CosmosBulkExecutionOptions();

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

        this.reconWriter = new ReconciliationWriter(client, config.database(), "ingestor");

        log.info("Ingestor initialized (bulk mode): endpoint={}, db={}, container={}, ops/sec={}, opsPerTick={}",
            config.endpoint(), config.database(), config.feedContainer(),
            config.opsPerSec(), opsPerTick);
    }

    public void run() throws InterruptedException {
        int durationSec = config.durationSeconds();
        log.info("Starting bulk ingestion at {} ops/sec, duration={}",
            config.opsPerSec(), durationSec > 0 ? durationSec + "s" : "unlimited");

        CountDownLatch latch = new CountDownLatch(1);

        // Each tick: build a batch of operations and submit via bulk API
        this.mainSubscription = Flux.interval(Duration.ofMillis(TICK_INTERVAL_MS))
            .takeWhile(tick -> running.get())
            .concatMap(tick -> executeBulkBatch())
            .doOnError(e -> log.error("Bulk ingestion error", e))
            .doOnComplete(latch::countDown)
            .subscribe();

        if (durationSec > 0) {
            Schedulers.single().schedule(() -> {
                log.info("Duration {}s reached, stopping ingestor...", durationSec);
                running.set(false);
            }, durationSec, java.util.concurrent.TimeUnit.SECONDS);
        }

        this.progressSubscription = Flux.interval(Duration.ofSeconds(30))
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

    private Flux<Void> executeBulkBatch() {
        List<CosmosItemOperation> operations = new ArrayList<>(opsPerTick);
        List<OpMeta> metas = new ArrayList<>(opsPerTick);

        for (int i = 0; i < opsPerTick; i++) {
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < 40) {
                addCreate(operations, metas);
            } else if (roll < 80) {
                addUpsert(operations, metas);
            } else {
                addDelete(operations, metas);
            }
        }

        if (operations.isEmpty()) {
            return Flux.empty();
        }

        return container.executeBulkOperations(Flux.fromIterable(operations), bulkOptions)
            .doOnNext(response -> handleBulkResponse(response, metas))
            .then()
            .flux();
    }

    private void addCreate(List<CosmosItemOperation> ops, List<OpMeta> metas) {
        String docId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String pk = "tenant-" + ThreadLocalRandom.current().nextInt(config.logicalPartitionCount());
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();

        ObjectNode doc = buildDoc(docId, pk, seq, eventId, "create", ts);
        ops.add(CosmosBulkOperations.getCreateItemOperation(doc, new PartitionKey(pk)));
        metas.add(new OpMeta(eventId, seq, "create", pk, ts, docId));
    }

    private void addUpsert(List<CosmosItemOperation> ops, List<OpMeta> metas) {
        String recent = getRecentId();
        String docId;
        String pk;
        if (recent != null) {
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

        ops.add(CosmosBulkOperations.getUpsertItemOperation(doc, new PartitionKey(pk)));
        metas.add(new OpMeta(eventId, seq, "upsert", pk, ts, docId));
    }

    private void addDelete(List<CosmosItemOperation> ops, List<OpMeta> metas) {
        String recent = getRecentId();
        if (recent == null) {
            addCreate(ops, metas);
            return;
        }

        String[] parts = recent.split("\\|");
        String docId = parts[0];
        String pk = parts[1];
        long seq = seqCounter.incrementAndGet();
        String ts = Instant.now().toString();

        ops.add(CosmosBulkOperations.getDeleteItemOperation(docId, new PartitionKey(pk)));
        metas.add(new OpMeta(docId, seq, "delete", pk, ts, docId));
        clearRecentId(recent);
    }

    private void handleBulkResponse(CosmosBulkOperationResponse<Object> response, List<OpMeta> metas) {
        CosmosItemOperation op = response.getOperation();

        // Find matching metadata by correlating operation index
        int idx = -1;
        // Use operation identity to find the matching meta
        // The operations list and metas list are parallel arrays
        // but executeBulkOperations may reorder responses.
        // Use the operation's id to find the correct meta.
        String opId = op.getId();
        for (int i = 0; i < metas.size(); i++) {
            if (metas.get(i).docId.equals(opId)) {
                idx = i;
                break;
            }
        }

        if (idx < 0) return;
        OpMeta meta = metas.get(idx);

        if (response.getResponse() != null && response.getResponse().isSuccessStatusCode()) {
            successCount.increment();
            eventLog.logProduced(meta.eventId, meta.seq, meta.opType, meta.pk, meta.ts);
            reconWriter.record(meta.eventId, meta.seq, meta.opType, meta.pk, -1, false, -1);
            if (!"delete".equals(meta.opType)) {
                trackRecentId(meta.docId + "|" + meta.pk);
            }
        } else {
            failureCount.increment();
            int status = response.getResponse() != null ? response.getResponse().getStatusCode() : -1;
            if (status != 404) {
                log.warn("Bulk op failed: op={}, docId={}, status={}", meta.opType, meta.docId, status);
            }
        }
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
        doc.put("payload", precomputedPayload);
        return doc;
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
        String val = recentDocIds[(start - offset + recentDocIds.length) % recentDocIds.length];
        return val; // may be null if cleared
    }

    @Override
    public void close() {
        log.info("Closing Ingestor...");
        running.set(false);
        if (progressSubscription != null) { progressSubscription.dispose(); }
        if (mainSubscription != null) { mainSubscription.dispose(); }
        try { eventLog.close(); } catch (Exception e) { /* ignore */ }
        reconWriter.close();
        client.close();
        log.info("Ingestor closed. Total ops: {}, success: {}, failures: {}",
            seqCounter.get(), successCount.sum(), failureCount.sum());
    }

    /** Metadata for correlating bulk operation responses back to produced events. */
    private static final class OpMeta {
        final String eventId;
        final long seq;
        final String opType;
        final String pk;
        final String ts;
        final String docId;

        OpMeta(String eventId, long seq, String opType, String pk, String ts, String docId) {
            this.eventId = eventId;
            this.seq = seq;
            this.opType = opType;
            this.pk = pk;
            this.ts = ts;
            this.docId = docId;
        }
    }
}
