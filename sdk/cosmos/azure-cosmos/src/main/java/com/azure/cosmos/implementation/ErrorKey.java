package com.azure.cosmos.implementation;

public class ErrorKey {
    private final int statusCode;
    private final int subStatusCode;

    public ErrorKey(int statusCode, int subStatusCode) {
        this.statusCode = statusCode;
        this.subStatusCode = subStatusCode;
    }
}
