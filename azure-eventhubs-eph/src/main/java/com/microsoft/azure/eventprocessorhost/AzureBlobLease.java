/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.LeaseState;

class AzureBlobLease extends Lease
{
	private transient CloudBlockBlob blob; // do not serialize
	private transient BlobRequestOptions options; // do not serialize
	private String offset = null; // null means checkpoint is uninitialized
	private long sequenceNumber = 0;
	
	// not intended to be used; built for GSon
	private AzureBlobLease()
	{
		super();
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
	public boolean isExpired() throws Exception
	{
		this.blob.downloadAttributes(null, options, null); // Get the latest metadata
		LeaseState currentState = this.blob.getProperties().getLeaseState();
		return (currentState != LeaseState.LEASED); 
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
