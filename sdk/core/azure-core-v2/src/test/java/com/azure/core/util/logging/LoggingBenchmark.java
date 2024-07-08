// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.v2.implementation.util.EnvironmentConfiguration;
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
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;

@Fork(3)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class LoggingBenchmark {
    ClientLogger logger;

    @Setup
    public void setup() {
        EnvironmentConfiguration.getGlobalConfiguration()
            .put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.WARNING));
        this.logger = new ClientLogger(LoggingBenchmark.class);

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));
    }

    @Benchmark
    public void loggingAtDisabledLevel() {
        logger.info("hello, connectionId={}, linkName={}", "foo", 1);
    }

    @Benchmark
    public void loggingAtDisabledLevelWithContext() {
        logger.atInfo().addKeyValue("connectionId", "foo").addKeyValue("linkName", 1).log("hello");
    }

    @Benchmark
    public void loggingAtEnabledLevel() {
        logger.error("hello, connectionId={}, linkName={}", "foo", 1);
    }

    @Benchmark
    public void loggingAtEnabledLevelWithContext() {
        logger.atError().addKeyValue("connectionId", "foo").addKeyValue("linkName", 1).log("hello");
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
