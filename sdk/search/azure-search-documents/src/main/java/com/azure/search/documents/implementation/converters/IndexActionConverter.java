// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        T document = obj.getDocument();

        ObjectMapper mapper = new JacksonAdapter().serializer();
        SerializationUtil.configureMapper(mapper);
        Map<String, Object> additionalProperties = mapper.convertValue(document, Map.class);

        indexAction.setAdditionalProperties(additionalProperties);

        if (obj.getParamMap() != null) {
            Map<String, Object> properties = obj.getParamMap();
            PrivateFieldAccessHelper.set(indexAction, "additionalProperties", properties);
        }
        return indexAction;
    }

    private IndexActionConverter() {
    }
}
