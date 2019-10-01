// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

/**
 * A record used in the health monitoring.
 */
public class HealthMonitoringRecord {
    public final HealthSeverity severity;
    public final MonitoredOperation operation;
    public final Lease lease;
    public final Throwable throwable;

    /**
     * Initializes a new instance of the {@link HealthMonitoringRecord} class.
     *
     * @param severity the health severity level.
     * @param operation the operation.
     * @param lease the lease.
     * @param throwable the exception.
     */
    public HealthMonitoringRecord(HealthSeverity severity, MonitoredOperation operation, Lease lease, Throwable throwable) {
        if (lease == null) throw new IllegalArgumentException("lease");
        this.severity = severity;
        this.operation = operation;
        this.lease = lease;
        this.throwable = throwable;
    }

    /**
     * @return the severity of this monitoring record.
     */
    public HealthSeverity getSeverity() {
        return this.severity;
    }

    /**
     * The health severity level.
     */
    public enum HealthSeverity {
        /**
         * Critical level.
         */
        CRITICAL(10),

        /**
         * Error level.
         */
        ERROR(20),

        /**
         * Information level.
         */
        INFORMATIONAL(30);

        public final int value;

        HealthSeverity(int value){
            this.value = value;
        }
    }

    /**
     * The health monitoring phase.
     */
    public enum MonitoredOperation {
        /**
         * A phase when the instance tries to acquire the lease.
         */
        ACQUIRE_LEASE,
    }
}
