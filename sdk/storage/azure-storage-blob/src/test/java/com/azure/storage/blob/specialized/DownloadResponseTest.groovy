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
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.DownloadRetryOptions
import reactor.core.Exceptions
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.util.concurrent.TimeoutException

class DownloadResponseTest extends APISpec {
    BlockBlobClient bu

    def setup() {
        bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu.upload(data.defaultInputStream, data.defaultText.length())
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */

    def "Network call"() {
        expect:
        OutputStream outputStream = new ByteArrayOutputStream()
        bu.download(outputStream)
        outputStream.toByteArray() == data.defaultBytes
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
        def bsc = getServiceClientBuilder(env.primaryAccount.credential, primaryBlobServiceClient.getAccountUrl(), removeEtagPolicy).buildClient()
        def cc = bsc.getBlobContainerClient(containerName)
        def bu = cc.getBlobClient(bu.getBlobName()).getBlockBlobClient()

        expect:
        OutputStream outputStream = new ByteArrayOutputStream()
        bu.download(outputStream)
        outputStream.toByteArray() == data.defaultBytes
    }

    @Unroll
    def "Successful"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(scenario, this)

        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5)

        def bsc = getServiceClientBuilder(env.primaryAccount.credential, primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient()
        def cc = bsc.getBlobContainerAsyncClient(containerName)
        def bu = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient()
        BlobRange range = setCount ? new BlobRange(0, flux.getScenarioData().remaining()) : new BlobRange(0);

        when:
        def response = bu.downloadStreamWithResponse(range, options, null, false).block()

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

        def bsc = getServiceClientBuilder(env.primaryAccount.credential, primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient()
        def cc = bsc.getBlobContainerAsyncClient(containerName)
        def bu = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient()

        when:
        def response = bu.downloadStreamWithResponse(null, options, null, false).block()
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
    def "Timeout"() {
        setup:
        DownloadResponseMockFlux flux = new DownloadResponseMockFlux(DownloadResponseMockFlux.DR_TEST_SCENARIO_TIMEOUT,
            this)
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(retryCount)

        def bsc = getServiceClientBuilder(env.primaryAccount.credential, primaryBlobServiceClient.getAccountUrl(), flux.asPolicy()).buildAsyncClient()
        def cc = bsc.getBlobContainerAsyncClient(containerName)
        def bu = cc.getBlobAsyncClient(bu.getBlobName()).getBlockBlobAsyncClient()

        when:
        def bufferMono = bu.downloadStreamWithResponse(null, options, null, false)
            .flatMapMany({ it.getValue() })

        then:
        StepVerifier.create(bufferMono)
            .expectSubscription()
            .verifyErrorMatches({ Exceptions.unwrap(it) instanceof TimeoutException })

        where:
        // We test retry count elsewhere. Just using small numbers to speed up the test.
        retryCount | _
        0          | _
        1          | _
    }
}
