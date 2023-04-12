// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumbersIntegrationTestBase extends TestBase {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
                    "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");
    protected static final String COUNTRY_CODE = Configuration.getGlobalConfiguration().get("COUNTRY_CODE", "US");
    protected static final String AREA_CODE = Configuration.getGlobalConfiguration().get("AREA_CODE", "833");
    protected static final String MS_USERAGENT_OVERRIDE = Configuration.getGlobalConfiguration()
            .get("AZURE_USERAGENT_OVERRIDE", "");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT = new StringJoiner("\":\"|\"", "\"", "\":\"")
            .add("id")
            .add("phoneNumber");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN = Pattern.compile(
            String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()), Pattern.CASE_INSENSITIVE);

    private static final String URI_PHONE_NUMBERS_REPLACER_REGEX = "/phoneNumbers/([\\+]?[0-9]{11,15})";

    protected PhoneNumbersClientBuilder getClientBuilder(HttpClient httpClient) {
        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(
                CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        PhoneNumbersClientBuilder builder = new PhoneNumbersClientBuilder();
        builder
                .httpClient(getHttpClient(httpClient))
                .addPolicy(getOverrideMSUserAgentPolicy())
                .endpoint(communicationEndpoint)
                .credential(new AzureKeyCredential(communicationAccessKey));

        if (shouldRecord()) {
            builder.addPolicy(getRecordPolicy());
        }

        return builder;
    }

    protected PhoneNumbersClientBuilder getClientBuilderWithConnectionString(HttpClient httpClient) {
        PhoneNumbersClientBuilder builder = new PhoneNumbersClientBuilder();
        builder
                .httpClient(getHttpClient(httpClient))
                .addPolicy(getOverrideMSUserAgentPolicy())
                .connectionString(CONNECTION_STRING);

        if (shouldRecord()) {
            builder.addPolicy(getRecordPolicy());
        }

        return builder;
    }

    protected PhoneNumbersClientBuilder getClientBuilderUsingManagedIdentity(HttpClient httpClient) {

        PhoneNumbersClientBuilder builder = new PhoneNumbersClientBuilder();
        builder
                .httpClient(getHttpClient(httpClient))
                .addPolicy(getOverrideMSUserAgentPolicy())
                .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (shouldRecord()) {
            builder.addPolicy(getRecordPolicy());
        }

        return builder;
    }

    private HttpClient getHttpClient(HttpClient httpClient) {
        if (httpClient == null || getTestMode() == TestMode.PLAYBACK) {
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
            phoneNumber = "+REDACTED";
        }
        return phoneNumber;
    }

    private boolean shouldRecord() {
        return getTestMode() == TestMode.RECORD;
    }

    private HttpPipelinePolicy getRecordPolicy() {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
        return interceptorManager.getRecordPolicy(redactors);
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
                    System.out.println("MS-CV header for " + testName + " request "
                            + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
                    return Mono.just(bufferedResponse);
                });
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }

    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), replacement);
            }
        }

        return content;
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

    @Override
    protected void afterTest() {
        super.afterTest();
        if (getTestMode() == TestMode.RECORD) {
            List<NetworkCallRecord> networkCallRecords = collectNetworkCallsWithPhoneNumberUri();
            sanitizePhoneNumerInUri(networkCallRecords);
        }
    }

    private List<NetworkCallRecord> collectNetworkCallsWithPhoneNumberUri() {
        List<NetworkCallRecord> networkCallRecords = new ArrayList<>();
        NetworkCallRecord networkCallRecord = interceptorManager.getRecordedData()
                .findFirstAndRemoveNetworkCall(record -> {
                    return Pattern.compile(URI_PHONE_NUMBERS_REPLACER_REGEX).matcher(record.getUri()).find();
                });
        do {
            if (networkCallRecord != null) {
                networkCallRecords.add(networkCallRecord);
            }
            networkCallRecord = interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(record -> {
                return Pattern.compile(URI_PHONE_NUMBERS_REPLACER_REGEX).matcher(record.getUri()).find();
            });
        } while (networkCallRecord != null);
        return networkCallRecords;
    }

    private void sanitizePhoneNumerInUri(List<NetworkCallRecord> networkCallRecords) {
        for (NetworkCallRecord networkCallRecord : networkCallRecords) {
            String sanitizedUri = networkCallRecord.getUri().replaceAll(URI_PHONE_NUMBERS_REPLACER_REGEX,
                    "/phoneNumbers/+REDACTED");
            networkCallRecord.setUri(sanitizedUri);
            interceptorManager.getRecordedData().addNetworkCall(networkCallRecord);
        }
    }
}
