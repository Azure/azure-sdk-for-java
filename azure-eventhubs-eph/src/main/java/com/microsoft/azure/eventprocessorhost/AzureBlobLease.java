/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.LeaseState;

final class AzureBlobLease extends Lease
{
	private final transient CloudBlockBlob blob; // do not serialize
	private final transient BlobRequestOptions options; // do not serialize
	private String offset = null; // null means checkpoint is uninitialized
	private long sequenceNumber = 0;

	// not intended to be used; built for GSon
	@SuppressWarnings("unused")
	private AzureBlobLease()
	{
		super();
		this.blob = null; // so that we can mark blob as final
		this.options = null; // so that we can mark options as final
	}

	AzureBlobLease(String partitionId, CloudBlockBlob blob, BlobRequestOptions options)
	{
		super(partitionId);
		this.blob = blob;
		this.options = options;
	}

	AzureBlobLease(AzureBlobLease source)
	{
		super(source);
		this.offset = source.offset;
		this.sequenceNumber = source.sequenceNumber;
		this.blob = source.blob;
		this.options = source.options;
	}

	AzureBlobLease(AzureBlobLease source, CloudBlockBlob blob, BlobRequestOptions options)
	{
		super(source);
		this.offset = source.offset;
		this.sequenceNumber = source.sequenceNumber;
		this.blob = blob;
		this.options = options;
	}
	
	AzureBlobLease(Lease source, CloudBlockBlob blob, BlobRequestOptions options)
	{
		super(source);
		this.blob = blob;
		this.options = options;
	}
	
	CloudBlockBlob getBlob() { return this.blob; }
	
	void setOffset(String offset) { this.offset = offset; }
	
	String getOffset() { return this.offset; }

	void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }
	
	long getSequenceNumber() { return this.sequenceNumber; }
	
	Checkpoint getCheckpoint()
	{
		return new Checkpoint(this.getPartitionId(), this.offset, this.sequenceNumber);
	}

	@Override
	public CompletableFuture<Boolean> isExpired()
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try
			{
				this.blob.downloadAttributes(null, options, null); // Get the latest metadata
			}
			catch (StorageException e)
			{
				throw new CompletionException(e);
			}
			LeaseState currentState = this.blob.getProperties().getLeaseState();
			// There are multiple lease states, but for our purposes anything but LEASED means that
			// the blob is no longer definitely owned by the last known owner and is potentially available.
			// It could be owned by another host, so just because the state is LEASED does not mean
			// that operations on the blob will not fail with lease lost.
			return (currentState != LeaseState.LEASED);
		});
	}
	
	@Override
	String getStateDebug()
	{
		String retval = "uninitialized";
		try
		{
			this.blob.downloadAttributes();
			BlobProperties props = this.blob.getProperties();
			retval = props.getLeaseState().toString() + " " + props.getLeaseStatus().toString() + " " + props.getLeaseDuration().toString();
		}
		catch (StorageException e)
		{
			retval = "downloadAttributes on the blob caught " + e.toString();
		}
		return retval; 
	}
}
