/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/**
 * Passed as an argument to the general exception handler that can be set via EventProcessorOptions.
 */
public final class ExceptionReceivedEventArgs {
    public static final String NO_ASSOCIATED_PARTITION = "N/A";
    private final String hostname;
    private final Exception exception;
    private final String action;
    private final String partitionId;

    ExceptionReceivedEventArgs(String hostname, Exception exception, String action) {
        this(hostname, exception, action, ExceptionReceivedEventArgs.NO_ASSOCIATED_PARTITION);
    }

    ExceptionReceivedEventArgs(String hostname, Exception exception, String action, String partitionId) {
        this.hostname = hostname;
        this.exception = exception;
        this.action = action;
        if ((partitionId == null) || partitionId.isEmpty()) {
            throw new IllegalArgumentException("PartitionId must not be null or empty");
        }
        this.partitionId = partitionId;
    }

    /**
     * Allows distinguishing the error source if multiple hosts in a single process.
     *
     * @return The name of the host that experienced the exception.
     */
    public String getHostname() {
        return this.hostname;
    }

    /**
     * Returns the exception that was thrown.
     *
     * @return The exception.
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * See EventProcessorHostActionString for a list of possible values.
     *
     * @return A short string that indicates what general activity threw the exception.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * If the error is associated with a particular partition (for example, failed to open the event processor
     * for the partition), the id of the partition. Otherwise, NO_ASSOCIATED_PARTITION.
     *
     * @return A partition id.
     */
    public String getPartitionId() {
        return this.partitionId;
    }
}
