// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized


import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.http.MockHttpResponse
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.DownloadRetryOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeoutException

class DownloadResponseMockFlux {
    static final int DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK = 0 // Data emitted in one chunk
    static final int DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK = 1 // Data emitted in multiple chunks
    static final int DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES = 2 // Stream failures successfully handled
    static final int DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED = 3 // We appropriate honor max retries
    static final int DR_TEST_SCENARIO_NON_RETRYABLE_ERROR = 4 // We will not retry on a non-retryable error
    static final int DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE = 6 // Throwing an error from the getter
    static final int DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION = 9 // We do not subscribe to the same stream twice
    static final int DR_TEST_SCENARIO_TIMEOUT = 10 // ReliableDownload with timeout after not receiving items for 60s
    static final int DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA = 11 // Don't actually issue another retry if we've read all the data and the source failed at the end

    private final int scenario
    private final ByteBuffer scenarioData

    private int tryNumber
    private DownloadRetryOptions options
    private boolean subscribed = false // Only used for multiple subscription test.

    DownloadResponseMockFlux(int scenario, APISpec apiSpec) {
        this.scenario = scenario

        if (scenario == DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK) {
            this.scenarioData = apiSpec.getRandomData(512 * 1024)
        } else if (scenario in [DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK, DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES,
                                DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION, DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA,
                                DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED, DR_TEST_SCENARIO_NON_RETRYABLE_ERROR,
                                DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE, DR_TEST_SCENARIO_TIMEOUT]) {
            this.scenarioData = apiSpec.getRandomData(1024)
        } else {
            throw new IllegalArgumentException("Invalid download resource test scenario.")
        }
    }

    /*
    For internal construction on NO_MULTIPLE_SUBSCRIPTION test
     */
    DownloadResponseMockFlux(int scenario, int tryNumber, ByteBuffer scenarioData,
        DownloadRetryOptions options) {
        this.scenario = scenario
        this.tryNumber = tryNumber
        this.scenarioData = scenarioData
        this.options = options
    }

    ByteBuffer getScenarioData() {
        return this.scenarioData
    }

    int getTryNumber() {
        return this.tryNumber
    }

    private Flux<ByteBuffer> getDownloadStream(long offset, Long count) {
        switch (this.scenario) {
            case DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                return Flux.just(scenarioData.duplicate())

            case DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                return Flux.range(0, 4).map({
                    def toSend = this.scenarioData.duplicate()
                    toSend.position(it * 256)
                    toSend.limit((it + 1) * 256)

                    return toSend
                })

            case DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION:
            case DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES:
                // Java 8 compiler didn't like the previous fall-through case structure
                if (this.scenario == DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION) {
                    if (this.subscribed) {
                        return Flux.error(new IllegalStateException("Cannot subscribe to the same flux twice"))
                    }
                    this.subscribed = true
                }

                if (this.tryNumber <= 3) {
                    // tryNumber is 1 indexed, so we have to sub 1.
                    if (offset != (this.tryNumber - 1L) * 256
                        || count != this.scenarioData.remaining() - (this.tryNumber - 1L) * 256) {
                        return Flux.error(new IllegalArgumentException("Info values are incorrect."))
                    }

                    def toSend = this.scenarioData.duplicate()
                    toSend.position((this.tryNumber - 1) * 256)
                    toSend.limit(this.tryNumber * 256)

                    def dataStream = Flux.just(toSend)

                    // A slightly odd but sufficient means of exercising the different retriable exceptions.
                    def e = tryNumber % 2 == 0 ? new IOException() : new TimeoutException()

                    return dataStream.concatWith(Flux.error(e))
                }
                if (offset != (this.tryNumber - 1L) * 256
                    || count != this.scenarioData.remaining() - (this.tryNumber - 1L) * 256) {
                    return Flux.error(new IllegalArgumentException("Info values are incorrect."))
                }
                def toSend = this.scenarioData.duplicate()
                toSend.position((this.tryNumber - 1) * 256)
                toSend.limit(this.tryNumber * 256)

                return Flux.just(toSend)

            case DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA:
                return Flux.just(scenarioData.duplicate()).concatWith(Flux.error(new IOException("Exception at end")))

            case DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED:
                return Flux.error(new IOException())

            case DR_TEST_SCENARIO_NON_RETRYABLE_ERROR:
                return Flux.error(new Exception())

            case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
                /*
                 * We return a retryable error here so we have to invoke the getter, which will throw an error in
                 * this case.
                 */
                return (this.tryNumber == 1)
                    ? Flux.error(new IOException())
                    : Flux.error(new IllegalArgumentException("Retried after getter error.")) as Flux<ByteBuffer>

            case DR_TEST_SCENARIO_TIMEOUT:
                return Flux.just(scenarioData.duplicate()).delayElements(Duration.ofSeconds(61))

            default:
                return Flux.error(new IllegalArgumentException("Invalid test case"))
        }
    }

    HttpPipelinePolicy asPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                tryNumber++
                def rangeHeader = context.getHttpRequest().getHeaders().get("x-ms-range")
                def eTag = context.getHttpRequest().getHeaders().getValue("if-match")
                def offset = 0L
                def count = null as Long
                if (rangeHeader != null) {
                    def ranges = rangeHeader.getValue().replace("bytes=", "").split("-")
                    offset = Long.parseLong(ranges[0])
                    if (ranges.length > 1) {
                        count = Long.parseLong(ranges[1]) - offset + 1
                    }
                }
                def finalOffset = offset
                def finalCount = count

                def response = new MockHttpResponse(null, 200) {
                    @Override
                    Flux<ByteBuffer> getBody() {
                        return getDownloadStream(finalOffset, finalCount)
                    }
                }
                def contentUpperBound = finalCount == null
                    ? scenarioData.remaining() - 1 : finalOffset + finalCount - 1
                response.addHeader("Content-Range", String.format("%d-%d/%d",
                    finalOffset, contentUpperBound, scenarioData.remaining()))

                switch (scenario) {
                    case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
                        switch (tryNumber) {
                            case 1:
                                return Mono.just(response)
                            case 2:
                                /*
                                 This validates that we don't retry in the getter even if it's a retryable error from the
                                 service.
                                 */
                                throw new BlobStorageException("Message", new HttpResponse(null) {
                                    @Override
                                    int getStatusCode() {
                                        return 500
                                    }

                                    @Override
                                    String getHeaderValue(String s) {
                                        return null
                                    }

                                    @Override
                                    HttpHeaders getHeaders() {
                                        return null
                                    }

                                    @Override
                                    Flux<ByteBuffer> getBody() {
                                        return null
                                    }

                                    @Override
                                    Mono<byte[]> getBodyAsByteArray() {
                                        return null
                                    }

                                    @Override
                                    Mono<String> getBodyAsString() {
                                        return null
                                    }

                                    @Override
                                    Mono<String> getBodyAsString(Charset charset) {
                                        return null
                                    }
                                }, null)
                            default:
                                throw new IllegalArgumentException("Retried after error in getter")
                        }
                    case DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION:
                        // Construct a new flux each time to mimic getting a new download stream.
                        // Construct a new flux each time to mimic getting a new download stream.
                        def nextFlux = new DownloadResponseMockFlux(scenario, tryNumber, scenarioData, options)
                        def newResponse = new MockHttpResponse(null, 200) {
                            @Override
                            Flux<ByteBuffer> getBody() {
                                return nextFlux.getDownloadStream(finalOffset, finalCount)
                            }
                        }
                        return Mono.just(newResponse)
                    default:
                        return Mono.just(response)
                }
            }
        }
    }
}
