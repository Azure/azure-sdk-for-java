// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for {@link AzureMonitorExporterBuilder}.
 */
public class AzureMonitorExporterBuilderTest {

    @ParameterizedTest
    @MethodSource("getInvalidConnectionStrings")
    public <T extends RuntimeException> void testInvalidConnectionStrings(String connectionString,
                                                                          Class<T> exceptionExpected) {
        Assertions.assertThrows(exceptionExpected, () -> new AzureMonitorExporterBuilder()
            .connectionString(connectionString)
            .buildTraceExporter());

    }

    private static Stream<Arguments> getInvalidConnectionStrings() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey=;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("Instrumentation=iKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("IngestionEndpoint=url", IllegalArgumentException.class)
        );
    }
}
