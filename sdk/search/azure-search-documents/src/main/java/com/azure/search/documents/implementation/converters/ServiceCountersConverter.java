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

        if (obj.getSkillsetCounter() != null) {
            ResourceCounter skillsetCounter = ResourceCounterConverter.map(obj.getSkillsetCounter());
            serviceCounters.setSkillsetCounter(skillsetCounter);
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
        com.azure.search.documents.indexes.implementation.models.ServiceCounters serviceCounters =
            new com.azure.search.documents.indexes.implementation.models.ServiceCounters();

        if (obj.getDocumentCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter documentCounter =
                ResourceCounterConverter.map(obj.getDocumentCounter());
            serviceCounters.setDocumentCounter(documentCounter);
        }

        if (obj.getIndexCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter indexCounter =
                ResourceCounterConverter.map(obj.getIndexCounter());
            serviceCounters.setIndexCounter(indexCounter);
        }

        if (obj.getSynonymMapCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter synonymMapCounter =
                ResourceCounterConverter.map(obj.getSynonymMapCounter());
            serviceCounters.setSynonymMapCounter(synonymMapCounter);
        }

        if (obj.getStorageSizeCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter storageSizeCounter =
                ResourceCounterConverter.map(obj.getStorageSizeCounter());
            serviceCounters.setStorageSizeCounter(storageSizeCounter);
        }

        if (obj.getDataSourceCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter dataSourceCounter =
                ResourceCounterConverter.map(obj.getDataSourceCounter());
            serviceCounters.setDataSourceCounter(dataSourceCounter);
        }

        if (obj.getIndexerCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter indexerCounter =
                ResourceCounterConverter.map(obj.getIndexerCounter());
            serviceCounters.setIndexerCounter(indexerCounter);
        }

        if (obj.getSkillsetCounter() != null) {
            com.azure.search.documents.indexes.implementation.models.ResourceCounter skillsetCounter =
                ResourceCounterConverter.map(obj.getSkillsetCounter());
            serviceCounters.setSkillsetCounter(skillsetCounter);
        }
        return serviceCounters;
    }

    private ServiceCountersConverter() {
    }
}
