package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;

public final class BlobBatchClient {
    public Response<Void>[] deleteBlobs(String... blobUrls) {
        return deleteBlobs(DeleteSnapshotsOptionType.INCLUDE, true, blobUrls).getValue();
    }

    public Response<Response<Void>[]> deleteBlobs(DeleteSnapshotsOptionType deleteOption, boolean throwOnAnyFailure,
        String... blobUrls) {
        return new SimpleResponse<>(null, null);
    }

    public Response<Void>[] setTiers(String... blobUrls) {
        return setTiers(true, blobUrls).getValue();
    }

    public Response<Response<Void>[]> setTiers(boolean throwOnAnyFailure, String... blobUrls) {
        return new SimpleResponse<>(null, null);
    }
}
