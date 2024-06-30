// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.validationstests.models;

public class IsEnabled {
    private String result;
    private String exception;

    public String getResult() {
        return result;
    }

    public String getException() {
        return exception;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
