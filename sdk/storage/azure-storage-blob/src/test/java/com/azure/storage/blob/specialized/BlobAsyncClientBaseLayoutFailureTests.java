// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlobAsyncClientBaseLayoutFailureTests {

    @ParameterizedTest
    @ValueSource(ints = { 400, 500, 503, 599, 401, 429 })
    public void nonFatalStatusesFallBackToTheOriginalEndpoint(int statusCode) {
        BlobStorageException exception = exception(statusCode);

        assertNull(Objects.requireNonNull(BlobAsyncClientBase.handleLayoutFetchError(exception).block()).getRanges());
    }

    @ParameterizedTest
    @ValueSource(ints = { 403, 404, 409, 412 })
    public void fatalStatusesFailTheDownload(int statusCode) {
        BlobStorageException exception = exception(statusCode);

        assertThrows(BlobStorageException.class, () -> BlobAsyncClientBase.handleLayoutFetchError(exception).block());
    }

    private static BlobStorageException exception(int statusCode) {
        return new BlobStorageException("layout failure", new MockHttpResponse(null, statusCode), null);
    }
}
