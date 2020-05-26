// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials;
import com.azure.search.documents.models.SearchResourceEncryptionKey;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchResourceEncryptionKey} and
 * {@link SearchResourceEncryptionKey}.
 */
public final class SearchResourceEncryptionKeyConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchResourceEncryptionKey} to
     * {@link SearchResourceEncryptionKey}.
     */
    public static SearchResourceEncryptionKey map(com.azure.search.documents.implementation.models.SearchResourceEncryptionKey obj) {
        if (obj == null) {
            return null;
        }
        SearchResourceEncryptionKey searchResourceEncryptionKey = new SearchResourceEncryptionKey();

        String keyVersion = obj.getKeyVersion();
        searchResourceEncryptionKey.setKeyVersion(keyVersion);

        if (obj.getAccessCredentials() != null) {
            AzureActiveDirectoryApplicationCredentials accessCredentials =
                AzureActiveDirectoryApplicationCredentialsConverter.map(obj.getAccessCredentials());
            searchResourceEncryptionKey.setAccessCredentials(accessCredentials);
        }

        String keyName = obj.getKeyName();
        searchResourceEncryptionKey.setKeyName(keyName);

        String vaultUri = obj.getVaultUri();
        searchResourceEncryptionKey.setVaultUri(vaultUri);
        return searchResourceEncryptionKey;
    }

    /**
     * Maps from {@link SearchResourceEncryptionKey} to
     * {@link com.azure.search.documents.implementation.models.SearchResourceEncryptionKey}.
     */
    public static com.azure.search.documents.implementation.models.SearchResourceEncryptionKey map(SearchResourceEncryptionKey obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchResourceEncryptionKey searchResourceEncryptionKey =
            new com.azure.search.documents.implementation.models.SearchResourceEncryptionKey();

        String keyVersion = obj.getKeyVersion();
        searchResourceEncryptionKey.setKeyVersion(keyVersion);

        if (obj.getAccessCredentials() != null) {
            com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials accessCredentials = AzureActiveDirectoryApplicationCredentialsConverter.map(obj.getAccessCredentials());
            searchResourceEncryptionKey.setAccessCredentials(accessCredentials);
        }

        String keyName = obj.getKeyName();
        searchResourceEncryptionKey.setKeyName(keyName);

        String vaultUri = obj.getVaultUri();
        searchResourceEncryptionKey.setVaultUri(vaultUri);
        return searchResourceEncryptionKey;
    }

    private SearchResourceEncryptionKeyConverter() {
    }
}
