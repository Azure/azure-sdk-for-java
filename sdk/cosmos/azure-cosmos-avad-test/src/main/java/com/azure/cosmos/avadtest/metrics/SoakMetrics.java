package com.azure.cosmos.avadtest.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * Centralized soak test metrics. Thread-safe counters for all
 * workload components.
 *
 * Exported as Prometheus-compatible text via /metrics endpoint.
 * Reusable: other workloads can extend or compose this class.
 */
public final class SoakMetrics {

    // Ingestor metrics
    private final LongAdder ingestorOpsSuccess = new LongAdder();
    private final LongAdder ingestorOpsFailure = new LongAdder();
    private final LongAdder ingestorCreates = new LongAdder();
    private final LongAdder ingestorReplaces = new LongAdder();
    private final LongAdder ingestorUpserts = new LongAdder();
    private final LongAdder ingestorDeletes = new LongAdder();

    // CFP consumer metrics
    private final LongAdder cfpAvadEventsConsumed = new LongAdder();
    private final LongAdder cfpLvEventsConsumed = new LongAdder();
    private final LongAdder cfpPreviousImageMissing = new LongAdder();
    private final LongAdder cfpLsnViolations = new LongAdder();

    // Reconciliation metrics
    private final LongAdder reconWrites = new LongAdder();
    private final LongAdder reconErrors = new LongAdder();
    private final LongAdder reconDrops = new LongAdder();

    // --- Ingestor ---
    public void recordIngestorSuccess() { ingestorOpsSuccess.increment(); }
    public void recordIngestorFailure() { ingestorOpsFailure.increment(); }
    public void recordIngestorCreate() { ingestorCreates.increment(); }
    public void recordIngestorReplace() { ingestorReplaces.increment(); }
    public void recordIngestorUpsert() { ingestorUpserts.increment(); }
    public void recordIngestorDelete() { ingestorDeletes.increment(); }

    // --- CFP ---
    public void recordAvadEvent() { cfpAvadEventsConsumed.increment(); }
    public void recordLvEvent() { cfpLvEventsConsumed.increment(); }
    public void recordMissingPreviousImage() { cfpPreviousImageMissing.increment(); }
    public void recordLsnViolation() { cfpLsnViolations.increment(); }

    // --- Reconciliation ---
    public void recordReconWrite() { reconWrites.increment(); }
    public void recordReconError() { reconErrors.increment(); }
    public void recordReconDrop() { reconDrops.increment(); }

    /**
     * Export all metrics as Prometheus-compatible plain text.
     */
    public String toPrometheusText() {
        StringBuilder sb = new StringBuilder(2048);

        appendMetric(sb, "cosmos_soak_ingestor_ops_success_total",
            "Total successful ingestor operations", ingestorOpsSuccess.sum());
        appendMetric(sb, "cosmos_soak_ingestor_ops_failure_total",
            "Total failed ingestor operations", ingestorOpsFailure.sum());
        appendMetric(sb, "cosmos_soak_ingestor_creates_total",
            "Total create operations", ingestorCreates.sum());
        appendMetric(sb, "cosmos_soak_ingestor_replaces_total",
            "Total replace operations", ingestorReplaces.sum());
        appendMetric(sb, "cosmos_soak_ingestor_upserts_total",
            "Total upsert operations", ingestorUpserts.sum());
        appendMetric(sb, "cosmos_soak_ingestor_deletes_total",
            "Total delete operations", ingestorDeletes.sum());

        appendMetric(sb, "cosmos_soak_cfp_avad_events_consumed_total",
            "Total AVAD change feed events consumed", cfpAvadEventsConsumed.sum());
        appendMetric(sb, "cosmos_soak_cfp_lv_events_consumed_total",
            "Total LV change feed events consumed", cfpLvEventsConsumed.sum());
        appendMetric(sb, "cosmos_soak_cfp_previous_image_missing_total",
            "Replace/delete events missing previousImage", cfpPreviousImageMissing.sum());
        appendMetric(sb, "cosmos_soak_cfp_lsn_violations_total",
            "LSN ordering violations", cfpLsnViolations.sum());

        appendMetric(sb, "cosmos_soak_reconciliation_writes_total",
            "Total reconciliation writes", reconWrites.sum());
        appendMetric(sb, "cosmos_soak_reconciliation_errors_total",
            "Total reconciliation errors", reconErrors.sum());
        appendMetric(sb, "cosmos_soak_reconciliation_drops_total",
            "Total reconciliation drops", reconDrops.sum());

        return sb.toString();
    }

    private void appendMetric(StringBuilder sb, String name, String help, long value) {
        sb.append("# HELP ").append(name).append(' ').append(help).append('\n');
        sb.append("# TYPE ").append(name).append(" counter\n");
        sb.append(name).append(' ').append(value).append('\n');
    }
}
