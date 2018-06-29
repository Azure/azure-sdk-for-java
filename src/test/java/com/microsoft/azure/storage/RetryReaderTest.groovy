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
import com.microsoft.azure.storage.blob.RetryReader
import com.microsoft.azure.storage.blob.RetryReaderOptions
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.RestResponse
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
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

    def "RetryReader"() {
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
        // Null options and info parameters and null internal fields
        // Getter returns an error
        // Exceed max tryCount
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

        when:
        RetryReader reader = new RetryReader(null, info, options,
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
        scenario                                                            | tryNumber
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK       | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK     | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES | 4
    }

    @Unroll
    def "Failure"() {
        setup:
        def flowable = new RetryReaderMockFlowable(scenario)
        def info = new RetryReader.HTTPGetterInfo()
        info.offset = 0
        info.count = 3 // ignored.


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
        def e = thrown(Throwable) // Blocking subscribe will wrap the IOException in a RuntimeException.
        exceptionType.isInstance(e.getCause()) // The exception we throw is the cause of the RuntimeException
        flowable.getTryNumber() == tryNumber

        where:
        scenario | exceptionType | tryNumber
        RetryReaderMockFlowable.RR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException | 6
        RetryReaderMockFlowable.RR_TEST_SCENARIO_NON_RETRYABLE_ERROR | Exception | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_ERROR_GETTER_INITIAL | IOException | 1
        RetryReaderMockFlowable.RR_TEST_SCENARIO_ERROR_GETTER_MIDDLE | RestException | 2
    }
}
