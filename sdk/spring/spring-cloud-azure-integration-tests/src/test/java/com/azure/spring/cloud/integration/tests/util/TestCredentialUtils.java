// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.util;


import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredential;
import com.azure.identity.AzurePipelinesCredentialBuilder;

import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import reactor.core.scheduler.Schedulers;

import static org.springframework.util.StringUtils.hasText;

public class TestCredentialUtils {

    public static TokenCredential getIntegrationTestTokenCredential() {
        ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder();
        TokenCredential createAzurePipelinesCredential = createAzurePipelinesCredential();
        if (createAzurePipelinesCredential != null) {
            builder.addLast(createAzurePipelinesCredential);
        }
        // Adds DefaultAzureCredential, which works for both local development and Azure environments
        builder.addLast(new DefaultAzureCredentialBuilder().build());
        return builder.build();
    }

    private static TokenCredential createAzurePipelinesCredential() {
        Configuration config = Configuration.getGlobalConfiguration();
        String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
        String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
        String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");
        if (hasText(serviceConnectionId)
            && hasText(clientId)
            && hasText(tenantId)
            && hasText(systemAccessToken)) {
            AzurePipelinesCredential pipelinesCredential = new AzurePipelinesCredentialBuilder()
                .systemAccessToken(systemAccessToken)
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .build();
            return request -> pipelinesCredential.getToken(request).subscribeOn(Schedulers.boundedElastic());
        }
        return null;
    }
}
