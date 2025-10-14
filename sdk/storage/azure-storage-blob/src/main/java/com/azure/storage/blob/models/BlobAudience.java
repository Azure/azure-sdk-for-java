// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience to be used when requesting a token from Azure Active Directory (AAD).
 * Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class BlobAudience extends ExpandableStringEnum<BlobAudience> {

    /**
     * Gets default Audience used to acquire a token for authorizing requests to any Azure Storage account.
     * If no audience is specified, this resource ID is the default value: "https://storage.azure.com/".
     */
    public static final BlobAudience AZURE_PUBLIC_CLOUD = fromString("https://storage.azure.com/");

    /**
     * Creates a new instance of {@link BlobAudience} without a {@link #toString()} value.
     * This constructor shouldn't be called as it will produce a {@link BlobAudience} which doesn't have a String enum
     * value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public BlobAudience() {
    }

    /**
     * The service endpoint for a given storage account. Use this method to acquire a token for authorizing requests to
     * that specific Azure Storage account and service only.
     *
     * @param storageAccountName The storage account name used to populate the service endpoint.
     * @return the audience with the blob service endpoint.
     */
    public static BlobAudience createBlobServiceAccountAudience(String storageAccountName) {
        return fromString(String.format("https://%s.blob.core.windows.net/", storageAccountName));
    }

    /**
     * The Azure Active Directory audience to use when forming authorization scopes.
     * For the Language service, this value corresponds to a URL that identifies the Azure cloud where the resource is
     * located.
     * For more information see
     * <a href="https://learn.microsoft.com/azure/storage/blobs/authorize-access-azure-active-directory">
     *     Authorize access to Azure blobs using Azure Active Directory</a>.
     *
     * @param audience The Azure Active Directory audience to use when forming authorization scopes.
     * @return the corresponding BlobAudience.
     */
    public static BlobAudience fromString(String audience) {
        return fromString(audience, BlobAudience.class);
    }

    /**
     * Get the known BlobAudience values.
     *
     * @return known BlobAudience values.
     */
    public static Collection<BlobAudience> values() {
        return values(BlobAudience.class);
    }
}
