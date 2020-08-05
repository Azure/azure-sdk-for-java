// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static com.azure.search.documents.implementation.util.Utility.MAP_STRING_OBJECT_TYPE_REFERENCE;
import static com.azure.search.documents.implementation.util.Utility.initializeSerializerAdapter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexAction} and {@link IndexAction}.
 */
public final class IndexActionConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexActionConverter.class);
    private static final JacksonAdapter searchJacksonAdapter = (JacksonAdapter) initializeSerializerAdapter();

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexAction} to {@link IndexAction}.
     */
    public static <T> IndexAction<T> map(com.azure.search.documents.implementation.models.IndexAction obj) {
        if (obj == null) {
            return null;
        }
        IndexAction<T> indexAction = new IndexAction<T>();

        if (obj.getActionType() != null) {
            IndexActionType actionType = IndexActionTypeConverter.map(obj.getActionType());
            indexAction.setActionType(actionType);
        }

        if (obj.getAdditionalProperties() != null) {
            Map<String, Object> properties = obj.getAdditionalProperties();
            PrivateFieldAccessHelper.set(indexAction, "properties", properties);
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
            new com.azure.search.documents.implementation.models.IndexAction();

        if (obj.getActionType() != null) {
            com.azure.search.documents.implementation.models.IndexActionType actionType =
                IndexActionTypeConverter.map(obj.getActionType());
            indexAction.setActionType(actionType);
        }


        Map<String, Object> mapProperties = PrivateFieldAccessHelper.get(obj, "properties", Map.class);
        if (mapProperties == null) {
            T properties = obj.getDocument();
            if (serializer == null) {
                try {
                    String serializedJson = searchJacksonAdapter.serialize(properties, SerializerEncoding.JSON);
                    mapProperties = searchJacksonAdapter.deserialize(serializedJson, MAP_STRING_OBJECT_TYPE_REFERENCE.getJavaType(),
                        SerializerEncoding.JSON);
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("Something wrong with the serialization."));
                }
            } else {

                ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
                serializer.serialize(sourceStream, properties);
                mapProperties = serializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()), MAP_STRING_OBJECT_TYPE_REFERENCE);
            }
        }

        indexAction.setAdditionalProperties(mapProperties);
        return indexAction;
    }

    private IndexActionConverter() {
    }
}
