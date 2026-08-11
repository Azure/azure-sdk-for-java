// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class InCompleteRoutingMapException extends RuntimeException {
    private final String message;
    public InCompleteRoutingMapException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
