// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.accesshelpers.ClientLoggerAccessHelper;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.implementation.logging.DefaultLogger;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.ListByteBufferContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Header;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures cache hits and request construction without network I/O.
 * <p>
 * Compile tests with {@code -Djmh-benchmark} and run this benchmark with JMH's {@code -prof gc} option.
 * Cache-hit states are initialized before measurement; the string cache-miss benchmark includes content construction.
 */
@Fork(2)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CoreFastPathBenchmark {
    @Benchmark
    public String headerCacheHit(HeaderState state) {
        return state.header.getValue();
    }

    @Benchmark
    public byte[] bodyBytesCacheHit(BodyState state) {
        return state.content.toBytes();
    }

    @Benchmark
    public byte[] stringBytesCacheMiss() {
        return new StringContent("a cold string body").toBytes();
    }

    @Benchmark
    public Object nestedGenericCacheHit(GenericState state) throws IOException {
        return state.mapper.readValue(state.json, state.type);
    }

    @Benchmark
    public HttpRequest createRequest(RequestState state) throws IOException {
        return state.proxy.createHttpRequest(state.parser, state.serializer, false, state.arguments);
    }

    @Benchmark
    public void httpLogging(LoggingState state) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.com/items").setBody("ping");
        HttpResponse response = state.async
            ? state.pipeline.send(request, state.context).block()
            : state.pipeline.sendSync(request, state.context);
        response.close();
    }

    @State(Scope.Thread)
    public static class HeaderState {
        @Param({ "1", "4" })
        private int values;
        private Header header;

        @Setup
        public void setup() {
            String[] parts = new String[values];
            for (int i = 0; i < values; i++) {
                parts[i] = "value" + i;
            }
            header = new Header("name", parts);
            header.getValue();
        }
    }

    @State(Scope.Thread)
    public static class BodyState {
        @Param({ "string", "byteBuffer", "flux", "stream", "file", "serializable", "list" })
        private String kind;
        private BinaryDataContent content;
        private Path file;

        @Setup
        public void setup() throws IOException {
            byte[] bytes = new byte[256];
            Arrays.fill(bytes, (byte) 'a');
            String text = new String(bytes, StandardCharsets.UTF_8);
            switch (kind) {
                case "string":
                    content = new StringContent(text);
                    break;

                case "byteBuffer":
                    content = new ByteBufferContent(ByteBuffer.wrap(bytes));
                    break;

                case "flux":
                    content = new FluxByteBufferContent(Flux.just(ByteBuffer.wrap(bytes)), (long) bytes.length);
                    break;

                case "stream":
                    content = new InputStreamContent(new ByteArrayInputStream(bytes), (long) bytes.length);
                    break;

                case "file":
                    file = Files.createTempFile("core-fast-path-benchmark", ".bin");
                    Files.write(file, bytes);
                    content = new FileContent(file, 8192, 0L, (long) bytes.length);
                    break;

                case "serializable":
                    content = BinaryDataHelper.getContent(BinaryData.fromObject(text));
                    break;

                case "list":
                    content = new ListByteBufferContent(Arrays.asList(ByteBuffer.wrap(bytes, 0, 128).slice(),
                        ByteBuffer.wrap(bytes, 128, 128).slice()));
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected content kind: " + kind);
            }
            content.toBytes();
        }

        @TearDown
        public void tearDown() throws IOException {
            if (file != null) {
                Files.delete(file);
            }
        }
    }

    @State(Scope.Thread)
    public static class GenericState {
        private final ObjectMapperShim mapper = new ObjectMapperShim(new ObjectMapper());
        private final Type type = new TypeReference<Map<String, List<String>>>() {
        }.getJavaType();
        private final String json = "{\"items\":[\"one\",\"two\",\"three\"]}";

        @Setup
        public void setup() throws IOException {
            mapper.readValue(json, type);
        }
    }

    @Host("https://example.com/root")
    @ServiceInterface(name = "requestBenchmark")
    public interface TestService {
        @Post("{path}")
        @ExpectedResponses({ 200 })
        Response<Void> json(@PathParam(value = "path", encoded = true) String path,
            @BodyParam("application/json") String body);

        @Post("{path}")
        @ExpectedResponses({ 200 })
        Response<Void> parameters(@PathParam(value = "path", encoded = true) String path,
            @BodyParam("application/json; charset=utf-8") String body);
    }

    @State(Scope.Thread)
    public static class RequestState {
        @Param({ "relative", "absolute" })
        private String path;
        @Param({ "json", "parameters" })
        private String mediaType;
        private SerializerAdapter serializer;
        private RestProxyBase proxy;
        private SwaggerMethodParser parser;
        private Object[] arguments;

        @Setup
        public void setup() throws ReflectiveOperationException, IOException {
            serializer = JacksonAdapter.createDefaultSerializerAdapter();
            proxy = new SyncRestProxy(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build(), serializer,
                SwaggerInterfaceParser.getInstance(TestService.class));
            parser = new SwaggerMethodParser(TestService.class.getMethod(mediaType, String.class, String.class));
            arguments = new Object[] {
                "relative".equals(path) ? "items" : "https://other.example.com/items?token=%2F%3A",
                "value" };
            proxy.createHttpRequest(parser, serializer, false, arguments);
        }
    }

    @State(Scope.Thread)
    public static class LoggingState {
        @Param({ "NONE", "BASIC", "BODY_AND_HEADERS" })
        private String detail;
        @Param({ "false", "true" })
        private boolean async;
        private HttpPipeline pipeline;
        private Context context;
        private PrintStream logStream;

        @Setup
        public void setup() {
            logStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int value) {
                }

                @Override
                public void write(byte[] bytes, int offset, int length) {
                }
            });
            context = new Context("caller-method-logger",
                ClientLoggerAccessHelper.createClientLogger(
                    new DefaultLogger(CoreFastPathBenchmark.class.getName(), logStream, LogLevel.INFORMATIONAL), null))
                        .addData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, 128);
            byte[] responseBody = "pong".getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "4")
                .set(HttpHeaderName.CONTENT_TYPE, "text/plain");
            pipeline = new HttpPipelineBuilder()
                .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.valueOf(detail))))
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, headers, responseBody)))
                .build();
        }

        @TearDown
        public void tearDown() {
            logStream.close();
        }
    }
}
