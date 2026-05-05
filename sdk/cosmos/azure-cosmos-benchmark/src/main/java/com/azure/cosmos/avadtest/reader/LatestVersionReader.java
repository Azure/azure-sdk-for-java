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

/**
 * Latest Version ChangeFeedProcessor reader.
 * Lease prefix "lv-" — isolated from AVAD reader leases.
 * Gateway mode, preferred region configurable (default: West Central US).
 */
public final class LatestVersionReader implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LatestVersionReader.class);
    private static final String LEASE_PREFIX = "lv-";

    private final TestConfig config;
    private final CosmosAsyncClient client;
    private final CosmosAsyncContainer feedContainer;
    private final CosmosAsyncContainer leaseContainer;
    private final EventLog eventLog;
    private final ReconciliationWriter reconWriter;
    private final List<ChangeFeedProcessor> processors = new ArrayList<>();

    public LatestVersionReader(TestConfig config) throws Exception {
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

        this.reconWriter = new ReconciliationWriter(client, config.database(), "cfp-lv");

        log.info("LatestVersionReader initialized: prefix={}, endpoint={}, region={}, workers={}",
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
            options.setStartTime(Instant.now().minus(Duration.ofDays(5)));

            ChangeFeedProcessor processor = new ChangeFeedProcessorBuilder()
                .hostName("lv-host-" + ManagementFactory.getRuntimeMXBean().getName() + "-w" + i)
                .feedContainer(feedContainer)
                .leaseContainer(leaseContainer)
                .options(options)
                .handleLatestVersionChanges(this::handleChanges)
                .buildChangeFeedProcessor();

            processor.start()
                .doOnSuccess(v -> log.info("LV ChangeFeedProcessor worker-{} started", workerIdx))
                .doOnError(e -> log.error("Failed to start LV CFP worker-{}", workerIdx, e))
                .block();

            processors.add(processor);
        }

        log.info("All {} LV workers started", workers);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal, stopping {} LV CFP workers...", processors.size());
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
            if (current == null || current.isNull()) continue; // LV mode shouldn't get null current

            ChangeFeedMetaData metadata = item.getChangeFeedMetaData();
            String eventId = getTextOrEmpty(current, "eventId");
            long seqNo = current.has("seqNo") ? current.get("seqNo").asLong() : -1;
            String opType = getTextOrEmpty(current, "operationType");
            String pk = getTextOrEmpty(current, "tenantId");
            String timestamp = getTextOrEmpty(current, "timestamp");
            long lsn = metadata != null ? metadata.getLogSequenceNumber() : -1;

            eventLog.logConsumed(eventId, seqNo, opType, pk, timestamp, lsn);
            reconWriter.record(eventId, seqNo, opType, pk, lsn, false, -1);
        }

        eventLog.flush();
    }

    private static String getTextOrEmpty(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
    }

    @Override
    public void close() {
        log.info("Closing LatestVersionReader...");
        for (ChangeFeedProcessor p : processors) {
            try { p.stop().block(Duration.ofSeconds(30)); } catch (Exception e) { /* ignore */ }
        }
        try { eventLog.close(); } catch (Exception e) { /* ignore */ }
        reconWriter.close();
        client.close();
        log.info("LatestVersionReader closed ({} workers)", processors.size());
    }
}
