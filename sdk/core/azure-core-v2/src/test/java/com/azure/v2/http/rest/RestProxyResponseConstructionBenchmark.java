// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.implementation.ReflectiveInvoker;
import com.azure.core.v2.implementation.TypeUtil;
import com.azure.core.v2.implementation.http.rest.ResponseConstructorsCache;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks {@link RestProxy} type checking and directly constructing the {@link Response} type vs using
 * {@link ResponseConstructorsCache} to reflective find and reflectively construct the {@link Response} type.
 */
@SuppressWarnings("unchecked")
@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class RestProxyResponseConstructionBenchmark {
    private static final ResponseConstructorsCache CONSTRUCTORS_CACHE = new ResponseConstructorsCache();

    private static final HttpRequest REQUEST = new HttpRequest(HttpMethod.GET, "https://example.com");
    private static final HttpHeaders HEADERS = new HttpHeaders();
    private static final Object DESERIALIZED_HEADERS = new Object();

    private static final Class<? extends Response<?>> RESPONSE_TYPE = (Class<? extends Response<?>>) TypeUtil
        .getRawClass(TypeUtil.createParameterizedType(ResponseBase.class, Object.class, String.class));

    /**
     * Benchmarks creating a {@link Response} type using the constructor directly when possible (types outside the
     * scope of azure-core must use reflection).
     * <p>
     * Benchmarking shows this as ~20-30x faster than using ResponseConstructorCache and the MethodHandle it returns.
     */
    @Benchmark
    public void directConstruction(Blackhole blackhole) {
        blackhole.consume(new ResponseBase<>(REQUEST, 200, HEADERS, "value", DESERIALIZED_HEADERS));
    }

    /**
     * Benchmarks creating a {@link Response} type using the {@link ResponseConstructorsCache} and the
     * {@link ReflectiveInvoker} it caches that points to the Response type's constructor.
     */
    @Benchmark
    public void reflectionConstruction(Blackhole blackhole) throws Throwable {
        ReflectiveInvoker constructor = CONSTRUCTORS_CACHE.get(RESPONSE_TYPE);
        blackhole.consume(constructor.invokeWithArguments(REQUEST, 200, HEADERS, "value", DESERIALIZED_HEADERS));
    }
}
