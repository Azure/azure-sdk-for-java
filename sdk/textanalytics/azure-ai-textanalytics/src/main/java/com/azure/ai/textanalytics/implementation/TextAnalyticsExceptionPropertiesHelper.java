// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsErrorInformation;
import com.azure.ai.textanalytics.models.TextAnalyticsException;

import java.util.List;

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
        void setErrorInformationList(TextAnalyticsException textAnalyticsException,
            List<TextAnalyticsErrorInformation> errorInformation);
    }

    /**
     * The method called from {@link TextAnalyticsException} to set it's accessor.
     *
     * @param textAnalyticsExceptionAccessor The accessor.
     */
    public static void setAccessor(final TextAnalyticsExceptionAccessor textAnalyticsExceptionAccessor) {
        accessor = textAnalyticsExceptionAccessor;
    }

    public static void setErrorInformationList(TextAnalyticsException textAnalyticsException,
        List<TextAnalyticsErrorInformation> errorInformation) {
        accessor.setErrorInformationList(textAnalyticsException, errorInformation);
    }
}
