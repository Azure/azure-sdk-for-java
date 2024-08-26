// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import reactor.core.scheduler.Schedulers;

/**
 * Common properties used in testing.
 */
final class TestUtils {
    private TestUtils() {
    }

    public static TokenCredential getIdentityTestCredential(TestMode testMode) {
        if (testMode == TestMode.PLAYBACK) {
            return new MockTokenCredential();
        }
        return getIdentityTestCredentialHelper();
    }

    private static TokenCredential getIdentityTestCredentialHelper() {
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
