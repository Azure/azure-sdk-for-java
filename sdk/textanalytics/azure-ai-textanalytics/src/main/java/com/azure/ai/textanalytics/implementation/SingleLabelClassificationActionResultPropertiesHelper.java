// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SingleLabelClassificationActionResult;
import com.azure.ai.textanalytics.util.LabelClassificationResultCollection;

/**
 * The helper class to set the non-public properties of an {@link SingleLabelClassificationActionResult} instance.
 */
public final class SingleLabelClassificationActionResultPropertiesHelper {
    private static SingleLabelClassificationActionResultAccessor accessor;

    private SingleLabelClassificationActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleLabelClassificationActionResult}
     * instance.
     */
    public interface SingleLabelClassificationActionResultAccessor {
        void setDocumentsResults(SingleLabelClassificationActionResult actionResult,
            LabelClassificationResultCollection documentsResults);
    }

    /**
     * The method called from {@link SingleLabelClassificationActionResult} to set it's accessor.
     *
     * @param singleLabelClassificationActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final SingleLabelClassificationActionResultAccessor singleLabelClassificationActionResultAccessor) {
        accessor = singleLabelClassificationActionResultAccessor;
    }

    public static void setDocumentsResults(SingleLabelClassificationActionResult actionResult,
        LabelClassificationResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
