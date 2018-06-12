/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/**
 * This class is used by ILeaseManager implementations to compactly return the state of leases.
 */
public class LeaseStateInfo implements Comparable<LeaseStateInfo> {
	private final String partitionId;
	private final String owner;
	private final boolean isOwned;
	
	private Lease lease = null;

	public LeaseStateInfo(String partitionId, String owner, boolean isOwned) {
		this.partitionId = partitionId;
		this.owner = owner;
		this.isOwned = isOwned;
	}
	
	/**
	 * Get the partition id of the lease.
	 * 
	 * @return partition id
	 */
	public String getPartitionId() {
		return this.partitionId;
	}
	
	/**
	 * Get the current owner of the lease, which is the host name of an EventProcessorHost instance. May be empty or null if not owned.
	 * 
	 * @return owner host name, or empty or null
	 */
	public String getOwner() {
		return this.owner;
	}
	
	/**
	 * Get the owned state of the lease.
	 * 
	 * @return true if the lease is owned, or false if it is not
	 */
	public boolean isOwned() {
		return this.isOwned;
	}
	
	/**
	 * Annotate the LeaseStateInfo with the corresponding Lease object.
	 * 
	 * @param lease Lease object for this lease
	 */
	public void setLease(Lease lease) {
		this.lease = lease;
	}
	
	/**
	 * Returns the Lease object previously set with setLease, or null.
	 * 
	 * @return object from most recent setLease, or null if not set
	 */
	public Lease getLease() {
		return this.lease;
	}

	// Compares by partition id
	@Override
	public int compareTo(LeaseStateInfo other) {
		return this.partitionId.compareTo(other.getPartitionId());
	}
}
