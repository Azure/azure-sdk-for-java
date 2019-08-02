// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.implementation.util.FluxUtil
import com.azure.storage.blob.models.ReliableDownloadOptions
import com.azure.storage.blob.models.StorageErrorException
import spock.lang.Unroll

class DownloadResponseTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cu.getBlockBlobClient(generateBlobName())
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
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario)

        HTTPGetterInfo info = new HTTPGetterInfo()
            .offset(0)
            .count(flux.getScenarioData().remaining())
            .eTag("etag")

        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5)

        when:
        DownloadAsyncResponse response = flux.getter(info).block()

        then:
        FluxUtil.collectByteBufStream(response.body(options), false).block().nioBuffer() == flux.getScenarioData()
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
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario)
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5)
        HTTPGetterInfo info = new HTTPGetterInfo().eTag("etag")

        when:
        DownloadAsyncResponse response = flux.getter(info).block()
        response.body(options).blockFirst()

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
        scenario                                                       | exceptionType         | tryNumber
        DownloadResponseMockFlux.DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException           | 7
        DownloadResponseMockFlux.DR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception             | 1
        DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | StorageErrorException | 2
    }

    @Unroll
    def "Info null IA"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        new DownloadAsyncResponse(flux.getter(info).block().rawResponse(), info, { HTTPGetterInfo newInfo -> flux.getter(newInfo) })

        then:
        thrown(IllegalArgumentException)

        where:
        info                                      | _
        null                                      | _
        new HTTPGetterInfo().eTag(null)     | _
    }

    def "Options IA"() {
        when:
        new ReliableDownloadOptions().maxRetryRequests(-1)

        then:
        thrown(IllegalArgumentException)
    }

    def "Getter IA"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        DownloadAsyncResponse response = new DownloadAsyncResponse(flux.getter(new HTTPGetterInfo()).block()
            .rawResponse(), new HTTPGetterInfo().eTag("etag"), null)
        response.body(null).blockFirst()

        then:
        thrown(IllegalArgumentException)
    }

    def "Info"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_INFO_TEST)
        HTTPGetterInfo info = new HTTPGetterInfo()
            .offset(20)
            .count(10)
            .eTag("etag")

        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5)

        when:
        DownloadAsyncResponse response = flux.getter(info).block()
        response.body(options).blockFirst()

        then:
        flux.getTryNumber() == 3
    }

    def "Info count IA"() {
        when:
        new HTTPGetterInfo().count(-1)

        then:
        thrown(IllegalArgumentException)
    }
}
