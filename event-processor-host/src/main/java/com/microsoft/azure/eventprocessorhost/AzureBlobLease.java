/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.LeaseState;

class AzureBlobLease extends Lease
{
	private transient CloudBlockBlob blob; // do not serialize
	private String offset = PartitionReceiver.START_OF_STREAM;
	private long sequenceNumber = 0;
	
	AzureBlobLease(String partitionId, CloudBlockBlob blob)
	{
		super(partitionId);
		this.blob = blob;
	}
	
	AzureBlobLease(AzureBlobLease source)
	{
		super(source);
		this.offset = source.offset;
		this.sequenceNumber = source.sequenceNumber;
		this.blob = source.blob;
	}
	
	AzureBlobLease(AzureBlobLease source, CloudBlockBlob blob)
	{
		super(source);
		this.offset = source.offset;
		this.sequenceNumber = source.sequenceNumber;
		this.blob = blob;
	}
	
	AzureBlobLease(Lease source, CloudBlockBlob blob)
	{
		super(source);
		this.blob = blob;
	}
	
	CloudBlockBlob getBlob() { return this.blob; }
	
	void setOffset(String offset) { this.offset = offset; }
	
	String getOffset() { return this.offset; }

	void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }
	
	long getSequenceNumber() { return this.sequenceNumber; }

	@Override
	public boolean isExpired() throws Exception
	{
		this.blob.downloadAttributes(); // Get the latest metadata
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
