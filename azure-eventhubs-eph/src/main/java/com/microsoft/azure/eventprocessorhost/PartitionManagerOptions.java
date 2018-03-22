/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/***
 * Options affecting the operation of the partition manager within the event processor host.
 * This class is broken out separately because many of these options also affect the operation
 * of the ILeaseManager and ICheckpointManager implementations, and different implementations
 * may need to subclass and provide different options or defaults. 
 */
public class PartitionManagerOptions {
    /**
     * The default duration after which a partition lease will expire unless renewed.
     */
    public final static int DefaultLeaseDurationInSeconds = 30;

    /**
     * The default duration between lease renewals.
     */
    public final static int DefaultLeaseRenewIntervalInSeconds = 10;

    /**
     * The default timeout for checkpoint operations.
     */
    public final static int DefaultCheckpointTimeoutInSeconds = 120;

    protected int leaseDurationInSeconds = PartitionManagerOptions.DefaultLeaseDurationInSeconds;
    protected int leaseRenewIntervalInSeconds = PartitionManagerOptions.DefaultLeaseRenewIntervalInSeconds;
    protected int checkpointTimeoutInSeconds = PartitionManagerOptions.DefaultCheckpointTimeoutInSeconds;

    /***
     * The base class automatically sets members to the static defaults.
     */
    public PartitionManagerOptions() {
    }

    /**
     * Gets the duration after which a partition lease will expire unless renewed.
     * Defaults to DefaultLeaseDurationInSeconds.
     *
     * @return lease duration
     */
    public int getLeaseDurationInSeconds() {
        return this.leaseDurationInSeconds;
    }

    /**
     * Sets the duration after which a partition lease will expire unless renewed.
     * Must be greater than 0 and should not be less than the renew interval. When using the
     * default, Azure Storage-based ILeaseManager, the duration cannot be greater than 60.
     *
     * @param duration new value for lease duration
     */
    public void setLeaseDurationInSeconds(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Lease duration must be greater than 0");
        }
        this.leaseDurationInSeconds = duration;
    }

    /**
     * Gets the duration between lease renewals. Defaults to DefaultLeaseRenewIntervalInSeconds.
     *
     * @return how often leases are renewed
     */
    public int getLeaseRenewIntervalInSeconds() {
        return this.leaseRenewIntervalInSeconds;
    }

    /**
     * Sets the duration between lease renewals. Must be greater than 0 and less than the current lease duration.
     *
     * @param interval new value for how often leases are renewed
     */
    public void setLeaseRenewIntervalInSeconds(int interval) {
        if ((interval <= 0) || (interval > this.leaseDurationInSeconds)) {
            throw new IllegalArgumentException("Lease renew interval must be greater than 0 and not more than lease duration");
        }
        this.leaseRenewIntervalInSeconds = interval;
    }

    /**
     * Gets the timeout for checkpoint operations. Defaults to DefaultCheckpointTimeoutInSeconds.
     *
     * @return timeout for checkpoint operations
     */
    public int getCheckpointTimeoutInSeconds() {
        return this.checkpointTimeoutInSeconds;
    }

    /**
     * Sets the timeout for checkpoint operations. Must be greater than 0.
     *
     * @param timeout new value for checkpoint timeout
     */
    public void setCheckpointTimeoutInSeconds(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Checkpoint timeout must be greater than 0");
        }
        this.checkpointTimeoutInSeconds = timeout;
    }
}
