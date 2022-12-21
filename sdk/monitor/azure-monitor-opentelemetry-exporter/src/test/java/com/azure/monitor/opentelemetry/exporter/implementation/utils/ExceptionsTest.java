// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionDetailBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.Exceptions;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionDetails;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionsTest {

    @Test
    public void testMinimalParse() {
        // given
        String str = toString(new IllegalStateException("test"));

        // when
        List<ExceptionDetailBuilder> list = Exceptions.minimalParse(str);

        // then
        assertThat(list.size()).isEqualTo(1);

        TelemetryExceptionDetails details = list.get(0).build();
        assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
        assertThat(details.getMessage()).isEqualTo("test");
    }

    @Test
    public void testMinimalParseWithColonInMessage() {
        // given
        String str = toString(new IllegalStateException("hello: world"));

        // when
        List<ExceptionDetailBuilder> list = Exceptions.minimalParse(str);

        // then
        assertThat(list.size()).isEqualTo(1);

        TelemetryExceptionDetails details = list.get(0).build();
        assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
        assertThat(details.getMessage()).isEqualTo("hello: world");
    }

    @Test
    public void testMinimalParseWithNoMessage() {
        // given
        String str = toString(new IllegalStateException());

        // when
        List<ExceptionDetailBuilder> list = Exceptions.minimalParse(str);

        // then
        assertThat(list.size()).isEqualTo(1);

        TelemetryExceptionDetails details = list.get(0).build();
        assertThat(details.getTypeName()).isEqualTo(IllegalStateException.class.getName());
        assertThat(details.getMessage()).isEqualTo(IllegalStateException.class.getName());
    }

    @Test
    public void testMinimalParseWithProblematicMessage() {
        // given
        String str = toString(new ProblematicException());

        // when
        List<ExceptionDetailBuilder> list = Exceptions.minimalParse(str);

        // then
        assertThat(list.size()).isEqualTo(1);

        TelemetryExceptionDetails details = list.get(0).build();
        assertThat(details.getTypeName()).isEqualTo(ProblematicException.class.getName());
        assertThat(details.getMessage()).isEqualTo(ProblematicException.class.getName());
    }

    private static String toString(Throwable t) {
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    @SuppressWarnings("OverrideThrowableToString")
    private static class ProblematicException extends Exception {
        @Override
        public String toString() {
            return ProblematicException.class.getName() + ":";
        }
    }
}
