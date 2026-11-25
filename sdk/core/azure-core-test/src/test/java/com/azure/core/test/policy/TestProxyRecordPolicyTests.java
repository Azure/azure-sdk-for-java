// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestProxyRecordPolicyTests {
    private static final HttpHeaderName X_RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    private static final HttpHeaderName X_RECORDING_SKIP = HttpHeaderName.fromString("x-recording-skip");
    private static final String RECORDING_ID = "recording-id";

    @TempDir
    Path testClassPath;

    @BeforeEach
    public void createTargetDirectory() throws IOException {
        // Recording resolves the closest directory containing 'target' from the test class path.
        Files.createDirectory(testClassPath.resolve("target"));
    }

    @ParameterizedTest
    @MethodSource("failedRegistrationResponseSupplier")
    public void failedSanitizerRegistrationDiscardsRecording(int statusCode, HttpHeaders headers) {
        MockTestProxy proxy = new MockTestProxy(request -> new MockHttpResponse(request, statusCode, headers));
        TestProxyRecordPolicy policy = new TestProxyRecordPolicy(proxy, false);

        assertThrows(RuntimeException.class, () -> policy.startRecording(new File("test.json"), testClassPath));

        assertRecordingDiscarded(policy, proxy);
    }

    private static Stream<Arguments> failedRegistrationResponseSupplier() {
        String error = Base64.getEncoder().encodeToString("Invalid sanitizer.".getBytes(StandardCharsets.UTF_8));
        return Stream.of(Arguments.of(400, new HttpHeaders()), Arguments.of(200,
            new HttpHeaders().set(HttpHeaderName.fromString("x-request-known-exception-error"), error)));
    }

    @ParameterizedTest
    @MethodSource("invalidSanitizerSupplier")
    public void invalidSanitizerDiscardsRecording(TestProxySanitizer sanitizer) {
        MockTestProxy proxy = new MockTestProxy(request -> new MockHttpResponse(request, 200));
        TestProxyRecordPolicy policy = new TestProxyRecordPolicy(proxy, false);
        policy.startRecording(new File("test.json"), testClassPath);

        assertThrows(RuntimeException.class, () -> policy.addProxySanitization(Collections.singletonList(sanitizer)));

        assertRecordingDiscarded(policy, proxy);
    }

    private static Stream<Arguments> invalidSanitizerSupplier() {
        return Stream.of(Arguments.of(new TestProxySanitizer(null, null, "REDACTED", TestProxySanitizerType.HEADER)),
            Arguments.of(new TestProxySanitizer("regex", "REDACTED", null)), Arguments.of((TestProxySanitizer) null));
    }

    private static void assertRecordingDiscarded(TestProxyRecordPolicy policy, MockTestProxy proxy) {
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(request -> Mono.error(new AssertionError("The request should have been rejected.")))
            .build();

        assertThrows(IllegalStateException.class,
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, "https://example.com"), Context.NONE));
        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com")))
            .verifyError(IllegalStateException.class);

        policy.stopRecording(new LinkedList<>(Collections.singletonList("variable")));

        HttpRequest stopRequest = proxy.requests.get(proxy.requests.size() - 1);
        assertEquals("/record/stop", stopRequest.getUrl().getPath());
        assertEquals(RECORDING_ID, stopRequest.getHeaders().getValue(X_RECORDING_ID));
        assertEquals("request-response", stopRequest.getHeaders().getValue(X_RECORDING_SKIP));
        assertEquals("{}", stopRequest.getBodyAsBinaryData().toString());
    }

    private static final class MockTestProxy implements HttpClient {
        private final Function<HttpRequest, HttpResponse> addSanitizersResponse;
        private final List<HttpRequest> requests = new ArrayList<>();

        MockTestProxy(Function<HttpRequest, HttpResponse> addSanitizersResponse) {
            this.addSanitizersResponse = addSanitizersResponse;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromCallable(() -> sendSync(request, Context.NONE));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            requests.add(request);
            String path = request.getUrl().getPath();
            if ("/record/start".equals(path)) {
                return new MockHttpResponse(request, 200, new HttpHeaders().set(X_RECORDING_ID, RECORDING_ID));
            } else if ("/Admin/AddSanitizers".equals(path)) {
                return addSanitizersResponse.apply(request);
            }
            return new MockHttpResponse(request, 200);
        }
    }
}
