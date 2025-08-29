// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The audience to be used when requesting a token from Azure Active Directory (AAD).
 * Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class ShareAudience extends ExpandableStringEnum<ShareAudience> {

    /**
     * Gets default Audience used to acquire a token for authorizing requests to any Azure Storage account.
     * If no audience is specified, this resource ID is the default value: "https://storage.azure.com/".
     */
    public static final ShareAudience AZURE_PUBLIC_CLOUD = fromString("https://storage.azure.com/");

    /**
     * Creates a new instance of {@link ShareAudience} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link ShareAudience} which doesn't have a String enum
     * value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ShareAudience() {
    }

    /**
     * The service endpoint for a given storage account. Use this method to acquire a token for authorizing requests to
     * that specific Azure Storage account and service only.
     *
     * @param storageAccountName The storage account name used to populate the service endpoint.
     * @return the audience with the file service endpoint.
     */
    public static ShareAudience createShareServiceAccountAudience(String storageAccountName) {
        return fromString(String.format("https://%s.file.core.windows.net/", storageAccountName));
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
     * @return the corresponding ShareAudience.
     */
    public static ShareAudience fromString(String audience) {
        return fromString(audience, ShareAudience.class);
    }

    /**
     * Gets known ShareAudience values.
     *
     * @return known ShareAudience values.
     */
    public static Collection<ShareAudience> values() {
        return values(ShareAudience.class);
    }
}
