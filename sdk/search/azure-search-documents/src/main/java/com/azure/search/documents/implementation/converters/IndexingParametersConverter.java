// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.IndexingParameters;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.IndexingParameters} and
 * {@link IndexingParameters}.
 */
public final class IndexingParametersConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.IndexingParameters} to
     * {@link IndexingParameters}.
     */
    public static IndexingParameters map(com.azure.search.documents.indexes.implementation.models.IndexingParameters obj) {
        if (obj == null) {
            return null;
        }
        IndexingParameters indexingParameters = new IndexingParameters();

        Integer maxFailedItemsPerBatch = obj.getMaxFailedItemsPerBatch();
        indexingParameters.setMaxFailedItemsPerBatch(maxFailedItemsPerBatch);

        Integer maxFailedItems = obj.getMaxFailedItems();
        indexingParameters.setMaxFailedItems(maxFailedItems);

        if (obj.getConfiguration() != null) {
            Map<String, Object> configuration =
                obj.getConfiguration().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            indexingParameters.setConfiguration(configuration);
        }

        Integer batchSize = obj.getBatchSize();
        indexingParameters.setBatchSize(batchSize);
        return indexingParameters;
    }

    /**
     * Maps from {@link IndexingParameters} to
     * {@link com.azure.search.documents.indexes.implementation.models.IndexingParameters}.
     */
    public static com.azure.search.documents.indexes.implementation.models.IndexingParameters map(IndexingParameters obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.IndexingParameters indexingParameters =
            new com.azure.search.documents.indexes.implementation.models.IndexingParameters();

        Integer maxFailedItemsPerBatch = obj.getMaxFailedItemsPerBatch();
        indexingParameters.setMaxFailedItemsPerBatch(maxFailedItemsPerBatch);

        Integer maxFailedItems = obj.getMaxFailedItems();
        indexingParameters.setMaxFailedItems(maxFailedItems);

        if (obj.getConfiguration() != null) {
            Map<String, Object> configuration =
                obj.getConfiguration().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            indexingParameters.setConfiguration(configuration);
        }

        Integer batchSize = obj.getBatchSize();
        indexingParameters.setBatchSize(batchSize);
        return indexingParameters;
    }

    private IndexingParametersConverter() {
    }
}
