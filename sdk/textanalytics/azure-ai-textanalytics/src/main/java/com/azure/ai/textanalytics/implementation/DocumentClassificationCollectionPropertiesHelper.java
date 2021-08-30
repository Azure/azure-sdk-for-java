// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DocumentClassificationCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link DocumentClassificationCollection} instance.
 */
public final class DocumentClassificationCollectionPropertiesHelper {
    private static DocumentClassificationCollectionAccessor accessor;

    private DocumentClassificationCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentClassificationCollection}
     * instance.
     */
    public interface DocumentClassificationCollectionAccessor {
        void setWarnings(DocumentClassificationCollection documentClassifications,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link DocumentClassificationCollection} to set it's accessor.
     *
     * @param documentClassificationCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final DocumentClassificationCollectionAccessor documentClassificationCollectionAccessor) {
        accessor = documentClassificationCollectionAccessor;
    }

    public static void setWarnings(DocumentClassificationCollection documentClassificationCollection,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(documentClassificationCollection, warnings);
    }
}
