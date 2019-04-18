// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

final class AzureBlobLease extends CompleteLease {
    private final transient CloudBlockBlob blob; // do not serialize
    private final transient BlobRequestOptions options; // do not serialize
    private String offset = null; // null means checkpoint is uninitialized
    private long sequenceNumber = 0;
    private String token = null;

    // not intended to be used; built for GSon
    @SuppressWarnings("unused")
    private AzureBlobLease() {
        super();
        this.blob = null; // so that we can mark blob as final
        this.options = null; // so that we can mark options as final
    }

    AzureBlobLease(String partitionId, CloudBlockBlob blob, BlobRequestOptions options) {
        super(partitionId);
        this.blob = blob;
        this.options = options;
    }

    AzureBlobLease(AzureBlobLease source) {
        super(source);
        this.offset = source.offset;
        this.sequenceNumber = source.sequenceNumber;
        this.blob = source.blob;
        this.options = source.options;
        this.token = source.token;
    }

    AzureBlobLease(AzureBlobLease source, CloudBlockBlob blob, BlobRequestOptions options) {
        super(source);
        this.offset = source.offset;
        this.sequenceNumber = source.sequenceNumber;
        this.blob = blob;
        this.options = options;
        this.token = source.token;
    }

    AzureBlobLease(CompleteLease source, CloudBlockBlob blob, BlobRequestOptions options) {
        super(source);
        this.blob = blob;
        this.options = options;
    }

    CloudBlockBlob getBlob() {
        return this.blob;
    }

    String getOffset() {
        return this.offset;
    }

    void setOffset(String offset) {
        this.offset = offset;
    }

    long getSequenceNumber() {
        return this.sequenceNumber;
    }

    void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    String getToken() {
        return this.token;
    }
    
    void setToken(String token) {
        this.token = token;
    }

    Checkpoint getCheckpoint() {
        return new Checkpoint(this.getPartitionId(), this.offset, this.sequenceNumber);
    }

    @Override
    String getStateDebug() {
        String retval = "uninitialized";
        try {
            this.blob.downloadAttributes();
            BlobProperties props = this.blob.getProperties();
            retval = props.getLeaseState().toString() + " " + props.getLeaseStatus().toString() + " " + props.getLeaseDuration().toString();
        } catch (StorageException e) {
            retval = "downloadAttributes on the blob caught " + e.toString();
        }
        return retval;
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
