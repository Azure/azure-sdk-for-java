// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.communication.identity.models.CommunicationTokenScope.CHAT;
import static com.azure.communication.identity.models.CommunicationTokenScope.VOIP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationIdentityClientTestBase extends TestProxyTestBase {

    private static final String REDACTED = "REDACTED";
    private static final String URI_IDENTITY_REPLACER_REGEX = "/identities/([^/?]+)";
    protected static final String SYNC_TEST_SUFFIX = "Sync";
    protected static final List<CommunicationTokenScope> SCOPES = Arrays.asList(CHAT, VOIP);
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    protected HttpClient httpClient;

    @Override
    public void beforeTest() {
        getHttpClients().forEach(client -> this.httpClient = client);
    }

    protected HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        HttpClient client = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;
        return new AssertingHttpClientBuilder(client)
            .skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }

    protected HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        HttpClient client = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;
        return new AssertingHttpClientBuilder(client)
            .skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    protected CommunicationIdentityClientBuilder createClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        builder
            .endpoint(communicationEndpoint)
            .credential(new AzureKeyCredential(communicationAccessKey))
            .httpClient(httpClient);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected CommunicationIdentityClientBuilder createClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();

        builder
            .endpoint(communicationEndpoint)
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
            if (interceptorManager.isRecordMode()) {
                builder.addPolicy(interceptorManager.getRecordPolicy());
            }
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    protected CommunicationIdentityClientBuilder createClientBuilderUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
                .connectionString(CONNECTION_STRING)
                .httpClient(httpClient);

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return builder;
    }

    private void addTestProxyTestSanitizersAndMatchers(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return;
        }

        List<TestProxySanitizer> customSanitizers = new ArrayList<>();
        customSanitizers.add(new TestProxySanitizer("$..id", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer("$..token", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer("$..appId", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer("$..userId", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(new TestProxySanitizer(URI_IDENTITY_REPLACER_REGEX, "/identities/" + REDACTED, TestProxySanitizerType.URL));
        interceptorManager.addSanitizers(customSanitizers);

        if (interceptorManager.isPlaybackMode()) {
            /** Skipping matching authentication headers since running in playback mode don't rely on environment variables */
            interceptorManager.addMatchers(Collections.singletonList(
                new CustomMatcher().setExcludedHeaders(Arrays.asList("x-ms-hmac-string-to-sign-base64", "x-ms-content-sha256"))));
        }
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

    protected void verifyTokenNotEmpty(AccessToken issuedToken) {
        assertNotNull(issuedToken);
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    protected void verifyUserNotEmpty(CommunicationUserIdentifier userIdentifier) {
        assertNotNull(userIdentifier);
        assertNotNull(userIdentifier.getId());
        assertFalse(userIdentifier.getId().isEmpty());
    }

}
