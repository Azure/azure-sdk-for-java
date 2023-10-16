// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

/**
 * Unit tests for {@link AzureMonitorExporterBuilder}.
 */
public class AzureMonitorExporterBuilderTest {
    @ParameterizedTest
    @MethodSource("getInvalidConnectionStrings")
    public <T extends RuntimeException> void testInvalidConnectionStrings(
        String connectionString, Class<T> exceptionExpected) {
        Assertions.assertThrows(
            exceptionExpected,
            () -> {
                AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
                new AzureMonitorExporterBuilder()
                    .connectionString(connectionString)
                    .build(sdkBuilder);
                sdkBuilder.build();
            });
    }

    private static Stream<Arguments> getInvalidConnectionStrings() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey=;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("Instrumentation=iKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("InstrumentationKey;IngestionEndpoint=url", IllegalArgumentException.class),
            Arguments.of("IngestionEndpoint=url", IllegalArgumentException.class));
    }
}
