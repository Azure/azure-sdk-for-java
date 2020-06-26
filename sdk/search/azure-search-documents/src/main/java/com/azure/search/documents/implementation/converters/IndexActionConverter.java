// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexAction} and {@link IndexAction}.
 */
public final class IndexActionConverter {
    private static final ObjectMapper DYNAMIC_TYPE_MAPPER;
    private static final ObjectMapper STRONGLY_TYPE_MAPPER;

    static {
        DYNAMIC_TYPE_MAPPER = new JacksonAdapter().serializer();
        STRONGLY_TYPE_MAPPER = new JacksonAdapter().serializer();
        SerializationUtil.configureMapper(DYNAMIC_TYPE_MAPPER);
        SerializationUtil.configureMapper(STRONGLY_TYPE_MAPPER);
    }

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
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};

        Map<String, Object> mapProperties = PrivateFieldAccessHelper.get(obj, "properties", Map.class);
        if (mapProperties != null) {
            DYNAMIC_TYPE_MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            additionalProperties = DYNAMIC_TYPE_MAPPER.convertValue(mapProperties, typeRef);
        } else {
            T properties = obj.getDocument();
            additionalProperties = STRONGLY_TYPE_MAPPER.convertValue(properties, typeRef);
        }

        indexAction.setAdditionalProperties(additionalProperties);
        return indexAction;
    }

    private IndexActionConverter() {
    }
}
