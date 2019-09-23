package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.BatchResult;
import com.azure.core.http.rest.Response;

import java.util.stream.Stream;

public class BlobBatchResult extends BatchResult {

    @Override
    public Stream<Response<?>> getRawOperationResponses() {
        return null;
    }
}
