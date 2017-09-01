package com.microsoft.azure.eventprocessorhost;

/**
 * Centralize log message generation
 */
public final class LoggingUtils {
    public static String withHost(String hostName, String logMessage) {
        return "host " + hostName + ": " + logMessage;
    }

    public static String withHostAndPartition(String hostName, String partitionId, String logMessage) {
        return "host " + hostName + ": " + partitionId + ": " + logMessage;
    }

    public static String withHostAndPartition(String hostName, PartitionContext context, String logMessage) {
        return "host " + hostName + ": " + context.getPartitionId() + ": " + logMessage;
    }
}
