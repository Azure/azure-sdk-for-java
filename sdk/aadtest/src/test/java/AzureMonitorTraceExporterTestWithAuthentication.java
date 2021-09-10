// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.implementation.IdentityClient;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorTraceExporter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Unit tests for {@AzureMonitorExporter}.
 */
public class AzureMonitorTraceExporterTestWithAuthentication {

    private static final String TRACE_ID = TraceId.fromLongs(10L, 2L);
    private static final String SPAN_ID = SpanId.fromLong(1);
    private static final TraceState TRACE_STATE = TraceState.builder().build();

    AzureMonitorExporterBuilder getClientBuilder() {
        HttpClient httpClient;
        httpClient = HttpClient.createDefault();

        // TokenCredential credential = new IntelliJCredentialBuilder()
        //     .keePassDatabasePath("C:\\Users\\kryalama\\AppData\\Roaming\\JetBrains\\IdeaIC2020.2\\c.kdbx")
        //     .build();
        //  TokenCredential credential = new ManagedIdentityCredentialBuilder()
        //      .build();
        //  TokenCredential credential = new DefaultAzureCredentialBuilder()
        //      .build();
        return new AzureMonitorExporterBuilder()
            //.credential(credential)
            .httpClient(httpClient);
        //.addPolicy(interceptorManager.getRecordPolicy());
    }

    @Test
    public void testExportRequestData() throws Exception {
        /*
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(getMockAccessToken(token1, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);
         */
        AzureMonitorTraceExporter azureMonitorTraceExporter = getClientBuilder()
            .connectionString("InstrumentationKey=88222143-ef05-4b82-90a2-53f1e3999132;IngestionEndpoint=https://westus2-2.in.applicationinsights.azure.com/")
            .buildTraceExporter();
        CompletableResultCode export = azureMonitorTraceExporter.export(Collections.singleton(new RequestSpanData()));
        //export.join(200, TimeUnit.SECONDS);
        Thread.sleep(20000);
        Assertions.assertTrue(export.isDone());
        Assertions.assertTrue(export.isSuccess());
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
