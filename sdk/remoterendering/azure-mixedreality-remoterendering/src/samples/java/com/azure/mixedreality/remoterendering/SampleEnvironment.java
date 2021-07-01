// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// These tests assume that the storage account is accessible from the remote rendering account.
// See https://docs.microsoft.com/azure/remote-rendering/how-tos/create-an-account
// Since the roles can take a while to propagate, we do not live test these samples.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.Configuration;

/**
 * Sample class holding all the parameters and values needed in a Remote Rendering application.
 * Used by all samples.
 */
public class SampleEnvironment {

    private final String accountId = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_ID");
    private final String accountDomain = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_DOMAIN");
    private final String accountKey = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_KEY");
    private final String storageAccountName = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_STORAGE_ACCOUNT_NAME");
    private final String storageAccountKey = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_STORAGE_ACCOUNT_KEY");
    private final String blobContainerName = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_BLOB_CONTAINER_NAME");
    private final String blobContainerSasToken = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_SAS_TOKEN");
    private final String serviceEndpoint = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_SERVICE_ENDPOINT");

    private final String tenantId = Configuration.getGlobalConfiguration().get("REMOTERENDERING_TENANT_ID");
    private final String clientId = Configuration.getGlobalConfiguration().get("REMOTERENDERING_CLIENT_ID");
    private final String clientSecret = Configuration.getGlobalConfiguration().get("REMOTERENDERING_CLIENT_SECRET");

    /**
     * Get the accounId used in samples.
     *
     * @return the accountId.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Get the accountDomain used in samples.
     *
     * @return the accountDomain.
     */
    public String getAccountDomain() {
        return accountDomain;
    }

    /**
     * Get the accountKey used in samples.
     *
     * @return the accountKey.
     */
    public String getAccountKey() {
        return accountKey;
    }

    /**
     * Get the storageAccountName used in samples.
     *
     * @return the storageAccountName.
     */
    public String getStorageAccountName() {
        return storageAccountName;
    }

    /**
     * Get the storageAccountKey used in samples.
     *
     * @return the storageAccountKey.
     */
    public String getStorageAccountKey() {
        return storageAccountKey;
    }

    /**
     * Get the blobContainerName used in samples.
     *
     * @return the blobContainerName.
     */
    public String getBlobContainerName() {
        return blobContainerName;
    }

    /**
     * Get the blobContainerSasToken used in samples.
     *
     * @return the blobContainerSasToken.
     */
    public String getBlobContainerSasToken() {
        return blobContainerSasToken;
    }

    /**
     * Get the serviceEndpoint used in samples.
     *
     * @return the serviceEndpoint.
     */
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Get the tenantId used in samples.
     *
     * @return the tenantId.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the clientId used in samples.
     *
     * @return the clientId.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the clientSecret used in samples.
     *
     * @return the clientSecret.
     */
    public String getClientSecret() {
        return clientSecret;
    }
}
