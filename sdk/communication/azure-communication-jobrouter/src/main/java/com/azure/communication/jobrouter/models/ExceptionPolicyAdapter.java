package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionPolicy;

public class ExceptionPolicyAdapter {
    public static ExceptionPolicy convertCreateOptionsToExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return new ExceptionPolicy()
            .setName(createExceptionPolicyOptions.getName())
            .setExceptionRules(createExceptionPolicyOptions.getExceptionRules());
    }

    public static ExceptionPolicy convertUpdateOptionsToExceptionPolicy(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        return new ExceptionPolicy()
            .setName(updateExceptionPolicyOptions.getName())
            .setExceptionRules(updateExceptionPolicyOptions.getExceptionRules());
    }
}
