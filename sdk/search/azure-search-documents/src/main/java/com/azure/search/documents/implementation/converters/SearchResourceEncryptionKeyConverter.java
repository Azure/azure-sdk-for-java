// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.implementation.models.AzureActiveDirectoryApplicationCredentials;
import com.azure.search.documents.indexes.models.SearchResourceEncryptionKey;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey} and
 * {@link SearchResourceEncryptionKey}.
 */
public final class SearchResourceEncryptionKeyConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey} to
     * {@link SearchResourceEncryptionKey}.
     */
    public static SearchResourceEncryptionKey map(com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey obj) {
        if (obj == null) {
            return null;
        }
        SearchResourceEncryptionKey searchResourceEncryptionKey = new SearchResourceEncryptionKey();

        String keyVersion = obj.getKeyVersion();
        searchResourceEncryptionKey.setKeyVersion(keyVersion);

        searchResourceEncryptionKey.setApplicationId(obj.getAccessCredentials().getApplicationId());
        searchResourceEncryptionKey.setApplicationSecret(obj.getAccessCredentials().getApplicationSecret());

        String keyName = obj.getKeyName();
        searchResourceEncryptionKey.setKeyName(keyName);

        String vaultUri = obj.getVaultUri();
        searchResourceEncryptionKey.setVaultUrl(vaultUri);
        return searchResourceEncryptionKey;
    }

    /**
     * Maps from {@link SearchResourceEncryptionKey} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey map(SearchResourceEncryptionKey obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey searchResourceEncryptionKey =
            new com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey(obj.getKeyName(),
                obj.getKeyVersion(), obj.getVaultUrl());

        AzureActiveDirectoryApplicationCredentials accessCredentials =
            new AzureActiveDirectoryApplicationCredentials(obj.getApplicationId());
        accessCredentials.setApplicationSecret(obj.getApplicationSecret());
        accessCredentials.validate();
        searchResourceEncryptionKey.setAccessCredentials(accessCredentials);

        searchResourceEncryptionKey.validate();
        return searchResourceEncryptionKey;
    }

    private SearchResourceEncryptionKeyConverter() {
    }
}
