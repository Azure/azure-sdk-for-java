// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.blob.perf.DownloadBlobTest;
import com.azure.storage.blob.perf.DownloadBlobToFileTest;
import com.azure.storage.blob.perf.ListBlobsTest;
import com.azure.storage.blob.perf.DownloadBlobNonSharedClientTest;
import com.azure.storage.blob.perf.UploadBlobNoLengthTest;
import com.azure.storage.blob.perf.UploadBlobTest;
import com.azure.storage.blob.perf.UploadBlockBlobTest;
import com.azure.storage.blob.perf.UploadFromFileTest;
import com.azure.storage.blob.perf.UploadOutputStreamTest;
import com.azure.storage.file.datalake.perf.AppendFileDatalakeTest;
import com.azure.storage.file.datalake.perf.ReadFileDatalakeTest;
import com.azure.storage.file.datalake.perf.UploadFileDatalakeTest;
import com.azure.storage.file.datalake.perf.UploadFromFileDatalakeTest;
import com.azure.storage.file.share.perf.DownloadFileShareTest;
import com.azure.storage.file.share.perf.DownloadToFileShareTest;
import com.azure.storage.file.share.perf.UploadFileShareTest;
import com.azure.storage.file.share.perf.UploadFromFileShareTest;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.instrumentation.runtimemetrics.Cpu;
import io.opentelemetry.instrumentation.runtimemetrics.GarbageCollector;
import io.opentelemetry.instrumentation.runtimemetrics.MemoryPools;
import io.opentelemetry.instrumentation.runtimemetrics.Threads;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Runs the Storage performance test.
 *
 * <p>To run from command line. Package the project into a jar with dependencies via mvn clean package.
 * Then run the program via java -jar 'compiled-jar-with-dependencies-path' </p>
 *
 * <p> To run from IDE, set all the required environment variables in IntelliJ via Run -&gt; EditConfigurations
 * section.
 * Then run the App's main method via IDE.</p>
 */
public class App {
    public static void main(String[] args) {
        InMemoryMetricReader metricReader = new InMemoryMetricReader();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader)
            .build();
        OpenTelemetry otel = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).buildAndRegisterGlobal();
        MemoryPools.registerObservers(otel);
        Cpu.registerObservers(otel);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                metricReader.collect();
            }
        }, 1000, 1000);

        PerfStressProgram.run(new Class<?>[]{
            DownloadBlobTest.class,
            DownloadBlobToFileTest.class,
            ListBlobsTest.class,
            UploadBlobTest.class,
            UploadBlobNoLengthTest.class,
            UploadBlockBlobTest.class,
            UploadFromFileTest.class,
            UploadOutputStreamTest.class,
            DownloadFileShareTest.class,
            DownloadToFileShareTest.class,
            UploadFileShareTest.class,
            UploadFromFileShareTest.class,
            AppendFileDatalakeTest.class,
            ReadFileDatalakeTest.class,
            UploadFileDatalakeTest.class,
            UploadFromFileDatalakeTest.class,
            DownloadBlobNonSharedClientTest.class
        }, args);

        metricReader.forceFlush();
        timer.cancel();
        metricReader.printMetrics();
    }

    static class InMemoryMetricReader implements MetricReader {
        private final AggregationTemporality aggregationTemporality;
        private volatile MetricProducer metricProducer = MetricProducer.noop();
        private final ConcurrentLinkedDeque<MetricData> jvmMem = new ConcurrentLinkedDeque<>();
        private final ConcurrentLinkedDeque<MetricData> cpuUtilization = new ConcurrentLinkedDeque<>();
        private static final AttributeKey<String> POOL_NAME = AttributeKey.stringKey("pool");
        private static final double MB = 1024 * 1024d;
        public InMemoryMetricReader() {
            this.aggregationTemporality = AggregationTemporality.CUMULATIVE;
        }

        /** Returns all metrics accumulated since the last call. */
        public void collect() {
            Collection<MetricData> metrics =  metricProducer.collectAllMetrics();
            for (MetricData metric : metrics) {
                if (metric.getName().equals("process.runtime.jvm.memory.usage")) {
                    this.jvmMem.add(metric);
                } else if (metric.getName().equals("process.runtime.jvm.cpu.utilization")) {
                    cpuUtilization.add(metric);
                }
            }
        }
        public void printMetrics() {
            System.out.println("| JVM memory usage: Eden Space | JVM memory usage: Survivor Space | JVM memory usage: Old Gen |   CPU    |");
            System.out.println("|------------------------------|----------------------------------|---------------------------|----------|");
            MetricData mem = jvmMem.poll(), cpu = cpuUtilization.poll();
            while (mem!= null && cpu != null) {
                double maxEden = 0;
                double maxSurv = 0;
                double maxOld = 0;
                double cpuUt = 0;
                for (LongPointData p : mem.getLongSumData().getPoints()) {
                    String pool = p.getAttributes().get(POOL_NAME);

                    if (pool.equals("PS Eden Space") || pool.equals("G1 Eden Space")) {
                        if (p.getValue() > maxEden) {
                            maxEden = p.getValue();
                        }
                    } else if (pool.equals("PS Survivor Space") || pool.equals("G1 Survivor Space")) {
                        if (p.getValue() > maxSurv) {
                            maxSurv = p.getValue();
                        }
                    } else if (pool.equals("PS Old Gen") || pool.equals("G1 Old Gen")) {
                        if (p.getValue() > maxOld) {
                            maxOld = p.getValue();
                        }
                    }

                    if (cpu != null) {
                        DoublePointData ut = cpu.getDoubleGaugeData().getPoints().stream().findFirst().orElse(null);
                        if (ut != null) {
                            cpuUt = ut.getValue();
                        }
                    }
                    mem = jvmMem.poll();
                    cpu = cpuUtilization.poll();
                    System.out.printf("|     %27d |     %31d |     %24d | %8d |\n", (long)(maxEden / MB), (long)(maxSurv / MB), (long)(maxOld / MB), (int)(cpuUt * 100));
                }

            }
            jvmMem.clear();
            cpuUtilization.clear();
        }

        @Override
        public void register(CollectionRegistration registration) {
            this.metricProducer = MetricProducer.asMetricProducer(registration);
        }

        @Override
        public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
            return aggregationTemporality;
        }

        @Override
        public CompletableResultCode forceFlush() {
            collect();
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public String toString() {
            return "InMemoryMetricReader{aggregationTemporality=" + aggregationTemporality + "}";
        }
    }

}
