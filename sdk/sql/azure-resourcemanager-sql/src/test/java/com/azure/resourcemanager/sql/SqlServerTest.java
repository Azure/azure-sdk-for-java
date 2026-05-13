// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.model.AzureUser;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class SqlServerTest extends ResourceManagerTestProxyTestBase {
    protected ResourceManager resourceManager;
    protected SqlServerManager sqlServerManager;
    protected StorageManager storageManager;
    protected String rgName = "";
    protected String sqlServerName = "";
    private volatile String cachedAdminLogin;
    private volatile String cachedAdminSid;

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("rgsql", 20);
        sqlServerName = generateRandomResourceName("javasqlserver", 20);
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        sqlServerManager = buildManager(SqlServerManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        resourceManager = sqlServerManager.resourceManager();
        setInternalContext(internalContext, sqlServerManager);
    }

    @Override
    protected void cleanUpResources() {
        ResourceManagerUtils.sleep(Duration.ofSeconds(1));
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    /**
     * Returns the signed-in user's principal name to use as the SQL Server Microsoft Entra administrator login.
     * The Azure SQL platform policy requires Microsoft Entra-only authentication enabled at server creation time.
     *
     * @return the signed-in user's principal name (sanitized in playback)
     */
    protected String adminLogin() {
        if (isPlaybackMode()) {
            return "REDACTED";
        }
        ensureAdminIdentityCached();
        return cachedAdminLogin != null ? cachedAdminLogin : "REDACTED";
    }

    /**
     * Returns the signed-in user's object ID to use as the SQL Server Microsoft Entra administrator SID.
     *
     * @return the signed-in user's object ID (sanitized in playback)
     */
    protected String adminSid() {
        if (isPlaybackMode()) {
            return "00000000-0000-0000-0000-000000000000";
        }
        ensureAdminIdentityCached();
        return cachedAdminSid != null ? cachedAdminSid : "00000000-0000-0000-0000-000000000000";
    }

    private synchronized void ensureAdminIdentityCached() {
        if (cachedAdminLogin != null && cachedAdminSid != null) {
            return;
        }

        AzureUser user = azureCliSignedInUser();
        String login = user.userPrincipalName();
        String id = user.id();
        cachedAdminLogin = login != null ? login : "REDACTED";
        cachedAdminSid = id != null ? id : "00000000-0000-0000-0000-000000000000";
    }
}
