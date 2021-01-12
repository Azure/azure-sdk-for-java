// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Locale;

public class CommunicationIdentityClientTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("ADMINISTRATION_SERVICE_ENDPOINT", "https://REDACTED.communication.azure.com");

    protected static final String ACCESSKEYRAW = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    protected static final String ACCESSKEYENCODED = Base64.getEncoder().encodeToString(ACCESSKEYRAW.getBytes());
    protected static final String ACCESSKEY = Configuration.getGlobalConfiguration()
        .get("ADMINISTRATION_SERVICE_ACCESS_TOKEN", ACCESSKEYENCODED);

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=" + ACCESSKEYENCODED);
    
    protected CommunicationIdentityClientBuilder getCommunicationIdentityClient(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(ENDPOINT)
            .accessKey(ACCESSKEY)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
       
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
        
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
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
        return builder.addPolicy(new CommunicationLoggerPolicy(testName));
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }
}
