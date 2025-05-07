// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.changefeed;

public class SplitTimeoutException extends RuntimeException {
    public SplitTimeoutException(String message) {
        super(message);
    }
}
