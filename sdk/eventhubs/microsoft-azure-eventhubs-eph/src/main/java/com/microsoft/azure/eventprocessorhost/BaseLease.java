// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.Objects;

/**
 * BaseLease class is public so that advanced users can implement an ILeaseManager.
 * Unless you are implementing ILeaseManager you should not have to deal with objects
 * of this class or derived classes directly.
 * <p>
 * This lightweight base exists to allow ILeaseManager.getAllLeases to operate as quickly
 * as possible -- for some lease manager implementations, loading the entire contents of a
 * lease form the store may be expensive. BaseLease contains only the minimum amount of
 * information required to allow PartitionScanner to operate.
 * <p>
 * Note that a Lease object just carries information about a partition lease. The APIs
 * to acquire/renew/release a lease are all on ILeaseManager.
 */
public class BaseLease implements Comparable<BaseLease> {
    private final String partitionId;
    private String owner = "";
    private transient boolean isOwned = false; // do not serialize

    /**
     * Do not use; added only for GSon deserializer
     */
    protected BaseLease() {
        partitionId = "-1";
    }

    /**
     * Create a BaseLease for the given partition.
     *
     * @param partitionId Partition id for this lease.
     */
    public BaseLease(String partitionId) {
        Objects.requireNonNull(partitionId, "'partitionId' cannot be null.");
        if (partitionId.isEmpty()) {
            throw new IllegalArgumentException("partitionId is Empty");
        }
        this.partitionId = partitionId;
    }

    /**
     * Create and populate a BaseLease for the given partition.
     *
     * @param partitionId Partition id for this lease.
     * @param owner Current owner of this lease, or empty.
     * @param isOwned True if the lease is owned, false if not.
     */
    public BaseLease(String partitionId, String owner, boolean isOwned) {
        Objects.requireNonNull(partitionId, "'partitionId' cannot be null.");
        if (partitionId.isEmpty()) {
            throw new IllegalArgumentException("partitionId is Empty");
        }
        this.partitionId = partitionId;
        this.owner = owner;
        this.isOwned = isOwned;
    }

    /**
     * Create a BaseLease by duplicating the given Lease.
     *
     * @param source BaseLease to clone.
     */
    public BaseLease(BaseLease source) {
        Objects.requireNonNull(source.partitionId, "'source.partitionId' cannot be null.");
        if (source.partitionId.isEmpty()) {
            throw new IllegalArgumentException("partitionId is Empty");
        }
        this.partitionId = source.partitionId;
        this.owner = source.owner;
        this.isOwned = source.isOwned;
    }

    /**
     * The owner of a lease is the name of the EventProcessorHost instance which currently holds the lease.
     *
     * @return name of the owning instance
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Set the owner string. Used when a host steals a lease.
     *
     * @param owner name of the new owning instance
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Set the owned state of the lease.
     *
     * @param newState true if the lease is owned, or false if it is not
     */
    public void setIsOwned(boolean newState) {
        this.isOwned = newState;
    }

    /**
     * Get the owned state of the lease.
     *
     * @return true if the lease is owned, or false if it is not
     */
    public boolean getIsOwned() {
        return this.isOwned;
    }

    /**
     * Convenience function for comparing possibleOwner against this.owner
     *
     * @param possibleOwner name to check
     * @return true if possibleOwner is the same as this.owner, false otherwise
     */
    public boolean isOwnedBy(String possibleOwner) {
        boolean retval = false;
        if (this.owner != null) {
            retval = (this.owner.compareTo(possibleOwner) == 0);
        }
        return retval;
    }

    /**
     * Returns the id of the partition that this Lease is for. Immutable so there is no corresponding setter.
     *
     * @return partition id
     */
    public String getPartitionId() {
        return this.partitionId;
    }

    // Compares by partition id
    @Override
    public int compareTo(BaseLease other) {
        return this.partitionId.compareTo(other.getPartitionId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseLease baseLease = (BaseLease) o;
        return Objects.equals(partitionId, baseLease.partitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionId);
    }

    String getStateDebug() {
        return "N/A";
    }
}
