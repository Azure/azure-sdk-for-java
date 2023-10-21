// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionPolicy;

/**
 * Converts request options for create and update Exception Policy to {@link ExceptionPolicy}.
 */
public class ExceptionPolicyAdapter {
    /**
     * Converts {@link CreateExceptionPolicyOptions} to {@link ExceptionPolicy}.
     * @param createExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicy convertCreateOptionsToExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return new ExceptionPolicy()
            .setName(createExceptionPolicyOptions.getName())
            .setExceptionRules(createExceptionPolicyOptions.getExceptionRules());
    }
}
