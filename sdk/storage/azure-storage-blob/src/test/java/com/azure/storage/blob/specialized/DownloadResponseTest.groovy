// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.http.HttpHeader
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.util.FluxUtil
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.HttpGetterInfo
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.DownloadRetryOptions
import reactor.core.Exceptions
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.Duration
import java.util.concurrent.TimeoutException

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

    def "Network call no etag returned"() {
        setup:
        def removeEtagPolicy = new HttpPipelinePolicy() {
            @Override
            Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                return next.process()
                .flatMap({ response ->
                    HttpHeader eTagHeader = response.getHeaders().get("eTag")
                    if (eTagHeader == null) {
                        return  Mono.just(response);
                    }
                    HttpHeaders headers = response.getHeaders()
                    headers.remove("eTag")
                    return  Mono.just(getStubDownloadResponse(response, response.getStatusCode(), response.getBody(), headers));
                })
            }
        }
        def bsc = getServiceClientBuilder(primaryCredential, primaryBlobServiceClient.getAccountUrl(), removeEtagPolicy).buildClient()
        def cc = bsc.getBlobContainerClient(containerName)
        def bu = cc.getBlobClient(bu.getBlobName()).getBlockBlobClient()

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
            .setCount(setCount ? flux.getScenarioData().remaining() : null)
            .setETag("etag")

        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5)

        when:
        ReliableDownload response = flux.setOptions(options).getter(info).block()

        then:
        FluxUtil.collectBytesInByteBufferStream(response.getValue()).block() == flux.getScenarioData().array()
        flux.getTryNumber() == tryNumber


        where:
        scenario                                                             | tryNumber | setCount
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK       | 1         | true
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK     | 1         | true
        DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES | 4         | true
        DownloadResponseMockFlux.DR_TEST_SCENARIO_NO_MULTIPLE_SUBSCRIPTION   | 4         | true
        DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA       | 1         | true // Range download
        DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_AFTER_ALL_DATA       | 1         | false // Non-range download
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

        where:
        scenario                                                       | exceptionType        | tryNumber
        DownloadResponseMockFlux.DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException          | 6
        DownloadResponseMockFlux.DR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception            | 1
        DownloadResponseMockFlux.DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | BlobStorageException | 2
    }

    @Unroll
    def "Info null IA"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK, this)
        def info = null

        when:
        new ReliableDownload(null, null, info, { HttpGetterInfo newInfo -> flux.getter(newInfo) })

        then:
        thrown(NullPointerException)
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

    @Unroll
    def "Timeout"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_TIMEOUT,
            this)
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(retryCount)
        HttpGetterInfo info = new HttpGetterInfo().setETag("etag")

        expect:
        StepVerifier.withVirtualTime({ flux.setOptions(options).getter(info)
            .flatMapMany({ it.getValue() }) })
            .expectSubscription()
            .thenAwait(Duration.ofSeconds((retryCount + 1) * 61))
            .verifyErrorMatches({ Exceptions.unwrap(it) instanceof TimeoutException })

        where:
        // We test retry count elsewhere. Just using small numbers to speed up the test.
        retryCount | _
        0          | _
        1          | _
    }
}
