// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.models.WarningCodeValue;

/**
 * The {@link TextAnalyticsWarning} model.
 */
public interface TextAnalyticsWarning {
    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    WarningCodeValue getCode();

    /**
     * Get the message property: Warning message.
     *
     * @return the message value.
     */
    String getMessage();
}
