package com.azure.perf.test.core;

public class MockErrorContext {
    private Throwable throwable;

    public MockErrorContext(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
