// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

// This class is never thrown into user code, so it can be package private.
class ExceptionWithAction extends Exception {
    private static final long serialVersionUID = 7480590197418857145L;

    private final String action;

    ExceptionWithAction(Throwable e, String action) {
        super(e);
        this.action = action;
    }

    ExceptionWithAction(Throwable e, String message, String action) {
        super(message, e);
        this.action = action;
    }

    String getAction() {
        return this.action;
    }
}
