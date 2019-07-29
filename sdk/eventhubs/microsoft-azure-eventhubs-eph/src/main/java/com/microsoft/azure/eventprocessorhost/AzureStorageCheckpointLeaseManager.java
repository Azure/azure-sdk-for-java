// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.google.gson.Gson;
import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.DeleteSnapshotsOption;
import com.microsoft.azure.storage.blob.LeaseState;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class AzureStorageCheckpointLeaseManager implements ICheckpointManager, ILeaseManager {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(AzureStorageCheckpointLeaseManager.class);
    private static final String METADATA_OWNER_NAME = "OWNINGHOST";

    private final String storageConnectionString;
    private final StorageCredentials storageCredentials;
    private final String storageBlobPrefix;
    private final BlobRequestOptions leaseOperationOptions = new BlobRequestOptions();
    private final BlobRequestOptions checkpointOperationOptions = new BlobRequestOptions();
    private final BlobRequestOptions renewRequestOptions = new BlobRequestOptions();
    private HostContext hostContext;
    private String storageContainerName;
    private CloudBlobClient storageClient;
    private CloudBlobContainer eventHubContainer;
    private CloudBlobDirectory consumerGroupDirectory;
    private Gson gson;

    private Hashtable<String, Checkpoint> latestCheckpoint = new Hashtable<String, Checkpoint>();

    AzureStorageCheckpointLeaseManager(String storageConnectionString, String storageContainerName, String storageBlobPrefix) {
        if ((storageConnectionString == null) || storageConnectionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Provide valid Azure Storage connection string when using Azure Storage");
        }
        this.storageConnectionString = storageConnectionString;
        this.storageCredentials = null;
        
        if ((storageContainerName != null) && storageContainerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Azure Storage container name must be a valid container name or null to use the default");
        }
        this.storageContainerName = storageContainerName;

        // Convert all-whitespace prefix to empty string. Convert null prefix to empty string.
        // Then the rest of the code only has one case to worry about.
        this.storageBlobPrefix = (storageBlobPrefix != null) ? storageBlobPrefix.trim() : "";
    }
    
    AzureStorageCheckpointLeaseManager(StorageCredentials storageCredentials, String storageContainerName, String storageBlobPrefix) {
        if (storageCredentials == null) {
            throw new IllegalArgumentException("Provide valid Azure Storage credentials when using Azure Storage");
        }
        this.storageConnectionString = null;
        this.storageCredentials = storageCredentials;
        
        if ((storageContainerName != null) && storageContainerName.trim().isEmpty()) {
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
    void initialize(HostContext hostContext) throws InvalidKeyException, URISyntaxException, StorageException {
        this.hostContext = hostContext;

        if (this.storageContainerName == null) {
            this.storageContainerName = this.hostContext.getEventHubPath();
        }

        // Validate that the event hub name is also a legal storage container name.
        // Regex pattern is copied from .NET version. The syntax for Java regexes seems to be the same.
        // Error message is also copied from .NET version.
        Pattern p = Pattern.compile("^(?-i)(?:[a-z0-9]|(?<=[0-9a-z])-(?=[0-9a-z])){3,63}$");
        Matcher m = p.matcher(this.storageContainerName);
        if (!m.find()) {
            throw new IllegalArgumentException("EventHub names must conform to the following rules to be able to use it with EventProcessorHost: "
                    + "Must start with a letter or number, and can contain only letters, numbers, and the dash (-) character. "
                    + "Every dash (-) character must be immediately preceded and followed by a letter or number; consecutive dashes are not permitted in container names. "
                    + "All letters in a container name must be lowercase. "
                    + "Must be from 3 to 63 characters long.");
        }

        CloudStorageAccount storageAccount = null;
        if (this.storageConnectionString != null) {
            storageAccount = CloudStorageAccount.parse(this.storageConnectionString);
        } else {
            storageAccount = new CloudStorageAccount(this.storageCredentials);
        }
        this.storageClient = storageAccount.createCloudBlobClient();

        this.eventHubContainer = this.storageClient.getContainerReference(this.storageContainerName);

        // storageBlobPrefix is either empty or a real user-supplied string. Either way we can just
        // stick it on the front and get the desired result.
        this.consumerGroupDirectory = this.eventHubContainer.getDirectoryReference(this.storageBlobPrefix + this.hostContext.getConsumerGroupName());

        this.gson = new Gson();

        this.leaseOperationOptions.setMaximumExecutionTimeInMs(this.hostContext.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000);
        this.storageClient.setDefaultRequestOptions(this.leaseOperationOptions);
        this.checkpointOperationOptions.setMaximumExecutionTimeInMs(this.hostContext.getPartitionManagerOptions().getCheckpointTimeoutInSeconds() * 1000);
        // The only option that .NET sets on renewRequestOptions is ServerTimeout, which doesn't exist in Java equivalent.
        // Keep it separate in case we need to change something later.
        // Only used for leases, not checkpoints, so set max execution time to lease value
        this.renewRequestOptions.setMaximumExecutionTimeInMs(this.hostContext.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000);
    }

    @Override
    public CompletableFuture<Boolean> checkpointStoreExists() {
        return storeExistsInternal(this.checkpointOperationOptions, EventProcessorHostActionStrings.CHECKING_CHECKPOINT_STORE,
            "Failure while checking checkpoint store existence");
    }


    //
    // In this implementation, checkpoints are data that's actually in the lease blob, so checkpoint operations
    // turn into lease operations under the covers.
    //

    @Override
    public CompletableFuture<Void> createCheckpointStoreIfNotExists() {
        // Because we control the caller, we know that this method will only be called after createLeaseStoreIfNotExists.
        // In this implementation, it's the same store, so the store will always exist if execution reaches here.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteCheckpointStore() {
        return deleteStoreInternal(this.checkpointOperationOptions);
    }

    @Override
    public CompletableFuture<Checkpoint> getCheckpoint(String partitionId) {
        CompletableFuture<Checkpoint> future = null;

        try {
            AzureBlobLease lease = getLeaseInternal(partitionId, this.checkpointOperationOptions);
            Checkpoint checkpoint = null;
            if (lease != null) {
                if ((lease.getOffset() != null) && !lease.getOffset().isEmpty()) {
                    checkpoint = new Checkpoint(partitionId);
                    checkpoint.setOffset(lease.getOffset());
                    checkpoint.setSequenceNumber(lease.getSequenceNumber());
                }
                // else offset is null meaning no checkpoint stored for this partition so return null
            }
            future = CompletableFuture.completedFuture(checkpoint);
        } catch (URISyntaxException | IOException | StorageException e) {
            future = new CompletableFuture<Checkpoint>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.GETTING_CHECKPOINT));
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> createAllCheckpointsIfNotExists(List<String> partitionIds) {
        // Because we control the caller, we know that this method will only be called after createAllLeasesIfNotExists.
        // In this implementation checkpoints are in the same blobs as leases, so the blobs will already exist if execution reaches here.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateCheckpoint(CompleteLease lease, Checkpoint checkpoint) {
        AzureBlobLease updatedLease = new AzureBlobLease((AzureBlobLease) lease);
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(checkpoint.getPartitionId(),
                "Checkpointing at " + checkpoint.getOffset() + " // " + checkpoint.getSequenceNumber()));
        updatedLease.setOffset(checkpoint.getOffset());
        updatedLease.setSequenceNumber(checkpoint.getSequenceNumber());

        CompletableFuture<Void> future = null;

        try {
            if (updateLeaseInternal(updatedLease, this.checkpointOperationOptions)) {
                future = CompletableFuture.completedFuture(null);
            } else {
                TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(lease, "Lease lost"));
                future = new CompletableFuture<Void>();
                future.completeExceptionally(LoggingUtils.wrapException(new RuntimeException("Lease lost while updating checkpoint"),
                        EventProcessorHostActionStrings.UPDATING_CHECKPOINT));
            }
        } catch (StorageException | IOException e) {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(lease, "Failure updating checkpoint"), e);
            future = new CompletableFuture<Void>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.UPDATING_CHECKPOINT));
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteCheckpoint(String partitionId) {
        // Not currently used by EventProcessorHost.
        return CompletableFuture.completedFuture(null);
    }


    //
    // Lease operations.
    //

    @Override
    public int getLeaseDurationInMilliseconds() {
        return this.hostContext.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000;
    }

    @Override
    public CompletableFuture<Boolean> leaseStoreExists() {
        return storeExistsInternal(this.leaseOperationOptions, EventProcessorHostActionStrings.CHECKING_LEASE_STORE,
                "Failure while checking lease store existence");
    }

    private CompletableFuture<Boolean> storeExistsInternal(BlobRequestOptions options, String action, String trace) {
        CompletableFuture<Boolean> future = null;
        try {
            future = CompletableFuture.completedFuture(this.eventHubContainer.exists(null, options, null));
        } catch (StorageException e) {
            TRACE_LOGGER.error(this.hostContext.withHost(trace), e);
            future = new CompletableFuture<Boolean>();
            future.completeExceptionally(LoggingUtils.wrapException(e, action));
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> createLeaseStoreIfNotExists() {
        CompletableFuture<Void> future = null;

        try {
            // returns true if the container was created, false if it already existed -- we don't care
            this.eventHubContainer.createIfNotExists(this.leaseOperationOptions, null);
            TRACE_LOGGER.info(this.hostContext.withHost("Created lease store OK or it already existed"));
            future = CompletableFuture.completedFuture(null);
        } catch (StorageException e) {
            future = new CompletableFuture<Void>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.CREATING_LEASE_STORE));
            TRACE_LOGGER.error(this.hostContext.withHost("Failure while creating lease store"), e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteLeaseStore() {
        return deleteStoreInternal(this.leaseOperationOptions);
    }

    private CompletableFuture<Void> deleteStoreInternal(BlobRequestOptions options) {
        CompletableFuture<Void> future = null;

        try {
            for (ListBlobItem blob : this.eventHubContainer.listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), options, null)) {
                if (blob instanceof CloudBlobDirectory) {
                    for (ListBlobItem subBlob : ((CloudBlobDirectory) blob).listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), options, null)) {
                        ((CloudBlockBlob) subBlob).deleteIfExists(DeleteSnapshotsOption.NONE, null, options, null);
                    }
                } else if (blob instanceof CloudBlockBlob) {
                    ((CloudBlockBlob) blob).deleteIfExists(DeleteSnapshotsOption.NONE, null, options, null);
                }
            }

            this.eventHubContainer.deleteIfExists(null, options, null);

            future = CompletableFuture.completedFuture(null);
        } catch (StorageException | URISyntaxException e) {
            TRACE_LOGGER.error(this.hostContext.withHost("Failure while deleting lease store"), e);
            future = new CompletableFuture<Void>();
            future.completeExceptionally(new CompletionException(e));
        }

        return future;
    }

    @Override
    public CompletableFuture<CompleteLease> getLease(String partitionId) {
        CompletableFuture<CompleteLease> future = null;

        try {
            future = CompletableFuture.completedFuture(getLeaseInternal(partitionId, this.leaseOperationOptions));
        } catch (URISyntaxException | IOException | StorageException e) {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(partitionId, "Failure while getting lease details"), e);
            future = new CompletableFuture<CompleteLease>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.GETTING_LEASE));
        }

        return future;
    }

    private AzureBlobLease getLeaseInternal(String partitionId, BlobRequestOptions options) throws URISyntaxException, IOException, StorageException {
        AzureBlobLease retval = null;

        CloudBlockBlob leaseBlob = this.consumerGroupDirectory.getBlockBlobReference(partitionId); // getBlockBlobReference does not take options
        if (leaseBlob.exists(null, options, null)) {
            retval = downloadLease(leaseBlob, options);
        }

        return retval;
    }

    @Override
    public CompletableFuture<List<BaseLease>> getAllLeases() {
        CompletableFuture<List<BaseLease>> future = null;

        try {
            ArrayList<BaseLease> infos = new ArrayList<BaseLease>();
            EnumSet<BlobListingDetails> details = EnumSet.of(BlobListingDetails.METADATA);
            Iterable<ListBlobItem> leaseBlobs = this.consumerGroupDirectory.listBlobs("", true, details, this.leaseOperationOptions, null);
            leaseBlobs.forEach((lbi) -> {
                CloudBlob blob = (CloudBlob) lbi;
                BlobProperties bp = blob.getProperties();
                HashMap<String, String> metadata = blob.getMetadata();
                Path p = Paths.get(lbi.getUri().getPath());
                Path pFileName = p.getFileName();
                String partitionId = pFileName != null ? pFileName.toString() : "";
                infos.add(new BaseLease(partitionId, metadata.get(AzureStorageCheckpointLeaseManager.METADATA_OWNER_NAME),
                        (bp.getLeaseState() == LeaseState.LEASED)));
            });
            future = CompletableFuture.completedFuture(infos);
        } catch (URISyntaxException | StorageException | NoSuchElementException e) {
            Throwable effective = e;
            if (e instanceof NoSuchElementException) {
                // If there is a StorageException in the forEach, it arrives wrapped in a NoSuchElementException.
                // Strip the misleading NoSuchElementException to provide a meaningful error for the user.
                effective = e.getCause();
            }

            TRACE_LOGGER.warn(this.hostContext.withHost("Failure while getting lease state details"), e);
            future = new CompletableFuture<>();
            future.completeExceptionally(LoggingUtils.wrapException(effective, EventProcessorHostActionStrings.GETTING_LEASE));
        }

        return future;
    }

    // NOTE NOTE NOTE: this is the one place where this lease manager implementation returns an uncompleted future.
    // This is to support creating the blobs in parallel, which can be an important part of fast startup.
    // Because it happens during startup, when no user code is running, it cannot deadlock with checkpointing.
    @Override
    public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
        CompletableFuture<Void> future = null;

        // Optimization: list the blobs currently existing in the directory. If there are the
        // expected number of blobs, then we can skip doing the creates.
        int blobCount = 0;
        try {
            Iterable<ListBlobItem> leaseBlobs = this.consumerGroupDirectory.listBlobs("", true, null, this.leaseOperationOptions, null);
            Iterator<ListBlobItem> blobIterator = leaseBlobs.iterator();
            while (blobIterator.hasNext()) {
                blobCount++;
                blobIterator.next();
            }
        } catch (URISyntaxException | StorageException e) {
            TRACE_LOGGER.error(this.hostContext.withHost("Exception checking lease existence - leaseContainerName: " + this.storageContainerName + " consumerGroupName: "
                    + this.hostContext.getConsumerGroupName() + " storageBlobPrefix: " + this.storageBlobPrefix), e);
            future = new CompletableFuture<Void>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.CREATING_LEASES));
        }

        if (future == null) {
            // No error checking the list, so keep going
            if (blobCount == partitionIds.size()) {
                // All expected blobs found, so short-circuit
                future = CompletableFuture.completedFuture(null);
            } else {
                // Create the blobs in parallel
                ArrayList<CompletableFuture<CompleteLease>> createFutures = new ArrayList<CompletableFuture<CompleteLease>>();

                for (String id : partitionIds) {
                    CompletableFuture<CompleteLease> oneCreate = CompletableFuture.supplyAsync(() -> {
                        CompleteLease returnLease = null;
                        try {
                            returnLease = createLeaseIfNotExistsInternal(id, this.leaseOperationOptions);
                        } catch (URISyntaxException | IOException | StorageException e) {
                            TRACE_LOGGER.error(this.hostContext.withHostAndPartition(id,
                                    "Exception creating lease - leaseContainerName: " + this.storageContainerName + " consumerGroupName: " + this.hostContext.getConsumerGroupName()
                                            + " storageBlobPrefix: " + this.storageBlobPrefix), e);
                            throw LoggingUtils.wrapException(e, EventProcessorHostActionStrings.CREATING_LEASES);
                        }
                        return returnLease;
                    }, this.hostContext.getExecutor());
                    createFutures.add(oneCreate);
                }

                CompletableFuture<?>[] dummy = new CompletableFuture<?>[createFutures.size()];
                future = CompletableFuture.allOf(createFutures.toArray(dummy));
            }
        }

        return future;
    }

    private AzureBlobLease createLeaseIfNotExistsInternal(String partitionId, BlobRequestOptions options) throws URISyntaxException, IOException, StorageException {
        AzureBlobLease returnLease = null;
        try {
            CloudBlockBlob leaseBlob = this.consumerGroupDirectory.getBlockBlobReference(partitionId); // getBlockBlobReference does not take options
            returnLease = new AzureBlobLease(partitionId, leaseBlob, this.leaseOperationOptions);
            uploadLease(returnLease, leaseBlob, AccessCondition.generateIfNoneMatchCondition("*"), UploadActivity.Create, options);
            // Do not set metadata on creation. No metadata/no owner value indicates that the lease is unowned.
            TRACE_LOGGER.info(this.hostContext.withHostAndPartition(partitionId,
                    "CreateLeaseIfNotExist OK - leaseContainerName: " + this.storageContainerName + " consumerGroupName: " + this.hostContext.getConsumerGroupName()
                            + " storageBlobPrefix: " + this.storageBlobPrefix));
        } catch (StorageException se) {
            StorageExtendedErrorInformation extendedErrorInfo = se.getExtendedErrorInformation();
            if ((extendedErrorInfo != null)
                    && ((extendedErrorInfo.getErrorCode().compareTo(StorageErrorCodeStrings.BLOB_ALREADY_EXISTS) == 0)
                            || (extendedErrorInfo.getErrorCode().compareTo(StorageErrorCodeStrings.LEASE_ID_MISSING) == 0))) { // occurs when somebody else already has leased the blob
                // The blob already exists.
                TRACE_LOGGER.info(this.hostContext.withHostAndPartition(partitionId, "Lease already exists"));
                returnLease = getLeaseInternal(partitionId, options);
            } else {
                throw se;
            }
        }

        return returnLease;
    }

    @Override
    public CompletableFuture<Void> deleteLease(CompleteLease lease) {
        CompletableFuture<Void> future = null;

        TRACE_LOGGER.info(this.hostContext.withHostAndPartition(lease, "Deleting lease"));
        try {
            // Fetching leases (using getLease) from AzureStorageCheckpointLeaseManager as the ILeaseManager returns an
            // AzureBlobLease. This unchecked cast won't fail.
            ((AzureBlobLease) lease).getBlob().deleteIfExists();
            future = CompletableFuture.completedFuture(null);
        } catch (StorageException e) {
            TRACE_LOGGER.error(this.hostContext.withHostAndPartition(lease, "Exception deleting lease"), e);
            future = new CompletableFuture<Void>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.DELETING_LEASE));
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
        CompletableFuture<Boolean> future = null;

        try {
            // Fetching leases (using getLease) from AzureStorageCheckpointLeaseManager as the ILeaseManager returns an
            // AzureBlobLease. This unchecked cast won't fail.
            future = CompletableFuture.completedFuture(acquireLeaseInternal((AzureBlobLease) lease));
        } catch (IOException | StorageException e) {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(lease, "Failure acquiring lease"), e);
            future = new CompletableFuture<Boolean>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.ACQUIRING_LEASE));
        }

        return future;
    }

    private boolean acquireLeaseInternal(AzureBlobLease lease) throws IOException, StorageException {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "Acquiring lease"));

        CloudBlockBlob leaseBlob = lease.getBlob();
        boolean succeeded = true;
        String newLeaseId = EventProcessorHost.safeCreateUUID();
        if ((newLeaseId == null) || newLeaseId.isEmpty()) {
            throw new IllegalArgumentException("acquireLeaseSync: newLeaseId really is " + ((newLeaseId == null) ? "null" : "empty"));
        }
        try {
            String newToken = null;
            leaseBlob.downloadAttributes();
            if (leaseBlob.getProperties().getLeaseState() == LeaseState.LEASED) {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "changeLease"));
                if ((lease.getToken() == null) || lease.getToken().isEmpty()) {
                    // We reach here in a race condition: when this instance of EventProcessorHost scanned the
                    // lease blobs, this partition was unowned (token is empty) but between then and now, another
                    // instance of EPH has established a lease (getLeaseState() is LEASED). We normally enforce
                    // that we only steal the lease if it is still owned by the instance which owned it when we
                    // scanned, but we can't do that when we don't know who owns it. The safest thing to do is just
                    // fail the acquisition. If that means that one EPH instance gets more partitions than it should,
                    // rebalancing will take care of that quickly enough.
                    succeeded = false;
                } else {
                    newToken = leaseBlob.changeLease(newLeaseId, AccessCondition.generateLeaseCondition(lease.getToken()));
                }
            } else {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "acquireLease"));
                newToken = leaseBlob.acquireLease(this.hostContext.getPartitionManagerOptions().getLeaseDurationInSeconds(), newLeaseId);
            }
            if (succeeded) {
                lease.setToken(newToken);
                lease.setOwner(this.hostContext.getHostName());
                lease.incrementEpoch(); // Increment epoch each time lease is acquired or stolen by a new host
                uploadLease(lease, leaseBlob, AccessCondition.generateLeaseCondition(lease.getToken()), UploadActivity.Acquire, this.leaseOperationOptions);
            }
        } catch (StorageException se) {
            if (wasLeaseLost(se, lease.getPartitionId())) {
                succeeded = false;
            } else {
                throw se;
            }
        }

        return succeeded;
    }

    @Override
    public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
        CompletableFuture<Boolean> future = null;

        try {
            future = CompletableFuture.completedFuture(renewLeaseInternal(lease));
        } catch (StorageException se) {
            future = new CompletableFuture<Boolean>();
            future.completeExceptionally(LoggingUtils.wrapException(se, EventProcessorHostActionStrings.RENEWING_LEASE));
        }

        return future;
    }

    private boolean renewLeaseInternal(CompleteLease lease) throws StorageException {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "Renewing lease"));

        boolean result = false;

        // Fetching leases (using getLease) from AzureStorageCheckpointLeaseManager as the ILeaseManager returns an
        // AzureBlobLease. This unchecked cast won't fail.
        AzureBlobLease azLease = (AzureBlobLease) lease;
        CloudBlockBlob leaseBlob = azLease.getBlob();

        try {
            leaseBlob.renewLease(AccessCondition.generateLeaseCondition(azLease.getToken()), this.renewRequestOptions, null);
            result = true;
        } catch (StorageException se) {
            if (!wasLeaseLost(se, azLease.getPartitionId())) {
                throw se;
            }
        }

        return result;
    }

    @Override
    public CompletableFuture<Void> releaseLease(CompleteLease lease) {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "Releasing lease"));

        CompletableFuture<Void> future = null;

        // Fetching leases (using getLease) from AzureStorageCheckpointLeaseManager as the ILeaseManager returns an
        // AzureBlobLease. This unchecked cast won't fail.
        AzureBlobLease inLease = (AzureBlobLease) lease;
        CloudBlockBlob leaseBlob = inLease.getBlob();

        try {
            String leaseId = inLease.getToken();
            AzureBlobLease releasedCopy = new AzureBlobLease(inLease);
            releasedCopy.setToken("");
            releasedCopy.setOwner("");
            uploadLease(releasedCopy, leaseBlob, AccessCondition.generateLeaseCondition(leaseId), UploadActivity.Release, this.leaseOperationOptions);
            leaseBlob.releaseLease(AccessCondition.generateLeaseCondition(leaseId));
            future = CompletableFuture.completedFuture(null);
        } catch (StorageException se) {
            if (wasLeaseLost(se, lease.getPartitionId())) {
                // If the lease was already lost, then the intent of releasing it has been achieved.
                future = CompletableFuture.completedFuture(null);
            } else {
                future = new CompletableFuture<Void>();
                future.completeExceptionally(LoggingUtils.wrapException(se, EventProcessorHostActionStrings.RELEASING_LEASE));
            }
        } catch (IOException ie) {
            future = new CompletableFuture<Void>();
            future.completeExceptionally(LoggingUtils.wrapException(ie, EventProcessorHostActionStrings.RELEASING_LEASE));
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
        CompletableFuture<Boolean> future = null;

        try {
            // Fetching leases (using getLease) from AzureStorageCheckpointLeaseManager as the ILeaseManager returns an
            // AzureBlobLease. This unchecked cast won't fail.
            boolean result = updateLeaseInternal((AzureBlobLease) lease, this.leaseOperationOptions);
            future = CompletableFuture.completedFuture(result);
        } catch (StorageException | IOException e) {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(lease, "Failure updating lease"), e);
            future = new CompletableFuture<Boolean>();
            future.completeExceptionally(LoggingUtils.wrapException(e, EventProcessorHostActionStrings.UPDATING_LEASE));
        }

        return future;
    }

    public boolean updateLeaseInternal(AzureBlobLease lease, BlobRequestOptions options) throws StorageException, IOException {
        if (lease == null) {
            return false;
        }

        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "Updating lease"));

        String token = lease.getToken();
        if ((token == null) || (token.length() == 0)) {
            return false;
        }

        // Renew the lease to make sure the update will go through.
        // Renewing the lease is always logically a lease operation, even if it is part of writing a checkpoint, so
        // don't pass options.
        boolean result = renewLeaseInternal(lease);
        if (result) {
            CloudBlockBlob leaseBlob = lease.getBlob();
            try {
                uploadLease(lease, leaseBlob, AccessCondition.generateLeaseCondition(token), UploadActivity.Update, options);
                // Success! Result is already true, so pass it up unchanged
            } catch (StorageException se) {
                if (wasLeaseLost(se, lease.getPartitionId())) {
                    result = false;
                } else {
                    throw se;
                }
            } catch (IOException ie) {
                throw ie;
            }
        }
        // else could not renew lease due to lease loss. Result is already false, so pass it up unchanged

        return result;
    }

    private AzureBlobLease downloadLease(CloudBlockBlob blob, BlobRequestOptions options) throws StorageException, IOException {
        String jsonLease = blob.downloadText(null, null, options, null);
        TRACE_LOGGER.debug(this.hostContext.withHost("Raw JSON downloaded: " + jsonLease));
        AzureBlobLease rehydrated = this.gson.fromJson(jsonLease, AzureBlobLease.class);
        AzureBlobLease blobLease = new AzureBlobLease(rehydrated, blob, this.leaseOperationOptions);

        if (blobLease.getOffset() != null) {
            this.latestCheckpoint.put(blobLease.getPartitionId(), blobLease.getCheckpoint());
        }

        return blobLease;
    }

    private void uploadLease(AzureBlobLease lease, CloudBlockBlob blob, AccessCondition condition, UploadActivity activity, BlobRequestOptions options)
            throws StorageException, IOException {
        if (activity != UploadActivity.Create) {
            // It is possible for AzureBlobLease objects in memory to have stale offset/sequence number fields if a
            // checkpoint was written but PartitionManager hasn't done its ten-second sweep which downloads new copies
            // of all the leases. This can happen because we're trying to maintain the fiction that checkpoints and leases
            // are separate -- which they can be in other implementations -- even though they are completely intertwined
            // in this implementation. To prevent writing stale checkpoint data to the store, merge the checkpoint data
            // from the most recently written checkpoint into this write, if needed.
            Checkpoint cached = this.latestCheckpoint.get(lease.getPartitionId()); // HASHTABLE
            if ((cached != null) && ((cached.getSequenceNumber() > lease.getSequenceNumber()) || (lease.getOffset() == null))) {
                lease.setOffset(cached.getOffset());
                lease.setSequenceNumber(cached.getSequenceNumber());
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease,
                        "Replacing stale offset/seqno while uploading lease"));
            } else if (lease.getOffset() != null) {
                this.latestCheckpoint.put(lease.getPartitionId(), lease.getCheckpoint());
            }
        }

        String jsonLease = this.gson.toJson(lease);
        blob.uploadText(jsonLease, null, condition, options, null);
        // During create, we blindly try upload and it may throw. Doing the logging after the upload
        // avoids a spurious trace in that case.
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease,
                "Raw JSON uploading for " + activity + ": " + jsonLease));

        if ((activity == UploadActivity.Acquire) || (activity == UploadActivity.Release)) {
            blob.downloadAttributes();
            HashMap<String, String> metadata = blob.getMetadata();
            switch (activity) {
                case Acquire:
                    // Add owner in metadata
                    metadata.put(AzureStorageCheckpointLeaseManager.METADATA_OWNER_NAME, lease.getOwner());
                    break;

                case Release:
                    // Remove owner in metadata
                    metadata.remove(AzureStorageCheckpointLeaseManager.METADATA_OWNER_NAME);
                    break;

                default:
                    // Should never get here, but passing the metadata through unchanged is harmless.
                    break;
            }
            blob.setMetadata(metadata);
            blob.uploadMetadata(condition, options, null);
        }
        // else don't touch metadata
    }

    private boolean wasLeaseLost(StorageException se, String partitionId) {
        boolean retval = false;
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId, "WAS LEASE LOST? Http " + se.getHttpStatusCode()));
        if (se.getExtendedErrorInformation() != null) {
            TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId,
                    "Http " + se.getExtendedErrorInformation().getErrorCode() + " :: " + se.getExtendedErrorInformation().getErrorMessage()));
        }
        if ((se.getHttpStatusCode() == 409) || // conflict
                (se.getHttpStatusCode() == 412)) { // precondition failed
            StorageExtendedErrorInformation extendedErrorInfo = se.getExtendedErrorInformation();
            if (extendedErrorInfo != null) {
                String errorCode = extendedErrorInfo.getErrorCode();
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId, "Error code: " + errorCode));
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId, "Error message: " + extendedErrorInfo.getErrorMessage()));
                if ((errorCode.compareTo(StorageErrorCodeStrings.LEASE_LOST) == 0)
                        || (errorCode.compareTo(StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_LEASE_OPERATION) == 0)
                        || (errorCode.compareTo(StorageErrorCodeStrings.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION) == 0)
                        || (errorCode.compareTo(StorageErrorCodeStrings.LEASE_ALREADY_PRESENT) == 0)) {
                    retval = true;
                }
            }
        }
        return retval;
    }

    private enum UploadActivity { Create, Acquire, Release, Update }
}
