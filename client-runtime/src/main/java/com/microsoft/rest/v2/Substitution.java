package com.microsoft.rest.v2;

public class Substitution {
    private final String urlParameterName;
    private final int methodParameterIndex;
    private final boolean shouldEncode;

    public Substitution(String urlParameterName, int methodParameterIndex, boolean shouldEncode) {
        this.urlParameterName = urlParameterName;
        this.methodParameterIndex = methodParameterIndex;
        this.shouldEncode = shouldEncode;
    }

    public String getURLParameterName() {
        return urlParameterName;
    }

    public int getMethodParameterIndex() {
        return methodParameterIndex;
    }

    public boolean shouldEncode() {
        return shouldEncode;
    }
}
