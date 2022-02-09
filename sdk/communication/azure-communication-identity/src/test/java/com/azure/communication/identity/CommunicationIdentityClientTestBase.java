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
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IPublicClientApplication;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationIdentityClientTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    private static final String COMMUNICATION_M365_APP_ID = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_M365_APP_ID", "Sanitized");

    private static final String COMMUNICATION_M365_AAD_AUTHORITY = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_M365_AAD_AUTHORITY", "Sanitized");

    private static final String COMMUNICATION_M365_AAD_TENANT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_M365_AAD_TENANT", "Sanitized");

    private static final String COMMUNICATION_M365_REDIRECT_URI = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_M365_REDIRECT_URI", "Sanitized");

    private static final String COMMUNICATION_M365_SCOPE = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_M365_SCOPE", "Sanitized");

    protected static final String COMMUNICATION_EXPIRED_TEAMS_TOKEN = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_EXPIRED_TEAMS_TOKEN", "Sanitized");

    private static final String COMMUNICATION_MSAL_USERNAME = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_MSAL_USERNAME", "Sanitized");

    private static final String COMMUNICATION_MSAL_PASSWORD = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_MSAL_PASSWORD", "Sanitized");

    private static final String COMMUNICATION_SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST = Configuration.getGlobalConfiguration()
        .get("SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST", "false");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("token");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()),
        Pattern.CASE_INSENSITIVE);

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
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
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
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
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
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(CommunicationIdentityClientTestBase.class);
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            System.out.println("azureTestMode: " + azureTestMode);
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }

    protected CommunicationIdentityClientBuilder addLoggingPolicy(CommunicationIdentityClientBuilder builder, String testName) {
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

    protected static String generateTeamsUserAadToken() throws MalformedURLException, ExecutionException, InterruptedException {
        String teamsUserAadToken = "Sanitized";
        if (TEST_MODE != TestMode.PLAYBACK) {
            try {
                IPublicClientApplication publicClientApplication = PublicClientApplication.builder(COMMUNICATION_M365_APP_ID)
                    .authority(COMMUNICATION_M365_AAD_AUTHORITY + "/" + COMMUNICATION_M365_AAD_TENANT)
                    .build();
                Set<String> scopes = Collections.singleton(COMMUNICATION_M365_SCOPE);
                char[] password = COMMUNICATION_MSAL_PASSWORD.toCharArray();
                UserNamePasswordParameters userNamePasswordParameters =  UserNamePasswordParameters.builder(scopes, COMMUNICATION_MSAL_USERNAME, password)
                        .build();
                Arrays.fill(password, '0');
                IAuthenticationResult result = publicClientApplication.acquireToken(userNamePasswordParameters).get();
                teamsUserAadToken = result.accessToken();
            } catch (Exception e) {
                throw e;
            }
        }
        return teamsUserAadToken;
    }

    protected void verifyTokenNotEmpty(AccessToken issuedToken) {
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    public static boolean skipExchangeAadTeamsTokenTest() {
        return Boolean.parseBoolean(COMMUNICATION_SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST);
    }
}
