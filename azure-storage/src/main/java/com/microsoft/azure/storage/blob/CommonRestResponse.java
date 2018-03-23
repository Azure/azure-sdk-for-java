package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.models.BlobPutHeaders;
import com.microsoft.azure.storage.models.BlockBlobPutBlockListHeaders;
import com.microsoft.rest.v2.RestResponse;

import java.util.Date;

/**
 * A generic wrapper for any type of blob REST API response. Used and returned by methods in the {@link Highlevel}
 * class.
 */
public final class CommonRestResponse {

    private RestResponse<BlobPutHeaders, Void> putBlobResponse;

    private RestResponse<BlockBlobPutBlockListHeaders, Void> putBlockListResponse;

    static CommonRestResponse createFromPutBlobResponse(RestResponse<BlobPutHeaders, Void> response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.putBlobResponse = response;
        return commonRestResponse;
    }

    static CommonRestResponse createFromPutBlockListResponse(
            RestResponse<BlockBlobPutBlockListHeaders, Void> response) {
        CommonRestResponse commonRestResponse = new CommonRestResponse();
        commonRestResponse.putBlockListResponse = response;
        return commonRestResponse;
    }

    private CommonRestResponse() {
        putBlobResponse = null;
        putBlockListResponse = null;
    }

    public String eTag() {
        if (putBlobResponse != null) {
            return putBlobResponse.headers().eTag();
        }
        return putBlockListResponse.headers().eTag();
    }

    public Date lastModifiedTime() {
        if (putBlobResponse != null) {
            return putBlobResponse.headers().lastModified().toDate(); // TODO: remove toDate
        }
        return putBlockListResponse.headers().lastModified().toDate();
    }

    public String requestId() {
        if (putBlobResponse != null) {
            return putBlobResponse.headers().requestId();
        }
        return putBlockListResponse.headers().requestId();
    }

    public Date date() {
        if (putBlobResponse != null) {
            return putBlobResponse.headers().dateProperty().toDate(); // TODO: remove toDate
        }
        return putBlockListResponse.headers().dateProperty().toDate();
    }

    public String version() {
        if (putBlobResponse != null) {
            return putBlobResponse.headers().version();
        }
        return putBlockListResponse.headers().version();
    }

    public RestResponse response() {
        if (putBlobResponse != null) {
            return putBlobResponse;
        }
        return putBlockListResponse;
    }

}
