// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.models.IndexAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static com.azure.search.documents.implementation.util.Utility.MAP_STRING_OBJECT_TYPE_REFERENCE;
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

        if (obj.getActionType() != null) {
            indexAction.setActionType(obj.getActionType());
        }

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
            new com.azure.search.documents.implementation.models.IndexAction();

        if (obj.getActionType() != null) {
            indexAction.setActionType(obj.getActionType());
        }

        Map<String, Object> mapProperties = IndexActionHelper.getProperties(obj);
        if (mapProperties == null) {
            T properties = obj.getDocument();
            if (serializer == null) {
                try {
                    String serializedJson = getDefaultSerializerAdapter().serialize(properties,
                        SerializerEncoding.JSON);
                    mapProperties = getDefaultSerializerAdapter().deserialize(serializedJson,
                        MAP_STRING_OBJECT_TYPE_REFERENCE.getJavaType(), SerializerEncoding.JSON);
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("Failed to serialize IndexAction.", ex));
                }
            } else {
                ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
                serializer.serialize(sourceStream, properties);
                mapProperties = serializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()),
                    MAP_STRING_OBJECT_TYPE_REFERENCE);
            }
        }

        indexAction.setAdditionalProperties(mapProperties);
        return indexAction;
    }

    private IndexActionConverter() {
    }
}
