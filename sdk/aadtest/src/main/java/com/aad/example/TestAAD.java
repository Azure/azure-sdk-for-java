package com.aad.example;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorTraceExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TestAAD {
    private static final String TRACE_ID = TraceId.fromLongs(10L, 2L);
    private static final String SPAN_ID = SpanId.fromLong(1);
    private static final TraceState TRACE_STATE = TraceState.builder().build();
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE = "https://monitor.azure.com//.default";
    public static void main(String args[]) throws InterruptedException {
        HttpClient httpClient = HttpClient.createDefault();
        TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
              .build();
        //TokenCredential credential = new ManagedIdentityCredentialBuilder().build();
        BearerTokenAuthenticationPolicy authenticationPolicy = new BearerTokenAuthenticationPolicy(defaultAzureCredential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE);
        AzureMonitorTraceExporter azureMonitorTraceExporter = new AzureMonitorExporterBuilder()
            .connectionString("InstrumentationKey=88222143-ef05-4b82-90a2-53f1e3999132;IngestionEndpoint=https://westus2-2.in.applicationinsights.azure.com/")
        //    .addPolicy(authenticationPolicy)
            .httpClient(httpClient)
            .buildTraceExporter();

        CompletableResultCode export = azureMonitorTraceExporter.export(Collections.singleton(new RequestSpanData()));
        //export.join(20, TimeUnit.SECONDS);
        Thread.sleep(20000);
        System.out.println(export.isSuccess());
        System.out.println(export.isDone());

    }

    static class RequestSpanData implements SpanData {

        @Override
        public SpanContext getSpanContext() {
            return SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE);
        }

        @Override
        public String getTraceId() {
            return TRACE_ID;
        }

        @Override
        public String getSpanId() {
            return SPAN_ID;
        }

        @Override
        public SpanContext getParentSpanContext() {
            return SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE);
        }

        @Override
        public String getParentSpanId() {
            return SpanId.fromLong(1);
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
        public SpanKind getKind() {
            return SpanKind.INTERNAL;
        }

        @Override
        public long getStartEpochNanos() {
            return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
        }

        @Override
        public Attributes getAttributes() {
            return Attributes.builder()
                .put("http.status_code", 200L)
                .put("http.url", "http://localhost")
                .put("http.method", "GET")
                .put("ai.sampling.percentage", 100.0)
                .build();
        }

        @Override
        public List<EventData> getEvents() {
            return new ArrayList<>();
        }

        @Override
        public List<LinkData> getLinks() {
            return new ArrayList<>();
        }

        @Override
        public StatusData getStatus() {
            return StatusData.ok();
        }

        @Override
        public long getEndEpochNanos() {
            return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
        }

        @Override
        public boolean hasEnded() {
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
