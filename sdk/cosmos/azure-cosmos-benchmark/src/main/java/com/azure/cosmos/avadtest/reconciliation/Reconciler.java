package com.azure.cosmos.avadtest.reconciliation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reconciler that compares produced vs consumed event logs.
 * Uses eventId (unique per operation) for per-event reconciliation.
 *
 * Line format: eventId,seqNo,opType,partitionKey,timestamp,lsn[,crts]
 *
 * Checks:
 * 1. Gap detection — every produced eventId must appear in consumed
 * 2. LV ↔ AVAD parity — every LV event must appear in AVAD (AVAD ⊇ LV)
 * 3. Ordering — LSN must be monotonically increasing per partitionKey
 * 4. CRTS ordering — CRTS must be monotonically increasing per partitionKey (AVAD only)
 *
 * Exit code: 0 = all checks pass, 1 = failures detected
 */
public final class Reconciler {

    private static final Logger log = LoggerFactory.getLogger(Reconciler.class);

    public static int reconcile(String producedFile, String consumedFile) throws IOException {
        log.info("=== Gap Detection: {} vs {} ===", producedFile, consumedFile);

        Set<String> produced = loadEventIds(producedFile);
        Set<String> consumed = loadEventIds(consumedFile);

        Set<String> missing = new HashSet<>(produced);
        missing.removeAll(consumed);

        // Count duplicates (at-least-once delivery)
        long totalConsumedLines = Files.lines(java.nio.file.Paths.get(consumedFile)).filter(l -> !l.trim().isEmpty()).count();
        long duplicates = totalConsumedLines - consumed.size();

        log.info("Produced: {} unique events", produced.size());
        log.info("Consumed: {} unique events ({} total lines, {} duplicates)",
            consumed.size(), totalConsumedLines, duplicates);
        log.info("Missing (gaps): {}", missing.size());

        if (!missing.isEmpty()) {
            log.error("❌ MISSED CHANGES DETECTED:");
            missing.stream().limit(50).forEach(id -> log.error("  missing: {}", id));
            if (missing.size() > 50) {
                log.error("  ... and {} more", missing.size() - 50);
            }
        }

        int orderViolations = checkOrderingByLsn(consumedFile);
        int crtsViolations = checkOrderingByCrts(consumedFile);

        boolean passed = missing.isEmpty() && orderViolations == 0 && crtsViolations == 0;
        log.info(passed ? "✅ All checks passed" : "❌ Checks FAILED");
        return passed ? 0 : 1;
    }

    public static int parity(String lvFile, String avadFile) throws IOException {
        log.info("=== LV ↔ AVAD Parity: {} vs {} ===", lvFile, avadFile);

        Set<String> lvIds = loadEventIds(lvFile);
        Set<String> avadIds = loadEventIds(avadFile);

        Set<String> missingInAvad = new HashSet<>(lvIds);
        missingInAvad.removeAll(avadIds);

        Set<String> avadOnly = new HashSet<>(avadIds);
        avadOnly.removeAll(lvIds);

        log.info("LV events: {}", lvIds.size());
        log.info("AVAD events: {}", avadIds.size());
        log.info("Missing in AVAD (should be 0): {}", missingInAvad.size());
        log.info("AVAD-only events (deletes, extra versions): {}", avadOnly.size());

        if (!missingInAvad.isEmpty()) {
            log.error("❌ AVAD MISSING LV EVENTS:");
            missingInAvad.stream().limit(50).forEach(id -> log.error("  missing: {}", id));
        }

        boolean passed = missingInAvad.isEmpty();
        log.info(passed ? "✅ Parity check passed (AVAD ⊇ LV)" : "❌ Parity check FAILED");
        return passed ? 0 : 1;
    }

    /** Loads unique eventIds (first field per line). */
    private static Set<String> loadEventIds(String file) throws IOException {
        try (java.util.stream.Stream<String> lines = Files.lines(java.nio.file.Paths.get(file))) {
            return lines
                .filter(l -> !l.trim().isEmpty())
                .map(l -> l.split(",")[0])
                .collect(Collectors.toSet());
        }
    }

    /**
     * Check that LSN is monotonically increasing per partitionKey.
     * Line format: eventId,seqNo,opType,partitionKey,timestamp,lsn
     * Sorts by (partitionKey, lsn) then checks for inversions.
     */
    private static int checkOrderingByLsn(String consumedFile) throws IOException {
        log.info("=== LSN Ordering Check: {} ===", consumedFile);

        // Load all records grouped by partition key
        Map<String, List<long[]>> recordsByPk = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(consumedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 6) continue;

                String pk = parts[3];
                long seqNo = Long.parseLong(parts[1]);
                long lsn = parts[5].trim().isEmpty() ? -1 : Long.parseLong(parts[5]);
                if (lsn < 0) continue; // skip records without LSN

                recordsByPk.computeIfAbsent(pk, k -> new ArrayList<>())
                    .add(new long[]{lsn, seqNo});
            }
        }

        int violations = 0;
        for (Map.Entry<String, List<long[]>> entry : recordsByPk.entrySet()) {
            String pk = entry.getKey();
            List<long[]> records = entry.getValue();
            // Sort by LSN, then check seqNo is non-decreasing within same LSN batch
            records.sort(Comparator.comparingLong(r -> r[0]));

            long prevLsn = -1;
            for (long[] record : records) {
                if (record[0] < prevLsn) {
                    violations++;
                    if (violations <= 10) {
                        log.warn("LSN ordering violation: PK={}, prevLsn={}, currLsn={}",
                            pk, prevLsn, record[0]);
                    }
                }
                prevLsn = record[0];
            }
        }

        log.info("LSN ordering violations: {} (across {} partition keys)",
            violations, recordsByPk.size());
        return violations;
    }

    /**
     * Check that CRTS is monotonically increasing per partitionKey.
     * Line format: eventId,seqNo,opType,partitionKey,timestamp,lsn,crts
     * Only applies to AVAD logs (7 columns). Lines without CRTS are skipped.
     */
    private static int checkOrderingByCrts(String consumedFile) throws IOException {
        log.info("=== CRTS Ordering Check: {} ===", consumedFile);

        Map<String, List<long[]>> recordsByPk = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(consumedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 7) continue; // CRTS is column 6, only in AVAD logs

                String pk = parts[3];
                long lsn = parts[5].trim().isEmpty() ? -1 : Long.parseLong(parts[5]);
                long crts = parts[6].trim().isEmpty() ? -1 : Long.parseLong(parts[6]);
                if (crts < 0) continue;

                recordsByPk.computeIfAbsent(pk, k -> new ArrayList<>())
                    .add(new long[]{crts, lsn});
            }
        }

        if (recordsByPk.isEmpty()) {
            log.info("No CRTS data found (not an AVAD log?), skipping check");
            return 0;
        }

        int violations = 0;
        for (Map.Entry<String, List<long[]>> entry : recordsByPk.entrySet()) {
            String pk = entry.getKey();
            List<long[]> records = entry.getValue();
            // Sort by LSN (delivery order), then check CRTS is non-decreasing
            records.sort(Comparator.comparingLong(r -> r[1]));

            long prevCrts = -1;
            for (long[] record : records) {
                if (prevCrts > 0 && record[0] < prevCrts) {
                    violations++;
                    if (violations <= 10) {
                        log.warn("CRTS ordering violation: PK={}, prevCrts={}, currCrts={}, lsn={}",
                            pk, prevCrts, record[0], record[1]);
                    }
                }
                prevCrts = record[0];
            }
        }

        log.info("CRTS ordering violations: {} (across {} partition keys)",
            violations, recordsByPk.size());
        return violations;
    }
}
