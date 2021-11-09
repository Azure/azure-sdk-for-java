// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class LoggingBenchmark {
    ClientLogger logger;

    @Setup
    public void setup() {
        Configuration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.WARNING));
        this.logger = new ClientLogger(LoggingBenchmark.class);

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));
    }

    @Benchmark
    public void loggingAtDisabledLevel() {
        logger.info("hello, connectionId={}, linkName={}", "foo", "bar");
    }

    @Benchmark
    public void loggingAtDisabledLevelWithContext() {
        logger.atLevel(LogLevel.INFORMATIONAL)
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log("hello");
    }

    @Benchmark
    public void loggingAtEnabledLevel() {
        logger.error("hello, connectionId={}, linkName={}", "foo", "bar");
    }

    @Benchmark
    public void loggingAtEnabledLevelWithContext() {
        logger.atLevel(LogLevel.ERROR)
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log("hello");
    }

    @Benchmark
    public void loggingAtEnabledLevelWithContext2() {
        logger.error("hello", null, "connectionId", "foo", "linkName", "bar");
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
