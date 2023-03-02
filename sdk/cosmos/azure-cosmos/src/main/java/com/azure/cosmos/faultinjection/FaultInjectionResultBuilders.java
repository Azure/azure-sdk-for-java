// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

/***
 * Fault injection result builders.
 * Based on the error type, it will return either {@link  FaultInjectionServerErrorResultBuilder} or {@link FaultInjectionConnectionErrorResultBuilder}.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class FaultInjectionResultBuilders {
    /***
     * Get the server error result builder.
     *
     * @param serverErrorType the server error type.
     * @return the fault injection server error builder.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static FaultInjectionServerErrorResultBuilder getResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        return new FaultInjectionServerErrorResultBuilder(serverErrorType);
    }

    /***
     * Get the connection error result builder.
     *
     * @param connectionErrorType the connection error type.
     * @return the fault injection connection error builder.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static FaultInjectionConnectionErrorResultBuilder getResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        return new FaultInjectionConnectionErrorResultBuilder(connectionErrorType);
    }
}
