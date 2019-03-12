// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

/**
 * This exception is thrown for the following reasons:
 * <ul>
 * <li> When the entity user attempted to connect does not exist
 * <li> The entity user wants to connect is disabled
 * </ul>
 *
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class IllegalEntityException extends EventHubException {
    private static final long serialVersionUID = 1842057379278310290L;

    // TEST HOOK - to be used by unit tests to inject non-transient failures
    private static volatile boolean isTransient = false;

    IllegalEntityException() {
        super(isTransient);
    }

    public IllegalEntityException(final String message) {
        super(isTransient, message);
    }

    public IllegalEntityException(final Throwable cause) {
        super(isTransient, cause);
    }

    public IllegalEntityException(final String message, final Throwable cause) {
        super(isTransient, message, cause);
    }
}
