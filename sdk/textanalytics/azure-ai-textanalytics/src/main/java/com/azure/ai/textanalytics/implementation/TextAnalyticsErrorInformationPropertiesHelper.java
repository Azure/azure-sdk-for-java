// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorInformation;

/**
 * The helper class to set the non-public properties of an {@link TextAnalyticsErrorInformation} instance.
 */
public final class TextAnalyticsErrorInformationPropertiesHelper {
    private static TextAnalyticsErrorInformationAccessor accessor;

    private TextAnalyticsErrorInformationPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextAnalyticsErrorInformation} instance.
     */
    public interface TextAnalyticsErrorInformationAccessor {
        void setErrorCode(TextAnalyticsErrorInformation textAnalyticsErrorInformation,
            TextAnalyticsErrorCode errorCode);
        void setMessage(TextAnalyticsErrorInformation textAnalyticsErrorInformation, String message);
    }

    /**
     * The method called from {@link TextAnalyticsErrorInformation} to set it's accessor.
     *
     * @param textAnalyticsErrorInformationAccessor The accessor.
     */
    public static void setAccessor(final TextAnalyticsErrorInformationAccessor textAnalyticsErrorInformationAccessor) {
        accessor = textAnalyticsErrorInformationAccessor;
    }

    public static void setErrorCode(TextAnalyticsErrorInformation textAnalyticsErrorInformation,
        TextAnalyticsErrorCode errorCode) {
        accessor.setErrorCode(textAnalyticsErrorInformation, errorCode);
    }

    public static void setMessage(TextAnalyticsErrorInformation textAnalyticsErrorInformation, String message) {
        accessor.setMessage(textAnalyticsErrorInformation, message);
    }
}
