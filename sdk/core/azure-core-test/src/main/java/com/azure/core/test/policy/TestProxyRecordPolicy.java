// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.models.RecordingRedactor;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A {@link HttpPipelinePolicy} for redirecting traffic through the test proxy for recording.
 */
public class TestProxyRecordPolicy implements HttpPipelinePolicy {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();
    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();
    private String xRecordingId;
    private Map<String, List<String>> sanitizers;

    /**
     * Starts a recording of test traffic.
     * @param recordFile The name of the file to save the recording to.
     * @param sanitizers The map of sanitizer to send to the test proxy.
     */
    public void startRecording(String recordFile, Map<String, List<String>> sanitizers) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/start", TestProxyUtils.getProxyUrl()))
            .setBody(String.format("{\"x-recording-file\": \"%s\"}", recordFile));

        HttpResponse response = client.sendSync(request, Context.NONE);

        this.xRecordingId = response.getHeaderValue("x-recording-id");
        this.sanitizers = sanitizers;
        addProxySanitization();
    }

    /**
     * Stops recording of test traffic.
     * @param variables A list of random variables generated during the test which is saved in the recording.
     */
    public void stopRecording(Queue<String> variables) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/record/stop", TestProxyUtils.getProxyUrl()))
            .setHeader("content-type", "application/json")
            .setHeader("x-recording-id", xRecordingId)
            .setBody(serializeVariables(variables));
        client.sendSync(request, Context.NONE);
    }

    /**
     * Transform the {@link Queue} containing variables into a JSON map for sending to the test proxy.
     * @param variables The variables to send.
     * @return A string containing the JSON map (or an empty map.)
     */
    private String serializeVariables(Queue<String> variables) {
        if (variables.isEmpty()) {
            return "{}";
        }
        AtomicInteger count = new AtomicInteger(0);
        Map<String, String> map = variables.stream().collect(Collectors.toMap(k -> String.format("%d", count.getAndIncrement()), k -> k));
        try {
            return SERIALIZER.serialize(map, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpRequest request = context.getHttpRequest();
        TestProxyUtils.changeHeaders(request, xRecordingId, "record");
        HttpResponse response = next.processSync();
        return response;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        TestProxyUtils.changeHeaders(request, xRecordingId, "record");
        return next.process();
    }

    private void addProxySanitization() {
        // TODO (sanitizer type enum?)
        sanitizers.forEach((sanitizerType, sanitizerRegex)  -> {
            switch (sanitizerType) {
                case "URL":
                    sanitizerRegex.forEach(regexValue -> addUrlRegexSanitizer(regexValue));
                    break;
                case "BODY":
                    sanitizerRegex.forEach(regexValue -> addBodySanitizer(regexValue));
                case "HEADER":
                    sanitizerRegex.forEach(regexValue -> addHeaderSanitizer(regexValue));
            }

        });
    }

    private void addUrlRegexSanitizer(String regexValue) {
        String requestBody = String.format("{\"value\":\"https://REDACTED.cognitiveservices.azure.com\",\"regex\":\"%s\"}", regexValue);
        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/AddSanitizer", TestProxyUtils.getProxyUrl()))
                .setBody(requestBody);
        request.setHeader("x-abstraction-identifier", "UriRegexSanitizer");
        request.setHeader("x-recording-id", xRecordingId);
        client.sendSync(request, Context.NONE);
    }

    private void addBodySanitizer(String regexValue) {
        String requestBody = String.format("{\"value\":\"REDACTED\",\"jsonPath\":\"%s\"}", regexValue);

        HttpRequest request = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/AddSanitizer", TestProxyUtils.getProxyUrl()))
            .setBody(requestBody);
        request.setHeader("x-abstraction-identifier", "BodyKeySanitizer");
        request.setHeader("x-recording-id", xRecordingId);
        client.sendSync(request, Context.NONE);
    }

    private void addHeaderSanitizer(String regexValue) {
        // TODO
    }

    // private static String redactionReplacement(String content, Matcher matcher, String replacement) {
    //     while (matcher.find()) {
    //         String captureGroup = matcher.group(2);
    //         if (!CoreUtils.isNullOrEmpty(captureGroup)) {
    //             content = content.replace(matcher.group(2), replacement);
    //         }
    //     }
    //
    //     return content;
    // }

    // private static String redactModelId(String content) {
    //     Pattern pattern = Pattern.compile("(.*\"modelId\":)(\"(.+?)\")");
    //     content = redactionReplacement(content, pattern.matcher(content.replaceAll(" *\" *", "\"")), "REDACTED");
    //     return content;
    // }
}

