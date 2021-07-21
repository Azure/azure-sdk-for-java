// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.util.Context
import com.azure.core.util.serializer.JacksonAdapter
import com.azure.core.util.serializer.SerializerEncoding
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.implementation.util.BlobSasImplUtil
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.PageList
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.common.Utility
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.SasImplUtils
import com.azure.storage.common.sas.CommonSasQueryParameters
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.time.ZoneOffset

class HelperTest extends APISpec {

    /*
    This test is to validate the workaround for the autorest bug that forgets to set the request property on the
    response.
     */

    def "Request property"() {
        when:
        def response = cc.deleteWithResponse(null, null, null)

        then:
        response.getRequest() != null
    }

    @Unroll
    def "Blob range"() {
        expect:
        new BlobRange(offset, count).toHeaderValue() == result

        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
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

    def "URLParser"() {
        when:
        def parts = BlobUrlParts.parse(new URL("http://host/container/" + originalBlobName + "?snapshot=snapshot&sv=" + Constants.SAS_SERVICE_VERSION + "&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.getScheme() == "http"
        parts.getHost() == "host"
        parts.getBlobContainerName() == "container"
        parts.getBlobName() == finalBlobName
        parts.getSnapshot() == "snapshot"
        parts.getCommonSasQueryParameters().getPermissions() == "r"
        parts.getCommonSasQueryParameters().getVersion() == Constants.SAS_SERVICE_VERSION
        parts.getCommonSasQueryParameters().getResource() == "c"
        parts.getCommonSasQueryParameters().getSignature() == Utility.urlDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                 | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    def "BlobURLParts"() {
        setup:
        def parts = new BlobUrlParts()
        parts.setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("blob")
            .setSnapshot("snapshot")
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission().setReadPermission(true)
        def sasValues = new BlobServiceSasSignatureValues(e, p)

        def implUtil = new BlobSasImplUtil(sasValues, "containerName", "blobName", "snapshot", null)
        def sas = implUtil.generateSas(env.primaryAccount.credential, Context.NONE)

        parts.setCommonSasQueryParameters(new CommonSasQueryParameters(SasImplUtils.parseQueryString(sas), true))

        when:
        def splitParts = parts.toUrl().toString().split("\\?")

        then:
        splitParts.size() == 2 // Ensure that there is only one question mark even when sas and snapshot are present
        splitParts[0] == "http://host/container/blob"
        splitParts[1].contains("snapshot=snapshot")
        splitParts[1].contains("sp=r")
        splitParts[1].contains("sig=")
        splitParts[1].split("&").size() == 6 // snapshot & sv & sr & sp & sig
    }

    def "BlobURLParts implicit root"() {
        when:
        def bup = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setBlobName("blob")

        then:
        new BlobUrlParts().parse(bup.toUrl()).getBlobContainerName() == BlobContainerAsyncClient.ROOT_CONTAINER_NAME
    }

    def "Utility convertStreamToBuffer replayable"() {
        setup:
        def data = getRandomByteArray(1024)

        when:
        def flux = Utility.convertStreamToByteBuffer(new ByteArrayInputStream(data), 1024, 1024, true)

        then:
        StepVerifier.create(flux)
            .assertNext(){buffer -> assert buffer.compareTo(ByteBuffer.wrap(data)) == 0 }
            .verifyComplete()
        // subscribe multiple times and ensure data is same each time
        StepVerifier.create(flux)
            .assertNext(){buffer -> assert buffer.compareTo(ByteBuffer.wrap(data)) == 0 }
            .verifyComplete()
    }

    /*
    This test covers the switch from using available() to using read() to check that a stream is done when converting
    from a stream to a flux. We previously used to assert that, when we had read length bytes from the stream that
    available() == 0, but available only returns an estimate and is not reliable. Now we assert that read() == -1
     */
    def "Utility convertStreamToBuffer available"() {
        setup:
        def data = getRandomByteArray(10)

        when:
        def flux = Utility.convertStreamToByteBuffer(new testBAIS(data), 10, 10, true)

        then: "When the stream is the right length but available always returns > 0, do not throw"
        StepVerifier.create(flux)
            .assertNext(){buffer -> assert buffer.compareTo(ByteBuffer.wrap(data)) == 0 }
            .verifyComplete()
        // subscribe multiple times and ensure data is same each time
        StepVerifier.create(flux)
            .assertNext(){buffer -> assert buffer.compareTo(ByteBuffer.wrap(data)) == 0 }
            .verifyComplete()

        when: "When the stream is actually longer than the length, throw"
        flux = Utility.convertStreamToByteBuffer(new testBAIS(data), 9, 10, true)

        then:
        StepVerifier.create(flux)
            .verifyError(IllegalStateException.class)
    }

    class testBAIS extends ByteArrayInputStream {

        testBAIS(byte[] data) {
            super(data)
        }

        @Override
        public synchronized int available() {
            return 10
        }
    }

    def "PageList custom deserializer"() {
        setup:
        def responseXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>  \n" +
            "<PageList>  \n" +
            "   <PageRange>  \n" +
            "      <Start>0</Start>  \n" +
            "      <End>511</End>  \n" +
            "   </PageRange>  \n" +
            "   <ClearRange>  \n" +
            "      <Start>512</Start>  \n" +
            "      <End>1023</End>  \n" +
            "   </ClearRange>  \n" +
            "   <PageRange>  \n" +
            "      <Start>1024</Start>  \n" +
            "      <End>2047</End>  \n" +
            "   </PageRange>  \n" +
            "</PageList>"

        when:
        def pageList = (PageList) new JacksonAdapter().deserialize(responseXml, PageList.class, SerializerEncoding.XML)

        then:
        pageList.getPageRange().size() == 2
        pageList.getClearRange().size() == 1
    }
}
