// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BenchmarkOrchestratorFaultInjectionTest {

    @Test(groups = {"unit"})
    public void waitsForEveryBenchmarkToArm() {
        AtomicBoolean firstArmed = new AtomicBoolean();
        AtomicBoolean secondArmed = new AtomicBoolean();

        BenchmarkOrchestrator.armFaultInjection(Arrays.asList(
            benchmarkWithArming(Mono.fromRunnable(() -> firstArmed.set(true))),
            benchmarkWithArming(Mono.fromRunnable(() -> secondArmed.set(true)))));

        assertThat(firstArmed).isTrue();
        assertThat(secondArmed).isTrue();
    }

    @Test(groups = {"unit"})
    public void propagatesArmingFailure() {
        Benchmark failingBenchmark = benchmarkWithArming(Mono.error(new IllegalStateException("arm failed")));

        assertThatThrownBy(() -> BenchmarkOrchestrator.armFaultInjection(Arrays.asList(failingBenchmark)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("arm failed");
    }

    private static Benchmark benchmarkWithArming(Mono<Void> arming) {
        return new Benchmark() {
            @Override
            public void shutdown() {
            }

            @Override
            public Mono<Void> armFaultInjection() {
                return arming;
            }

            @Override
            public Mono<?> performSingleOperation() {
                return Mono.empty();
            }
        };
    }
}