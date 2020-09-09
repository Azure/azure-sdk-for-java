// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
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

        List<IndexAction<T>> actions = obj.getActions() == null ? null
            : obj.getActions().stream().map(IndexActionConverter::<T>map).collect(Collectors.toList());
        return new IndexBatchBase<T>(actions);
    }

    /**
     * Maps from {@link IndexBatchBase} to {@link com.azure.search.documents.implementation.models.IndexBatch}.
     */
    public static <T> com.azure.search.documents.implementation.models.IndexBatch map(IndexBatchBase<T> obj,
        ObjectSerializer jsonSerializer) {
        if (obj == null) {
            return null;
        }

        List<com.azure.search.documents.implementation.models.IndexAction> actions = obj.getActions() == null ? null
            : obj.getActions().stream().map(indexAction -> IndexActionConverter.map(indexAction, jsonSerializer))
                .collect(Collectors.toList());
        com.azure.search.documents.implementation.models.IndexBatch indexBatch =
            new com.azure.search.documents.implementation.models.IndexBatch(actions);

        indexBatch.validate();
        return indexBatch;
    }

    private IndexBatchBaseConverter() {
    }
}
