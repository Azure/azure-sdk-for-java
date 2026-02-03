// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for instrumentation.
 */
public final class InstrumentationUtils {
    // Histogram boundaries are optimized for common latency ranges (in seconds). They are
    // provided as advice at metric creation time and could be overriden by the user application via
    // OTel configuration.
    // TODO (limolkova): document client core metric conventions along with logical operation histogram boundaries.
    private static final List<Double> DURATION_BOUNDARIES_ADVICE = Collections.unmodifiableList(
        Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d));

    public static final SdkInstrumentationOptions UNKNOWN_LIBRARY_OPTIONS = new SdkInstrumentationOptions("unknown");

    /**
     * Creates a new {@link DoubleHistogram} for measuring the duration of client operations.
     * This metric is experimental.
     *
     * @param isExperimentalFeaturesEnabled whether experimental features are enabled
     * @param sdkName the name of the library - corresponds to artifact id and does not include group id
     * @param meter the meter to use for creating the histogram
     * @return a new {@link DoubleHistogram} for measuring the duration of client operations
     */
    public static DoubleHistogram createOperationDurationHistogram(boolean isExperimentalFeaturesEnabled,
        String sdkName, Meter meter) {
        if (isExperimentalFeaturesEnabled && meter.isEnabled() && sdkName != null) {
            String metricDescription = "Duration of client operation";
            String metricName = sdkName.replace("-", ".") + ".client.operation.duration";
            return meter.createDoubleHistogram(metricName, metricDescription, "s", DURATION_BOUNDARIES_ADVICE);
        }

        return NoopMeter.NOOP_LONG_HISTOGRAM;
    }

    /**
     * Does the best effort to capture the server port with minimum perf overhead.
     * If port is not set, we check scheme for "http" and "https" (case-sensitive).
     * If scheme is not one of those, returns -1.
     *
     * @param uri request URI
     * @return server port
     */
    public static int getServerPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            switch (uri.getScheme()) {
                case "http":
                    return 80;

                case "https":
                    return 443;

                default:
                    break;
            }
        }
        return port;
    }

    private InstrumentationUtils() {
    }
}
