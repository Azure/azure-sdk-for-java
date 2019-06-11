// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.storage.blob.models.BlockBlobsUploadResponse;

import java.time.OffsetDateTime;

/**
 * A generic wrapper for any type of blob REST API response. Used and returned by methods in the {@link TransferManager}
 * class. The methods there return this type because they represent composite operations which may conclude with any of
 * several possible REST calls depending on the data provided.
 */
final class CommonRestResponse {

    private BlockBlobsUploadResponse uploadBlobResponse;

    private BlockBlobsCommitBlockListResponse commitBlockListResponse;

    private CommonRestResponse() {
        uploadBlobResponse = null;
        commitBlockListResponse = null;
    }

    static CommonRestResponse createFromPutBlobResponse(BlockBlobsUploadResponse response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.uploadBlobResponse = response;
        return commonRestResponse;
    }

    static CommonRestResponse createFromPutBlockListResponse(BlockBlobsCommitBlockListResponse response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.commitBlockListResponse = response;
        return commonRestResponse;
    }

    /**
     * @return The status code for the response
     */
    public int statusCode() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.statusCode();
        }
        return commitBlockListResponse.statusCode();
    }

    /**
     * @return An HTTP Etag for the blob at the time of the request.
     */
    public String eTag() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.deserializedHeaders().eTag();
        }
        return commitBlockListResponse.deserializedHeaders().eTag();
    }

    /**
     * @return The time when the blob was last modified.
     */
    public OffsetDateTime lastModified() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.deserializedHeaders().lastModified();
        }
        return commitBlockListResponse.deserializedHeaders().lastModified();
    }

    /**
     * @return The id of the service request for which this is the response.
     */
    public String requestId() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.deserializedHeaders().requestId();
        }
        return commitBlockListResponse.deserializedHeaders().requestId();
    }

    /**
     * @return The date of the response.
     */
    public OffsetDateTime date() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.deserializedHeaders().dateProperty();
        }
        return commitBlockListResponse.deserializedHeaders().dateProperty();
    }

    /**
     * @return The service version responding to the request.
     */
    public String version() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.deserializedHeaders().version();
    }
        return commitBlockListResponse.deserializedHeaders().version();
    }

    /**
     * @return The underlying response.
     */
    public Response<Void> response() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse;
        }
        return commitBlockListResponse;
    }

}
