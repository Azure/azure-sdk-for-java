package com.azure.storage.common.test.shared.policy;

import java.io.IOException;

public class UncheckedIOExceptionWrapper extends IOException {
    private final IOException cause;

    public UncheckedIOExceptionWrapper(IOException cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public synchronized IOException getCause() {
        return cause;
    }
}
