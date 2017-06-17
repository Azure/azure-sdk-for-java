/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

public class PartitionManagerOptions
{
	public final static int DefaultLeaseDurationInSeconds = 30;
	public final static int DefaultLeaseRenewIntervalInSeconds = 10;
	public final static int DefaultCheckpointTimeoutInSeconds = 120;
	
	protected int leaseDurationInSeconds = PartitionManagerOptions.DefaultLeaseDurationInSeconds;
	protected int leaseRenewIntervalInSeconds = PartitionManagerOptions.DefaultLeaseRenewIntervalInSeconds;
	protected int checkpointTimeoutInSeconds = PartitionManagerOptions.DefaultCheckpointTimeoutInSeconds;
	
	public PartitionManagerOptions()
	{
	}
	
	public int getLeaseDurationInSeconds() { return this.leaseDurationInSeconds; }
	
	public void setLeaseDurationInSeconds(int duration)
	{
		if (duration <= 0)
		{
			throw new IllegalArgumentException("Lease duration must be greater than 0");
		}
		this.leaseDurationInSeconds = duration;
	}
	
	public int getLeaseRenewIntervalInSeconds() { return this.leaseRenewIntervalInSeconds; }
	
	public void setLeaseRenewIntervalInSeconds(int interval)
	{
		if ((interval <= 0) || (interval > this.leaseDurationInSeconds))
		{
			throw new IllegalArgumentException("Lease renew interval must be greater than 0 and not more than lease duration");
		}
		this.leaseRenewIntervalInSeconds = interval;
	}
	
	public int getCheckpointTimeoutInSeconds() { return this.checkpointTimeoutInSeconds; }
	
	public void setCheckpointTimeoutInSeconds(int timeout)
	{
		if (timeout <= 0)
		{
			throw new IllegalArgumentException("Checkpoint timeout must be greater than 0");
		}
		this.checkpointTimeoutInSeconds = timeout;
	}
}
