// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;

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
    
    protected CommunicationIdentityClientBuilder getCommunicationIdentityClient() {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(ENDPOINT)
            .accessKey(ACCESSKEY);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            return builder;
        } else {
            HttpClient client = new NettyAsyncHttpClientBuilder().build();
            builder.httpClient(client);
        }

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientUsingConnectionString() {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.connectionString(CONNECTION_STRING);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            return builder;
        } else {
            HttpClient client = new NettyAsyncHttpClientBuilder().build();
            builder.httpClient(client);
        }

        if (!interceptorManager.isLiveMode()) {
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
}
