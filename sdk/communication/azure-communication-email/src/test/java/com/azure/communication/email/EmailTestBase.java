// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class EmailTestBase extends TestProxyTestBase {
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ENDPOINT", "https://endpoint.communication.azure.com");

    protected static final String RECIPIENT_ADDRESS = Configuration.getGlobalConfiguration()
        .get("RECIPIENT_ADDRESS", "customer@contoso.com");

    protected static final String SECOND_RECIPIENT_ADDRESS = Configuration.getGlobalConfiguration()
        .get("SECOND_RECIPIENT_ADDRESS", "customer2@contoso.com");

    protected static final String SENDER_ADDRESS = Configuration.getGlobalConfiguration()
        .get("SENDER_ADDRESS", "sender@domain.com");

    protected EmailClient getEmailClient(HttpClient httpClient) {
        return getEmailClientBuilder(httpClient).buildClient();
    }

    protected EmailAsyncClient getEmailAsyncClient(HttpClient httpClient) {
        return getEmailClientBuilder(httpClient).buildAsyncClient();
    }

    private EmailClientBuilder getEmailClientBuilder(HttpClient httpClient) {
        EmailClientBuilder emailClientBuilder = new EmailClientBuilder()
            .credential(getTestTokenCredential(interceptorManager))
            .endpoint(ENDPOINT)
            .httpClient(getHttpClientOrUsePlayback(httpClient));
        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))
                .setComparingBodies(false)));
        }
        if (getTestMode() == TestMode.RECORD) {
            HttpPipelinePolicy recordPolicy = interceptorManager.getRecordPolicy();
            emailClientBuilder.addPolicy(recordPolicy);
        }

        if (!interceptorManager.isLiveMode()) {
            // Remove `operation-location` sanitizers from list of common sanitizers
            interceptorManager.removeSanitizers("AZSDK2030");
        }

        return emailClientBuilder;
    }

    /**
     * Retrieve the appropriate TokenCredential based on the test mode.
     *
     * @param interceptorManager the interceptor manager
     * @return The appropriate token credential
     */
    public static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }

    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients()
            .forEach(httpClient -> argumentsList.add(Arguments.of(httpClient)));

        return argumentsList.stream();
    }
}
