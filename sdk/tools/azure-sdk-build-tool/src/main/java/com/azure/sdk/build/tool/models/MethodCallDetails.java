package com.azure.sdk.build.tool.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MethodCallDetails {
    @JsonProperty
    private String methodName;

    @JsonProperty
    private int callFrequency;

    public String getMethodName() {
        return methodName;
    }

    public MethodCallDetails setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public int getCallFrequency() {
        return callFrequency;
    }

    public MethodCallDetails setCallFrequency(int callFrequency) {
        this.callFrequency = callFrequency;
        return this;
    }
}
