// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * This example shows how to use request level timeouts. These timeouts relate to the round trip time for an individual
 * request. It is the time between the request leaving the client and the response headers being received by the client.
 * These options offer a mid-level granularity. If one of these values times out, it will be automatically retried.
 * Note that for write operations in particular, this includes the time it takes to complete writing the body as the
 * service will not return a response until this is finished.
 */
public class RequestLevelTimeoutExample {

    /**
     * Entry point into the basic examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */
    public static void main(String[] args) throws IOException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);


        /*
        Use a Request Retry Policy that has a fixed back-off retry policy.
         */
        RequestRetryOptions retryOptions = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 3, 1000L, 1500L, null);
        HttpResponse mockHttpResponse = new MockHttpResponse(new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")), 202);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential, retry options, and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .retryOptions(retryOptions)
            .addPolicy(new TimeoutPolicy(mockHttpResponse))
            .buildClient();

        /*
         * Make a call on the client to trigger the pipeline policy.
         */
        try {
            storageClient.getProperties();
        } catch (Exception ex) {
            if (ex.getCause() instanceof TimeoutException) {
                System.out.println("Operation failed due to timeout: " + ex.getMessage());
            }
        }
    }

    /**
     * A simple policy that sets duration timeout per call of 5 seconds.
     */
    static class TimeoutPolicy implements HttpPipelinePolicy {

        HttpResponse mockHttpResponse;
        int count;

        TimeoutPolicy(HttpResponse httpResponse) {
            mockHttpResponse = httpResponse;
            count = 0;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            System.out.println("Number of retries: " + ++count);
            return Mono.just(mockHttpResponse).delayElement(Duration.ofSeconds(5L));
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    static class MockHttpResponse extends HttpResponse {
        int statusCode;

        protected MockHttpResponse(HttpRequest request, int code) {
            super(request);
            this.statusCode = code;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String s) {
            return null;
        }


        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(new byte[0]);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just("");
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just("");
        }
    }
}
