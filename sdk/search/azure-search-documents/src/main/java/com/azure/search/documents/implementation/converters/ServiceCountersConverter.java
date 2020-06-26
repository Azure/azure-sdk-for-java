// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ResourceCounter;
import com.azure.search.documents.indexes.models.ServiceCounters;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters} and
 * {@link ServiceCounters}.
 */
public final class ServiceCountersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters} to {@link ServiceCounters}.
     */
    public static ServiceCounters map(com.azure.search.documents.indexes.implementation.models.ServiceCounters obj) {
        if (obj == null) {
            return null;
        }
        ServiceCounters serviceCounters = new ServiceCounters();

        if (obj.getDocumentCounter() != null) {
            ResourceCounter documentCounter = ResourceCounterConverter.map(obj.getDocumentCounter());
            serviceCounters.setDocumentCounter(documentCounter);
        }

        if (obj.getIndexCounter() != null) {
            ResourceCounter indexCounter = ResourceCounterConverter.map(obj.getIndexCounter());
            serviceCounters.setIndexCounter(indexCounter);
        }

        if (obj.getSynonymMapCounter() != null) {
            ResourceCounter synonymMapCounter = ResourceCounterConverter.map(obj.getSynonymMapCounter());
            serviceCounters.setSynonymMapCounter(synonymMapCounter);
        }

        if (obj.getStorageSizeCounter() != null) {
            ResourceCounter storageSizeCounter = ResourceCounterConverter.map(obj.getStorageSizeCounter());
            serviceCounters.setStorageSizeCounter(storageSizeCounter);
        }

        if (obj.getDataSourceCounter() != null) {
            ResourceCounter dataSourceCounter = ResourceCounterConverter.map(obj.getDataSourceCounter());
            serviceCounters.setDataSourceCounter(dataSourceCounter);
        }

        if (obj.getIndexerCounter() != null) {
            ResourceCounter indexerCounter = ResourceCounterConverter.map(obj.getIndexerCounter());
            serviceCounters.setIndexerCounter(indexerCounter);
        }

        return serviceCounters;
    }

    /**
     * Maps from {@link ServiceCounters} to {@link com.azure.search.documents.indexes.implementation.models.ServiceCounters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ServiceCounters map(ServiceCounters obj) {
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
