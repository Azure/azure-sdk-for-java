// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * The audience to be used when requesting a token from Azure Active Directory (AAD).
 * Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class BlobAudience implements Comparable<BlobAudience> {
    private final String audience;

    /**
     * The Azure Active Directory audience to use when forming authorization scopes.
     * For the Language service, this value corresponds to a URL that identifies the Azure cloud where the resource is
     * located.
     * For more information see
     * <a href="https://learn.microsoft.com/en-us/azure/storage/blobs/authorize-access-azure-active-directory">
     *     Authorize access to Azure blobs using Azure Active Directory</a>.
     *
     * @param audience The Azure Active Directory audience to use when forming authorization scopes.
     */
    public BlobAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Gets default Audience used to acquire a token for authorizing requests to any Azure Storage account.
     * If no audience is specified, this resource ID is the default value: "https://storage.azure.com/".
     *
     * @return public default audience.
     */
    public static BlobAudience getPublicAudience() {
        String publicAudience = "https://storage.azure.com/";
        return new BlobAudience(publicAudience);
    }

    /**
     * The service endpoint for a given storage account. Use this method to acquire a token for authorizing requests to
     * that specific Azure Storage account and service only.
     *
     * @param storageAccountName The storage account name used to populate the service endpoint.
     * @return the audience with the blob service endpoint.
     */
    public static BlobAudience getBlobServiceAccountAudience(String storageAccountName) {
        return new BlobAudience(String.format("https://%s.blob.core.windows.net/", storageAccountName));
    }

    /**
     * Get the audience for the blob account.
     *
     * @return the audience.
     */
    public String getAudience() {
        return this.audience;
    }

    @Override
    public int compareTo(BlobAudience other) {
        return this.audience.compareTo(other.audience);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlobAudience) {
            return this.audience.equals(((BlobAudience) obj).audience);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.audience;
    }

    @Override
    public int hashCode() {
        return this.audience.hashCode();
    }

    /**
     * Creates a scope with the respective audience and the default scope.
     *
     * @return the scope with the respective audience and the default scope.
     */
    public String createDefaultScope() {
        if (this.audience.endsWith("/")) {
            return this.audience + ".default";
        } else {
            return this.audience + "/.default";
        }
    }
}
