// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public class FaultInjectionResultBuilders {
    public static FaultInjectionServerErrorResultBuilder getResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        return new FaultInjectionServerErrorResultBuilder(serverErrorType);
    }

    public static FaultInjectionConnectionErrorResultBuilder getResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        return new FaultInjectionConnectionErrorResultBuilder(connectionErrorType);
    }
}
