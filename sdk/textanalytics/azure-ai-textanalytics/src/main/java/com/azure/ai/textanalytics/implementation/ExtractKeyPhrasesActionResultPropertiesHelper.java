// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ExtractKeyPhrasesActionResult} instance.
 */
public final class ExtractKeyPhrasesActionResultPropertiesHelper {
    private static ExtractKeyPhrasesActionResultAccessor accessor;

    private ExtractKeyPhrasesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractKeyPhrasesActionResult}
     * instance.
     */
    public interface ExtractKeyPhrasesActionResultAccessor {
        void setDocumentsResults(ExtractKeyPhrasesActionResult actionResult,
            ExtractKeyPhrasesResultCollection documentsResults);
    }

    /**
     * The method called from {@link ExtractKeyPhrasesActionResult} to set it's accessor.
     *
     * @param extractKeyPhrasesActionResultAccessor The accessor.
     */
    public static void setAccessor(final ExtractKeyPhrasesActionResultAccessor extractKeyPhrasesActionResultAccessor) {
        accessor = extractKeyPhrasesActionResultAccessor;
    }

    public static void setDocumentsResults(ExtractKeyPhrasesActionResult actionResult,
        ExtractKeyPhrasesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
