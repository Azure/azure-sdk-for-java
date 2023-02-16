// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.exporter.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TelemetryItemExporterTest {

    private static final String CONNECTION_STRING =
        "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar";
    private static final String REDIRECT_CONNECTION_STRING =
        "InstrumentationKey=11111111-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=http://foo.bar";

    private static final String INSTRUMENTATION_KEY = "00000000-0000-0000-0000-0FEEDDADBEEF";
    private static final String REDIRECT_URL = "http://foo.bar.redirect";

    RecordingHttpClient recordingHttpClient;

    @TempDir
    File tempFolder;

    private TelemetryItemExporter getExporter() {
        HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder().httpClient(recordingHttpClient);
        TelemetryPipeline telemetryPipeline = new TelemetryPipeline(pipelineBuilder.build());

        return new TelemetryItemExporter(
            telemetryPipeline,
            new LocalStorageTelemetryPipelineListener(50, tempFolder, telemetryPipeline, null, false));
    }

    private static String getRequestBodyString(Flux<ByteBuffer> requestBody) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] compressed = FluxUtil.collectBytesInByteBufferStream(requestBody).block();
        int bufferSize = compressed.length;
        String requestBodyString = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(bis, bufferSize);
            StringBuilder sb = new StringBuilder();
            byte[] data = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                sb.append(new String(data, 0, bytesRead, Charset.defaultCharset()));
            }
            bis.close();
            bos.close();
            gis.close();
            requestBodyString = new String(sb);
        } catch (IOException e) {
            // It's ok when this exception is thrown. The tests will fail. Added this comment to satisfy
            // style guide.
        }
        return requestBodyString;
    }

    @BeforeEach
    public void setup() {
        recordingHttpClient =
            new RecordingHttpClient(
                request -> {
                    if (request.getUrl().toString().contains(REDIRECT_URL)) {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                    Flux<ByteBuffer> requestBody = request.getBody();
                    String requestBodyString = getRequestBodyString(requestBody);
                    if (requestBodyString != null && requestBodyString.contains(INSTRUMENTATION_KEY)) {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Location", REDIRECT_URL);
                    HttpHeaders httpHeaders = new HttpHeaders(headers);
                    return Mono.just(new MockHttpResponse(request, 307, httpHeaders));
                });
    }

    @Test
    public void singleIkeyTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(1);
    }

    @Test
    public void dualIkeyTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 2, 2, REDIRECT_CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(3);
    }

    @Test
    public void singleIkeyBatchTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 2, 2, CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(1);
    }

    @Test
    public void dualIkeyBatchTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 2, 2, CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 3, 3, REDIRECT_CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 4, 4, REDIRECT_CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(3);
    }

    @Test
    public void dualIkeyBatchWithDelayTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 2, 2, CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 3, 3, REDIRECT_CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 4, 4, REDIRECT_CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(3);

        completableResultCode = exporter.send(telemetryItems);

        // then
        // the redirect url should be cached and should not invoke another redirect.
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(5);
    }

    @Test
    public void dualIkeyBatchWithDelayAndRedirectFlagFalseTest() {
        // given
        List<TelemetryItem> telemetryItems = new ArrayList<>();
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 1, 1, CONNECTION_STRING));
        telemetryItems.add(TestUtils.createMetricTelemetry("metric" + 2, 2, CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 3, 3, REDIRECT_CONNECTION_STRING));
        telemetryItems.add(
            TestUtils.createMetricTelemetry("metric" + 4, 4, REDIRECT_CONNECTION_STRING));
        TelemetryItemExporter exporter = getExporter();

        // when
        CompletableResultCode completableResultCode = exporter.send(telemetryItems);

        // then
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(3);

        completableResultCode = exporter.send(telemetryItems);

        // then
        // the redirect url should be cached and should not invoke another redirect.
        assertThat(completableResultCode.isSuccess()).isEqualTo(true);
        assertThat(recordingHttpClient.getCount()).isEqualTo(5);
    }

    static class RecordingHttpClient implements HttpClient {

        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Mono<HttpResponse>> handler;

        RecordingHttpClient(Function<HttpRequest, Mono<HttpResponse>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            count.getAndIncrement();
            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }
    }
}
