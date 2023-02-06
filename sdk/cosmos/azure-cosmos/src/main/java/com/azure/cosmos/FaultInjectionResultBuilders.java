// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FaultInjectionConnectionErrorType;
import com.azure.cosmos.models.FaultInjectionServerErrorType;

public class FaultInjectionResultBuilders {
    public static FaultInjectionServerErrorResultBuilder getResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        return new FaultInjectionServerErrorResultBuilder(serverErrorType);
    }

    public static FaultInjectionConnectionErrorResultBuilder getResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        return new FaultInjectionConnectionErrorResultBuilder(connectionErrorType);
    }
}
