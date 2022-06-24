// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.DefaultJsonReader;
import com.azure.search.documents.models.IndexAction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.azure.search.documents.implementation.util.Utility.getDefaultSerializerAdapter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexAction} and {@link IndexAction}.
 */
public final class IndexActionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexActionConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexAction} to {@link IndexAction}.
     */
    public static <T> IndexAction<T> map(com.azure.search.documents.implementation.models.IndexAction obj) {
        if (obj == null) {
            return null;
        }

        IndexAction<T> indexAction = new IndexAction<>();
        indexAction.setActionType(obj.getActionType());

        if (obj.getAdditionalProperties() != null) {
            Map<String, Object> properties = obj.getAdditionalProperties();
            IndexActionHelper.setProperties(indexAction, properties);
        }

        return indexAction;
    }

    /**
     * Maps from {@link IndexAction} to {@link com.azure.search.documents.implementation.models.IndexAction}.
     */
    @SuppressWarnings("unchecked")
    public static <T> com.azure.search.documents.implementation.models.IndexAction map(IndexAction<T> obj,
        ObjectSerializer serializer) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexAction indexAction =
            new com.azure.search.documents.implementation.models.IndexAction().setActionType(obj.getActionType());

        // Attempt to get the document as the Map<String, Object> properties.
        Object document = IndexActionHelper.getProperties(obj);
        if (document == null) {
            // If ths document wasn't a Map type, get the generic document type.
            document = obj.getDocument();
        }

        // Convert the document to the JSON string representation.
        byte[] documentJson;
        if (serializer == null) {
            // A custom ObjectSerializer isn't being used, fallback to default JacksonAdapter.
            try {
                documentJson = getDefaultSerializerAdapter().serializeToBytes(document, SerializerEncoding.JSON);
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }
        } else {
            // A custom ObjectSerializer is being used, use it.
            documentJson = serializer.serializeToBytes(document);
        }

        if (documentJson != null) {
            indexAction.setAdditionalProperties(
                (Map<String, Object>) JsonUtils.readUntypedField(DefaultJsonReader.fromBytes(documentJson)));
        }

        return indexAction;
    }

    private IndexActionConverter() {
    }
}
