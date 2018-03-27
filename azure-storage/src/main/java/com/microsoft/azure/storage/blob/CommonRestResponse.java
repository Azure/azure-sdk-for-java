package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListResponse;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.rest.v2.RestResponse;

import java.time.OffsetDateTime;

/**
 * A generic wrapper for any type of blob REST API response. Used and returned by methods in the {@link Highlevel}
 * class. The methods there return this type because they represent composite operations which may conclude with any of
 * several possible REST calls depending on the data provided.
 */
public final class CommonRestResponse {

    private BlockBlobUploadResponse uploadBlobResponse;

    private BlockBlobCommitBlockListResponse commitBlockListResponse;

    static CommonRestResponse createFromPutBlobResponse(BlockBlobUploadResponse response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.uploadBlobResponse = response;
        return commonRestResponse;
    }

    static CommonRestResponse createFromPutBlockListResponse(BlockBlobCommitBlockListResponse response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.commitBlockListResponse = response;
        return commonRestResponse;
    }

    private CommonRestResponse() {
        uploadBlobResponse = null;
        commitBlockListResponse = null;
    }

    /**
     * @return
     *      An HTTP Etag for the blob at the time of the request.
     */
    public String eTag() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.headers().eTag();
        }
        return commitBlockListResponse.headers().eTag();
    }

    /**
     * @return
     *      The time when the blob was last modified.
     */
    public OffsetDateTime lastModifiedTime() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.headers().lastModified();
        }
        return commitBlockListResponse.headers().lastModified();
    }

    /**
     * @return
     *      The id of the service request for which this is the response.
     */
    public String requestId() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.headers().requestId();
        }
        return commitBlockListResponse.headers().requestId();
    }

    /**
     * @return
     *      The date of the response.
     */
    public OffsetDateTime date() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.headers().dateProperty();
        }
        return commitBlockListResponse.headers().dateProperty();
    }

    /**
     * @return
     *       The service version responding to the request.
     */
    public String version() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse.headers().version();
        }
        return commitBlockListResponse.headers().version();
    }

    /**
     * @return
     *      The underlying response.
     */
    public RestResponse response() {
        if (uploadBlobResponse != null) {
            return uploadBlobResponse;
        }
        return commitBlockListResponse;
    }

}
