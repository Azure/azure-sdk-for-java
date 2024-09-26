// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Common properties used in testing.
 */
final class TestUtils {
    static final String HUB_NAME = "Hub";

    static String getEndpoint() {
        return Configuration.getGlobalConfiguration()
            .get("WEB_PUB_SUB_ENDPOINT", "http://testendpoint.webpubsubdev.azure.com");
    }

    static String getConnectionString() {
        return Configuration.getGlobalConfiguration()
            .get("WEB_PUB_SUB_CONNECTION_STRING", "Endpoint=https://testendpoint.webpubsubdev.azure.com;AccessKey=LoremIpsumDolorSitAmetConsectetur;Version=1.0;");
    }

    static RetryOptions getRetryOptions() {
        return new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(20)));
    }

    static HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .skipRequest((httpRequest, context) -> false)
            .build();
    }

    private TestUtils() {
    }


    public static TokenCredential getIdentityTestCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isPlaybackMode()) {
            return new MockTokenCredential();
        }

        Configuration config = Configuration.getGlobalConfiguration();

        ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
            .addLast(new EnvironmentCredentialBuilder().build())
            .addLast(new AzureCliCredentialBuilder().build())
            .addLast(new AzureDeveloperCliCredentialBuilder().build());


        String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
        String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
        String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

        if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
            && !CoreUtils.isNullOrEmpty(clientId)
            && !CoreUtils.isNullOrEmpty(tenantId)
            && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

            AzurePipelinesCredential azurePipelinesCredential = new AzurePipelinesCredentialBuilder()
                .systemAccessToken(systemAccessToken)
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .build();

            builder.addLast(trc -> azurePipelinesCredential.getToken(trc).subscribeOn(Schedulers.boundedElastic()));
        }

        builder.addLast(new AzurePowerShellCredentialBuilder().build());


        return builder.build();
    }
}
