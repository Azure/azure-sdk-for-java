// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.models.IndexAction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexAction} and {@link IndexAction}.
 */
public final class IndexActionConverter {
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

        // Convert the document to the JSON representation.
        byte[] documentJson = serializer.serializeToBytes(document);

        if (documentJson != null) {
            try (JsonReader reader = JsonProviders.createReader(documentJson)) {
                indexAction.setAdditionalProperties(reader.readMap(JsonReader::readUntyped));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return indexAction;
    }

    private IndexActionConverter() {
    }
}
