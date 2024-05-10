// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.TypeUtil;
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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ResponseConstructorsCacheBenchMark {
    private ResponseConstructorsCacheBenchMarkTestData testData;
    // Cache Types
    private ResponseConstructorsCache defaultCache;
    private ResponseConstructorsCacheLambdaMetaFactory lambdaMetaCache;
    private ResponseConstructorsNoCacheReflection reflectionNoCache;

    @Setup
    public void setup() {
        testData = new ResponseConstructorsCacheBenchMarkTestData();
        defaultCache = new ResponseConstructorsCache();
        lambdaMetaCache = new ResponseConstructorsCacheLambdaMetaFactory();
        reflectionNoCache = new ResponseConstructorsNoCacheReflection();
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void reflectionCache(Blackhole blackhole) {
        ResponseConstructorsCacheBenchMarkTestData.Input[] inputs = testData.inputs();

        for (ResponseConstructorsCacheBenchMarkTestData.Input input : inputs) {
            Class<? extends Response<?>> responseClass = (Class<? extends Response<?>>) TypeUtil.getRawClass(
                input.getReturnType());
            // Step1: Locate Constructor using Reflection.
            ReflectiveInvoker reflectiveInvoker = defaultCache.get(responseClass);
            // Step2: Invoke Constructor using Reflection.
            Response<?> response = defaultCache.invoke(reflectiveInvoker, input.getResponse(), input.getBodyAsObject());
            // avoid JVM dead code detection
            blackhole.consume(response);
        }
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void lambdaMetaFactoryCache(Blackhole blackhole) {
        ResponseConstructorsCacheBenchMarkTestData.Input[] inputs = testData.inputs();

        for (ResponseConstructorsCacheBenchMarkTestData.Input input : inputs) {
            Class<? extends Response<?>> responseClass = (Class<? extends Response<?>>) TypeUtil.getRawClass(
                input.getReturnType());
            // Step1: Locate Constructor using LambdaMetaFactory.
            ResponseConstructorsCacheLambdaMetaFactory.ResponseConstructor constructor = lambdaMetaCache.get(
                responseClass);

            if (constructor == null) {
                throw new IllegalStateException("Response constructor with expected parameters not found.");
            }
            // Step2: Invoke Constructor using LambdaMetaFactory functional interface.
            Response<?> response = constructor.invoke(input.getResponse(), input.getBodyAsObject());

            // avoid JVM dead code detection
            blackhole.consume(response);
        }
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void reflectionNoCache(Blackhole blackhole) {
        ResponseConstructorsCacheBenchMarkTestData.Input[] inputs = testData.inputs();

        for (ResponseConstructorsCacheBenchMarkTestData.Input input : inputs) {
            Class<? extends Response<?>> responseClass = (Class<? extends Response<?>>) TypeUtil.getRawClass(
                input.getReturnType());
            // Step1: Locate Constructor using Reflection.
            Constructor<? extends Response<?>> constructor = reflectionNoCache.get(responseClass);

            if (constructor == null) {
                throw new IllegalStateException("Response constructor with expected parameters not found.");
            }
            // Step2: Invoke Constructor using Reflection.

            Response<?> response = reflectionNoCache.invoke(constructor, input.getResponse(), input.getBodyAsObject());

            // avoid JVM dead code detection
            blackhole.consume(response);
        }
    }

    public static void main(String... args) throws IOException {
        Main.main(args);
    }
}
