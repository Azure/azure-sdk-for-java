// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.util.FluxUtil
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.HttpGetterInfo
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.DownloadRetryOptions
import spock.lang.Unroll

class DownloadResponseTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu.upload(defaultInputStream.get(), defaultText.length())
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */

    def "Network call"() {
        expect:
        OutputStream outputStream = new ByteArrayOutputStream()
        bu.download(outputStream)
        outputStream.toByteArray() == defaultData.array()
    }

    @Unroll
    def "Successful"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario, this)

        HttpGetterInfo info = new HttpGetterInfo()
            .setOffset(0)
            .setCount(flux.getScenarioData().remaining())
            .setETag("etag")

        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5)

        when:
        ReliableDownload response = flux.setOptions(options).getter(info).block()

        then:
        FluxUtil.collectBytesInByteBufferStream(response.getValue()).block() == flux.getScenarioData().array()
        flux.getTryNumber() == tryNumber


        where:
        scenario                                                             | tryNumber
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK       | 1
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK     | 1
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES | 4
    }

    @Unroll
    def "Failure"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario, this)
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5)
        HttpGetterInfo info = new HttpGetterInfo().setETag("etag")

        when:
        ReliableDownload response = flux.setOptions(options).getter(info).block()
        response.getValue().blockFirst()

        then:
        def e = thrown(Throwable) // Blocking subscribe will sometimes wrap the IOException in a RuntimeException.
        if (e.getCause() != null) {
            e = e.getCause()
        }
        exceptionType.isInstance(e)
        flux.getTryNumber() == tryNumber

        /*
        tryNumber is 7 because the initial request is the first try, then it will fail when retryCount>maxRetryCount,
        which is when retryCount=6 and therefore tryNumber=7
         */
        where:
        scenario                                                       | exceptionType        | tryNumber
        DownloadResponseMockFlux.DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException          | 7
        DownloadResponseMockFlux.DR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception            | 1
        DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | BlobStorageException | 2
    }

    @Unroll
    def "Info null IA"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK, this)

        when:
        new ReliableDownload(null, null, info, { HttpGetterInfo newInfo -> flux.getter(newInfo) })

        then:
        thrown(NullPointerException)

        where:
        info                               | _
        null                               | _
        new HttpGetterInfo().setETag(null) | _
    }

    def "Options IA"() {
        when:
        new DownloadRetryOptions().setMaxRetryRequests(-1)

        then:
        thrown(IllegalArgumentException)
    }

    def "Getter IA"() {
        when:
        new ReliableDownload(null, null, new HttpGetterInfo().setETag("etag"), null)

        then:
        thrown(NullPointerException)
    }

    def "Info"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_INFO_TEST, this)
        HttpGetterInfo info = new HttpGetterInfo()
            .setOffset(20)
            .setCount(10)
            .setETag("etag")

        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5)

        when:
        ReliableDownload response = flux.setOptions(options).getter(info).block()
        response.getValue().blockFirst()

        then:
        flux.getTryNumber() == 3
    }

    def "Info count IA"() {
        when:
        new HttpGetterInfo().setCount(-1)

        then:
        thrown(IllegalArgumentException)
    }
}
