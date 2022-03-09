// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link TextAnalyticsException} instance.
 */
public final class TextAnalyticsExceptionPropertiesHelper {
    private static TextAnalyticsExceptionAccessor accessor;

    private TextAnalyticsExceptionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextAnalyticsException} instance.
     */
    public interface TextAnalyticsExceptionAccessor {
        void setErrors(TextAnalyticsException textAnalyticsException, IterableStream<TextAnalyticsError> errors);
    }

    /**
     * The method called from {@link TextAnalyticsException} to set it's accessor.
     *
     * @param textAnalyticsExceptionAccessor The accessor.
     */
    public static void setAccessor(final TextAnalyticsExceptionAccessor textAnalyticsExceptionAccessor) {
        accessor = textAnalyticsExceptionAccessor;
    }

    public static void setErrors(TextAnalyticsException textAnalyticsException,
        IterableStream<TextAnalyticsError> errors) {
        accessor.setErrors(textAnalyticsException, errors);
    }
}
