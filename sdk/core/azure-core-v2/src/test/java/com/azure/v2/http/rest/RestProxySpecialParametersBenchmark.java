// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.util.Context;
import com.azure.core.v2.util.CoreUtils;
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
 * Benchmarks retrieving the special parameters {@link Context} and {@link RequestOptions}.
 */
@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class RestProxySpecialParametersBenchmark {
    // Context is last as it's usually the last parameter in the generated interface.
    // There isn't an exact known size of parameters but 7 is a good rough estimate for the average.
    private static final Object[] REST_PROXY_PARAMETERS
        = new Object[] { "a string", 1, 1.5D, "another string", new Object(), -7, Context.none() };

    /**
     * Benchmarks retrieving either {@link Context} or {@link RequestOptions} from the parameters array passed into
     * {@link RestProxy} by using the predetermined index where the type would be located.
     * <p>
     * Benchmarking shows this as ~5x faster than the iterative approach and is the current implementation.
     */
    @Benchmark
    public void directArrayAccessForSpecialParameters(Blackhole blackhole) {
        // Direct array access, Context is known to be contained in index 6.
        blackhole.consume(REST_PROXY_PARAMETERS[6]);
    }

    /**
     * Benchmarks retrieving either {@link Context} or {@link RequestOptions} from the parameters array passed into
     * {@link RestProxy} by using an iterate-and-type check approach to determine where the parameter is located on
     * a per-request basis.
     */
    @Benchmark
    public void iterativeCheckForSpecialParameters(Blackhole blackhole) {
        // Iterate through the parameters until the first instance of Context is found.
        blackhole.consume(CoreUtils.findFirstOfType(REST_PROXY_PARAMETERS, Context.class));
    }
}
