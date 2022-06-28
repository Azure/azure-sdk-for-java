// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SingleLabelClassifyActionResult;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link SingleLabelClassifyActionResult} instance.
 */
public final class SingleLabelClassifyActionResultPropertiesHelper {
    private static SingleLabelClassifyActionResultAccessor accessor;

    private SingleLabelClassifyActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleLabelClassifyActionResult}
     * instance.
     */
    public interface SingleLabelClassifyActionResultAccessor {
        void setDocumentsResults(SingleLabelClassifyActionResult actionResult,
            LabelClassifyResultCollection documentsResults);
    }

    /**
     * The method called from {@link SingleLabelClassifyActionResult} to set it's accessor.
     *
     * @param singleLabelClassifyActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final SingleLabelClassifyActionResultAccessor singleLabelClassifyActionResultAccessor) {
        accessor = singleLabelClassifyActionResultAccessor;
    }

    public static void setDocumentsResults(SingleLabelClassifyActionResult actionResult,
        LabelClassifyResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
