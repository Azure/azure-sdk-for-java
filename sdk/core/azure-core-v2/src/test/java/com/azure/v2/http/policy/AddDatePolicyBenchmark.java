// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class AddDatePolicyBenchmark {
    private static final Function<HttpRequest,
    Response<?>  MOCK_RESPONSE_GENERATOR
        = request -> new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return 0;
            }

            @Override
            @Deprecated
            public String getHeaderValue(String name) {
                return null;
            }

            @Override
            public String getHeaderValue(HttpHeaderName headerName) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return null;
            }

            @Override
            public byte[]> getBodyAsByteArray() {
                return null;
            }

            @Override
            public String> getBodyAsString() {
                return null;
            }

            @Override
            public String> getBodyAsString(Charset charset) {
                return null;
            }
        };

    private static final HttpPipelinePolicy DATE_TIME_RFC_1123 = (context, next) -> () -> {
        OffsetDateTime now = OffsetDateTime.now();
        httpRequest.getHeaders().set(HttpHeaderName.DATE, DateTimeRfc1123.toRfc1123String(now));
        return next.process();
    });

    private static final DateTimeFormatter FORMATTER
        = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneOffset.UTC).withLocale(Locale.US);

    private static final HttpPipelinePolicy DATE_TIME_FORMATTER = (context, next) -> () -> {
        OffsetDateTime now = OffsetDateTime.now();
        httpRequest.getHeaders().set(HttpHeaderName.DATE, FORMATTER.format(now));
        return next.process();
    });

    private HttpPipeline dateTimeRfc1123Pipeline;
    private HttpPipeline dateTimeFormatterPipeline;

    @Setup
    public void setup() {
        dateTimeRfc1123Pipeline = new HttpPipelineBuilder().policies(DATE_TIME_RFC_1123)
            .httpClient(request -> MOCK_RESPONSE_GENERATOR.apply(request)))
            .build();

        dateTimeFormatterPipeline = new HttpPipelineBuilder().policies(DATE_TIME_FORMATTER)
            .httpClient(request -> MOCK_RESPONSE_GENERATOR.apply(request)))
            .build();
    }

    /**
     * Benchmarks {@link AddDatePolicy} using {@link DateTimeRfc1123#toRfc1123String(OffsetDateTime)} to set the Date
     * header on a request.
     * <p>
     * Benchmarking shows this as ~30-40% faster than using DateTimeFormatter and is the current default behavior.
     */
    @Benchmark
    public void dateTimeRfc1123(Blackhole blackhole) {
        blackhole.consume(dateTimeRfc1123Pipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com")).block());
    }

    /**
     * Benchmarks {@link AddDatePolicy} using {@link DateTimeFormatter#format(TemporalAccessor)} to set the Date
     * header on a request.
     */
    @Benchmark
    public void dateTimeFormatter(Blackhole blackhole) {
        blackhole
            .consume(dateTimeFormatterPipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com")).block());
    }
}
