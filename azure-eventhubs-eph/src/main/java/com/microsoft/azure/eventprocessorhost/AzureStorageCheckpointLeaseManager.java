/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.DeleteSnapshotsOption;
import com.microsoft.azure.storage.blob.LeaseState;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class AzureStorageCheckpointLeaseManager implements ICheckpointManager, ILeaseManager
{
    private EventProcessorHost host;
    private final String storageConnectionString;
    private String storageContainerName;
    private String storageBlobPrefix;
    
    private CloudBlobClient storageClient;
    private CloudBlobContainer eventHubContainer;
    private CloudBlobDirectory consumerGroupDirectory;
    
    private Gson gson;
    
    private final BlobRequestOptions leaseOperationOptions = new BlobRequestOptions();
    private final BlobRequestOptions checkpointOperationOptions = new BlobRequestOptions();
    private final BlobRequestOptions renewRequestOptions = new BlobRequestOptions();
    
    private enum UploadActivity { Create, Acquire, Release, Update };

    private Hashtable<String, Checkpoint> latestCheckpoint = new Hashtable<String, Checkpoint>();

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(AzureStorageCheckpointLeaseManager.class);

    AzureStorageCheckpointLeaseManager(String storageConnectionString)
    {
        this(storageConnectionString, null);
    }
    
    AzureStorageCheckpointLeaseManager(String storageConnectionString, String storageContainerName)
    {
    	this(storageConnectionString, storageContainerName, "");
    }

    AzureStorageCheckpointLeaseManager(String storageConnectionString, String storageContainerName, String storageBlobPrefix)
    {
    	if ((storageConnectionString == null) || storageConnectionString.trim().isEmpty())
		{
    		throw new IllegalArgumentException("Provide valid Azure Storage connection string when using Azure Storage");
		}
        this.storageConnectionString = storageConnectionString;
        
        if ((storageContainerName != null) && storageContainerName.trim().isEmpty())
        {
        	throw new IllegalArgumentException("Azure Storage container name must be a valid container name or null to use the default");
        }
        this.storageContainerName = storageContainerName;
        
        // Convert all-whitespace prefix to empty string. Convert null prefix to empty string.
        // Then the rest of the code only has one case to worry about.
        this.storageBlobPrefix = (storageBlobPrefix != null) ? storageBlobPrefix.trim() : "";
    }

    // The EventProcessorHost can't pass itself to the AzureStorageCheckpointLeaseManager constructor
    // because it is still being constructed. Do other initialization here also because it might throw and
    // hence we don't want it in the constructor.
    void initialize(EventProcessorHost host) throws InvalidKeyException, URISyntaxException, StorageException
    {
        this.host = host;
        if (this.storageContainerName == null)
        {
        	this.storageContainerName = this.host.getEventHubPath();

            // Validate that the event hub name is also a legal storage container name.
            // Regex pattern is copied from .NET version. The syntax for Java regexes seems to be the same.
            // Error message is also copied from .NET version.
            Pattern p = Pattern.compile("^(?-i)(?:[a-z0-9]|(?<=[0-9a-z])-(?=[0-9a-z])){3,63}$");
            Matcher m = p.matcher(this.storageContainerName);
            if (!m.find())
            {
                 throw new IllegalArgumentException("EventHub names must conform to the following rules to be able to use it with EventProcessorHost: " +
                    "Must start with a letter or number, and can contain only letters, numbers, and the dash (-) character. " +
                    "Every dash (-) character must be immediately preceded and followed by a letter or number; consecutive dashes are not permitted in container names. " +
                    "All letters in a container name must be lowercase. " +
                    "Must be from 3 to 63 characters long.");
            }
        }
        
        this.storageClient = CloudStorageAccount.parse(this.storageConnectionString).createCloudBlobClient();
        
        this.eventHubContainer = this.storageClient.getContainerReference(this.storageContainerName);
        
        // storageBlobPrefix is either empty or a real user-supplied string. Either way we can just
        // stick it on the front and get the desired result. 
        this.consumerGroupDirectory = this.eventHubContainer.getDirectoryReference(this.storageBlobPrefix + this.host.getConsumerGroupName());
        
        this.gson = new Gson();

        this.leaseOperationOptions.setMaximumExecutionTimeInMs(host.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000);
        this.storageClient.setDefaultRequestOptions(this.leaseOperationOptions);
        this.checkpointOperationOptions.setMaximumExecutionTimeInMs(host.getPartitionManagerOptions().getCheckpointTimeoutInSeconds() * 1000);
        // The only option that .NET sets on renewRequestOptions is ServerTimeout, which doesn't exist in Java equivalent.
        // Keep it separate in case we need to change something later.
        // Only used for leases, not checkpoints, so set max execution time to lease value
        this.renewRequestOptions.setMaximumExecutionTimeInMs(host.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000);
    }
    
    
    //
    // In this implementation, checkpoints are data that's actually in the lease blob, so checkpoint operations
    // turn into lease operations under the covers.
    //
    
    @Override
    public Future<Boolean> checkpointStoreExists()
    {
        return leaseStoreExists(this.checkpointOperationOptions);
    }

    @Override
    public Future<Boolean> createCheckpointStoreIfNotExists()
    {
        return createLeaseStoreIfNotExists(this.checkpointOperationOptions);
    }
    
    @Override
    public Future<Boolean> deleteCheckpointStore()
    {
    	return deleteLeaseStore(this.checkpointOperationOptions);
    }

    @Override
    public Future<Checkpoint> getCheckpoint(String partitionId)
    {
        return this.host.getExecutorService().submit(() -> getCheckpointSync(partitionId));
    }
    
    private Checkpoint getCheckpointSync(String partitionId) throws URISyntaxException, IOException, StorageException
    {
    	AzureBlobLease lease = getLeaseSync(partitionId, this.checkpointOperationOptions);
    	Checkpoint checkpoint = null;
    	if (lease.getOffset() != null)
    	{
	    	checkpoint = new Checkpoint(partitionId);
	    	checkpoint.setOffset(lease.getOffset());
	    	checkpoint.setSequenceNumber(lease.getSequenceNumber());
    	}
    	// else offset is null meaning no checkpoint stored for this partition so return null
    	return checkpoint;
    }

    @Override
    public Future<Checkpoint> createCheckpointIfNotExists(String partitionId)
    {
        return this.host.getExecutorService().submit(() -> createCheckpointIfNotExistsSync(partitionId));
    }
    
    private Checkpoint createCheckpointIfNotExistsSync(String partitionId) throws Exception
    {
    	// Normally the lease will already be created, checkpoint store is initialized after lease store.
    	AzureBlobLease lease = createLeaseIfNotExistsSync(partitionId, this.checkpointOperationOptions);
    	
    	Checkpoint checkpoint = null;
    	if (lease.getOffset() != null)
    	{
    		checkpoint = new Checkpoint(partitionId, lease.getOffset(), lease.getSequenceNumber());
    	}
    	
    	return checkpoint;
    }

    @Deprecated
    @Override
    public Future<Void> updateCheckpoint(Checkpoint checkpoint)
    {
        throw new RuntimeException("Use updateCheckpoint(checkpoint, lease) instead.");
    }
    
    @Override
    public Future<Void> updateCheckpoint(Lease lease, Checkpoint checkpoint)
    {
        return this.host.getExecutorService().submit(() -> updateCheckpointSync(lease, checkpoint));
    }
    
    private Void updateCheckpointSync(Lease lease, Checkpoint checkpoint) throws Exception
    {
    	AzureBlobLease updatedLease = new AzureBlobLease((AzureBlobLease) lease);

        TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), checkpoint.getPartitionId(),
                "Checkpointing at " + checkpoint.getOffset() + " // " + checkpoint.getSequenceNumber()));
    	updatedLease.setOffset(checkpoint.getOffset());
    	updatedLease.setSequenceNumber(checkpoint.getSequenceNumber());
    	updateLeaseSync(updatedLease, this.checkpointOperationOptions);
    	return null;
    }

    @Override
    public Future<Void> deleteCheckpoint(String partitionId)
    {
        return this.host.getExecutorService().submit(() -> deleteCheckpointSync(partitionId));
    }
    
    private Void deleteCheckpointSync(String partitionId) throws Exception
    {
    	// "Delete" a checkpoint by changing the offset to null, so first we need to fetch the most current lease
    	AzureBlobLease lease = getLeaseSync(partitionId, this.checkpointOperationOptions);
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "Deleting checkpoint for " + partitionId));
    	lease.setOffset(null);
    	lease.setSequenceNumber(0L);
    	updateLeaseSync(lease, this.checkpointOperationOptions);
        return null;
    }

    
    //
    // Lease operations.
    //

    @Override
    public int getLeaseRenewIntervalInMilliseconds()
    {
    	return this.host.getPartitionManagerOptions().getLeaseRenewIntervalInSeconds() * 1000;
    }
    
    @Override
    public int getLeaseDurationInMilliseconds()
    {
    	return this.host.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000;
    }
    
    @Override
    public Future<Boolean> leaseStoreExists()
    {
        return leaseStoreExists(this.leaseOperationOptions);
    }
    
    private Future<Boolean> leaseStoreExists(BlobRequestOptions options)
    {
        return this.host.getExecutorService().submit(() -> this.eventHubContainer.exists(null, options, null));
    }

    @Override
    public Future<Boolean> createLeaseStoreIfNotExists()
    {
        return createLeaseStoreIfNotExists(this.leaseOperationOptions);
    }
    
    private Future<Boolean> createLeaseStoreIfNotExists(BlobRequestOptions options)
    {
        return this.host.getExecutorService().submit(() -> this.eventHubContainer.createIfNotExists(options, null));
    }

    @Override
    public Future<Boolean> deleteLeaseStore()
    {
        return this.host.getExecutorService().submit(() -> deleteLeaseStoreSync(this.leaseOperationOptions));
    }
    
    private Future<Boolean> deleteLeaseStore(BlobRequestOptions options)
    {
        return this.host.getExecutorService().submit(() -> deleteLeaseStoreSync(options));
    }
    
    private Boolean deleteLeaseStoreSync(BlobRequestOptions options)
    {
    	boolean retval = true;
    	
    	for (ListBlobItem blob : this.eventHubContainer.listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), options, null))
    	{
    		if (blob instanceof CloudBlobDirectory)
    		{
    			try
    			{
					for (ListBlobItem subBlob : ((CloudBlobDirectory)blob).listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), options, null))
					{
						((CloudBlockBlob)subBlob).deleteIfExists(DeleteSnapshotsOption.NONE, null, options, null);
					}
				}
    			catch (StorageException | URISyntaxException e)
    			{
    				TRACE_LOGGER.warn(LoggingUtils.withHost(this.host.getHostName(), "Failure while deleting lease store"), e);
    				retval = false;
				}
    		}
    		else if (blob instanceof CloudBlockBlob)
    		{
    			try
    			{
					((CloudBlockBlob)blob).deleteIfExists(DeleteSnapshotsOption.NONE, null, options, null);
				}
    			catch (StorageException e)
    			{
    			    TRACE_LOGGER.warn(LoggingUtils.withHost(this.host.getHostName(), "Failure while deleting lease store"), e);
    				retval = false;
				}
    		}
    	}
    	
    	try
    	{
			this.eventHubContainer.deleteIfExists(null, options, null);
		}
    	catch (StorageException e)
    	{
			TRACE_LOGGER.warn(LoggingUtils.withHost(this.host.getHostName(), "Failure while deleting lease store"), e);
			retval = false;
		}
    	
    	return retval;
    }
    
    @Override
    public Future<Lease> getLease(String partitionId)
    {
        return this.host.getExecutorService().submit(() -> getLeaseSync(partitionId, this.leaseOperationOptions));
    }
    
    private AzureBlobLease getLeaseSync(String partitionId, BlobRequestOptions options) throws URISyntaxException, IOException, StorageException
    {
    	AzureBlobLease retval = null;
    	
		CloudBlockBlob leaseBlob = this.consumerGroupDirectory.getBlockBlobReference(partitionId); // getBlockBlobReference does not take options
		if (leaseBlob.exists(null, options, null))
		{
			retval = downloadLease(leaseBlob, options);
		}

    	return retval;
    }

    @Override
    public Iterable<Future<Lease>> getAllLeases() throws IllegalEntityException
    {
        ArrayList<Future<Lease>> leaseFutures = new ArrayList<Future<Lease>>();
        String[] partitionIds = this.host.getPartitionManager().getPartitionIds();
        for (String id : partitionIds)
        {
            leaseFutures.add(getLease(id));
        }
        return leaseFutures;
    }

    @Override
    public Future<Lease> createLeaseIfNotExists(String partitionId)
    {
        return this.host.getExecutorService().submit(() -> createLeaseIfNotExistsSync(partitionId, this.leaseOperationOptions));
    }
    
    private AzureBlobLease createLeaseIfNotExistsSync(String partitionId, BlobRequestOptions options) throws URISyntaxException, IOException, StorageException
    {
    	AzureBlobLease returnLease = null;
    	try
    	{
    		CloudBlockBlob leaseBlob = this.consumerGroupDirectory.getBlockBlobReference(partitionId); // getBlockBlobReference does not take options
    		returnLease = new AzureBlobLease(partitionId, leaseBlob, this.leaseOperationOptions);
            TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "CreateLeaseIfNotExist - leaseContainerName: " + this.storageContainerName + " consumerGroupName: " + this.host.getConsumerGroupName() +
                            "storageBlobPrefix: " + this.storageBlobPrefix));
    		uploadLease(returnLease, leaseBlob, AccessCondition.generateIfNoneMatchCondition("*"), UploadActivity.Create, options);
    	}
    	catch (StorageException se)
    	{
    		StorageExtendedErrorInformation extendedErrorInfo = se.getExtendedErrorInformation();
    		if ((extendedErrorInfo != null) &&
    				((extendedErrorInfo.getErrorCode().compareTo(StorageErrorCodeStrings.BLOB_ALREADY_EXISTS) == 0) ||
    				 (extendedErrorInfo.getErrorCode().compareTo(StorageErrorCodeStrings.LEASE_ID_MISSING) == 0))) // occurs when somebody else already has leased the blob
    		{
    			// The blob already exists.
    			TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "Lease already exists"));
        		returnLease = getLeaseSync(partitionId, options);
    		}
    		else
    		{
                TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
    				"CreateLeaseIfNotExist StorageException - leaseContainerName: " + this.storageContainerName + " consumerGroupName: " + this.host.getConsumerGroupName() +
    				"storageBlobPrefix: " + this.storageBlobPrefix), se);
    			throw se;
    		}
    	}
    	
    	return returnLease;
    }

    @Override
    public Future<Void> deleteLease(Lease lease)
    {
        return this.host.getExecutorService().submit(() -> deleteLeaseSync((AzureBlobLease)lease));
    }
    
    private Void deleteLeaseSync(AzureBlobLease lease) throws StorageException
    {
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(),"Deleting lease"));
    	lease.getBlob().deleteIfExists();
    	return null;
    }

    @Override
    public Future<Boolean> acquireLease(Lease lease)
    {
        return this.host.getExecutorService().submit(() -> acquireLeaseSync((AzureBlobLease)lease));
    }
    
    private Boolean acquireLeaseSync(AzureBlobLease lease) throws Exception
    {
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "Acquiring lease"));
    	
    	CloudBlockBlob leaseBlob = lease.getBlob();
    	boolean succeeded = true;
    	String newLeaseId = EventProcessorHost.safeCreateUUID();
    	if ((newLeaseId == null) || newLeaseId.isEmpty())
    	{
    		throw new IllegalArgumentException("acquireLeaseSync: newLeaseId really is " + ((newLeaseId == null) ? "null" : "empty"));
    	}
    	try
    	{
    		String newToken = null;
    		leaseBlob.downloadAttributes();
	    	if (leaseBlob.getProperties().getLeaseState() == LeaseState.LEASED)
	    	{
	    		TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "changeLease"));
	    		if ((lease.getToken() == null) || lease.getToken().isEmpty())
	    		{
	    			// We reach here in a race condition: when this instance of EventProcessorHost scanned the
	    			// lease blobs, this partition was unowned (token is empty) but between then and now, another
	    			// instance of EPH has established a lease (getLeaseState() is LEASED). We normally enforce
	    			// that we only steal the lease if it is still owned by the instance which owned it when we
	    			// scanned, but we can't do that when we don't know who owns it. The safest thing to do is just
	    			// fail the acquisition. If that means that one EPH instance gets more partitions than it should,
	    			// rebalancing will take care of that quickly enough.
	    			succeeded = false;
	    		}
	    		else
	    		{
		    		newToken = leaseBlob.changeLease(newLeaseId, AccessCondition.generateLeaseCondition(lease.getToken()));
	    		}
	    	}
	    	else
	    	{
	    		TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "acquireLease"));
	    		newToken = leaseBlob.acquireLease(this.host.getPartitionManagerOptions().getLeaseDurationInSeconds(), newLeaseId);
	    	}
	    	if (succeeded)
	    	{
		    	lease.setToken(newToken);
		    	lease.setOwner(this.host.getHostName());
		    	lease.incrementEpoch(); // Increment epoch each time lease is acquired or stolen by a new host
		    	uploadLease(lease, leaseBlob, AccessCondition.generateLeaseCondition(lease.getToken()), UploadActivity.Acquire, this.leaseOperationOptions);
	    	}
    	}
    	catch (StorageException se)
    	{
    		if (wasLeaseLost(se, lease.getPartitionId()))
    		{
    			succeeded = false;
    		}
    		else
    		{
    			throw se;
    		}
    	}
    	
    	return succeeded;
    }

    @Override
    public Future<Boolean> renewLease(Lease lease)
    {
        return this.host.getExecutorService().submit(() -> renewLeaseSync((AzureBlobLease)lease));
    }
    
    private Boolean renewLeaseSync(AzureBlobLease lease) throws Exception
    {
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "Renewing lease"));
    	
    	CloudBlockBlob leaseBlob = lease.getBlob();
    	boolean retval = true;
    	
    	try
    	{
    		leaseBlob.renewLease(AccessCondition.generateLeaseCondition(lease.getToken()), this.renewRequestOptions, null);
    	}
    	catch (StorageException se)
    	{
    		if (wasLeaseLost(se, lease.getPartitionId()))
    		{
    			retval = false;
    		}
    		else
    		{
    			throw se;
    		}
    	}
    	
    	return retval;
    }

    @Override
    public Future<Boolean> releaseLease(Lease lease)
    {
        return this.host.getExecutorService().submit(() -> releaseLeaseSync((AzureBlobLease)lease));
    }
    
    private Boolean releaseLeaseSync(AzureBlobLease lease) throws Exception
    {
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "Releasing lease"));
    	
    	CloudBlockBlob leaseBlob = lease.getBlob();
    	boolean retval = true;
    	try
    	{
    		String leaseId = lease.getToken();
    		AzureBlobLease releasedCopy = new AzureBlobLease(lease);
    		releasedCopy.setToken("");
    		releasedCopy.setOwner("");
    		uploadLease(releasedCopy, leaseBlob, AccessCondition.generateLeaseCondition(leaseId), UploadActivity.Release, this.leaseOperationOptions);
    		leaseBlob.releaseLease(AccessCondition.generateLeaseCondition(leaseId));
    	}
    	catch (StorageException se)
    	{
    		if (wasLeaseLost(se, lease.getPartitionId()))
    		{
    			retval = false;
    		}
    		else
    		{
    			throw se;
    		}
    	}
    	
    	return retval;
    }

    @Override
    public Future<Boolean> updateLease(Lease lease)
    {
        return this.host.getExecutorService().submit(() -> updateLeaseSync((AzureBlobLease)lease, this.leaseOperationOptions));
    }
    
    public Boolean updateLeaseSync(AzureBlobLease lease, BlobRequestOptions options) throws Exception
    {
    	if (lease == null)
    	{
    		return false;
    	}
    	
    	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(), "Updating lease"));
    	
    	String token = lease.getToken();
    	if ((token == null) || (token.length() == 0))
    	{
    		return false;
    	}
    	
    	// First, renew the lease to make sure the update will go through.
    	// Renewing the lease is always logically a lease operation, even if it is part of writing a checkpoint, so
    	// don't pass options.
    	if (!renewLeaseSync(lease))
    	{
    		return false;
    	}
    	
    	CloudBlockBlob leaseBlob = lease.getBlob();
    	try
    	{
    		uploadLease(lease, leaseBlob, AccessCondition.generateLeaseCondition(token), UploadActivity.Update, options);
    	}
    	catch (StorageException se)
    	{
    		if (wasLeaseLost(se, lease.getPartitionId()))
    		{
    			throw new LeaseLostException(lease, se);
    		}
    		else
    		{
    			throw se;
    		}
    	}
    	
    	return true;
    }

    private AzureBlobLease downloadLease(CloudBlockBlob blob, BlobRequestOptions options) throws StorageException, IOException
    {
    	String jsonLease = blob.downloadText(null, null, options, null);
    	TRACE_LOGGER.info(LoggingUtils.withHost(this.host.getHostName(), "Raw JSON downloaded: " + jsonLease));
    	AzureBlobLease rehydrated = this.gson.fromJson(jsonLease, AzureBlobLease.class);
    	AzureBlobLease blobLease = new AzureBlobLease(rehydrated, blob, this.leaseOperationOptions);
    	
    	if (blobLease.getOffset() != null)
    	{
    		this.latestCheckpoint.put(blobLease.getPartitionId(), blobLease.getCheckpoint());
    	}
    	
    	return blobLease;
    }
    
    private void uploadLease(AzureBlobLease lease, CloudBlockBlob blob, AccessCondition condition, UploadActivity activity, BlobRequestOptions options)
    		throws StorageException, IOException
    {
    	if (activity != UploadActivity.Create)
    	{
    		// It is possible for AzureBlobLease objects in memory to have stale offset/sequence number fields if a
    		// checkpoint was written but PartitionManager hasn't done its ten-second sweep which downloads new copies
    		// of all the leases. This can happen because we're trying to maintain the fiction that checkpoints and leases
    		// are separate -- which they can be in other implementations -- even though they are completely intertwined
    		// in this implementation. To prevent writing stale checkpoint data to the store, merge the checkpoint data
    		// from the most recently written checkpoint into this write, if needed.
    		Checkpoint cached = this.latestCheckpoint.get(lease.getPartitionId());
    		if ((cached != null) && ((cached.getSequenceNumber() > lease.getSequenceNumber()) || (lease.getOffset() == null)))
    		{
				lease.setOffset(cached.getOffset());
				lease.setSequenceNumber(cached.getSequenceNumber());
				TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(),
                        "Replacing stale offset/seqno while uploading lease"));
			}
			else if (lease.getOffset() != null)
			{
				this.latestCheckpoint.put(lease.getPartitionId(), lease.getCheckpoint());
			}
    	}
    	
    	String jsonLease = this.gson.toJson(lease);
 		blob.uploadText(jsonLease, null, condition, options, null);
		// During create, we blindly try upload and it may throw. Doing the logging after the upload
		// avoids a spurious trace in that case.
        TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), lease.getPartitionId(),
                "Raw JSON uploading for " + activity + ": " + jsonLease));
    }
    
    private boolean wasLeaseLost(StorageException se, String partitionId)
    {
    	boolean retval = false;
        TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "WAS LEASE LOST?"));
		TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "Http " + se.getHttpStatusCode()));
		if (se.getExtendedErrorInformation() != null)
		{
            TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "Http " + se.getExtendedErrorInformation().getErrorCode() + " :: " + se.getExtendedErrorInformation().getErrorMessage()));
		}
    	if ((se.getHttpStatusCode() == 409) || // conflict
    		(se.getHttpStatusCode() == 412)) // precondition failed
    	{
    		StorageExtendedErrorInformation extendedErrorInfo = se.getExtendedErrorInformation();
    		if (extendedErrorInfo != null)
    		{
    			String errorCode = extendedErrorInfo.getErrorCode();
                TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "Error code: " + errorCode));
                TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId, "Error message: " + extendedErrorInfo.getErrorMessage()));
    			if ((errorCode.compareTo(StorageErrorCodeStrings.LEASE_LOST) == 0) ||
    				(errorCode.compareTo(StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_LEASE_OPERATION) == 0) ||
    				(errorCode.compareTo(StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION) == 0) ||
    				(errorCode.compareTo(StorageErrorCodeStrings.LEASE_ALREADY_PRESENT) == 0))
    			{
    				retval = true;
    			}
    		}
    	}
    	return retval;
    }
}
