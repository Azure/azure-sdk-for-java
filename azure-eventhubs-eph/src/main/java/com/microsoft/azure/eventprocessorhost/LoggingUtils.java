/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/**
 * Centralize log message generation
 */
public final class LoggingUtils {
    static String withHost(String hostName, String logMessage) {
        return "host " + hostName + ": " + logMessage;
    }

    static String withHostAndPartition(String hostName, String partitionId, String logMessage) {
        return "host " + hostName + ": " + partitionId + ": " + logMessage;
    }

    static String withHostAndPartition(String hostName, PartitionContext context, String logMessage) {
        return "host " + hostName + ": " + context.getPartitionId() + ": " + logMessage;
    }
}
