package com.azure.cosmos.avadtest.reader;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.avadtest.reconciliation.EventLog;
import com.azure.cosmos.avadtest.reconciliation.ReconciliationWriter;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedMetaData;
import com.azure.cosmos.models.ChangeFeedOperationType;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

/**
 * All Versions and Deletes (AVAD) ChangeFeedProcessor reader.
 * Lease prefix "avad-" — isolated from Latest Version reader leases.
 * Gateway mode, preferred region configurable (default: West Central US).
 *
 * Additional validations vs LatestVersionReader:
 * - previousImage must be non-null on replace and delete events
 * - operationType metadata is checked for create/replace/delete
 */
public final class AvadReader implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(AvadReader.class);
    private static final String LEASE_PREFIX = "avad-";

    private final TestConfig config;
    private final CosmosAsyncClient client;
    private final CosmosAsyncContainer feedContainer;
    private final CosmosAsyncContainer leaseContainer;
    private final EventLog eventLog;
    private final ReconciliationWriter reconWriter;
    private final List<ChangeFeedProcessor> processors = new ArrayList<>();

    // Correctness counters (thread-safe for concurrent CFP batch processing)
    private final LongAdder missingPreviousImageCount = new LongAdder();
    private final LongAdder totalReplaces = new LongAdder();
    private final LongAdder totalDeletes = new LongAdder();
    private final LongAdder totalCreates = new LongAdder();

    public AvadReader(TestConfig config) throws Exception {
        this.config = config;
        this.eventLog = new EventLog(config.consumedLogFile());

        this.client = new CosmosClientBuilder()
            .endpoint(config.readerEndpoint())
            .key(config.key())
            .gatewayMode()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(config.preferredRegions())
            .buildAsyncClient();

        this.feedContainer = client
            .getDatabase(config.database())
            .getContainer(config.feedContainer());

        this.leaseContainer = client
            .getDatabase(config.database())
            .getContainer(config.leaseContainer());

        this.reconWriter = new ReconciliationWriter(client, config.database(), "cfp-avad");

        log.info("AvadReader initialized: prefix={}, endpoint={}, region={}, workers={}",
            LEASE_PREFIX, config.readerEndpoint(), config.preferredRegion(), config.workerCount());
    }

    public void run() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        int workers = config.workerCount();

        for (int i = 0; i < workers; i++) {
            final int workerIdx = i;
            ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
            options.setLeasePrefix(LEASE_PREFIX);
            options.setFeedPollDelay(Duration.ofSeconds(1));

            ChangeFeedProcessor processor = new ChangeFeedProcessorBuilder()
                .hostName("avad-host-" + ManagementFactory.getRuntimeMXBean().getName() + "-w" + workerIdx)
                .feedContainer(feedContainer)
                .leaseContainer(leaseContainer)
                .options(options)
                .handleAllVersionsAndDeletesChanges(this::handleChanges)
                .buildChangeFeedProcessor();

            processor.start()
                .doOnSuccess(v -> log.info("AVAD ChangeFeedProcessor worker-{} started", workerIdx))
                .doOnError(e -> log.error("Failed to start AVAD CFP worker-{}", workerIdx, e))
                .block();

            processors.add(processor);
        }

        log.info("All {} AVAD workers started", workers);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal, stopping {} AVAD CFP workers...", processors.size());
            for (ChangeFeedProcessor p : processors) {
                try { p.stop().block(Duration.ofSeconds(30)); } catch (Exception e) { /* ignore */ }
            }
            latch.countDown();
        }));

        latch.await();
    }

    private void handleChanges(List<ChangeFeedProcessorItem> items) {
        for (ChangeFeedProcessorItem item : items) {
            JsonNode current = item.getCurrent();
            JsonNode previous = item.getPrevious();
            ChangeFeedMetaData metadata = item.getChangeFeedMetaData();

            if (metadata == null) {
                log.warn("Null metadata on AVAD change feed item, skipping");
                continue;
            }

            // LSN and CRTS come from metadata — not from the document body.
            // This is critical for deletes where current may be a tombstone.
            ChangeFeedOperationType opEnum = metadata.getOperationType();
            String opType = opEnum != null ? opEnum.toString().toLowerCase() : "unknown";
            long lsn = metadata.getLogSequenceNumber();
            Instant crtsInstant = metadata.getConflictResolutionTimestamp();
            long crts = crtsInstant != null ? crtsInstant.toEpochMilli() : -1;

            // For deletes, current is a tombstone — extract business fields from previous
            JsonNode source;
            if (opEnum == ChangeFeedOperationType.DELETE) {
                source = previous;
            } else {
                source = (current != null && !current.isNull()) ? current : previous;
            }

            String eventId = source != null ? getTextOrEmpty(source, "eventId") : "";
            long seqNo = source != null && source.has("seqNo") ? source.get("seqNo").asLong() : -1;
            String pk = source != null ? getTextOrEmpty(source, "tenantId") : "";
            String timestamp = source != null ? getTextOrEmpty(source, "timestamp") : "";

            boolean hasPrevious = previous != null && !previous.isNull();

            // Track operation types and validate previousImage
            if ("create".equals(opType)) {
                totalCreates.increment();
            } else if ("replace".equals(opType)) {
                totalReplaces.increment();
                if (!hasPrevious) {
                    missingPreviousImageCount.increment();
                    log.warn("⚠️ MISSING previous on REPLACE: eventId={}, pk={}", eventId, pk);
                }
            } else if ("delete".equals(opType)) {
                totalDeletes.increment();
                if (!hasPrevious) {
                    missingPreviousImageCount.increment();
                    log.warn("⚠️ MISSING previous on DELETE: eventId={}, pk={}", eventId, pk);
                }
            }

            eventLog.logConsumedAvad(eventId, seqNo, opType, pk, timestamp, lsn, crts);
            reconWriter.record(eventId, seqNo, opType, pk, lsn, hasPrevious, crts);
        }

        eventLog.flush();
    }

    private void logCorrectnessReport() {
        log.info("=== AVAD Correctness Report ===");
        log.info("  Creates: {}", totalCreates.sum());
        log.info("  Replaces: {} (missing previous: {})", totalReplaces.sum(), missingPreviousImageCount.sum());
        log.info("  Deletes: {}", totalDeletes.sum());
        long missing = missingPreviousImageCount.sum();
        if (missing > 0) {
            log.error("❌ previous MISSING on {} replace/delete events", missing);
        } else {
            log.info("✅ All replace/delete events have previous image");
        }
    }

    private static String getTextOrEmpty(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
    }

    @Override
    public void close() {
        log.info("Closing AvadReader...");
        logCorrectnessReport();
        for (ChangeFeedProcessor p : processors) {
            try { p.stop().block(Duration.ofSeconds(30)); } catch (Exception e) { /* ignore */ }
        }
        try { eventLog.close(); } catch (Exception e) { /* ignore */ }
        reconWriter.close();
        client.close();
        log.info("AvadReader closed ({} workers)", processors.size());
    }
}
