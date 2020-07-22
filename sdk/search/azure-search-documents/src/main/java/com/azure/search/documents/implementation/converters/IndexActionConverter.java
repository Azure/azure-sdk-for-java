// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.serializer.SearchSerializerProviders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexAction} and {@link IndexAction}.
 */
public final class IndexActionConverter {

    private static final JsonSerializer SERIALIZER = SearchSerializerProviders.createInstance(
        new JsonOptions().includeNulls());

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
    public static <T> com.azure.search.documents.implementation.models.IndexAction map(IndexAction<T> obj) {
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

        Map<String, Object> additionalProperties;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> mapProperties = PrivateFieldAccessHelper.get(obj, "properties", Map.class);
        if (mapProperties != null) {
            additionalProperties = SERIALIZER.serialize(outputStream, mapProperties)
                .flatMap(sourceStream -> SERIALIZER.deserializeToMap(new ByteArrayInputStream(sourceStream.toByteArray()))
                    .map(Utility::convertMaps)).block();
        } else {
            T properties = obj.getDocument();
            JsonSerializer searchSerializer = SearchSerializerProviders.createInstance();
            additionalProperties = searchSerializer.serialize(outputStream, properties)
                .flatMap(sourceStream -> searchSerializer.deserializeToMap(new ByteArrayInputStream(
                    sourceStream.toByteArray())).map(Utility::convertMaps)).block();
        }

        indexAction.setAdditionalProperties(additionalProperties);
        return indexAction;
    }

    private IndexActionConverter() {
    }
}
