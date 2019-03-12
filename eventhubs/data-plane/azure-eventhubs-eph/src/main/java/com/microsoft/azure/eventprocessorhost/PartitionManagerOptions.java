// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
    public static final int DEFAULT_LEASE_DURATION_IN_SECONDS = 30;

    /**
     * The default duration between lease renewals.
     */
    public static final int DEFAULT_LEASE_RENEW_INTERVAL_IN_SECONDS = 10;

    /**
     * The default timeout for checkpoint operations.
     */
    public static final int DEFAULT_CHECKPOINT_TIMEOUT_IN_SECONDS = 120;

    public static final int DEFAULT_STARTUP_SCAN_DELAY_IN_SECONDS = 30;
    public static final int DEFAULT_FAST_SCAN_INTERVAL_IN_SECONDS = 3;
    public static final int DEFAULT_SLOW_SCAN_INTERVAL_IN_SECONDS = 5;

    protected int leaseDurationInSeconds = PartitionManagerOptions.DEFAULT_LEASE_DURATION_IN_SECONDS;
    protected int leaseRenewIntervalInSeconds = PartitionManagerOptions.DEFAULT_LEASE_RENEW_INTERVAL_IN_SECONDS;
    protected int checkpointTimeoutInSeconds = PartitionManagerOptions.DEFAULT_CHECKPOINT_TIMEOUT_IN_SECONDS;

    protected int startupScanDelayInSeconds = PartitionManagerOptions.DEFAULT_STARTUP_SCAN_DELAY_IN_SECONDS;
    protected int fastScanIntervalInSeconds = PartitionManagerOptions.DEFAULT_FAST_SCAN_INTERVAL_IN_SECONDS;
    protected int slowScanIntervalInSeconds = PartitionManagerOptions.DEFAULT_SLOW_SCAN_INTERVAL_IN_SECONDS;

    /***
     * The base class automatically sets members to the static defaults.
     */
    public PartitionManagerOptions() {
    }

    /**
     * Gets the duration after which a partition lease will expire unless renewed.
     * Defaults to DEFAULT_LEASE_DURATION_IN_SECONDS.
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
     * Gets the duration between lease renewals. Defaults to DEFAULT_LEASE_RENEW_INTERVAL_IN_SECONDS.
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
     * Gets the timeout for checkpoint operations. Defaults to DEFAULT_CHECKPOINT_TIMEOUT_IN_SECONDS.
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

    /**
     * Gets the delay time between the first scan for available partitions and the second. This is
     * part of a startup optimization which allows individual hosts to become visible to other
     * hosts, and thereby get a more accurate count of the number of hosts in the system, before
     * they try to estimate how many partitions they should own.
     * 
     * Defaults to DEFAULT_STARTUP_SCAN_DELAY_IN_SECONDS.
     * 
     * @return delay time in seconds
     */    
    public int getStartupScanDelayInSeconds() {
        return this.startupScanDelayInSeconds;
    }

    /**
     * Sets the delay time in seconds between the first scan and the second.
     * 
     * @param delay  new delay time in seconds
     */    
    public void setStartupScanDelayInSeconds(int delay) {
        if (delay <= 0) {
            throw new IllegalArgumentException("Startup scan delay must be greater than 0");
        }
        this.startupScanDelayInSeconds = delay;
    }

    /**
     * There are two possible interval times between scans for available partitions, fast and slow.
     * The fast (short) interval is used after a scan in which lease stealing has occurred, to
     * promote quicker rebalancing.
     * 
     * Defaults to DEFAULT_FAST_SCAN_INTERVAL_IN_SECONDS.
     * 
     * @return interval time in seconds
     */
    public int getFastScanIntervalInSeconds() {
        return this.fastScanIntervalInSeconds;
    }

    /**
     * Sets the time for fast interval.
     * 
     * @param interval  new fast interval in seconds
     */
    public void setFastScanIntervalInSeconds(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Fast scan interval must be greater than 0");
        }
        this.fastScanIntervalInSeconds = interval;
    }

    /**
     * The slow (long) interval is used after a scan in which lease stealing did not occur, to
     * reduce unnecessary scanning when the system is in steady state.
     * 
     * Defaults to DEFAULT_SLOW_SCAN_INTERVAL_IN_SECONDS.
     * 
     * @return interval time in seconds
     */
    public int getSlowScanIntervalInSeconds() {
        return this.slowScanIntervalInSeconds;
    }

    /**
     * Sets the time for slow interval.
     * 
     * @param interval  new slow interval in seconds
     */
    public void setSlowScanIntervalInSeconds(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Slow scan interval must be greater than 0");
        }
        this.slowScanIntervalInSeconds = interval;
    }
}
