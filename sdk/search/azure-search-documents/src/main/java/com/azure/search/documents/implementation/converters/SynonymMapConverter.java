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
    private static final ClientLogger LOGGER = new ClientLogger(SynonymMapConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SynonymMap} to {@link SynonymMap}.
     */
    public static SynonymMap map(com.azure.search.documents.implementation.models.SynonymMap obj) {
        if (obj == null) {
            return null;
        }
        SynonymMap synonymMap = new SynonymMap();

        String _synonyms = obj.getSynonyms();
        synonymMap.setSynonyms(_synonyms);

        String _name = obj.getName();
        synonymMap.setName(_name);

        String _format = obj.getFormat();
        PrivateFieldAccessHelper.set(synonymMap, "format", _format);

        String _eTag = obj.getETag();
        synonymMap.setETag(_eTag);

        if (obj.getEncryptionKey() != null) {
            SearchResourceEncryptionKey _encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            synonymMap.setEncryptionKey(_encryptionKey);
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

        String _synonyms = obj.getSynonyms();
        synonymMap.setSynonyms(_synonyms);

        String _name = obj.getName();
        synonymMap.setName(_name);

        synonymMap.setFormat("solr");

        String _eTag = obj.getETag();
        synonymMap.setETag(_eTag);

        if (obj.getEncryptionKey() != null) {
            com.azure.search.documents.implementation.models.SearchResourceEncryptionKey _encryptionKey =
                SearchResourceEncryptionKeyConverter.map(obj.getEncryptionKey());
            synonymMap.setEncryptionKey(_encryptionKey);
        }
        return synonymMap;
    }
}
