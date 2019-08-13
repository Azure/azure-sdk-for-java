package com.azure.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.UnknownHostException;

public class NetworkCallException {
    @JsonProperty("Throwable")
    private Throwable throwable;

    @JsonProperty("ClassName")
    private String className;

    public NetworkCallException() {
    }

    public NetworkCallException(Throwable throwable) {
        this.throwable = throwable;
        this.className = throwable.getClass().getName();
    }

    public Throwable get() {
        switch (className) {
            case "java.lang.NullPointerException":
                return new NullPointerException(throwable.getMessage());

            case "java.lang.IndexOutOfBoundsException":
                return new IndexOutOfBoundsException(throwable.getMessage());

            case "java.net.UnknownHostException":
                return new UnknownHostException(throwable.getMessage());

            default:
                return throwable;
        }
    }

    public void throwable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void className(String className) {
        this.className = className;
    }
}
