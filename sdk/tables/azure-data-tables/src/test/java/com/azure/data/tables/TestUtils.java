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
import com.azure.data.tables.models.TableServiceProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    static void assertPropertiesEquals(TableServiceProperties expected,
                                       TableServiceProperties actual) {
        if (expected.getLogging() != null && actual.getLogging() != null) {
            assertEquals(expected.getLogging().isReadLogged(), actual.getLogging().isReadLogged());
            assertEquals(expected.getLogging().isDeleteLogged(), actual.getLogging().isDeleteLogged());
            assertEquals(expected.getLogging().isWriteLogged(), actual.getLogging().isWriteLogged());
            assertEquals(expected.getLogging().getAnalyticsVersion(), actual.getLogging().getAnalyticsVersion());

            if (expected.getLogging().getRetentionPolicy() != null
                && actual.getLogging().getRetentionPolicy() != null) {

                assertEquals(expected.getLogging().getRetentionPolicy().getDaysToRetain(),
                    actual.getLogging().getRetentionPolicy().getDaysToRetain());
                assertEquals(expected.getLogging().getRetentionPolicy().isEnabled(),
                    actual.getLogging().getRetentionPolicy().isEnabled());
            } else {
                assertNull(expected.getLogging().getRetentionPolicy());
                assertNull(actual.getLogging().getRetentionPolicy());
            }
        } else {
            assertNull(expected.getLogging());
            assertNull(actual.getLogging());
        }

        if (expected.getCorsRules() != null && actual.getCorsRules() != null) {
            assertEquals(expected.getCorsRules().size(), actual.getCorsRules().size());

            for (int i = 0; i < expected.getCorsRules().size(); i++) {
                assertEquals(expected.getCorsRules().get(i).getAllowedMethods(),
                    actual.getCorsRules().get(i).getAllowedMethods());
                assertEquals(expected.getCorsRules().get(i).getAllowedHeaders(),
                    actual.getCorsRules().get(i).getAllowedHeaders());
                assertEquals(expected.getCorsRules().get(i).getAllowedOrigins(),
                    actual.getCorsRules().get(i).getAllowedOrigins());
                assertEquals(expected.getCorsRules().get(i).getExposedHeaders(),
                    actual.getCorsRules().get(i).getExposedHeaders());
                assertEquals(expected.getCorsRules().get(i).getMaxAgeInSeconds(),
                    actual.getCorsRules().get(i).getMaxAgeInSeconds());
            }
        } else {
            assertNull(expected.getCorsRules());
            assertNull(actual.getCorsRules());
        }

        if (expected.getHourMetrics() != null && actual.getHourMetrics() != null) {
            assertEquals(expected.getHourMetrics().isEnabled(), actual.getHourMetrics().isEnabled());
            assertEquals(expected.getHourMetrics().isIncludeApis(), actual.getHourMetrics().isIncludeApis());
            assertEquals(expected.getHourMetrics().getVersion(), actual.getHourMetrics().getVersion());

            if (expected.getHourMetrics().getTableServiceRetentionPolicy() != null
                && actual.getHourMetrics().getTableServiceRetentionPolicy() != null) {

                assertEquals(expected.getHourMetrics().getTableServiceRetentionPolicy().isEnabled(),
                    actual.getHourMetrics().getTableServiceRetentionPolicy().isEnabled());
                assertEquals(expected.getHourMetrics().getTableServiceRetentionPolicy().getDaysToRetain(),
                    actual.getHourMetrics().getTableServiceRetentionPolicy().getDaysToRetain());
            } else {
                assertNull(expected.getHourMetrics().getTableServiceRetentionPolicy());
                assertNull(actual.getHourMetrics().getTableServiceRetentionPolicy());
            }
        } else {
            assertNull(expected.getHourMetrics());
            assertNull(actual.getHourMetrics());
        }

        if (expected.getMinuteMetrics() != null && actual.getMinuteMetrics() != null) {
            assertEquals(expected.getMinuteMetrics().isEnabled(), actual.getMinuteMetrics().isEnabled());
            assertEquals(expected.getMinuteMetrics().isIncludeApis(), actual.getMinuteMetrics().isIncludeApis());
            assertEquals(expected.getMinuteMetrics().getVersion(), actual.getMinuteMetrics().getVersion());

            if (expected.getMinuteMetrics().getTableServiceRetentionPolicy() != null
                && actual.getMinuteMetrics().getTableServiceRetentionPolicy() != null) {

                assertEquals(expected.getMinuteMetrics().getTableServiceRetentionPolicy().isEnabled(),
                    actual.getMinuteMetrics().getTableServiceRetentionPolicy().isEnabled());
                assertEquals(expected.getMinuteMetrics().getTableServiceRetentionPolicy().getDaysToRetain(),
                    actual.getMinuteMetrics().getTableServiceRetentionPolicy().getDaysToRetain());
            } else {
                assertNull(expected.getMinuteMetrics().getTableServiceRetentionPolicy());
                assertNull(actual.getMinuteMetrics().getTableServiceRetentionPolicy());
            }
        } else {
            assertNull(expected.getMinuteMetrics());
            assertNull(actual.getMinuteMetrics());
        }
    }
}
