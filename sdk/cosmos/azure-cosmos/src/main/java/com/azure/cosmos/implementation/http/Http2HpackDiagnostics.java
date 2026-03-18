// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http2.Http2HeadersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Diagnostic wrapper around {@link Http2HeadersEncoder.SensitivityDetector} that tracks
 * per-header cardinality and HPACK dynamic table utilization.
 * <p>
 * This class is designed for profiling and benchmarking. It answers two key questions:
 * <ol>
 *   <li><b>Header cardinality:</b> Which headers have many unique values and therefore
 *       pollute the HPACK dynamic table? (candidates for never-indexed treatment)</li>
 *   <li><b>HPACK table utilization:</b> How full is the dynamic table, and how often
 *       are entries evicted (churn)?</li>
 * </ol>
 * <p>
 * <b>Activation:</b> Set system property {@code COSMOS_HTTP2_HPACK_DIAGNOSTICS_ENABLED=true}
 * or environment variable {@code COSMOS_HTTP2_HPACK_DIAGNOSTICS_ENABLED=true}.
 * <p>
 * <b>How it works:</b> The HPACK encoder calls {@link #isSensitive(CharSequence, CharSequence)}
 * for every header on every request. This wrapper intercepts that call to:
 * <ul>
 *   <li>Track unique value hashes per header name (bounded to avoid memory growth)</li>
 *   <li>Count total invocations per header name</li>
 *   <li>Periodically sample HPACK encoder table size via reflection</li>
 *   <li>Log a diagnostic report every N requests</li>
 * </ul>
 * <p>
 * Performance overhead is minimal: one ConcurrentHashMap lookup + one hash computation per header.
 * The bounded unique-value set caps at {@value #MAX_UNIQUE_VALUES_PER_HEADER} entries per header.
 */
final class Http2HpackDiagnostics implements Http2HeadersEncoder.SensitivityDetector {

    private static final Logger logger = LoggerFactory.getLogger(Http2HpackDiagnostics.class);

    static final String DIAGNOSTICS_ENABLED_PROPERTY = "COSMOS_HTTP2_HPACK_DIAGNOSTICS_ENABLED";

    private static final int REPORT_INTERVAL_REQUESTS = 1000;
    private static final int MAX_UNIQUE_VALUES_PER_HEADER = 500;

    private final Http2HeadersEncoder.SensitivityDetector delegate;

    // Per-header-name statistics
    private final ConcurrentHashMap<String, HeaderStats> headerStatsMap = new ConcurrentHashMap<>();

    // Global request counter (approximate — one "request" = one isSensitive call for :method pseudo-header)
    private final AtomicLong totalHeadersEncoded = new AtomicLong();
    private final AtomicLong reportCounter = new AtomicLong();

    // HPACK encoder reference for table metrics (set after installation)
    // Typed as Object because HpackEncoder is package-private in Netty
    private volatile Object hpackEncoder;
    private volatile Method hpackSizeMethod;
    private volatile Method hpackMaxTableSizeMethod;
    private volatile Method hpackLengthMethod;

    Http2HpackDiagnostics(Http2HeadersEncoder.SensitivityDetector delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isSensitive(CharSequence name, CharSequence value) {
        trackHeader(name, value);
        return delegate.isSensitive(name, value);
    }

    /**
     * Sets the HpackEncoder reference for reading table utilization metrics.
     * HpackEncoder is package-private in Netty, so we use Object + reflection for its methods.
     */
    void setHpackEncoder(Object encoder) {
        this.hpackEncoder = encoder;
        try {
            Class<?> encoderClass = encoder.getClass();
            this.hpackSizeMethod = encoderClass.getMethod("size");
            this.hpackMaxTableSizeMethod = encoderClass.getMethod("getMaxHeaderTableSize");
            this.hpackLengthMethod = encoderClass.getMethod("length");
        } catch (NoSuchMethodException e) {
            logger.debug("Could not resolve HpackEncoder metric methods", e);
        }
    }

    private void trackHeader(CharSequence name, CharSequence value) {
        String headerName = name.toString().toLowerCase();
        HeaderStats stats = headerStatsMap.computeIfAbsent(headerName, k -> new HeaderStats());
        stats.recordValue(value);

        long total = totalHeadersEncoded.incrementAndGet();
        // Log report periodically (approximate — may fire slightly more/less due to concurrency)
        if (total % (REPORT_INTERVAL_REQUESTS * 15L) == 0) {
            // ~15 headers per request × REPORT_INTERVAL_REQUESTS
            long reportNum = reportCounter.incrementAndGet();
            logDiagnosticReport(reportNum);
        }
    }

    /**
     * Forces a diagnostic report to be logged. Can be called externally for on-demand reporting.
     */
    void logDiagnosticReport() {
        logDiagnosticReport(reportCounter.incrementAndGet());
    }

    private void logDiagnosticReport(long reportNumber) {
        if (!logger.isInfoEnabled()) {
            return;
        }

        StringBuilder sb = new StringBuilder(1024);
        sb.append("\n=== HPACK Diagnostics Report #").append(reportNumber).append(" ===\n");

        // HPACK table metrics
        Object encoder = this.hpackEncoder;
        if (encoder != null && hpackSizeMethod != null) {
            try {
                long tableSize = ((Number) hpackSizeMethod.invoke(encoder)).longValue();
                long maxTableSize = ((Number) hpackMaxTableSizeMethod.invoke(encoder)).longValue();
                int entryCount = ((Number) hpackLengthMethod.invoke(encoder)).intValue();
                double utilization = maxTableSize > 0 ? (double) tableSize / maxTableSize * 100.0 : 0;
                sb.append(String.format("HPACK Table: %d/%d bytes (%.1f%% utilized), %d entries%n",
                    tableSize, maxTableSize, utilization, entryCount));
            } catch (Exception e) {
                sb.append("HPACK Table: [error reading metrics: ").append(e.getMessage()).append("]\n");
            }
        } else {
            sb.append("HPACK Table: [encoder reference not available]\n");
        }

        sb.append(String.format("Total headers encoded: %d%n%n", totalHeadersEncoded.get()));

        // Per-header cardinality report, sorted by unique values descending
        sb.append(String.format("%-45s %10s %10s %12s %s%n",
            "Header Name", "Invocations", "Unique Vals", "Avg Size(B)", "Cardinality"));
        sb.append(String.format("%-45s %10s %10s %12s %s%n",
            "---------------------------------------------",
            "----------", "----------", "------------", "------------"));

        headerStatsMap.entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, HeaderStats>>comparingInt(
                    e -> e.getValue().getUniqueValueCount())
                .reversed())
            .forEach(entry -> {
                HeaderStats stats = entry.getValue();
                long count = stats.getInvocationCount();
                int uniqueValues = stats.getUniqueValueCount();
                long avgSize = count > 0 ? stats.getTotalValueBytes() / count : 0;
                String cardinality = classifyCardinality(count, uniqueValues);
                boolean capped = uniqueValues >= MAX_UNIQUE_VALUES_PER_HEADER;

                sb.append(String.format("%-45s %10d %9d%s %12d %s%n",
                    truncate(entry.getKey(), 45),
                    count,
                    uniqueValues,
                    capped ? "+" : " ",
                    avgSize,
                    cardinality));
            });

        sb.append("\nCardinality legend: STATIC=1 value, LOW=2-10, MEDIUM=11-100, HIGH=101+\n");
        sb.append("'+' after unique values means the tracking cap (")
            .append(MAX_UNIQUE_VALUES_PER_HEADER)
            .append(") was reached; actual count may be higher.\n");

        // Recommendations
        sb.append("\n--- Recommendations ---\n");
        headerStatsMap.entrySet().stream()
            .filter(e -> e.getValue().getUniqueValueCount() >= 50)
            .sorted(Comparator.<Map.Entry<String, HeaderStats>>comparingLong(
                    e -> e.getValue().getTotalValueBytes() / Math.max(1, e.getValue().getInvocationCount()))
                .reversed())
            .forEach(entry -> {
                HeaderStats stats = entry.getValue();
                long avgSize = stats.getTotalValueBytes() / Math.max(1, stats.getInvocationCount());
                sb.append(String.format(
                    "  CONSIDER never-indexing '%s' (%d unique values, avg %d bytes)%n",
                    entry.getKey(), stats.getUniqueValueCount(), avgSize));
            });

        logger.info(sb.toString());
    }

    private static String classifyCardinality(long invocations, int uniqueValues) {
        if (uniqueValues <= 1) {
            return "STATIC";
        } else if (uniqueValues <= 10) {
            return "LOW";
        } else if (uniqueValues <= 100) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    /**
     * Returns whether HPACK diagnostics is enabled via system property or environment variable.
     */
    static boolean isEnabled() {
        String value = System.getProperty(DIAGNOSTICS_ENABLED_PROPERTY);
        if (value == null) {
            value = System.getenv(DIAGNOSTICS_ENABLED_PROPERTY);
        }
        return "true".equalsIgnoreCase(value);
    }

    /**
     * Per-header-name statistics tracker. Thread-safe via atomic operations and bounded set.
     */
    static final class HeaderStats {
        private final AtomicLong invocationCount = new AtomicLong();
        private final AtomicLong totalValueBytes = new AtomicLong();
        // Bounded set of value hashes to approximate unique value count
        private final ConcurrentHashMap<Integer, Boolean> uniqueValueHashes
            = new ConcurrentHashMap<>();

        void recordValue(CharSequence value) {
            invocationCount.incrementAndGet();
            int valueLen = value != null ? value.length() : 0;
            totalValueBytes.addAndGet(valueLen);
            if (uniqueValueHashes.size() < MAX_UNIQUE_VALUES_PER_HEADER && value != null) {
                uniqueValueHashes.putIfAbsent(value.hashCode(), Boolean.TRUE);
            }
        }

        long getInvocationCount() {
            return invocationCount.get();
        }

        int getUniqueValueCount() {
            return uniqueValueHashes.size();
        }

        long getTotalValueBytes() {
            return totalValueBytes.get();
        }
    }
}
