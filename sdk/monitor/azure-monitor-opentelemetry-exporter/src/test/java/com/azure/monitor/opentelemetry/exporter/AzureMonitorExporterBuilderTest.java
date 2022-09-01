/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link AzureMonitorExporterBuilder}. */
public class AzureMonitorExporterBuilderTest {
    @ParameterizedTest
    @MethodSource("getInvalidConnectionStrings")
    public <T extends RuntimeException> void testInvalidConnectionStrings(
        String connectionString, Class<T> exceptionExpected) {
        Assertions.assertThrows(
            exceptionExpected,
            () ->
                new AzureMonitorExporterBuilder()
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
            Arguments.of("IngestionEndpoint=url", IllegalArgumentException.class));
    }
}
