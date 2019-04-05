// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.core;

import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeader;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.ProxyOptions;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.utils.SdkContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

public class InterceptorManager implements Closeable {
    private final static String RECORD_FOLDER = "session-records/";
    private final static int DEFAULT_BUFFER_LENGTH = 1024;

    private final Logger logger = LoggerFactory.getLogger(InterceptorManager.class);
    private final Map<String, String> textReplacementRules = new HashMap<>();
    private final String testName;
    private final TestMode testMode;

    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup
    private final RecordedData recordedData;

    private InterceptorManager(String testName, TestMode testMode) throws IOException {
        this.testName = testName;
        this.testMode = testMode;

        this.recordedData = testMode == TestMode.PLAYBACK
            ? readDataFromFile()
            : new RecordedData();
    }

    // factory method
    public static InterceptorManager create(String testName, TestMode testMode) throws IOException {
        InterceptorManager interceptorManager = new InterceptorManager(testName, testMode);

        //TODO: Do we need this?
        SdkContext.setResourceNamerFactory(new TestResourceNamerFactory(interceptorManager));
        SdkContext.setDelayProvider(new TestDelayProvider(interceptorManager.isRecordMode()));

        return interceptorManager;
    }

    public boolean isRecordMode() {
        return testMode == TestMode.RECORD;
    }

    public boolean isPlaybackMode() {
        return testMode == TestMode.PLAYBACK;
    }

    public RecordPolicy getRecordPolicy() {
        return new RecordPolicy();
    }

    public HttpClient getPlaybackClient() {
        return new PlaybackClient();
    }

    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }

    @Override
    public void close() {
        switch (testMode) {
            case RECORD:
                try {
                    writeDataToFile();
                } catch (IOException e) {
                    logger.error("Unable to write data to playback file.", e);
                }
                break;
            case PLAYBACK:
                // Do nothing
                break;
            default:
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
        }
    }

    public class RecordPolicy implements HttpPipelinePolicy {
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            final NetworkCallRecord networkCallRecord = new NetworkCallRecord();

            networkCallRecord.Headers = new HashMap<>();

            if (context.httpRequest().headers().value("Content-Type") != null) {
                networkCallRecord.Headers.put("Content-Type", context.httpRequest().headers().value("Content-Type"));
            }
            if (context.httpRequest().headers().value("x-ms-version") != null) {
                networkCallRecord.Headers.put("x-ms-version", context.httpRequest().headers().value("x-ms-version"));
            }
            if (context.httpRequest().headers().value("User-Agent") != null) {
                networkCallRecord.Headers.put("User-Agent", context.httpRequest().headers().value("User-Agent"));
            }

            networkCallRecord.Method = context.httpRequest().httpMethod().toString();
            networkCallRecord.Uri = applyReplacementRule(context.httpRequest().url().toString().replaceAll("\\?$", ""));

            return next.process().flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                return extractResponseData(bufferedResponse).map(responseData -> {
                    networkCallRecord.Response = responseData;
                    String body = networkCallRecord.Response.get("Body");

                    // Remove pre-added header if this is a waiting or redirection
                    if (body != null && body.contains("<Status>InProgress</Status>")
                        || Integer.parseInt(networkCallRecord.Response.get("StatusCode")) == HttpResponseStatus.TEMPORARY_REDIRECT.code()) {
                        logger.info("Waiting for a response or redirection.");
                    } else {
                        synchronized (recordedData.getNetworkCallRecords()) {
                            recordedData.getNetworkCallRecords().add(networkCallRecord);
                        }
                    }

                    return bufferedResponse;
                });
            });
        }
    }

    final class PlaybackClient implements HttpClient {
        AtomicInteger count = new AtomicInteger(0);

        @Override
        public Mono<HttpResponse> send(final HttpRequest request) {
            return Mono.defer(() -> playbackHttpResponse(request));
        }

        @Override
        public HttpClient proxy(Supplier<ProxyOptions> supplier) {
            return this;
        }

        @Override
        public HttpClient wiretap(boolean b) {
            return this;
        }

        @Override
        public HttpClient port(int i) {
            return this;
        }

        private Mono<HttpResponse> playbackHttpResponse(final HttpRequest request) {
            String incomingUrl = applyReplacementRule(request.url().toString());
            String incomingMethod = request.httpMethod().toString();

            incomingUrl = removeHost(incomingUrl);
            NetworkCallRecord networkCallRecord = null;
            synchronized (recordedData) {
                for (Iterator<NetworkCallRecord> iterator = recordedData.getNetworkCallRecords().iterator(); iterator.hasNext(); ) {
                    NetworkCallRecord record = iterator.next();
                    if (record.Method.equalsIgnoreCase(incomingMethod) && removeHost(record.Uri).equalsIgnoreCase(incomingUrl)) {
                        networkCallRecord = record;
                        iterator.remove();
                        break;
                    }
                }
            }

            count.incrementAndGet();
            if (networkCallRecord == null) {
                logger.warn("NOT FOUND - Method: {} URL: {}", incomingMethod, incomingUrl);
                logger.warn("Records requested: {}. Remaining Records: {}.", count, recordedData.getNetworkCallRecords().size());

                Assert.fail("==> Unexpected request: " + incomingMethod + " " + incomingUrl);
            }

            int recordStatusCode = Integer.parseInt(networkCallRecord.Response.get("StatusCode"));
            HttpHeaders headers = new HttpHeaders();

            for (Map.Entry<String, String> pair : networkCallRecord.Response.entrySet()) {
                if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
                    String rawHeader = pair.getValue();
                    for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                        if (rule.getValue() != null) {
                            rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                        }
                    }
                    headers.set(pair.getKey(), rawHeader);
                }
            }

            String rawBody = networkCallRecord.Response.get("Body");
            byte[] bytes = new byte[0];

            if (rawBody != null) {
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                    }
                }

                bytes = rawBody.getBytes(StandardCharsets.UTF_8);
                headers.set("Content-Length", String.valueOf(bytes.length));
            }

            HttpResponse response = new MockHttpResponse(recordStatusCode, headers, bytes)
                .withRequest(request);
            return Mono.just(response);
        }
    }

    private Mono<Map<String, String>> extractResponseData(final HttpResponse response) {
        final Map<String, String> responseData = new HashMap<>();
        responseData.put("StatusCode", Integer.toString(response.statusCode()));

        boolean addedRetryAfter = false;
        for (HttpHeader header : response.headers()) {
            String headerValueToStore = header.value();

            if (header.name().equalsIgnoreCase("location") || header.name().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore);
            }
            if (header.name().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.name().toLowerCase(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String contentType = response.headerValue("content-type");
        if (contentType == null) {
            return Mono.just(responseData);
        } else if (contentType.contains("json") || response.headerValue("content-encoding") == null) {
            return response.bodyAsString().map(content -> {
                content = applyReplacementRule(content);
                responseData.put("Body", content);
                return responseData;
            });
        } else {
            return response.bodyAsByteArray().map(bytes -> {
                String content;
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_LENGTH];
                    int position = 0;
                    int bytesRead = gis.read(buffer, position, buffer.length);

                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead);
                        position += bytesRead;
                        bytesRead = gis.read(buffer, position, buffer.length);
                    }

                    content = new String(output.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }

                responseData.remove("content-encoding");
                responseData.put("content-length", Integer.toString(content.length()));

                content = applyReplacementRule(content);
                responseData.put("body", content);
                return responseData;
            });
        }
    }

    private RecordedData readDataFromFile() throws IOException {
        File recordFile = getRecordFile(testName);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        RecordedData recordedData = mapper.readValue(recordFile, RecordedData.class);

        logger.info("Total records: {}", recordedData.getNetworkCallRecords().size());

        return recordedData;
    }

    private void writeDataToFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File recordFile = getRecordFile(testName);
        recordFile.createNewFile();
        mapper.writeValue(recordFile, recordedData);
    }

    private static File getRecordFile(String testName) {
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String filePath = folderFile.getPath() + "/" + testName + ".json";
        System.out.println("==> Playback file path: " + filePath);
        return new File(filePath);
    }

    private String applyReplacementRule(String text) {
        for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    private static String removeHost(String url) {
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }

    //TODO: Do we really need this method?
    public void pushVariable(String variable) {
        if (this.isRecordMode()) {
            synchronized (recordedData.getVariables()) {
                recordedData.getVariables().add(variable);
            }
        }
    }

    //TODO: Do we really need this method?
    public String popVariable() {
        synchronized (recordedData.getVariables()) {
            return recordedData.getVariables().remove();
        }
    }
}
