// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.IndexingParameters;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexingParameters} and
 * {@link IndexingParameters}.
 */
public final class IndexingParametersConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexingParametersConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexingParameters} to
     * {@link IndexingParameters}.
     */
    public static IndexingParameters map(com.azure.search.documents.implementation.models.IndexingParameters obj) {
        if (obj == null) {
            return null;
        }
        IndexingParameters indexingParameters = new IndexingParameters();

        Integer _maxFailedItemsPerBatch = obj.getMaxFailedItemsPerBatch();
        indexingParameters.setMaxFailedItemsPerBatch(_maxFailedItemsPerBatch);

        Integer _maxFailedItems = obj.getMaxFailedItems();
        indexingParameters.setMaxFailedItems(_maxFailedItems);

        if (obj.getConfiguration() != null) {
            Map<String, Object> _configuration =
                obj.getConfiguration().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            indexingParameters.setConfiguration(_configuration);
        }

        Integer _batchSize = obj.getBatchSize();
        indexingParameters.setBatchSize(_batchSize);
        return indexingParameters;
    }

    /**
     * Maps from {@link IndexingParameters} to
     * {@link com.azure.search.documents.implementation.models.IndexingParameters}.
     */
    public static com.azure.search.documents.implementation.models.IndexingParameters map(IndexingParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexingParameters indexingParameters =
            new com.azure.search.documents.implementation.models.IndexingParameters();

        Integer _maxFailedItemsPerBatch = obj.getMaxFailedItemsPerBatch();
        indexingParameters.setMaxFailedItemsPerBatch(_maxFailedItemsPerBatch);

        Integer _maxFailedItems = obj.getMaxFailedItems();
        indexingParameters.setMaxFailedItems(_maxFailedItems);

        if (obj.getConfiguration() != null) {
            Map<String, Object> _configuration =
                obj.getConfiguration().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            indexingParameters.setConfiguration(_configuration);
        }

        Integer _batchSize = obj.getBatchSize();
        indexingParameters.setBatchSize(_batchSize);
        return indexingParameters;
    }
}
