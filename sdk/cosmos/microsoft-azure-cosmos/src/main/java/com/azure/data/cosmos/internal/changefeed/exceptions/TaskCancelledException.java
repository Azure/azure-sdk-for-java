// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.exceptions;

/**
 * Exception occurred when an operation in a ChangeFeedObserver was canceled.
 */
public class TaskCancelledException extends RuntimeException {
    private static final String DefaultMessage = "Operations were canceled.";

    /**
     * Initializes a new instance of the {@link TaskCancelledException} class.
     */
    public TaskCancelledException() {
        super(DefaultMessage);
    }
}
