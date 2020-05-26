// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SearchResourceEncryptionKey;
import com.azure.search.documents.models.SynonymMap;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SynonymMap} and {@link SynonymMap}.
 */
public final class SynonymMapConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SynonymMap} to {@link SynonymMap}.
     */
    public static SynonymMap map(com.azure.search.documents.implementation.models.SynonymMap obj) {
        if (obj == null) {
            return null;
        }
        SynonymMap synonymMap = new SynonymMap();

        String synonyms = obj.getSynonyms();
        synonymMap.setSynonyms(synonyms);

        String name = obj.getName();
        synonymMap.setName(name);

        String format = obj.getFormat();
        PrivateFieldAccessHelper.set(synonymMap, "format", format);

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
     * Maps from {@link SynonymMap} to {@link com.azure.search.documents.implementation.models.SynonymMap}.
     */
    public static com.azure.search.documents.implementation.models.SynonymMap map(SynonymMap obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SynonymMap synonymMap =
            new com.azure.search.documents.implementation.models.SynonymMap();

        String synonyms = obj.getSynonyms();
        synonymMap.setSynonyms(synonyms);

        String name = obj.getName();
        synonymMap.setName(name);

        synonymMap.setFormat("solr");

        String eTag = obj.getETag();
        synonymMap.setETag(eTag);

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.implementation.models.SearchResourceEncryptionKey encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            synonymMap.setEncryptionKey(encryptionKey);
        }
        return synonymMap;
    }

    private SynonymMapConverter() {
    }
}
