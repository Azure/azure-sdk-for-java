// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

public class WrongConnectionStringException extends RuntimeException {

    public WrongConnectionStringException() {
        super("Your Application Insights connection string seems to have the wrong format. Please check the connection string and try again.");
    }
}
