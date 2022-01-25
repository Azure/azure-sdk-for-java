// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.TestConfigurationBuilder;
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

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;

@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ConfigurationBenchmark {
    private Configuration retryConfig;
    private Configuration envVarRetryConfig;

    @SuppressWarnings("deprecation")
    @Setup
    public void setup() {
        EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_REQUEST_RETRY_COUNT, "5");
        this.envVarRetryConfig = new TestConfigurationBuilder().setEnv(PROPERTY_AZURE_REQUEST_RETRY_COUNT, "5").build();
        this.retryConfig = new TestConfigurationBuilder(
            "http-retry.exponential.max-retries", "5",
            "http-retry.exponential.base-delay", "1000",
            "http-retry.exponential.max-delay", "2000",
            "http-retry.mode", "exponential").build();
    }

    @Benchmark
    public void retryPolicyConstructorImplicitConfig(Blackhole bh) {
        bh.consume(new RetryPolicy());
    }

    @Benchmark
    public void retryPolicyConstructorExplicitEnvVarConfig(Blackhole bh) {
        bh.consume(RetryPolicy.fromConfiguration(this.envVarRetryConfig));
    }

    @Benchmark
    public void retryPolicyConstructorExplicitFullConfig(Blackhole bh) {
        bh.consume(RetryPolicy.fromConfiguration(this.retryConfig));
    }

    @Benchmark
    public void retryPolicyConstructorExplicitCodeConfig(Blackhole bh) {
        bh.consume(new RetryPolicy(new ExponentialBackoff(5, Duration.ofMillis(1000), Duration.ofMillis(2000)), null, null));
    }


    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
