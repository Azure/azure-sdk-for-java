/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed;

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
