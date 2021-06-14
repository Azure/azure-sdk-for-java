// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Test;

import java.util.List;

public abstract class KeyVaultBackupClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected KeyVaultBackupClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        List<HttpPipelinePolicy> policies = getPolicies();

        if (getTestMode() == TestMode.RECORD && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return new KeyVaultBackupClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline);
    }

    @Test
    public abstract void beginBackup(HttpClient httpClient);

    @Test
    public abstract void beginRestore(HttpClient httpClient);

    @Test
    public abstract void beginSelectiveKeyRestore(HttpClient httpClient);
}
