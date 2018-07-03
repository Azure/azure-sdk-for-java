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
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.RetryReader
import com.microsoft.azure.storage.blob.RetryReaderOptions
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.RestResponse
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Single
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.util.function.Function

class RetryReaderTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null,
                null).blockingGet()
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */

    def "Network call"() {
        setup:
        def info = new RetryReader.HTTPGetterInfo()
        info.offset = 0
        info.count = defaultData.remaining()

        def options = new RetryReaderOptions()
        options.maxRetryRequests = 5

        expect:
        FlowableUtil.collectBytesInBuffer(new RetryReader(bu.download(null, null, false), info, options, new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
            @Override
            Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo httpGetterInfo) {
                bu.download(new BlobRange(httpGetterInfo.offset, httpGetterInfo.count), null, false)
            }
        })).blockingGet() == defaultData

        // Go DownloadResponse, Download, DownloadResponse.body

        // Test with the different kinds of errors that are retryable: Timeout, IOException, 500, 503--assert that the data at the end is still the same - Use the RetryTestFactory (or similar)
        // Another policy which returns a custom flowable that injects an error after a certain amount of data.
        // Different values of options. Valid and invalid. See Adam's comment on CR about count and offset.
        // Null options and info parameters and null internal fields (null count)
    }

    @Unroll
    def "Successful"() {
        setup:
        RetryReaderMockFlowable flowable = new RetryReaderMockFlowable(scenario)
        def info = new RetryReader.HTTPGetterInfo()
        info.offset = 0
        info.count = flowable.getScenarioData().remaining()

        def options = new RetryReaderOptions()
        options.maxRetryRequests = 5

        def initialResponse = null
        if (provideInitialResponse) {
            initialResponse = flowable.getter(new RetryReader.HTTPGetterInfo())
        }
        when:
        RetryReader reader = new RetryReader(initialResponse, info, options,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        flowable.getter(i)
                    }
                })

        then:
        FlowableUtil.collectBytesInBuffer(reader).blockingGet() == flowable.getScenarioData()
        flowable.getTryNumber() == tryNumber

        where:
        scenario                                                             | tryNumber | provideInitialResponse
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK        | 1         | false
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK      | 1         | false
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES  | 4         | false
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_INITIAL_RESPONSE | 1         | true
    }

    @Unroll
    def "Failure"() {
        setup:
        def flowable = new RetryReaderMockFlowable(scenario)

        def options = new RetryReaderOptions()
        options.maxRetryRequests = 5

        when:
        RetryReader reader = new RetryReader(null, null, options,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        flowable.getter(i)
                    }
                })
        reader.blockingSubscribe()

        then:
        def e = thrown(Throwable) // Blocking subscribe will wrap the IOException in a RuntimeException.
        exceptionType.isInstance(e.getCause()) // The exception we throw is the cause of the RuntimeException
        flowable.getTryNumber() == tryNumber

        /*
        tryNumber is 7 because the initial request is the first try, then it will fail when retryCount>maxRetryCount,
        which is when retryCount=6 and therefore tryNumber=6
         */
        where:
        scenario                                                      | exceptionType | tryNumber
        RetryReaderMockFlowable.RR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException   | 7
        RetryReaderMockFlowable.RR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception     | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_ERROR_GETTER_INITIAL | IOException   | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | RestException | 2
    }

    @Unroll
    def "Nulls"() {
        setup:
        def flowable = new RetryReaderMockFlowable(RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        RetryReader reader = new RetryReader(null, null, options,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        flowable.getter(i)
                    }
                })
        reader.blockingSubscribe()

        then:
        FlowableUtil.collectBytesInBuffer(reader).blockingGet() == flowable.getScenarioData()
        flowable.getTryNumber() == tryNumber

        where:
        info                             | options                  | tryNumber
        null                             | new RetryReaderOptions() | 1
        new RetryReader.HTTPGetterInfo() | null                     | 1
    }

    def "Options fail"() {
        setup:
        def flowable = new RetryReaderMockFlowable(RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        def options = new RetryReaderOptions()
        options.maxRetryRequests = -1

        when:
        RetryReader reader = new RetryReader(null, null, options,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        flowable.getter(i)
                    }
                })
        reader.blockingSubscribe()

        then:
        thrown(IllegalArgumentException)
    }

    def "Getter fail"() {
        setup:
        def flowable = new RetryReaderMockFlowable(RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        RetryReader reader = new RetryReader(null, null, null, null)
        reader.blockingSubscribe()

        then:
        thrown(IllegalArgumentException)
    }

    def "Info"() {
        setup:
        def flowable = new RetryReaderMockFlowable(RetryReaderMockFlowable.RR_TEST_SCENARIO_INFO_TEST)
        def info = new RetryReader.HTTPGetterInfo()
        info.offset = 20
        info.count = 10
        info.eTag = new ETag("etag")
        def options = new RetryReaderOptions()
        options.maxRetryRequests = 5

        when:
        RetryReader reader = new RetryReader(null, info, options,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        flowable.getter(i)
                    }
                })
        reader.blockingSubscribe()

        then:
        flowable.tryNumber == 3
    }

    def "Info fail"() {
        setup:
        def info = new RetryReader.HTTPGetterInfo()
        info.count = -1

        when:
        new RetryReader(null, info, null,
                new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
                    @Override
                    Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo i) {
                        return null
                    }
                })

        then:
        thrown(IllegalArgumentException)
    }
}
