// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ResourceCounter;
import com.azure.search.documents.models.ServiceCounters;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ServiceCounters} and
 * {@link ServiceCounters}.
 */
public final class ServiceCountersConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceCountersConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ServiceCounters} to {@link ServiceCounters}.
     */
    public static ServiceCounters map(com.azure.search.documents.implementation.models.ServiceCounters obj) {
        if (obj == null) {
            return null;
        }
        ServiceCounters serviceCounters = new ServiceCounters();

        if (obj.getDocumentCounter() != null) {
            ResourceCounter _documentCounter = ResourceCounterConverter.map(obj.getDocumentCounter());
            serviceCounters.setDocumentCounter(_documentCounter);
        }

        if (obj.getIndexCounter() != null) {
            ResourceCounter _indexCounter = ResourceCounterConverter.map(obj.getIndexCounter());
            serviceCounters.setIndexCounter(_indexCounter);
        }

        if (obj.getSynonymMapCounter() != null) {
            ResourceCounter _synonymMapCounter = ResourceCounterConverter.map(obj.getSynonymMapCounter());
            serviceCounters.setSynonymMapCounter(_synonymMapCounter);
        }

        if (obj.getStorageSizeCounter() != null) {
            ResourceCounter _storageSizeCounter = ResourceCounterConverter.map(obj.getStorageSizeCounter());
            serviceCounters.setStorageSizeCounter(_storageSizeCounter);
        }

        if (obj.getDataSourceCounter() != null) {
            ResourceCounter _dataSourceCounter = ResourceCounterConverter.map(obj.getDataSourceCounter());
            serviceCounters.setDataSourceCounter(_dataSourceCounter);
        }

        if (obj.getIndexerCounter() != null) {
            ResourceCounter _indexerCounter = ResourceCounterConverter.map(obj.getIndexerCounter());
            serviceCounters.setIndexerCounter(_indexerCounter);
        }

        if (obj.getSkillsetCounter() != null) {
            ResourceCounter _skillsetCounter = ResourceCounterConverter.map(obj.getSkillsetCounter());
            serviceCounters.setSkillsetCounter(_skillsetCounter);
        }
        return serviceCounters;
    }

    /**
     * Maps from {@link ServiceCounters} to {@link com.azure.search.documents.implementation.models.ServiceCounters}.
     */
    public static com.azure.search.documents.implementation.models.ServiceCounters map(ServiceCounters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ServiceCounters serviceCounters =
            new com.azure.search.documents.implementation.models.ServiceCounters();

        if (obj.getDocumentCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _documentCounter =
                ResourceCounterConverter.map(obj.getDocumentCounter());
            serviceCounters.setDocumentCounter(_documentCounter);
        }

        if (obj.getIndexCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _indexCounter =
                ResourceCounterConverter.map(obj.getIndexCounter());
            serviceCounters.setIndexCounter(_indexCounter);
        }

        if (obj.getSynonymMapCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _synonymMapCounter =
                ResourceCounterConverter.map(obj.getSynonymMapCounter());
            serviceCounters.setSynonymMapCounter(_synonymMapCounter);
        }

        if (obj.getStorageSizeCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _storageSizeCounter =
                ResourceCounterConverter.map(obj.getStorageSizeCounter());
            serviceCounters.setStorageSizeCounter(_storageSizeCounter);
        }

        if (obj.getDataSourceCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _dataSourceCounter =
                ResourceCounterConverter.map(obj.getDataSourceCounter());
            serviceCounters.setDataSourceCounter(_dataSourceCounter);
        }

        if (obj.getIndexerCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _indexerCounter =
                ResourceCounterConverter.map(obj.getIndexerCounter());
            serviceCounters.setIndexerCounter(_indexerCounter);
        }

        if (obj.getSkillsetCounter() != null) {
            com.azure.search.documents.implementation.models.ResourceCounter _skillsetCounter =
                ResourceCounterConverter.map(obj.getSkillsetCounter());
            serviceCounters.setSkillsetCounter(_skillsetCounter);
        }
        return serviceCounters;
    }
}
