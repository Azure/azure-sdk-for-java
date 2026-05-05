package com.azure.cosmos.avadtest.reconciliation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.avadtest.config.TestConfig;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reconciler that queries the shared "reconciliation" Cosmos container
 * to detect gaps, ordering violations, and missing previousImage across
 * all source types (ingestor, cfp-lv, cfp-avad, spark-lv, spark-avad).
 */
public final class Reconciler implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Reconciler.class);
    private static final Duration QUERY_TIMEOUT = Duration.ofSeconds(60);

    private final CosmosAsyncClient client;
    private final CosmosAsyncContainer container;

    public Reconciler(TestConfig config) {
        this.client = new CosmosClientBuilder()
            .endpoint(config.endpoint())
            .key(config.key())
            .gatewayMode()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(config.preferredRegions())
            .buildAsyncClient();

        this.container = client
            .getDatabase(config.database())
            .getContainer("reconciliation");

        log.info("Reconciler initialized: endpoint={}, db={}", config.endpoint(), config.database());
    }

    /** Run all reconciliation checks across all source pairs. */
    public int runFullSuite() {
        log.info("=== Full Reconciliation Suite ===");
        int failures = 0;

        logSummary();

        // Gap detection: ingestor → each consumer
        failures += checkGaps("ingestor", "cfp-lv", "Ingestor → CFP LV");
        failures += checkGaps("ingestor", "cfp-avad", "Ingestor → CFP AVAD");
        failures += checkGaps("ingestor", "spark-lv", "Ingestor → Spark LV");
        failures += checkGaps("ingestor", "spark-avad", "Ingestor → Spark AVAD");

        // Parity: LV ⊆ AVAD
        failures += checkGaps("cfp-lv", "cfp-avad", "CFP Parity (AVAD ⊇ LV)");
        failures += checkGaps("spark-lv", "spark-avad", "Spark Parity (AVAD ⊇ LV)");

        // Cross-engine
        failures += checkGaps("cfp-lv", "spark-lv", "Cross-engine LV");
        failures += checkGaps("cfp-avad", "spark-avad", "Cross-engine AVAD");

        // LSN ordering
        for (String s : new String[]{"cfp-lv", "cfp-avad", "spark-lv", "spark-avad"}) {
            failures += checkLsnOrdering(s);
        }

        // CRTS ordering (AVAD only)
        failures += checkCrtsOrdering("cfp-avad");
        failures += checkCrtsOrdering("spark-avad");

        // previousImage (AVAD only)
        failures += checkPreviousImage("cfp-avad");
        failures += checkPreviousImage("spark-avad");

        logDuplicates();

        log.info("=== Suite Complete: {} failures ===", failures);
        return failures > 0 ? 1 : 0;
    }

    /** Reconcile a single source pair. Auto-selects checks by source types. */
    public int reconcilePair(String source, String against) {
        log.info("=== Reconcile: {} → {} ===", source, against);
        int failures = 0;

        failures += checkGaps(source, against, source + " → " + against);

        // LSN ordering on the consumer side
        if (!against.equals("ingestor")) {
            failures += checkLsnOrdering(against);
        }

        // AVAD-specific checks
        if (against.endsWith("-avad")) {
            failures += checkCrtsOrdering(against);
            failures += checkPreviousImage(against);
        }

        return failures > 0 ? 1 : 0;
    }

    /** Q1: Summary dashboard — count, unique, min/max seq/lsn per source */
    private void logSummary() {
        log.info("=== Summary Dashboard ===");
        String query = "SELECT c.source, COUNT(1) AS total, "
            + "COUNT(DISTINCT c.correlationId) AS uniqueIds, "
            + "MIN(c.seqNo) AS minSeq, MAX(c.seqNo) AS maxSeq, "
            + "MIN(c.lsn) AS minLsn, MAX(c.lsn) AS maxLsn "
            + "FROM c GROUP BY c.source";

        container.queryItems(query, new CosmosQueryRequestOptions(), JsonNode.class)
            .byPage(100)
            .timeout(QUERY_TIMEOUT)
            .toIterable()
            .forEach(page -> {
                for (JsonNode row : page.getResults()) {
                    log.info("  source={}, total={}, unique={}, seq=[{},{}], lsn=[{},{}]",
                        row.path("source").asText(),
                        row.path("total").asLong(),
                        row.path("uniqueIds").asLong(),
                        row.path("minSeq").asLong(),
                        row.path("maxSeq").asLong(),
                        row.path("minLsn").asLong(),
                        row.path("maxLsn").asLong());
                }
            });
    }

    /** Q2/Q3/Q4: Gap detection — every correlationId in sourceA must exist in sourceB */
    private int checkGaps(String sourceA, String sourceB, String label) {
        log.info("=== Gap Check: {} ===", label);

        Set<String> idsA = loadCorrelationIds(sourceA);
        Set<String> idsB = loadCorrelationIds(sourceB);

        if (idsA.isEmpty()) {
            log.info("  SKIP: {} has no data yet", sourceA);
            return 0;
        }
        if (idsB.isEmpty()) {
            log.info("  SKIP: {} has no data yet", sourceB);
            return 0;
        }

        Set<String> missing = new HashSet<>(idsA);
        missing.removeAll(idsB);

        log.info("  {} ids={}, {} ids={}, missing={}", sourceA, idsA.size(), sourceB, idsB.size(), missing.size());

        if (!missing.isEmpty()) {
            log.error("❌ {} GAPS DETECTED:", label);
            missing.stream().limit(50).forEach(id -> log.error("  missing: {}", id));
            if (missing.size() > 50) {
                log.error("  ... and {} more", missing.size() - 50);
            }
        } else {
            log.info("✅ {} — no gaps", label);
        }

        return missing.size();
    }

    /** Q5: LSN ordering — per partition, sorted by seqNo, LSN must be non-decreasing */
    private int checkLsnOrdering(String source) {
        log.info("=== LSN Ordering: {} ===", source);
        Map<String, List<long[]>> events = loadEventsForOrdering(source, "lsn");

        if (events.isEmpty()) {
            log.info("  SKIP: {} has no LSN data", source);
            return 0;
        }

        int violations = 0;
        for (Map.Entry<String, List<long[]>> entry : events.entrySet()) {
            String pk = entry.getKey();
            List<long[]> records = entry.getValue();
            records.sort(Comparator.comparingLong(r -> r[0]));

            long prev = -1;
            for (long[] record : records) {
                if (prev > 0 && record[1] < prev) {
                    violations++;
                    if (violations <= 10) {
                        log.warn("  LSN violation: pk={}, seqNo={}, prevLsn={}, currLsn={}",
                            pk, record[0], prev, record[1]);
                    }
                }
                prev = record[1];
            }
        }

        log.info("  LSN violations: {} (across {} partitions)", violations, events.size());
        return violations;
    }

    /** Q6: CRTS ordering — per partition, sorted by seqNo, CRTS must be non-decreasing */
    private int checkCrtsOrdering(String source) {
        log.info("=== CRTS Ordering: {} ===", source);
        Map<String, List<long[]>> events = loadEventsForOrdering(source, "crts");

        if (events.isEmpty()) {
            log.info("  SKIP: {} has no CRTS data", source);
            return 0;
        }

        int violations = 0;
        for (Map.Entry<String, List<long[]>> entry : events.entrySet()) {
            String pk = entry.getKey();
            List<long[]> records = entry.getValue();
            records.sort(Comparator.comparingLong(r -> r[0]));

            long prev = -1;
            for (long[] record : records) {
                if (prev > 0 && record[1] < prev) {
                    violations++;
                    if (violations <= 10) {
                        log.warn("  CRTS violation: pk={}, seqNo={}, prevCrts={}, currCrts={}",
                            pk, record[0], prev, record[1]);
                    }
                }
                prev = record[1];
            }
        }

        log.info("  CRTS violations: {} (across {} partitions)", violations, events.size());
        return violations;
    }

    /** Q7: previousImage — replace/delete with hasPreviousImage=false must be 0 */
    private int checkPreviousImage(String source) {
        log.info("=== Previous Image Check: {} ===", source);

        SqlQuerySpec querySpec = new SqlQuerySpec(
            "SELECT VALUE COUNT(1) FROM c WHERE c.source = @source "
                + "AND c.opType IN ('replace', 'delete') AND c.hasPreviousImage = false",
            Collections.singletonList(new SqlParameter("@source", source)));

        long count = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Long.class)
            .byPage(1)
            .timeout(QUERY_TIMEOUT)
            .toIterable()
            .iterator().next()
            .getResults()
            .stream()
            .findFirst()
            .orElse(0L);

        if (count > 0) {
            log.error("❌ {} missing previousImage on {} replace/delete events", source, count);
        } else {
            log.info("✅ {} — all replace/delete have previousImage", source);
        }

        return (int) count;
    }

    /** Q8: Duplicate detection — total vs unique correlationIds per source */
    private void logDuplicates() {
        log.info("=== Duplicate Detection ===");
        String query = "SELECT c.source, COUNT(1) AS total, "
            + "COUNT(DISTINCT c.correlationId) AS uniqueIds "
            + "FROM c GROUP BY c.source";

        container.queryItems(query, new CosmosQueryRequestOptions(), JsonNode.class)
            .byPage(100)
            .timeout(QUERY_TIMEOUT)
            .toIterable()
            .forEach(page -> {
                for (JsonNode row : page.getResults()) {
                    long total = row.path("total").asLong();
                    long unique = row.path("uniqueIds").asLong();
                    long duplicates = total - unique;
                    log.info("  source={}, total={}, unique={}, duplicates={}",
                        row.path("source").asText(), total, unique, duplicates);
                }
            });
    }

    /** Helper: load all distinct correlationIds for a source */
    private Set<String> loadCorrelationIds(String source) {
        SqlQuerySpec querySpec = new SqlQuerySpec(
            "SELECT DISTINCT c.correlationId FROM c WHERE c.source = @source",
            Collections.singletonList(new SqlParameter("@source", source)));

        Set<String> ids = new HashSet<>();

        container.queryItems(querySpec, new CosmosQueryRequestOptions(), JsonNode.class)
            .byPage(1000)
            .timeout(QUERY_TIMEOUT)
            .toIterable()
            .forEach(page -> {
                for (JsonNode row : page.getResults()) {
                    String cid = row.path("correlationId").asText("");
                    if (!cid.isEmpty()) {
                        ids.add(cid);
                    }
                }
            });

        return ids;
    }

    /** Helper: load events for ordering checks */
    private Map<String, List<long[]>> loadEventsForOrdering(String source, String field) {
        String query = "SELECT c.seqNo, c." + field + ", c.partitionKey FROM c "
            + "WHERE c.source = @source AND c." + field + " >= 0";

        SqlQuerySpec querySpec = new SqlQuerySpec(query,
            Collections.singletonList(new SqlParameter("@source", source)));

        Map<String, List<long[]>> result = new HashMap<>();

        container.queryItems(querySpec, new CosmosQueryRequestOptions(), JsonNode.class)
            .byPage(1000)
            .timeout(QUERY_TIMEOUT)
            .toIterable()
            .forEach(page -> {
                for (JsonNode row : page.getResults()) {
                    String pk = row.path("partitionKey").asText("");
                    long seqNo = row.path("seqNo").asLong();
                    long fieldValue = row.path(field).asLong();

                    result.computeIfAbsent(pk, k -> new ArrayList<>())
                        .add(new long[]{seqNo, fieldValue});
                }
            });

        return result;
    }

    @Override
    public void close() {
        client.close();
        log.info("Reconciler closed");
    }
}
