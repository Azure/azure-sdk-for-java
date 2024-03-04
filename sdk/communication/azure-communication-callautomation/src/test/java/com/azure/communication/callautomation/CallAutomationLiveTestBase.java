// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallAutomationLiveTestBase extends TestProxyTestBase {
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT",
        "https://REDACTED.communication.azure.com/");
    protected static final String ENDPOINT_401 = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT_401",
        "https://REDACTED.communication.azure.com/");
    protected static final String PMA_ENDPOINT = Configuration.getGlobalConfiguration().get("PMA_Endpoint", "https://REDACTED.communication.azure.com/");
    protected static final Boolean COMMUNICATION_CUSTOM_ENDPOINT_ENABLED = Configuration.getGlobalConfiguration().get("COMMUNICATION_CUSTOM_ENDPOINT_ENABLED", false);
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
    protected static final String RANDOM_RESOURCE_IDENTIFIER = "82e890fc-188a-4b67-bb7d-defRANDOM1e";
    protected static final String ACS_USER_1 = Configuration.getGlobalConfiguration()
        .get("TARGET_USER_ID", String.format("8:acs:%s_00000014-00d6-e250-28df-44482200202a", RANDOM_RESOURCE_IDENTIFIER));
    protected static final String ACS_USER_2 = Configuration.getGlobalConfiguration()
        .get("ANOTHER_TARGET_USER_ID", String.format("8:acs:%s_00000014-00d7-31b3-28df-444822002030", RANDOM_RESOURCE_IDENTIFIER));
    protected static final String ACS_USER_CALL_RECORDING = Configuration.getGlobalConfiguration()
        .get("CALL_RECORDING_USER_ID");

    protected static final String ACS_RESOURCE_PHONE = Configuration.getGlobalConfiguration()
        .get("AZURE_PHONE_NUMBER", "+18331234567");
    protected static final String PHONE_USER_1 = Configuration.getGlobalConfiguration()
        .get("TARGET_PHONE_NUMBER", "+16471234567");
    protected static final String MEDIA_SOURCE = Configuration.getGlobalConfiguration()
        .get("ACS_MEDIA_SOURCE", "https://contoso.com/music.wav");

    private static final String REDACTED = "REDACTED";
    private static final String URI_IDENTITY_REPLACER_REGEX = "/identities/([^/?]+)";
    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("value")
        .add("rawId")
        .add("id")
        .add("callbackUri")
        .add("botAppId")
        .add("ivrContext")
        .add("incomingCallContext")
        .add("serverCallId");
    protected static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT),
        Pattern.CASE_INSENSITIVE);

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected CallAutomationClientBuilder getCallAutomationClientUsingConnectionString(HttpClient httpClient) {
        CallAutomationClientBuilder builder;
        if (COMMUNICATION_CUSTOM_ENDPOINT_ENABLED) {
            builder = new CallAutomationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .endpoint(PMA_ENDPOINT)
                .httpClient(getHttpClientOrUsePlayback(httpClient));
        } else {
            builder = new CallAutomationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .httpClient(getHttpClientOrUsePlayback(httpClient));
        }

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected CallAutomationClientBuilder getCallAutomationClientUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallAutomationClientBuilder builder = new CallAutomationClientBuilder()
            .endpoint(ENDPOINT)
            .credential(tokenCredential)
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected CallAutomationClientBuilder getCallAutomationClientUsingInvalidTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallAutomationClientBuilder builder = new CallAutomationClientBuilder()
            .credential(tokenCredential)
            .endpoint(ENDPOINT_401)
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data)));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();
                return Mono.just(bufferedResponse);
            });
    }

    protected void waitForOperationCompletion(int milliSeconds) throws InterruptedException {
        if (getTestMode() != TestMode.PLAYBACK) {
            Thread.sleep(milliSeconds);
        }
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }

    private void addTestProxyTestSanitizersAndMatchers(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return;
        }

        List<TestProxySanitizer> customSanitizers = new ArrayList<>();
        customSanitizers.add(new TestProxySanitizer("x-ms-content-sha256", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("x-ms-hmac-string-to-sign-base64", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("X-Azure-Ref", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("Repeatability-First-Sent", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("Repeatability-First-Sent", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("MS-CV", null, REDACTED, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("$..rawId", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer("$..id", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer("$..callbackUri", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer(URI_IDENTITY_REPLACER_REGEX, "/identities/" + REDACTED, TestProxySanitizerType.URL));
        interceptorManager.addSanitizers(customSanitizers);
        if (interceptorManager.isPlaybackMode()) {
            /** Skipping matching authentication headers since running in playback mode don't rely on environment variables */
            interceptorManager.addMatchers(Collections.singletonList(
                new CustomMatcher().setExcludedHeaders(Arrays.asList("x-ms-hmac-string-to-sign-base64", "x-ms-content-sha256"))));
        }

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(),
                    new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent",
                            "repeatability-request-id", "x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))));
        }
    }
    protected String redact(String content, Matcher matcher) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), "REDACTED");
            }
        }
        return content;
    }
}
