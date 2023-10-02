// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.tracing;

import com.typespec.core.SyncAsyncExtension;
import com.typespec.core.SyncAsyncTest;
import com.typespec.core.annotation.ExpectedResponses;
import com.typespec.core.annotation.Get;
import com.typespec.core.annotation.Host;
import com.typespec.core.annotation.Post;
import com.typespec.core.annotation.Put;
import com.typespec.core.annotation.ServiceInterface;
import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.RestProxy;
import com.typespec.core.implementation.http.policy.InstrumentationPolicy;
import com.typespec.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestProxyTracingTests {
    private TestTracer tracer;
    private TestInterface testInterface;

    @BeforeEach
    void beforeEach() {
        tracer = new TestTracer();
        HttpClient client = new SimpleMockHttpClient();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new InstrumentationPolicy())
            .httpClient(client)
            .tracer(tracer)
            .build();

        testInterface = RestProxy.create(TestInterface.class, pipeline);
    }

    @SyncAsyncTest
    public void restProxySuccess() throws Exception {
        SyncAsyncExtension.execute(
            () -> testInterface.testMethodReturnsMonoVoidSync(),
            () -> testInterface.testMethodReturnsMonoVoid().block());

        assertEquals(2, tracer.getSpans().size());
        Span restProxy = tracer.getSpans().get(0);
        Span http = tracer.getSpans().get(1);

        assertEquals(getSpan(http.getStartContext()), restProxy);
        assertTrue(restProxy.getName().startsWith("myService.testMethodReturnsMonoVoid"));
        assertNull(restProxy.getThrowable());
        assertNull(restProxy.getErrorMessage());
    }

    @Test
    public void restProxyCancelAsync() {
        testInterface.testMethodDelays()
            .timeout(Duration.ofMillis(10))
            .toFuture().cancel(true);

        assertEquals(2, tracer.getSpans().size());
        Span restProxy = tracer.getSpans().get(0);
        Span http = tracer.getSpans().get(1);

        assertEquals(getSpan(http.getStartContext()), restProxy);
        assertTrue(restProxy.getName().startsWith("myService.testMethodDelays"));
        assertNull(restProxy.getThrowable());
        assertEquals("cancel", restProxy.getErrorMessage());

        assertTrue(http.getName().startsWith("HTTP PUT"));
        assertNull(http.getThrowable());
        assertEquals("cancel", http.getErrorMessage());
    }

    @Test
    public void restProxyThrows() {
        StepVerifier.create(testInterface.testMethodThrows())
            .consumeErrorWith((ex) -> { })
            .verify();

        assertEquals(2, tracer.getSpans().size());
        Span restProxy = tracer.getSpans().get(0);
        Span http = tracer.getSpans().get(1);

        assertEquals(getSpan(http.getStartContext()), restProxy);
        assertEquals("myService.testMethodThrows", restProxy.getName());
        assertNotNull(restProxy.getThrowable());
        assertNull(restProxy.getErrorMessage());
    }

    @Test
    public void restProxyThrowsSync() {
        assertThrows(RuntimeException.class, () -> testInterface.testMethodThrowsSync());

        assertEquals(2, tracer.getSpans().size());
        Span restProxy = tracer.getSpans().get(0);
        Span http = tracer.getSpans().get(1);

        assertEquals(getSpan(http.getStartContext()), restProxy);
        assertEquals("myService.testMethodThrowsSync", restProxy.getName());
        assertNotNull(restProxy.getThrowable());
        assertNull(restProxy.getErrorMessage());
    }

    private static Span getSpan(Context context) {
        Optional<Object> spanOpt = context.getData("span");
        assertTrue(spanOpt.isPresent());
        assertTrue(spanOpt.get() instanceof Span);
        return (Span) spanOpt.get();
    }

    private static class TestTracer implements Tracer {
        private final List<Span> spans = new ArrayList<>();

        @Override
        public Context start(String methodName, Context context) {
            return start(methodName, null, context);
        }

        @Override
        public Context start(String methodName, StartSpanOptions options, Context context) {
            Span span = new Span(methodName, options, context);
            spans.add(span);
            return context.addData("span", span);
        }

        @Override
        public void end(String errorMessage, Throwable throwable, Context context) {
            getSpan(context).end(errorMessage, throwable, context);
        }

        @Override
        public void setAttribute(String key, String value, Context context) {
            getSpan(context).setAttribute(key, value);
        }

        public List<Span> getSpans() {
            return spans;
        }
    }

    static class Span {
        private Context startContext;
        private String name;
        private StartSpanOptions startOptions;
        private String errorMessage;
        private Throwable throwable;
        private Context endContext;

        private final Map<String, Object> attributes = new HashMap<>();

        Span(String name, StartSpanOptions startOptions, Context context) {
            this.startContext = context;
            this.name = name;
            this.startOptions = startOptions;
        }

        public void end(String errorMessage, Throwable throwable, Context context) {
            this.errorMessage = errorMessage;
            this.throwable = throwable;
            this.endContext = context;
        }

        public void setAttribute(String key, Object value) {
            this.attributes.put(key, value);
        }

        public Context getStartContext() {
            return startContext;
        }

        public Context getEndContext() {
            return endContext;
        }

        public String getName() {
            return name;
        }

        public StartSpanOptions getStartSpanOptions() {
            return startOptions;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    private static class SimpleMockHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() == HttpMethod.GET) {
                return Mono.just(new MockHttpResponse(request, 200));
            } else if (request.getHttpMethod() == HttpMethod.PUT) {
                return Mono.delay(Duration.ofSeconds(10)).map(l -> new MockHttpResponse(request, 200));
            } else {
                throw new RuntimeException("error");
            }
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            if (request.getHttpMethod() == HttpMethod.GET) {
                return new MockHttpResponse(request, 200);
            } else {
                throw new RuntimeException("error");
            }
        }
    }

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface {
        @Get("my/url/path")
        @ExpectedResponses({200})
        Mono<Void> testMethodReturnsMonoVoid();

        @Get("my/url/path")
        @ExpectedResponses({200})
        Response<Void> testMethodReturnsMonoVoidSync();

        @Post("my/url/path")
        @ExpectedResponses({500})
        Mono<Void> testMethodThrows();

        @Post("my/url/path")
        @ExpectedResponses({500})
        Response<Void> testMethodThrowsSync();

        @Put("my/url/path")
        @ExpectedResponses({200})
        Mono<Void> testMethodDelays();
    }
}
