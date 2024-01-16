// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentationsTest {

    private static final Set<String> instrumentations;
    private static final long[] EXPECTED_INSTRUMENTATIONS;

    static {
        instrumentations = new HashSet<>();
        instrumentations.add("io.opentelemetry.jdbc");
        instrumentations.add("io.opentelemetry.tomcat-7.0");
        instrumentations.add("io.opentelemetry.http-url-connection");
        instrumentations.add("io.opentelemetry.apache-camel-2.20");
        instrumentations.add("io.opentelemetry.akka-http-10.0");
        instrumentations.add("io.opentelemetry.methods");
        instrumentations.add("io.opentelemetry.okhttp-2.2");
        instrumentations.add("io.opentelemetry.play-ws-2.0");
        instrumentations.add("io.opentelemetry.vertx-kafka-client-3.5");

        EXPECTED_INSTRUMENTATIONS = new long[2];
        EXPECTED_INSTRUMENTATIONS[0] =
            (long)
                (Math.pow(2, 5)
                    + Math.pow(2, 13)
                    + Math.pow(2, 21)
                    + Math.pow(2, 42)
                    + Math.pow(2, 47));
        // Exponents are keys from StatsbeatTestUtils.INSTRUMENTATION_MAP_DECODING.
        EXPECTED_INSTRUMENTATIONS[1] =
            (long)
                (Math.pow(2, 64 - 64)
                    + Math.pow(2, 65 - 64)
                    + Math.pow(2, 69 - 64)
                    + Math.pow(2, 71 - 64));
        // Exponents are keys from StatsbeatTestUtils.INSTRUMENTATION_MAP_DECODING - 64.
    }

    @Test
    public void testEncodeAndDecodeInstrumentations() {
        long[] longVal = Instrumentations.encode(instrumentations);
        assertThat(longVal).isEqualTo(EXPECTED_INSTRUMENTATIONS);
        Set<String> result = StatsbeatTestUtils.decodeInstrumentations(longVal);
        assertThat(result).isEqualTo(instrumentations);
    }
}
