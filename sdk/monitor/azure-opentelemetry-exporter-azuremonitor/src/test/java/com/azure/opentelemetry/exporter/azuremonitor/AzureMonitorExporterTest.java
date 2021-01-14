// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.attributes.SemanticAttributes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Unit tests for {@AzureMonitorExporter}.
 */
public class AzureMonitorExporterTest extends MonitorExporterClientTestBase {

    @Test
    public void testExportRequestData() {
        AzureMonitorExporter azureMonitorExporter = getClientBuilder()
            .connectionString("InstrumentationKey=ikey;IngestionEndpoint=https://testendpoint.com")
            .buildExporter();
        CompletableResultCode export = azureMonitorExporter.export(Collections.singleton(new RequestSpanData()));
        Assertions.assertTrue(export.isDone());
        Assertions.assertTrue(export.isSuccess());
    }

    static class RequestSpanData implements SpanData {
        @Override
        public TraceId getTraceId() {
            return new TraceId(10L, 2L);
        }

        @Override
        public SpanId getSpanId() {
            return new SpanId(1);
        }

        @Override
        public TraceFlags getTraceFlags() {
            return null;
        }

        @Override
        public TraceState getTraceState() {
            return TraceState.builder().build();
        }

        @Override
        public SpanId getParentSpanId() {
            return new SpanId(0);
        }

        @Override
        public Resource getResource() {
            return null;
        }

        @Override
        public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
            return InstrumentationLibraryInfo.create("TestLib", "1");
        }

        @Override
        public String getName() {
            return "/service/resource";
        }

        @Override
        public Span.Kind getKind() {
            return Span.Kind.INTERNAL;
        }

        @Override
        public long getStartEpochNanos() {
            return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
        }

        @Override
        public ReadableAttributes getAttributes() {
            return Attributes.newBuilder()
                .setAttribute(SemanticAttributes.HTTP_STATUS_CODE.key(), 200L)
                .setAttribute(SemanticAttributes.HTTP_URL.key(), "http://localhost")
                .setAttribute(SemanticAttributes.HTTP_METHOD.key(), "GET")
                .setAttribute("ai.sampling.percentage", 100.0)
                .build();
        }

        @Override
        public List<Event> getEvents() {
            return new ArrayList<>();
        }

        @Override
        public List<Link> getLinks() {
            return new ArrayList<>();
        }

        @Override
        public Status getStatus() {
            return Status.OK;
        }

        @Override
        public long getEndEpochNanos() {
            return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
        }

        @Override
        public boolean getHasRemoteParent() {
            return false;
        }

        @Override
        public boolean getHasEnded() {
            return false;
        }

        @Override
        public int getTotalRecordedEvents() {
            return 0;
        }

        @Override
        public int getTotalRecordedLinks() {
            return 0;
        }

        @Override
        public int getTotalAttributeCount() {
            return 0;
        }
    }
}
