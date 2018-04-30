/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

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
	
	public String getPartitionId() {
		return this.partitionId;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public boolean isOwned() {
		return this.isOwned;
	}
	
	public void setLease(Lease lease) {
		this.lease = lease;
	}
	
	public Lease getLease() {
		return this.lease;
	}

	// Compares by partition id
	@Override
	public int compareTo(LeaseStateInfo other) {
		return this.partitionId.compareTo(other.getPartitionId());
	}
}
