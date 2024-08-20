// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.CorePerfStressOptions.BackendType;
import com.azure.core.perf.core.CorePerfStressOptions.BinaryDataSource;
import com.azure.perf.test.core.PerfStressTest;
import com.beust.jcommander.JCommander;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.perf.test.core.PerfStressOptions.HttpClientType;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.NETTY;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.OKHTTP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CorePerfIntegrationTest {

    @ParameterizedTest
    @MethodSource("providePerfTests")
    public void testSync(PerfStressTest<? extends CorePerfStressOptions> perfTest) {
        perfTest.globalSetup();
        perfTest.setup();
        perfTest.run();
        perfTest.cleanup();
        perfTest.globalCleanup();
    }

    @ParameterizedTest
    @MethodSource("providePerfTests")
    public void testAsync(PerfStressTest<? extends CorePerfStressOptions> perfTest) {
        StepVerifier.create(Mono.defer(perfTest::globalSetupAsync)
            .then(Mono.defer(perfTest::setupAsync))
            .then(Mono.defer(perfTest::runAsync))
            .then(Mono.defer(perfTest::cleanupAsync))
            .then(Mono.defer(perfTest::globalCleanupAsync))).verifyComplete();
    }

    private static Stream<Arguments> providePerfTests() {
        return generateArgsCombination().map(args -> {
            CorePerfStressOptions options = new CorePerfStressOptions();
            JCommander jc = new JCommander();
            jc.addCommand("unused", options);
            jc.parse(args);
            String parsedCommand = jc.getParsedCommand();
            assertNotNull(parsedCommand);
            assertFalse(parsedCommand.isEmpty());
            return options;
        })
            .flatMap(options -> Stream.<Supplier<PerfStressTest<? extends CorePerfStressOptions>>>of(
                () -> new BinaryDataReceiveTest(options), () -> new BinaryDataSendTest(options),
                () -> new ByteBufferReceiveTest(options), () -> new ByteBufferSendTest(options),
                () -> new JsonReceiveTest(options), () -> new JsonSendTest(options),
                () -> new PipelineSendTest(options), () -> new XmlReceiveTest(options), () -> new XmlSendTest(options),
                () -> new TracingTest(options)))
            .map(Supplier::get)
            .map(Arguments::of);
    }

    private static Stream<String[]> generateArgsCombination() {
        List<String[]> args = new ArrayList<>();

        for (BackendType backendType : Arrays.asList(BackendType.MOCK, BackendType.WIREMOCK)) {
            for (HttpClientType httpClientType : Arrays.asList(NETTY, OKHTTP)) {
                for (BinaryDataSource binaryDataSource : BinaryDataSource.values()) {
                    for (Boolean includePipelinePolicies : Arrays.asList(true, false)) {
                        List<String> argLine = new ArrayList<>(
                            Arrays.asList("unused", "--backend-type", backendType.name(), "--http-client",
                                httpClientType.toString(), "--binary-data-source", binaryDataSource.name()));
                        if (includePipelinePolicies) {
                            argLine.add("--include-pipeline-policies");
                        }
                        args.add(argLine.toArray(new String[0]));
                    }
                }
            }
        }

        return args.stream();
    }
}
