// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.MultiLabelClassificationActionResult;
import com.azure.ai.textanalytics.util.LabelClassificationResultCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiLabelClassificationActionResult} instance.
 */
public final class MultiLabelClassificationActionResultPropertiesHelper {

    private static MultiLabelClassificationActionResultAccessor accessor;

    private MultiLabelClassificationActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiLabelClassificationActionResult}
     * instance.
     */
    public interface MultiLabelClassificationActionResultAccessor {
        void setDocumentsResults(MultiLabelClassificationActionResult actionResult,
            LabelClassificationResultCollection documentsResults);
    }

    /**
     * The method called from {@link MultiLabelClassificationActionResult} to set it's accessor.
     *
     * @param multiLabelClassificationActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final MultiLabelClassificationActionResultAccessor multiLabelClassificationActionResultAccessor) {
        accessor = multiLabelClassificationActionResultAccessor;
    }

    public static void setDocumentsResults(MultiLabelClassificationActionResult actionResult,
        LabelClassificationResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
