// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationIdentityClientTestBase extends TestBase {

    private static final String REDACTED = "REDACTED";
    private static final String URI_IDENTITY_REPLACER_REGEX = "/identities/([^/?]+)";
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
            = new StringJoiner("\":\"|\"", "\"", "\":\"")
            .add("token")
            .add("id");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
            = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()),
            Pattern.CASE_INSENSITIVE);

    protected HttpClient httpClient;

    @BeforeEach
    public void setup() {
        getHttpClients().forEach(client -> {
            this.httpClient = client;
            return;
        });
    }

    protected CommunicationIdentityClientBuilder createClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        builder.endpoint(communicationEndpoint)
                .credential(new AzureKeyCredential(communicationAccessKey))
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), REDACTED));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder createClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();

        builder
                .endpoint(communicationEndpoint)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), REDACTED));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder createClientBuilderUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
                .connectionString(CONNECTION_STRING)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), REDACTED));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClient setupClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CommunicationIdentityAsyncClient setupAsyncClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private CommunicationIdentityClientBuilder addLoggingPolicy(CommunicationIdentityClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
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

    @Override
    protected void afterTest() {
        super.afterTest();
        List<NetworkCallRecord> networkCallRecords = collectNetworkCallsWithIdentityUri();
        sanitizeIdentityInUri(networkCallRecords);
    }

    private List<NetworkCallRecord> collectNetworkCallsWithIdentityUri() {
        List<NetworkCallRecord> networkCallRecords = new ArrayList<>();
        NetworkCallRecord networkCallRecord = interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(record -> {
            return Pattern.compile(URI_IDENTITY_REPLACER_REGEX).matcher(record.getUri()).find();
        });
        do {
            if (networkCallRecord != null) {
                networkCallRecords.add(networkCallRecord);
            }
            networkCallRecord = interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(record -> {
                return Pattern.compile(URI_IDENTITY_REPLACER_REGEX).matcher(record.getUri()).find();
            });
        } while (networkCallRecord != null);
        return networkCallRecords;
    }

    private void sanitizeIdentityInUri(List<NetworkCallRecord> networkCallRecords) {
        for (NetworkCallRecord networkCallRecord: networkCallRecords) {
            String sanitizedUri = networkCallRecord.getUri().replaceAll(URI_IDENTITY_REPLACER_REGEX, "/identities/" + REDACTED);
            networkCallRecord.setUri(sanitizedUri);
            interceptorManager.getRecordedData().addNetworkCall(networkCallRecord);
        }
    }

    protected void verifyTokenNotEmpty(AccessToken issuedToken) {
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

}
