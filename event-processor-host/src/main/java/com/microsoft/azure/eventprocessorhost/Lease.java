/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/**
 * Lease class is public so that advanced users can implement an ILeaseManager. 
 * Unless you are implementing ILeaseManager you should not have to deal with objects
 * of this class or derived classes directly.
 * <p>
 * When implementing an ILeaseManager it may be necessary to derive from this class to
 * carry around more information and override isExpired. The data fields have been left
 * private instead of protected because they have a full set of getters and setters
 * (except partitionId, which is immutable) which provide equivalent access. When
 * implementing AzureBlobLease, for example, there was no need for more access than
 * the getters and setters provide.
 * <p>
 * Note that a Lease object just carries information about a partition lease. The functionality
 * to acquire/renew/release a lease is all on the ILeaseManager.
 */
public class Lease
{
    private final String partitionId;

    private long epoch;
    private String owner;
    private String token;

    /**
     * Create a Lease for the given partition.
     * 
     * @param partitionId
     */
    public Lease(String partitionId)
    {
        this.partitionId = partitionId;

        this.epoch = 0;
        this.owner = "";
        this.token = "";
    }

    /**
     * Create a Lease by duplicating the given Lease.
     * 
     * @param source
     */
    public Lease(Lease source)
    {
        this.partitionId = source.partitionId;

        this.epoch = source.epoch;
        this.owner = source.owner;
        this.token = source.token;
    }

    /**
     * Epoch is a concept used by Event Hub receivers. Basically, if a receiver is created on a partition
     * with a higher epoch than the existing receiver, the previous receiver is forcibly disconnected.
     * Attempting to create a receiver with a lower epoch that the existing receiver will fail. The Lease
     * carries the epoch around so that when a host steals a lease, it can create a receiver with a higher epoch.
     *  
     * @return
     */
    public long getEpoch()
    {
        return this.epoch;
    }

    /**
     * Set the epoch value.
     * 
     * @param epoch
     */
    public void setEpoch(long epoch)
    {
        this.epoch = epoch;
    }
    
    /**
     * The most common operation on the epoch value is incrementing it after stealing a lease. This
     * convenience function replaces the get-increment-set that would otherwise be required.
     * 
     * @return The new value of the epoch.
     */
    public long incrementEpoch()
    {
    	this.epoch++;
    	return this.epoch;
    }
    
    /**
     * The owner of a lease is the name of the EventProcessorHost which currently holds the lease.
     * 
     * @return
     */
    public String getOwner()
    {
        return this.owner;
    }

    /**
     * Set the owner string. Used when a host steals a lease.
     * 
     * @param owner
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     * Returns the id of the partition that this Lease is for. Immutable so there is no corresponding setter.
     * 
     * @return
     */
    public String getPartitionId()
    {
        return this.partitionId;
    }

    /**
     * The Lease carries an arbitrary string called the "token". AzureStorageCheckpointLeaseManager uses this to
     * store the blob lease ID used by the Azure Storage API. Other implementations of ILeaseManager may use it
     * for anything.
     * 
     * @return
     */
    public String getToken()
    {
        return this.token;
    }

    /**
     * Set the token value.
     * 
     * @param token
     */
    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * If an implementation of ILeaseManager supports the concept of lease expiration, then a class derived from Lease
     * may override this function to inspect the lease and return whether it has expired.
     *  
     * @return true if the lease is expired, false if it is still valid
     * @throws Exception An override which does significant work may need to throw exceptions.
     */
    public boolean isExpired() throws Exception
    {
    	// this function is meaningless in the base class
    	return false;
    }
    
    String getStateDebug()
    {
    	return "N/A";
    }
}
