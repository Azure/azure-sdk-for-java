// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    /**
     * Gets the connection string for running tests.
     *
     * @param isPlaybackMode {@code true} if the code is not running against a live service. false otherwise.
     *
     * @return The corresponding connection string.
     */
    public static String getConnectionString(boolean isPlaybackMode) {
        return isPlaybackMode
            ? "DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net"
            : System.getenv("AZURE_TABLES_CONNECTION_STRING");
    }

    public static HttpRequest request(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.HEAD,
            new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty());
    }

    public static final class FreshDateTestClient implements HttpClient {
        private DateTimeRfc1123 firstDate;
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = convertToDateObject(request.getHeaders().getValue("Date"));

                return Mono.error(new IOException("IOException!"));
            }

            assert !firstDate.equals(convertToDateObject(request.getHeaders().getValue("Date")));

            return Mono.just(new MockHttpResponse(request, 200));
        }
        private static DateTimeRfc1123 convertToDateObject(String dateHeader) {
            if (CoreUtils.isNullOrEmpty(dateHeader)) {
                throw new RuntimeException("Failed to set 'Date' header.");
            }

            return new DateTimeRfc1123(dateHeader);
        }
    }

    static class PerCallPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");
            return next.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    static class PerRetryPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");
            return next.process();
        }
    }
}
