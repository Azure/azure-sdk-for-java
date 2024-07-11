// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

public class TestUtils {
    public static TokenCredential getCredential(TestMode testMode) {
        switch (testMode) {
            case RECORD:
                return new DefaultAzureCredentialBuilder().build();
            case LIVE:
                Configuration config = Configuration.getGlobalConfiguration();

                ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
                    .addLast(new EnvironmentCredentialBuilder().build())
                    .addLast(new AzureCliCredentialBuilder().build())
                    .addLast(new AzureDeveloperCliCredentialBuilder().build())
                    .addLast(new AzurePowerShellCredentialBuilder().build());

                String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
                String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
                String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
                String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

                if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                    && !CoreUtils.isNullOrEmpty(clientId)
                    && !CoreUtils.isNullOrEmpty(tenantId)
                    && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                    builder.addLast(new AzurePipelinesCredentialBuilder()
                        .systemAccessToken(systemAccessToken)
                        .clientId(clientId)
                        .tenantId(tenantId)
                        .serviceConnectionId(serviceConnectionId)
                        .build());
                }

                return builder.build();
            default:
                // On PLAYBACK mode
                return new MockTokenCredential();
        }
    }
}
