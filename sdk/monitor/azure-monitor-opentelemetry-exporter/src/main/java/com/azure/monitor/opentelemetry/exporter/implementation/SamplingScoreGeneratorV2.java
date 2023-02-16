// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.util.annotation.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This class generates the sample using the random number generator. It also contains the logic to
 * preserve the correlated telemetry items.
 */
public class SamplingScoreGeneratorV2 {

    /**
     * This method takes the telemetry and returns the hash of the operation id if it is present
     * already or uses the random number generator to generate the sampling score.
     *
     * @return [0.0, 1.0)
     */
    @SuppressFBWarnings(
        value = "SECPR", // Predictable pseudorandom number generator
        justification = "Predictable random is ok for sampling score")
    public static double getSamplingScore(@Nullable String operationId) {
        if (operationId != null && !operationId.isEmpty()) {
            return 100 * ((double) getSamplingHashCode(operationId) / Integer.MAX_VALUE);
        } else {
            return 100 * ThreadLocalRandom.current().nextDouble();
        }
    }

    /**
     * Returns value in [0, Integer.MAX_VALUE).
     */
    private static int getSamplingHashCode(String operationId) {

        CharSequence opId;
        if (operationId.length() < 8) {
            StringBuilder opIdBuilder = new StringBuilder(operationId);
            while (opIdBuilder.length() < 8) {
                opIdBuilder.append(operationId);
            }
            opId = opIdBuilder;
        } else {
            opId = operationId;
        }

        int hash = 5381;

        for (int i = 0; i < opId.length(); ++i) {
            hash = ((hash << 5) + hash) + (int) opId.charAt(i);
        }

        if (hash == Integer.MIN_VALUE || hash == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE - 1;
        }
        return Math.abs(hash);
    }

    private SamplingScoreGeneratorV2() {
    }
}
