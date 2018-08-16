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
import com.microsoft.azure.storage.blob.AccountSASResourceType
import com.microsoft.azure.storage.blob.AccountSASService
import com.microsoft.azure.storage.blob.AccountSASSignatureValues
import com.microsoft.azure.storage.blob.BlobSASPermission
import com.microsoft.azure.storage.blob.ContainerSASPermission
import com.microsoft.azure.storage.blob.IPRange
import com.microsoft.azure.storage.blob.SASProtocol
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.Utility
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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

    def "serviceSasSignatures"() {
        when:
        def v = new ServiceSASSignatureValues()
        v.blobName = "foo"
        v.containerName = "bar"
        v.expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.protocol = SASProtocol.HTTPS_ONLY
        def p = new BlobSASPermission()
        p.read = true
        v.permissions = p.toString();
        def expectedCanonicalName = "/blob/${primaryCreds.accountName}/${v.containerName}/${v.blobName}"
        def expectedStringToSign = "${v.permissions}\n\n${Utility.ISO8601UTCDateFormatter.format(v.expiryTime)}\n${expectedCanonicalName}\n\n${IPRange.DEFAULT.toString()}\n${v.protocol}\n${v.version}\n\n\n\n\n"

        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)
    }

    def "accountSasSignatures"() {
        when:
        def v = new AccountSASSignatureValues()
        v.expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.protocol = SASProtocol.HTTPS_ONLY
        def p = new ContainerSASPermission()
        p.read = true
        v.permissions = p.toString()
        def s = new AccountSASService()
        s.blob = true
        v.services = s.toString()
        def t = new AccountSASResourceType()
        t.container = true
        t.object = true
        v.resourceTypes = t.toString()
        def expectedStringToSign = "${primaryCreds.accountName}\n${p.toString()}\n${v.services}\n${v.resourceTypes}\n\n${Utility.ISO8601UTCDateFormatter.format(v.expiryTime)}\n${IPRange.DEFAULT.toString()}\n${SASProtocol.HTTPS_ONLY}\n${v.version}\n"
        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)

    }
}
