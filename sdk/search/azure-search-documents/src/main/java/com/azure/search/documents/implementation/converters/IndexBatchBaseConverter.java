// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexBatchBase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexBatch} and
 * {@link IndexBatchBase}.
 */
public final class IndexBatchBaseConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexBatch} to {@link IndexBatchBase}.
     */
    public static <T> IndexBatchBase<T> map(com.azure.search.documents.implementation.models.IndexBatch obj) {
        if (obj == null) {
            return null;
        }
        IndexBatchBase<T> indexBatchBase = new IndexBatchBase<T>();

        if (obj.getActions() != null) {
            List<IndexAction<T>> actions =
                obj.getActions().stream().map(IndexActionConverter::<T>map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexBatchBase, "actions", actions);
        }
        return indexBatchBase;
    }

    /**
     * Maps from {@link IndexBatchBase} to {@link com.azure.search.documents.implementation.models.IndexBatch}.
     */
    public static <T> com.azure.search.documents.implementation.models.IndexBatch map(IndexBatchBase<T> obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexBatch indexBatch =
            new com.azure.search.documents.implementation.models.IndexBatch();

        if (obj.getActions() != null) {
            List<com.azure.search.documents.implementation.models.IndexAction> actions =
                obj.getActions().stream().map(IndexActionConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexBatch, "actions", actions);
        }
        return indexBatch;
    }

    private IndexBatchBaseConverter() {
    }
}
