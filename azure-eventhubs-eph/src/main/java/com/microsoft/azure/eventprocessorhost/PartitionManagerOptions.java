/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

public class PartitionManagerOptions
{
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
	
	public PartitionManagerOptions()
	{
	}

	/**
	 * Gets the duration after which a partition lease will expire unless renewed.
	 * Defaults to DefaultLeaseDurationInSeconds.
	 *
	 * @return
	 */
	public int getLeaseDurationInSeconds() { return this.leaseDurationInSeconds; }
	
	/**
	 * Sets the duration after which a partition lease will expire unless renewed.
	 * Must be greater than 0 and should not be less than the renew interval. When using the
	 * default, Azure Storage-based ILeaseManager, the duration cannot be greater than 60.
	 *
	 * @param duration
	 */
	public void setLeaseDurationInSeconds(int duration)
	{
		if (duration <= 0)
		{
			throw new IllegalArgumentException("Lease duration must be greater than 0");
		}
		this.leaseDurationInSeconds = duration;
	}
	
	/**
	 * Gets the duration between lease renewals. Defaults to DefaultLeaseRenewIntervalInSeconds.
	 *
	 * @return
	 */
	public int getLeaseRenewIntervalInSeconds() { return this.leaseRenewIntervalInSeconds; }
	
	/**
	 * Sets the duration between lease renewals. Must be greater than 0 and less than the current lease duration.
	 *
	 * @param interval
	 */
	public void setLeaseRenewIntervalInSeconds(int interval)
	{
		if ((interval <= 0) || (interval > this.leaseDurationInSeconds))
		{
			throw new IllegalArgumentException("Lease renew interval must be greater than 0 and not more than lease duration");
		}
		this.leaseRenewIntervalInSeconds = interval;
	}
	
	/**
	 * Gets the timeout for checkpoint operations. Defaults to DefaultCheckpointTimeoutInSeconds.
	 *
	 * @return
	 */
	public int getCheckpointTimeoutInSeconds() { return this.checkpointTimeoutInSeconds; }
	
	/**
	 * Sets the timeout for checkpoint operations. Must be greater than 0.
	 *
	 * @param timeout
	 */
	public void setCheckpointTimeoutInSeconds(int timeout)
	{
		if (timeout <= 0)
		{
			throw new IllegalArgumentException("Checkpoint timeout must be greater than 0");
		}
		this.checkpointTimeoutInSeconds = timeout;
	}
}
