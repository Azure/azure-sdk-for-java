// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class PhoneNumbersIntegrationTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(PhoneNumbersIntegrationTestBase.class);

    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
                    "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");
    protected static final String COUNTRY_CODE = Configuration.getGlobalConfiguration().get("COUNTRY_CODE", "US");
    protected static final String MS_USERAGENT_OVERRIDE = Configuration.getGlobalConfiguration()
            .get("AZURE_USERAGENT_OVERRIDE", "");

    protected PhoneNumbersClientBuilder getClientBuilderWithConnectionString(HttpClient httpClient) {
        PhoneNumbersClientBuilder builder = new PhoneNumbersClientBuilder();
        builder
                .httpClient(getHttpClient(httpClient))
                .addPolicy(getOverrideMSUserAgentPolicy())
                .connectionString(CONNECTION_STRING);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            addTestProxySanitizer();
        }

        if (interceptorManager.isPlaybackMode()) {
            addTestProxyMatchers();
        }

        return builder;
    }

    private void addTestProxyMatchers() {
        interceptorManager.addMatchers(Arrays.asList(
            new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))));
    }

    private void addTestProxySanitizer() {
        // sanitize phone numbers
        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("(?<=/phoneNumbers/)([^/?]+)", "REDACTED", TestProxySanitizerType.URL),
            new TestProxySanitizer("$..id", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..phoneNumber", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..nationalFormat", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..internationalFormat", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("((?:\\\\u002B)[0-9]{11,})|((?:\\\\%2B)[0-9]{11,})|((?:[+]?)[0-9]{11,})", "REDACTED", TestProxySanitizerType.BODY_REGEX)));
    }

    protected PhoneNumbersClientBuilder getClientBuilderUsingManagedIdentity(HttpClient httpClient) {

        PhoneNumbersClientBuilder builder = new PhoneNumbersClientBuilder();
        builder
                .httpClient(getHttpClient(httpClient))
                .addPolicy(getOverrideMSUserAgentPolicy())
                .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint());

        if (interceptorManager.isRecordMode()) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            addTestProxySanitizer();
        }

        if (interceptorManager.isLiveMode()) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential());
            addTestProxyMatchers();
        }

        return builder;
    }

    private HttpClient getHttpClient(HttpClient httpClient) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return interceptorManager.getPlaybackClient();
        }
        return httpClient;
    }

    protected PhoneNumbersClientBuilder addLoggingPolicy(PhoneNumbersClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    protected String getTestPhoneNumber() {
        boolean skipCapabilitiesTests = Configuration.getGlobalConfiguration()
                .get("SKIP_UPDATE_CAPABILITIES_LIVE_TESTS", "false").equals("true");

        if (getTestMode() == TestMode.PLAYBACK || skipCapabilitiesTests) {
            return getDefaultPhoneNumber();
        }

        return getPhoneNumberByTestAgent();
    }

    protected String redactIfPlaybackMode(String phoneNumber) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "REDACTED";
        }
        return phoneNumber;
    }

    private HttpPipelinePolicy getOverrideMSUserAgentPolicy() {
        HttpHeaders headers = new HttpHeaders();
        if (!MS_USERAGENT_OVERRIDE.isEmpty()) {
            headers.add("x-ms-useragent", MS_USERAGENT_OVERRIDE);
        }

        return new AddHeadersPolicy(headers);
    }

    private Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
                .flatMap(httpResponse -> {
                    final HttpResponse bufferedResponse = httpResponse.buffer();

                    // Should sanitize printed reponse url
                    LOGGER.log(LogLevel.VERBOSE, () -> "MS-CV header for " + testName + " request "
                        + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
                    return Mono.just(bufferedResponse);
                });
    }

    private String getDefaultPhoneNumber() {
        return Configuration.getGlobalConfiguration().get("AZURE_PHONE_NUMBER", "+11234567891");
    }

    private String getPhoneNumberByTestAgent() {
        String testAgent = Configuration.getGlobalConfiguration().get("AZURE_TEST_AGENT");
        if (testAgent == null) {
            throw new IllegalStateException(
                "AZURE_TEST_AGENT value is required to run update capabilities live tests.");
        }

        String phoneNumber = Configuration.getGlobalConfiguration()
            .get(String.format("AZURE_PHONE_NUMBER_%s", testAgent));
        if (phoneNumber == null) {
            throw new IllegalStateException(
                "A phone number specific to the current test agent is required to run update capabilities live tests.");
        }

        return phoneNumber;
    }
}
