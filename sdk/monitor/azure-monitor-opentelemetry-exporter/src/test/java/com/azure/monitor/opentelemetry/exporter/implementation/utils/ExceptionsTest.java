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
