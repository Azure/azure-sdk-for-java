// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

/**
 * CompleteLease class is public so that advanced users can implement an ILeaseManager.
 * Unless you are implementing ILeaseManager you should not have to deal with objects
 * of this class or derived classes directly.
 * <p>
 * CompleteLease carries around complete information about a lease. By itself, it has the
 * epoch. Any lease manager implementation can derive from this class to add data which
 * the lease manager needs to function -- see AzureBlobLease for an example. Having two
 * distinct classes allows the code to clearly express which variety of lease any variable
 * holds or a method requires, and avoids the problem of accidentally supplying a lightweight
 * BaseLease to a method which needs the lease-manager-specific fields.
 */
public class CompleteLease extends BaseLease {
    protected long epoch = -1; // start with illegal epoch

    /**
     * Do not use; added only for GSon deserializer
     */
    protected CompleteLease() {
        super();
    }

    /**
     * Create a CompleteLease for the given partition.
     *
     * @param partitionId Partition id for this lease.
     */
    public CompleteLease(String partitionId) {
        super(partitionId);
    }

    /**
     * Create a Lease by duplicating the given Lease.
     *
     * @param source Lease to clone.
     */
    public CompleteLease(CompleteLease source) {
        super(source);
        this.epoch = source.epoch;
    }

    /**
     * Epoch is a concept used by Event Hub receivers. If a receiver is created on a partition
     * with a higher epoch than the existing receiver, the previous receiver is forcibly disconnected.
     * Attempting to create a receiver with a lower epoch than the existing receiver will fail. The Lease
     * carries the epoch around so that when a host instance steals a lease, it can create a receiver with a higher epoch.
     *
     * @return the epoch of the current receiver
     */
    public long getEpoch() {
        return this.epoch;
    }

    /**
     * Set the epoch value. Used to update the lease after creating a new receiver with a higher epoch.
     *
     * @param epoch updated epoch value
     */
    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    /**
     * The most common operation on the epoch value is incrementing it after stealing a lease. This
     * convenience function replaces the get-increment-set that would otherwise be required.
     *
     * @return The new value of the epoch.
     */
    public long incrementEpoch() {
        this.epoch++;
        return this.epoch;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
