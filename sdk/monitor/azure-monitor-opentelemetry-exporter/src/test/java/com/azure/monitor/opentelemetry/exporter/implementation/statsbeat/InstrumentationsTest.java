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
        instrumentations.add("io.opentelemetry.jdbc"); // 21 INDEX
        instrumentations.add("io.opentelemetry.tomcat-7.0"); // 5 INDEX
        instrumentations.add("io.opentelemetry.http-url-connection"); // 13 INDEX
        instrumentations.add("io.opentelemetry.apache-camel-2.20"); // 42 INDEX
        instrumentations.add("io.opentelemetry.akka-http-10.0"); // 47 INDEX
        instrumentations.add("io.opentelemetry.methods"); // 64 INDEX
        instrumentations.add("io.opentelemetry.okhttp-2.2"); // 65 INDEX
        instrumentations.add("io.opentelemetry.play-ws-2.0"); // 69 INDEX
        instrumentations.add("io.opentelemetry.vertx-kafka-client-3.5"); // 71 INDEX
        instrumentations.add("io.opentelemetry.hikaricp-3.0"); // 72 INDEX
        instrumentations.add("io.opentelemetry.micrometer-1.5"); // 73 INDEX
        instrumentations.add("io.opentelemetry.kafka-clients-2.6"); // 74 INDEX
        instrumentations.add("io.opentelemetry.spring-kafka-2.7"); // 75 INDEX
        instrumentations.add("io.opentelemetry.spring-web-3.1"); // 76 INDEX
        instrumentations.add("io.opentelemetry.spring-webmvc-5.3"); // 77 INDEX
        instrumentations.add("io.opentelemetry.spring-webmvc-6.0"); // 78 INDEX
        instrumentations.add("io.opentelemetry.spring-webflux-5.3"); // 79 INDEX
        instrumentations.add("io.opentelemetry.runtime-telemetry-java8"); // 80 INDEX
        instrumentations.add("io.opentelemetry.runtime-telemetry-java17"); // 81 INDEX

        EXPECTED_INSTRUMENTATIONS = new long[2];
        EXPECTED_INSTRUMENTATIONS[0]
            = (long) (Math.pow(2, 5) + Math.pow(2, 13) + Math.pow(2, 21) + Math.pow(2, 42) + Math.pow(2, 47));
        // Exponents are keys from StatsbeatTestUtils.INSTRUMENTATION_MAP_DECODING.
        EXPECTED_INSTRUMENTATIONS[1] = (long) (Math.pow(2, 64 - 64) + Math.pow(2, 65 - 64) + Math.pow(2, 69 - 64)
            + Math.pow(2, 71 - 64) + Math.pow(2, 72 - 64) + Math.pow(2, 73 - 64) + Math.pow(2, 74 - 64)
            + Math.pow(2, 75 - 64) + Math.pow(2, 76 - 64) + Math.pow(2, 77 - 64) + Math.pow(2, 78 - 64)
            + Math.pow(2, 79 - 64) + Math.pow(2, 80 - 64) + Math.pow(2, 81 - 64));
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
