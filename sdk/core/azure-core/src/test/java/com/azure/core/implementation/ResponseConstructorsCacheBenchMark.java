// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.TypeUtil;
import org.openjdk.jmh.Main;
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
import org.openjdk.jmh.runner.RunnerException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ResponseConstructorsCacheBenchMark {
    public ResponseConstructorsCacheBenchMarkTestData testData;
    // Cache Types
    public ResponseConstructorsCache defaultCache;
    public ResponseConstructorsCacheReflection reflectionCache;

    @Setup
    public void setup() {
        testData = new ResponseConstructorsCacheBenchMarkTestData();
        defaultCache = new ResponseConstructorsCache();
        reflectionCache = new ResponseConstructorsCacheReflection();
    }

    @Benchmark
    public void lambdaMetaFactoryCache(Blackhole blackhole) {
        for (int i = 0; i < testData.inputs.length; i++) {
            Class<? extends Response<?>> responseClass =
                    (Class<? extends Response<?>>) TypeUtil.getRawClass(testData.inputs[i].returnType);
            // Step1: Locate Constructor using LambdaMetaFactory.
            ResponseConstructorsCache.ResponseConstructor constructor = defaultCache.get(responseClass);
            if (constructor == null) {
                throw new IllegalStateException("Response constructor with expected parameters not found.");
            }
            // Step2: Invoke Constructor using LambdaMetaFactory functional interface.
            Mono<Response<?>> response = constructor.invoke(testData.inputs[i].decodedResponse,
                    testData.inputs[i].bodyAsObject);
            // avoid JVM dead code detection
            blackhole.consume(response.block());
        }
    }

    @Benchmark
    public void reflectionCache(Blackhole blackhole) {
        for (int i = 0; i < testData.inputs.length; i++) {
            Class<? extends Response<?>> responseClass =
                    (Class<? extends Response<?>>) TypeUtil.getRawClass(testData.inputs[i].returnType);
            // Step1: Locate Constructor using Reflection.
            Constructor<? extends Response<?>> constructor = reflectionCache.get(responseClass);
            if (constructor == null) {
                throw new IllegalStateException("Response constructor with expected parameters not found.");
            }
            // Step2: Invoke Constructor using Reflection.
            Mono<Response<?>> response = reflectionCache.invoke(constructor, testData.inputs[i].decodedResponse,
                    testData.inputs[i].bodyAsObject);
            // avoid JVM dead code detection
            blackhole.consume(response.block());
        }
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
