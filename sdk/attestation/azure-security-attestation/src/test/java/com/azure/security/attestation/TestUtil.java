// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

public class TestUtil {

    /**
     * Gets a token credential for use in tests.
     * @param interceptorManager the interceptor manager
     * @return the TokenCredential
     */
    public static TokenCredential getIdentityTestCredential(InterceptorManager interceptorManager,
                                                            HttpClient httpClient) {
        if (interceptorManager.isPlaybackMode()) {
            return  new MockTokenCredential();
        }

        Configuration config = Configuration.getGlobalConfiguration();

        ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
            .addLast(new EnvironmentCredentialBuilder().httpClient(httpClient).build())
            .addLast(new AzureCliCredentialBuilder().httpClient(httpClient).build())
            .addLast(new AzureDeveloperCliCredentialBuilder().httpClient(httpClient).build());


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
                .httpClient(httpClient)
                .serviceConnectionId(serviceConnectionId)
                .build());
        }

        builder.addLast(new AzurePowerShellCredentialBuilder().httpClient(httpClient).build());
        return builder.build();
    }
}
