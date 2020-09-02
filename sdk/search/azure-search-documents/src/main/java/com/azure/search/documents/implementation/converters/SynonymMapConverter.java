// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchResourceEncryptionKey;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.Objects;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SynonymMap} and {@link SynonymMap}.
 */
public final class SynonymMapConverter {

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SynonymMap} to {@link SynonymMap}.
     */
    public static SynonymMap map(com.azure.search.documents.indexes.implementation.models.SynonymMap obj) {
        if (obj == null) {
            return null;
        }
        SynonymMap synonymMap = new SynonymMap(obj.getName(), obj.getSynonyms());

        String eTag = obj.getETag();
        synonymMap.setETag(eTag);

        if (obj.getEncryptionKey() != null) {
            SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            synonymMap.setEncryptionKey(encryptionKey);
        }
        return synonymMap;
    }

    /**
     * Maps from {@link SynonymMap} to {@link com.azure.search.documents.indexes.implementation.models.SynonymMap}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SynonymMap map(SynonymMap obj) {
        if (obj == null) {
            return null;
        }
        Objects.requireNonNull(obj.getName(), "SynonymMap name cannot be null.");
        com.azure.search.documents.indexes.implementation.models.SynonymMap synonymMap =
            new com.azure.search.documents.indexes.implementation.models.SynonymMap()
                .setName(obj.getName())
                .setFormat("solr")
                .setSynonyms(obj.getSynonyms());

        String eTag = obj.getETag();
        synonymMap.setETag(eTag);

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.indexes.implementation.models.SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            synonymMap.setEncryptionKey(encryptionKey);
        }
        return synonymMap;
    }

    private SynonymMapConverter() {
    }
}
