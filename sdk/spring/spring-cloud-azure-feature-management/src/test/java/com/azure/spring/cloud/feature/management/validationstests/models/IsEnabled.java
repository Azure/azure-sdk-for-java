// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.validationstests.models;

public class IsEnabled {
    private String result;
    private String exception;

    /**
     * @return result
     * */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result of validation test case
     * */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return exception
     * */
    public String getException() {
        return exception;
    }

    /**
     * @param exception the exception message throws when run test case
     * */
    public void setException(String exception) {
        this.exception = exception;
    }
}
