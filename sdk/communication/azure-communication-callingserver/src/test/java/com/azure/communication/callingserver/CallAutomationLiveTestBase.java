// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallAutomationLiveTestBase extends TestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CallAutomationLiveTestBase.class);

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    protected static final String ENDPOINT = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT",
        "https://REDACTED.communication.azure.com/");

    protected static final String ENDPOINT_401 = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT_401",
        "https://REDACTED.communication.azure.com/");

    protected static final String METADATA_URL = Configuration.getGlobalConfiguration()
        .get("METADATA_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata");

    protected static final String VIDEO_URL = Configuration.getGlobalConfiguration()
        .get("VIDEO_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video");

    protected static final String CONTENT_URL_404 = Configuration.getGlobalConfiguration()
        .get("CONTENT_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata");

    protected static final String RECORDING_DELETE_URL = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d10-598a3ea36bfbc27e68c026b17982af22");

    protected static final String RECORDING_DELETE_URL_404 = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("to");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT),
        Pattern.CASE_INSENSITIVE);

    protected CallAutomationClientBuilder getCallingServerClientUsingConnectionString(HttpClient httpClient) {
        CallAutomationClientBuilder builder = new CallAutomationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected CallAutomationClientBuilder getCallingServerClientUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallAutomationClientBuilder builder = new CallAutomationClientBuilder()
            .endpoint(ENDPOINT)
            .credential(tokenCredential)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected CallAutomationClientBuilder getCallingServerClientUsingInvalidTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallAutomationClientBuilder builder = new CallAutomationClientBuilder()
            .credential(tokenCredential)
            .endpoint(ENDPOINT_401)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                /* Should sanitize printed response url */
                LOGGER.log(LogLevel.VERBOSE, () -> "Chain-ID header for " + testName + " request "
                    + bufferedResponse.getRequest().getUrl()
                    + ": " + bufferedResponse.getHeaderValue("X-Microsoft-Skype-Chain-ID"));
                return Mono.just(bufferedResponse);
            });
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }

    private String redact(String content, Matcher matcher) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), "REDACTED");
            }
        }
        return content;
    }
}
