// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.MultiLabelClassifyActionResult;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiLabelClassifyActionResult} instance.
 */
public final class MultiLabelClassifyActionResultPropertiesHelper {

    private static MultiLabelClassifyActionResultAccessor accessor;

    private MultiLabelClassifyActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiLabelClassifyActionResult}
     * instance.
     */
    public interface MultiLabelClassifyActionResultAccessor {
        void setDocumentsResults(MultiLabelClassifyActionResult actionResult,
            LabelClassifyResultCollection documentsResults);
    }

    /**
     * The method called from {@link MultiLabelClassifyActionResult} to set it's accessor.
     *
     * @param multiLabelClassifyActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final MultiLabelClassifyActionResultAccessor multiLabelClassifyActionResultAccessor) {
        accessor = multiLabelClassifyActionResultAccessor;
    }

    public static void setDocumentsResults(MultiLabelClassifyActionResult actionResult,
        LabelClassifyResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
