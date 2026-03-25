// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Categorizes exceptions into low-cardinality reason strings for customer-facing SDKStats
 * drop.reason and retry.reason dimensions.
 */
final class CustomerSdkStatsExceptionCategory {

    static final String TIMEOUT_EXCEPTION = "Timeout exception";
    static final String NETWORK_EXCEPTION = "Network exception";
    static final String STORAGE_EXCEPTION = "Storage exception";
    static final String CLIENT_EXCEPTION = "Client exception";

    private static final int MAX_CAUSE_CHAIN_DEPTH = 10;

    /**
     * Returns a low-cardinality exception category string.
     */
    static String categorize(Throwable throwable) {
        if (throwable == null) {
            return CLIENT_EXCEPTION;
        }
        return categorizeByType(throwable);
    }

    /**
     * Returns true if any exception in the cause chain represents a timeout scenario
     * (use CLIENT_TIMEOUT retry code), false otherwise (use CLIENT_EXCEPTION retry code).
     */
    static boolean containsTimeout(Throwable throwable) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < MAX_CAUSE_CHAIN_DEPTH) {
            if (isTimeoutType(current)) {
                return true;
            }
            current = current.getCause();
            depth++;
        }
        return false;
    }

    private static String categorizeByType(Throwable throwable) {
        // Traverse the cause chain to find the most specific category.
        // Each type-check method examines only the current node (no nested traversal)
        // so that priority ordering (timeout > network > storage) is evaluated
        // consistently at each level of the chain.
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < MAX_CAUSE_CHAIN_DEPTH) {
            if (isTimeoutType(current)) {
                return TIMEOUT_EXCEPTION;
            }
            if (isNetworkType(current)) {
                return NETWORK_EXCEPTION;
            }
            if (isStorageType(current)) {
                return STORAGE_EXCEPTION;
            }
            current = current.getCause();
            depth++;
        }
        return CLIENT_EXCEPTION;
    }

    /**
     * Checks whether a single throwable (not its cause chain) is a timeout type.
     */
    private static boolean isTimeoutType(Throwable throwable) {
        if (throwable instanceof SocketTimeoutException || throwable instanceof TimeoutException) {
            return true;
        }
        // Check class name for netty timeout types without creating a hard dependency
        String className = throwable.getClass().getName();
        return className.contains("TimeoutException")
            || className.contains("ReadTimeoutException")
            || className.contains("WriteTimeoutException");
    }

    private static boolean isNetworkType(Throwable throwable) {
        return throwable instanceof UnknownHostException
            || throwable instanceof ConnectException
            || throwable instanceof NoRouteToHostException;
    }

    private static boolean isStorageType(Throwable throwable) {
        // Storage-related IOExceptions typically involve file system operations
        String message = throwable.getMessage();
        if (message == null) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("no space left")
            || lowerMessage.contains("disk full")
            || lowerMessage.contains("read-only file system")
            || lowerMessage.contains("permission denied");
    }

    private CustomerSdkStatsExceptionCategory() {
    }
}
