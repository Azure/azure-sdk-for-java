// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

/***
 * Fault injection result builders.
 * Based on the error type, it will return either {@link  FaultInjectionServerErrorResultBuilder} or {@link FaultInjectionConnectionErrorResultBuilder}.
 */
public final class FaultInjectionResultBuilders {
    /***
     * Get the server error result builder.
     *
     * @param serverErrorType the server error type.
     * @return the fault injection server error builder.
     */
    public static FaultInjectionServerErrorResultBuilder getResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        return new FaultInjectionServerErrorResultBuilder(serverErrorType);
    }

    /***
     * Get the connection error result builder.
     *
     * @param connectionErrorType the connection error type.
     * @return the fault injection connection error builder.
     */
    public static FaultInjectionConnectionErrorResultBuilder getResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        return new FaultInjectionConnectionErrorResultBuilder(connectionErrorType);
    }
}
