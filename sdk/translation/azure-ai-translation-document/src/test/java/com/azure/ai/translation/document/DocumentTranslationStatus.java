// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

public enum DocumentTranslationStatus {    
    NOT_STARTED("NotStarted"),
    RUNNING("Running"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    CANCELLING("Cancelling"),
    VALIDATION_FAILED("ValidationFailed");

    private final String value;

    DocumentTranslationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
