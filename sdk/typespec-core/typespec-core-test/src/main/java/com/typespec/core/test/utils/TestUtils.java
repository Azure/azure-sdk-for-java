// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.Context;
import com.typespec.core.util.HttpClientOptions;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains utility methods used for testing.
 */
public final class TestUtils {

    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    private static final String RECORD_FOLDER = "session-records/";

    private static final HttpHeaderName UPSTREAM_URI_HEADER = HttpHeaderName.fromString("X-Upstream-Base-Uri");
    private static final HttpHeaderName HTTP_FAULT_INJECTOR_RESPONSE_HEADER
        = HttpHeaderName.fromString("x-ms-faultinjector-response-option");

    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays.
     * <p>
     * If the arrays aren't equal this will call {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage
     * of the better error message, but this is the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param actual The actual byte array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            Assertions.assertArrayEquals(expected, actual);
        }
    }

    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays and allows for
     * comparing subsections of the arrays.
     * <p>
     * If the arrays aren't equal this will copy the array ranges and call
     * {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage of the better error message, but this is
     * the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param expectedOffset Starting offset to begin comparing in the expected array.
     * @param actual The actual byte array.
     * @param actualOffset Starting offset to begin comparing in the actual array.
     * @param length Amount of bytes to compare.
     */
    public static void assertArraysEqual(byte[] expected, int expectedOffset, byte[] actual,
        int actualOffset, int length) {
        // Use ByteBuffer comparison as it provides an optimized byte array comparison.
        // In Java 9+ there is Arrays.mismatch that provides this functionality directly, but Java 8 needs support.
        assertByteBuffersEqual(ByteBuffer.wrap(expected, expectedOffset, length),
            ByteBuffer.wrap(actual, actualOffset, length));
    }

    /**
     * Asserts that two {@link ByteBuffer ByteBuffers} are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the ByteBuffers.
     * <p>
     * If the ByteBuffers aren't equal this will copy the ByteBuffer contents into byte arrays and call
     * {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage of the better error message, but this is
     * the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected {@link ByteBuffer}.
     * @param actual The actual {@link ByteBuffer}.
     */
    public static void assertByteBuffersEqual(ByteBuffer expected, ByteBuffer actual) {
        int expectedPosition = 0;
        int actualPosition = 0;
        if (expected != null) {
            expectedPosition = expected.position();
        }

        if (actual != null) {
            actualPosition = actual.position();
        }

        if (!Objects.equals(expected, actual)) {
            // Reset the ByteBuffers in case their position was changed.
            byte[] expectedArray = null;
            if (expected != null) {
                expected.position(expectedPosition);
                expectedArray = new byte[expected.remaining()];
                expected.get(expectedArray);
            }

            byte[] actualArray = null;
            if (actual != null) {
                actual.position(actualPosition);
                actualArray = new byte[actual.remaining()];
                actual.get(actualArray);
            }

            Assertions.assertArrayEquals(expectedArray, actualArray);
        }
    }

    /**
     * Get the {@link File} pointing to the folder where session records live.
     *
     * @return The session-records folder.
     * @throws IllegalStateException if the session-records folder cannot be found.
     */
    public static File getRecordFolder() {
        URL folderUrl = TestUtils.class.getClassLoader().getResource(RECORD_FOLDER);

        if (folderUrl != null) {
            // Use toURI as getResource will return a URL encoded file path that can only be cleaned up using the
            // URI-based constructor of File.
            return new File(toURI(folderUrl));
        }

        throw new IllegalStateException("Unable to locate session-records folder. Please create a session-records "
            + "folder in '/src/test/resources' of the module (ex. for azure-core-test this is "
            + "'/sdk/core/azure-core-test/src/test/resources/session-records').");
    }


    /**
     *  Returns a {@link java.net.URI} equivalent to this URL.
     * @param url the url to be converted to URI
     * @return the URI
     */
    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
        }
    }

    private TestUtils() {
    }

    /**
     * Locates the root of the current repo until the provided folder's parent.
     *
     * @param testClassPath the test class path
     * @param resolveFolder the folder parent to resolve the path until
     * @return The {@link Path} to the root of the repo.
     * @throws RuntimeException The specified folder could not be located.
     */
    public static Path getRepoRootResolveUntil(Path testClassPath, String resolveFolder) {
        String repoName = "\\azure-sdk-for-java";
        Path path = testClassPath;
        Path candidate = null;
        while (path != null && !path.endsWith(repoName)) {
            candidate = path.resolve(resolveFolder);
            if (Files.exists(candidate)) {
                break;
            }
            path = path.getParent();
        }
        if (path == null) {
            throw new RuntimeException(String.format(
                "Could not locate %s folder within repository %s", resolveFolder, repoName));
        }
        return path;
    }

    /**
     * Wraps an {@link HttpClient} to make calls to
     * <a href="https://github.com/Azure/azure-sdk-tools/tree/main/tools/http-fault-injector">HTTP fault injector</a>
     * to test random network failures.
     * <p>
     * Using the {@link HttpClient} returned by this method requires all setup required by HTTP fault injector to be
     * configured. {@code useHttps} determines whether requests are forwarded to HTTP fault injector using HTTPS or
     * HTTP, using HTTP doesn't require the self-signed certificate used by HTTP fault injector to be trusted by the JVM
     * making it easier to prototype tests using HTTP fault injector. Merge ready tests should always use HTTPS.
     * <p>
     * The {@link HttpClient} returned will use the default successful and failure response percentages. 75% of request
     * will succeed, 24% of requests will fail with a partial body returned, and 1% of requests will never return a
     * response. It is recommended for tests using HTTP fault injector to set
     * {@link HttpClientOptions#setResponseTimeout(Duration)} and {@link HttpClientOptions#setReadTimeout(Duration)}, or
     * the equivalent methods directly in the HTTP client builder being used, as the default timeouts are 60 seconds and
     * with responses failing randomly reducing the timeout will let the tests run faster while still generally testing
     * the same.
     *
     * @param clientToWrap The {@link HttpClient} being wrapped that will send the actual request.
     * @param useHttps Whether HTTPS should be used to communicate with HTTP fault injector.
     * @return An {@link HttpClient} that forwards requests to HTTP fault injector with automatic fault injection
     * handling to run tests with flaky network.
     */
    public static HttpClient getFaultInjectingHttpClient(HttpClient clientToWrap, boolean useHttps) {
        return getFaultInjectingHttpClient(clientToWrap, useHttps, 75, 24, 1);
    }

    /**
     * Wraps an {@link HttpClient} to make calls to
     * <a href="https://github.com/Azure/azure-sdk-tools/tree/main/tools/http-fault-injector">HTTP fault injector</a>
     * to test random network failures.
     * <p>
     * Using the {@link HttpClient} returned by this method requires all setup required by HTTP fault injector to be
     * configured. {@code useHttps} determines whether requests are forwarded to HTTP fault injector using HTTPS or
     * HTTP, using HTTP doesn't require the self-signed certificate used by HTTP fault injector to be trusted by the JVM
     * making it easier to prototype tests using HTTP fault injector. Merge ready tests should always use HTTPS.
     * <p>
     * The {@link HttpClient} returned will use the specified successful and failure response percentages. The
     * combination of {@code successRate}, {@code partialRate}, and {@code failureRate} must equal 100, if not an
     * {@link IllegalArgumentException} will be thrown. An {@link IllegalArgumentException} will also be thrown if any
     * of the values are negative. It is recommended for tests using HTTP fault injector to set
     * {@link HttpClientOptions#setResponseTimeout(Duration)} and {@link HttpClientOptions#setReadTimeout(Duration)}, or
     * the equivalent methods directly in the HTTP client builder being used, as the default timeouts are 60 seconds and
     * with responses failing randomly reducing the timeout will let the tests run faster while still generally testing
     * the same.
     *
     * @param clientToWrap The {@link HttpClient} being wrapped that will send the actual request.
     * @param useHttps Whether HTTPS should be used to communicate with HTTP fault injector.
     * @param successRate Percent of requests that will succeed.
     * @param partialRate Percent of requests that will partially succeed.
     * @param failureRate Percent of requests that will fail.
     * @return An {@link HttpClient} that forwards requests to HTTP fault injector with automatic fault injection
     * handling to run tests with flaky network.
     * @throws IllegalArgumentException If {@code successRate}, {@code partialRate}, and {@code failureRate} don't add
     * up to 100 or if any of the values are negative.
     */
    public static HttpClient getFaultInjectingHttpClient(HttpClient clientToWrap, boolean useHttps, int successRate,
        int partialRate, int failureRate) {
        if (successRate + partialRate + failureRate != 100
            || successRate < 0 || partialRate < 0 || failureRate < 0) {
            throw LOGGER.atError()
                .addKeyValue("successRate", successRate)
                .addKeyValue("partialRage", partialRate)
                .addKeyValue("failureRate", failureRate)
                .log(new IllegalStateException(
                    "'successRate', 'partialRate', and 'failureRate' must add to 100 and no values can be negative."));
        }

        return new HttpFaultInjectingHttpClient(clientToWrap, useHttps, successRate, partialRate);
    }

    private static final class HttpFaultInjectingHttpClient implements HttpClient {
        private final HttpClient wrappedHttpClient;
        private final boolean useHttps;
        private final int successRate;
        private final int partialRate;

        HttpFaultInjectingHttpClient(HttpClient wrappedHttpClient, boolean useHttps, int successRate, int partialRate) {
            this.wrappedHttpClient = wrappedHttpClient;
            this.useHttps = useHttps;
            this.successRate = successRate;
            this.partialRate = partialRate;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return send(request, Context.NONE);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
            String faultType = faultInjectorHandling();
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

            return wrappedHttpClient.send(request, context)
                .map(response -> {
                    HttpRequest request1 = response.getRequest();
                    request1.getHeaders().remove(UPSTREAM_URI_HEADER);
                    request1.setUrl(originalUrl);

                    return response;
                });
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
            String faultType = faultInjectorHandling();
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

            HttpResponse response = wrappedHttpClient.sendSync(request, context);
            response.getRequest().setUrl(originalUrl);
            response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER);

            return response;
        }

        private URL rewriteUrl(URL originalUrl) {
            try {
                return UrlBuilder.parse(originalUrl)
                    .setScheme(useHttps ? "https" : "http")
                    .setHost("localhost")
                    .setPort(useHttps ? 7778 : 7777)
                    .toUrl();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        private String faultInjectorHandling() {
            // f: Full response
            // p: Partial Response (full headers, 50% of body), then wait indefinitely
            // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
            // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
            // pn: Partial Response (full headers, 50% of body), then finish normally
            // n: No response, then wait indefinitely
            // nc: No response, then close (TCP FIN)
            // na: No response, then abort (TCP RST)
            double random = ThreadLocalRandom.current().nextDouble();
            int choice = (int) (random * 100);

            if (choice >= (100 - successRate)) {
                // 75% of requests complete without error.
                return "f";
            } else if (choice >= (100 - successRate - partialRate)) {
                if (random <= 0.34D) {
                    return "n";
                } else if (random <= 0.67D) {
                    return "nc";
                } else {
                    return "na";
                }
            } else {
                if (random <= 0.25D) {
                    return "p";
                } else if (random <= 0.50D) {
                    return "pc";
                } else if (random <= 0.75D) {
                    return "pa";
                } else {
                    return "pn";
                }
            }
        }
    }
}
