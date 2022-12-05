// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

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
import java.util.concurrent.atomic.AtomicInteger;

@Fork(3)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ExpandableStringEnumDesignBenchmark {
    @State(Scope.Benchmark)
    public static class ExpandableEnums {
        private static final AtomicInteger COUNTER = new AtomicInteger();

        // 967.602 ± 47.455 ns/op
        public ConcurrentExpandableStringEnum getStaticConcurrent() {
            return ConcurrentExpandableStringEnum.fromString("static");
        }

        // 976.863 ± 19.951 ns/op
        public ConcurrentExpandableStringEnum getNewConcurrent() {
            return ConcurrentExpandableStringEnum.fromString("static" + COUNTER.getAndIncrement());
        }

        // 890.019 ± 7.070 ns/op
        public SynchronizedExpandableStringEnum getStaticSynchronized() {
            return SynchronizedExpandableStringEnum.fromString("static");
        }

        // 950.374 ± 12.297 ns/op
        public SynchronizedExpandableStringEnum getNewSynchronized() {
            return SynchronizedExpandableStringEnum.fromString("static" + COUNTER.getAndIncrement());
        }
    }
    @Benchmark
    public void getExistingConcurrentHashMap(ExpandableEnums expandableEnums, Blackhole blackhole) {
        blackhole.consume(expandableEnums.getStaticConcurrent());
    }

    @Benchmark
    public void getExistingSynchronizedHashMapOnWrite(ExpandableEnums expandableEnums, Blackhole blackhole) {
        blackhole.consume(expandableEnums.getStaticSynchronized());
    }

    @Benchmark
    public void getNewConcurrentHashMap(ExpandableEnums expandableEnums, Blackhole blackhole) {
        blackhole.consume(expandableEnums.getNewConcurrent());
    }

    @Benchmark
    public void getNewSynchronizedHashMapOnWrite(ExpandableEnums expandableEnums, Blackhole blackhole) {
        blackhole.consume(expandableEnums.getNewSynchronized());
    }

    private static final class ConcurrentExpandableStringEnum
        extends ConcurrentHashMapExpandableStringEnum<ConcurrentExpandableStringEnum> {

        @Deprecated
        private ConcurrentExpandableStringEnum() {
        }

        private static final ConcurrentExpandableStringEnum STATIC = fromString("static");

        private static ConcurrentExpandableStringEnum fromString(String string) {
            return fromString(string, ConcurrentExpandableStringEnum.class);
        }
    }

    private static final class SynchronizedExpandableStringEnum
        extends ConcurrentHashMapExpandableStringEnum<SynchronizedExpandableStringEnum> {

        @Deprecated
        private SynchronizedExpandableStringEnum() {
        }

        private static final SynchronizedExpandableStringEnum STATIC = fromString("static");

        private static SynchronizedExpandableStringEnum fromString(String string) {
            return fromString(string, SynchronizedExpandableStringEnum.class);
        }
    }
}
