// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob

import com.microsoft.azure.storage.APISpec
import com.microsoft.azure.storage.blob.models.StorageErrorException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

class DownloadResponseTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null, null, null).blockingGet()
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */
    def "Network call"() {
        expect:
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false, null).blockingGet().body(null))
                .blockingGet() == defaultData
    }

    @Unroll
    def "Successful"() {
        setup:
        DownloadResponseMockFlowable flowable = new DownloadResponseMockFlowable(scenario)
        def info = new HTTPGetterInfo()
        info.withOffset(0)
                .withCount(flowable.getScenarioData().remaining())
                .withETag("etag")

        def options = new ReliableDownloadOptions()
        options.withMaxRetryRequests(5)

        def mockRawResponse = flowable.getter(info).blockingGet().rawResponse()

        when:
        DownloadResponse response = new DownloadResponse(mockRawResponse, info, { HTTPGetterInfo newInfo ->
            flowable.getter(newInfo)
        })

        then:
        FlowableUtil.collectBytesInBuffer(response.body(options)).blockingGet() == flowable.getScenarioData()
        flowable.getTryNumber() == tryNumber


        where:
        scenario                                                                 | tryNumber | provideInitialResponse
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK       | 1         | false
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK     | 1         | false
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES | 4         | false
    }

    @Unroll
    def "Failure"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(scenario)

        def options = new ReliableDownloadOptions()
                .withMaxRetryRequests(5)

        def info = new HTTPGetterInfo().withETag("etag")
        def mockRawResponse = flowable.getter(info).blockingGet().rawResponse()

        when:
        DownloadResponse response = new DownloadResponse(mockRawResponse, info, { HTTPGetterInfo newInfo ->
            flowable.getter(newInfo)
        })
        response.body(options).blockingSubscribe()

        then:
        def e = thrown(Throwable) // Blocking subscribe will sometimes wrap the IOException in a RuntimeException.
        if (e.getCause() != null) {
            e = e.getCause()
        }
        exceptionType.isInstance(e)
        flowable.getTryNumber() == tryNumber

        /*
        tryNumber is 7 because the initial request is the first try, then it will fail when retryCount>maxRetryCount,
        which is when retryCount=6 and therefore tryNumber=7
         */
        where:
        scenario                                                           | exceptionType         | tryNumber
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException           | 7
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception             | 1
        DownloadResponseMockFlowable.DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | StorageErrorException | 2
    }

    @Unroll
    def "Info null IA"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(
                DownloadResponseMockFlowable.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        new DownloadResponse(flowable.getter(info).blockingGet().rawResponse(), info,
                { HTTPGetterInfo newInfo ->
                    flowable.getter(newInfo)
                })


        then:
        thrown(IllegalArgumentException)

        where:
        info                                | _
        null                                | _
        new HTTPGetterInfo().withETag(null) | _
    }

    def "Options IA"() {
        when:
        new ReliableDownloadOptions().withMaxRetryRequests(-1)

        then:
        thrown(IllegalArgumentException)
    }

    def "Getter IA"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(
                DownloadResponseMockFlowable.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        def response = new DownloadResponse(flowable.getter(new HTTPGetterInfo()).blockingGet()
                .rawResponse(), new HTTPGetterInfo().withETag("etag"), null)
        response.body(null).blockingSubscribe()

        then:
        thrown(IllegalArgumentException)
    }

    def "Info"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(DownloadResponseMockFlowable.DR_TEST_SCENARIO_INFO_TEST)
        def info = new HTTPGetterInfo()
        info.withOffset(20)
        info.withCount(10)
        info.withETag("etag")
        def options = new ReliableDownloadOptions()
        options.withMaxRetryRequests(5)

        when:
        def response = new DownloadResponse(flowable.getter(info).blockingGet().rawResponse(), info,
                { HTTPGetterInfo newInfo ->
                    return flowable.getter(newInfo)
                })
        response.body(options).blockingSubscribe()

        then:
        flowable.tryNumber == 3
    }

    def "Info count IA"() {
        when:
        new HTTPGetterInfo().withCount(-1)

        then:
        thrown(IllegalArgumentException)
    }
}
