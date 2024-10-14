// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor.implementation;

class WrongConnectionStringException extends RuntimeException {

    WrongConnectionStringException() {
        super("Your Application Insights connection string seems to have the wrong format. Please check the connection string and try again.");
    }
}
