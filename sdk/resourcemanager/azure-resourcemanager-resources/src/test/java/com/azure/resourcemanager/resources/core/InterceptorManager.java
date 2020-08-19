// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.core;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nimbusds.jose.util.IOUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by vlashch on 7/13/2017.
 */
public final class InterceptorManager {

    private static final String RECORD_FOLDER = "session-records/";
    private static final String BODY_LOGGING = "x-ms-body-logging";
    private static final int HTTP_TEMPORARY_REDIRECT = 307; // HTTP Status Code

    private Map<String, String> textReplacementRules = new HashMap<>();
    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup

    protected RecordedData recordedData = new RecordedData();

    private final String testName;

    private final TestBase.TestMode testMode;

    private InterceptorManager(String testName, TestBase.TestMode testMode) {
        this.testName = testName;
        this.testMode = testMode;
    }

    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }

    // factory method
    public static InterceptorManager create(String testName, TestBase.TestMode testMode) throws IOException {
        InterceptorManager interceptorManager = new InterceptorManager(testName, testMode);
        SdkContext.setDelayProvider(new TestDelayProvider(interceptorManager.isRecordMode() || interceptorManager.isNoneMode()));
        SdkContext.setFileProvider(new TestFileProvider(interceptorManager.isRecordMode()));
        if (!interceptorManager.isNoneMode()) {
            SdkContext.setReactorScheduler(Schedulers.boundedElastic());
        }
        return interceptorManager;
    }

    public boolean isRecordMode() {
        return testMode == TestBase.TestMode.RECORD;
    }

    public boolean isNoneMode() {
        return testMode == TestBase.TestMode.NONE;
    }

    public boolean isPlaybackMode() {
        return testMode == TestBase.TestMode.PLAYBACK;
    }

    public HttpPipelinePolicy initInterceptor() throws IOException {
        switch (testMode) {
            case RECORD:
                recordedData = new RecordedData();
                return (context, next) -> record(context, next);
            case PLAYBACK:
                readDataFromFile();
                return (context, next) -> playback(context, next);
            case NONE:
                System.out.println("==> No interceptor defined for AZURE_TEST_MODE: " + testMode);
                break;
            default:
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
        }
        return null;
    }

    public void finalizeInterceptor() throws IOException {
        switch (testMode) {
            case RECORD:
                writeDataToFile();
                break;
            case PLAYBACK:
            case NONE:
                // Do nothing
                break;
            default:
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
        }
    }

    private Mono<HttpResponse> record(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        HttpHeaders headers = request.getHeaders();

        NetworkCallRecord networkCallRecord = new NetworkCallRecord();
        networkCallRecord.setHeaders(new HashMap<>());

        if (headers.get("Content-Type") != null) {
            networkCallRecord.headers().put("Content-Type", headers.getValue("Content-Type"));
        }
        if (headers.get("x-ms-version") != null) {
            networkCallRecord.headers().put("x-ms-version", headers.getValue("x-ms-version"));
        }
        if (headers.get("User-Agent") != null) {
            networkCallRecord.headers().put("User-Agent", headers.getValue("User-Agent"));
        }

        networkCallRecord.setMethod(request.getHttpMethod().toString());
        networkCallRecord.setUri(applyReplacementRule(request.getUrl().toString().replaceAll("\\?$", "")));

        return next.process().flatMap(response -> {
            networkCallRecord.setResponse(new HashMap<>());
            networkCallRecord.response().put("StatusCode", Integer.toString(response.getStatusCode()));
            Mono<HttpResponse> bufferResponse = extractResponseData(networkCallRecord.response(), response);

            // remove pre-added header if this is a waiting or redirection
            if (networkCallRecord.response().containsKey("Body")
                    && networkCallRecord.response().get("Body").contains("<Status>InProgress</Status>")
                    || Integer.parseInt(networkCallRecord.response().get("StatusCode")) == HTTP_TEMPORARY_REDIRECT) {
                // Do nothing
                return bufferResponse;
            } else {
                synchronized (recordedData.getNetworkCallRecords()) {
                    recordedData.getNetworkCallRecords().add(networkCallRecord);
                }
            }

            return bufferResponse;
        });
    }

    private Mono<HttpResponse> playback(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        String incomingUrl = applyReplacementRule(request.getUrl().toString());
        String incomingMethod = request.getHttpMethod().toString();

        incomingUrl = removeHost(incomingUrl);
        NetworkCallRecord networkCallRecord = null;
        synchronized (recordedData) {
            for (Iterator<NetworkCallRecord> iterator = recordedData.getNetworkCallRecords().iterator(); iterator.hasNext();) {
                NetworkCallRecord record = iterator.next();
                if (record.method().equalsIgnoreCase(incomingMethod) && removeHost(record.uri()).equalsIgnoreCase(incomingUrl)) {
                    networkCallRecord = record;
                    iterator.remove();
                    break;
                }
            }
        }

        if (networkCallRecord == null) {
            System.out.println("NOT FOUND - " + incomingMethod + " " + incomingUrl);
            System.out.println("Remaining records " + recordedData.getNetworkCallRecords().size());
            return Mono.error(new IOException("==> Unexpected request: " + incomingMethod + " " + incomingUrl));
        }

        int recordStatusCode = Integer.parseInt(networkCallRecord.response().get("StatusCode"));

        final NetworkCallRecord finalNetworkCallRecord = networkCallRecord;

        RecordedHttpResponse response = new RecordedHttpResponse(recordStatusCode, context.getHttpRequest());

        for (Map.Entry<String, String> pair : finalNetworkCallRecord.response().entrySet()) {
            if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
                String rawHeader = pair.getValue();
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                response.getHeaders().put(pair.getKey(), rawHeader);
            }
        }

        try {
            String rawBody = finalNetworkCallRecord.response().get("Body");
            if (rawBody != null) {
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                    }
                }

                String rawContentType = finalNetworkCallRecord.response().get("content-type");
                String contentType = rawContentType == null
                        ? "application/json; charset=utf-8"
                        : rawContentType;

                response.setBody(rawBody.getBytes());
                response.getHeaders().put("Content-Length", String.valueOf(rawBody.getBytes("UTF-8").length));
            }
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(response);
    }

    private Mono<HttpResponse> extractResponseData(Map<String, String> responseData, final HttpResponse response) {
        Map<String, String> headers = response.getHeaders().toMap();
        boolean addedRetryAfter = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String headerValueToStore = header.getValue();

            if (header.getKey().equalsIgnoreCase("location") || header.getKey().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore);
            }
            if (header.getKey().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.getKey().toLowerCase(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        String bodyLoggingHeader = response.getRequest().getHeaders().getValue(BODY_LOGGING);
        boolean bodyLogging = bodyLoggingHeader == null || Boolean.parseBoolean(bodyLoggingHeader);
        if (bodyLogging) {
            HttpResponse bufferedResponse = response.buffer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            return bufferedResponse.getBody()
                    .doOnNext(byteBuffer -> {
                        for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
                            outputStream.write(byteBuffer.get(i));
                        }
                    })
                    .doFinally(ignored -> {
                        try {
                            String encoding = response.getHeaderValue("Content-Encoding");
                            String content = null;
                            if (encoding == null || encoding == "") {
                                content = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                            } else if (encoding.equalsIgnoreCase("gzip")) {
                                GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                                content = IOUtils.readInputStreamToString(gis, StandardCharsets.UTF_8);
                                responseData.remove("Content-Encoding".toLowerCase());
                                responseData.put("Content-Length".toLowerCase(), Integer.toString(content.length()));
                            }

                            if (content != null) {
                                content = applyReplacementRule(content);
                                responseData.put("Body", content);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .then(Mono.just(bufferedResponse));
        }
        return Mono.just(response);
    }

    private long getContentLength(HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue("Content-Length");
        if (contentLengthString == null || contentLengthString.isEmpty()) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException e) { }

        return contentLength;
    }

    private void readDataFromFile() throws IOException {
        File recordFile = getRecordFile(testName);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        recordedData = mapper.readValue(recordFile, RecordedData.class);
        System.out.println("Total records " + recordedData.getNetworkCallRecords().size());
    }

    private void writeDataToFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File recordFile = getRecordFile(testName);
        recordFile.createNewFile();
        mapper.writeValue(recordFile, recordedData);
    }

    private File getRecordFile(String testName) {
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

    private String removeHost(String url) {
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }

    public void pushVariable(String variable) {
        if (isRecordMode()) {
            synchronized (recordedData.getVariables()) {
                recordedData.getVariables().add(variable);
            }
        }
    }

    public String popVariable() {
        synchronized (recordedData.getVariables()) {
            return recordedData.getVariables().remove();
        }
    }
}
