// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class DataLakeImplUtilsTests {
    @Test
    public void canMapBlobsException() {
        HttpResponse response = new MockHttpResponse(null, 412);
        BlobStorageException blobException = new BlobStorageException("fail", response, null);

        Throwable dataLakeException = DataLakeImplUtils.transformBlobStorageException(blobException);

        DataLakeStorageException dataLakeStorageException = assertInstanceOf(DataLakeStorageException.class,
            dataLakeException);
        assertEquals(response.getStatusCode(), dataLakeStorageException.getStatusCode());
        assertEquals("fail", dataLakeStorageException.getMessage());
    }

    @Test
    public void canMapCompositeBlobsException() {
        HttpResponse response = new MockHttpResponse(null, 412);
        BlobStorageException blobException1 = new BlobStorageException("fail1", response, null);
        BlobStorageException blobException2 = new BlobStorageException("fail2", response, null);
        RuntimeException composite = Exceptions.multiple(blobException1, blobException2);

        Throwable dataLakeComposite = DataLakeImplUtils.transformBlobStorageException(composite);
        Throwable dataLakeException1 = Exceptions.unwrapMultiple(dataLakeComposite).get(0);
        Throwable dataLakeException2 = Exceptions.unwrapMultiple(dataLakeComposite).get(1);

        DataLakeStorageException dataLakeStorageException1 = assertInstanceOf(DataLakeStorageException.class,
            dataLakeException1);
        assertEquals(response.getStatusCode(), dataLakeStorageException1.getStatusCode());
        assertEquals("fail1", dataLakeStorageException1.getMessage());

        DataLakeStorageException dataLakeStorageException2 = assertInstanceOf(DataLakeStorageException.class,
            dataLakeException2);
        assertEquals(response.getStatusCode(), dataLakeStorageException2.getStatusCode());
        assertEquals("fail2", dataLakeStorageException2.getMessage());
    }
}
