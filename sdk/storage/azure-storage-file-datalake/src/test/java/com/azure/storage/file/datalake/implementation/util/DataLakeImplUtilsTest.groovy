// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util

import com.azure.core.test.http.MockHttpResponse
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.core.Exceptions
import spock.lang.Specification

class DataLakeImplUtilsTest extends Specification {
    def "can map blobs exception"() {
        given:
        def response = new MockHttpResponse(null, 412, null)
        def blobException = new BlobStorageException("fail", response, null)

        when:
        def dataLakeException = DataLakeImplUtils.transformBlobStorageException(blobException)

        then:
        dataLakeException instanceof DataLakeStorageException
        ((DataLakeStorageException) dataLakeException).statusCode == response.statusCode
        ((DataLakeStorageException) dataLakeException).message == "fail"
    }

    def "can map composite blobs exception"() {
        given:
        def response = new MockHttpResponse(null, 412, null)
        def blobException1 = new BlobStorageException("fail1", response, null)
        def blobException2 = new BlobStorageException("fail2", response, null)
        def composite = Exceptions.multiple(blobException1, blobException2)

        when:
        def dataLakeComposite = DataLakeImplUtils.transformBlobStorageException(composite)
        def dataLakeException1 = Exceptions.unwrapMultiple(dataLakeComposite)[0]
        def dataLakeException2 = Exceptions.unwrapMultiple(dataLakeComposite)[1]

        then:
        dataLakeException1 instanceof DataLakeStorageException
        ((DataLakeStorageException) dataLakeException1).statusCode == response.statusCode
        ((DataLakeStorageException) dataLakeException1).message == "fail1"
        dataLakeException2 instanceof DataLakeStorageException
        ((DataLakeStorageException) dataLakeException2).statusCode == response.statusCode
        ((DataLakeStorageException) dataLakeException2).message == "fail2"
    }
}
