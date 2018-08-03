/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import spock.lang.Unroll

class HelperTest extends APISpec {

    def "responseError"() {
        when:
        cu.listBlobsFlatSegment("garbage", null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        e.statusCode() == 400
        e.message().contains("Value for one of the query parameters specified in the request URI is invalid.")
        e.getMessage().contains("<?xml") // Ensure that the details in the payload are printable
    }

    @Unroll
    def "Blob range"() {
        expect:
        new BlobRange(offset, count).toString() == result

        where:
        offset | count || result
        0      | null  || "bytes=0-"
        0      | 5     || "bytes=0-4"
    }

    @Unroll
    def "Blob range IA"() {
        when:
        new BlobRange(offset, count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }
}
