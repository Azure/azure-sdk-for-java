// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchServiceCounters;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters} and
 * {@link SearchServiceCounters}.
 */
public final class ServiceCountersConverter {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters} to {@link SearchServiceCounters}.
     */
    public static SearchServiceCounters map(com.azure.search.documents.indexes.implementation.models.ServiceCounters obj) {
        return OBJECT_MAPPER.convertValue(obj, SearchServiceCounters.class);
    }

    /**
     * Maps from {@link SearchServiceCounters} to {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ServiceCounters map(SearchServiceCounters obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.ResourceCounter documentCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getDocumentCounter());

        com.azure.search.documents.indexes.implementation.models.ResourceCounter indexCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getIndexCounter());

        com.azure.search.documents.indexes.implementation.models.ResourceCounter synonymMapCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getSynonymMapCounter());

        com.azure.search.documents.indexes.implementation.models.ResourceCounter storageSizeCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getStorageSizeCounter());


        com.azure.search.documents.indexes.implementation.models.ResourceCounter dataSourceCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getDataSourceCounter());

        com.azure.search.documents.indexes.implementation.models.ResourceCounter indexerCounter =
            obj.getDocumentCounter() == null ? null
                : ResourceCounterConverter.map(obj.getIndexerCounter());


        com.azure.search.documents.indexes.implementation.models.ServiceCounters serviceCounters =
            new com.azure.search.documents.indexes.implementation.models.ServiceCounters(
                documentCounter, indexCounter, indexerCounter, dataSourceCounter, storageSizeCounter,
                synonymMapCounter);
        serviceCounters.validate();
        return serviceCounters;
    }

    private ServiceCountersConverter() {
    }
}
