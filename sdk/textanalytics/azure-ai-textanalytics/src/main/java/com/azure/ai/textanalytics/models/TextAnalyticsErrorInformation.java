// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TextAnalyticsErrorInformationPropertiesHelper;

/**
 * The TextAnalyticsErrorInformation model
 */
public final class TextAnalyticsErrorInformation {
    /*
     * The errorCode property.
     */
    private TextAnalyticsErrorCode errorCode;

    /*
     * The message property.
     */
    private String message;

    static {
        TextAnalyticsErrorInformationPropertiesHelper.setAccessor(new
            TextAnalyticsErrorInformationPropertiesHelper.TextAnalyticsErrorInformationAccessor() {
                @Override
                public void setErrorCode(TextAnalyticsErrorInformation textAnalyticsErrorInformation,
                    TextAnalyticsErrorCode errorCode) {
                    textAnalyticsErrorInformation.setErrorCode(errorCode);
                }

                @Override
                public void setMessage(TextAnalyticsErrorInformation textAnalyticsErrorInformation, String message) {
                    textAnalyticsErrorInformation.setMessage(message);
                }
            });
    }

    /**
     * Get the error code property returned by the service.
     *
     * @return the error code property returned by the service.
     */
    public TextAnalyticsErrorCode getErrorCode() {
        return this.errorCode;
    }

    /**
     * Get the message property returned by the service.
     *
     * @return the message property returned by the service.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * The private setter to set the errorCode property
     * via {@link TextAnalyticsErrorInformationPropertiesHelper.TextAnalyticsErrorInformationAccessor}.
     *
     * @param errorCode the error code property returned by the service.
     */
    private void setErrorCode(TextAnalyticsErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * The private setter to set the message property
     * via {@link TextAnalyticsErrorInformationPropertiesHelper.TextAnalyticsErrorInformationAccessor}.
     *
     * @param message the message property returned by the service.
     */
    private void setMessage(String message) {
        this.message = message;
    }
}
