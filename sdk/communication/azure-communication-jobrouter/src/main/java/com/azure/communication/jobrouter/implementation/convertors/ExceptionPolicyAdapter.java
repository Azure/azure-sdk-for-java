// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicy;
import com.azure.communication.jobrouter.implementation.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;

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

    /**
     * Converts {@link UpdateExceptionPolicyOptions} to {@link ExceptionPolicy}.
     * @param updateExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicy convertUpdateOptionsToExceptionPolicy(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        return new ExceptionPolicy()
            .setName(updateExceptionPolicyOptions.getName())
            .setExceptionRules(updateExceptionPolicyOptions.getExceptionRules());
    }
}
