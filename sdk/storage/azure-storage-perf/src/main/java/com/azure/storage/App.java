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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
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
        Cpu.registerObservers(otel);
        GarbageCollector.registerObservers(otel);
        Threads.registerObservers(otel);
        MemoryPools.registerObservers(otel);

        Mono.delay(Duration.ofSeconds(5))
            .repeat()
            .subscribe(p -> metricReader.collectAndPrint(), e -> System.out.println(e));

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
    }

    static class InMemoryMetricReader implements MetricReader {
        private final AggregationTemporality aggregationTemporality;
        private final AtomicBoolean isShutdown = new AtomicBoolean(false);
        private volatile MetricProducer metricProducer = MetricProducer.noop();

        private static final AttributeKey<String> POOL_NAME = AttributeKey.stringKey("pool");
        private static final double MB = 1024 * 1024d;
        public InMemoryMetricReader() {
            this.aggregationTemporality = AggregationTemporality.CUMULATIVE;
            System.out.println("| JVM memory usage: G1 Eden Space | JVM memory usage: G1 Survivor Space | JVM memory usage: G1 Old Gen |");
            System.out.println("|---------------------------------|-------------------------------------|------------------------------|");
        }

        /** Returns all metrics accumulated since the last call. */
        public void collectAndPrint() {
            if (isShutdown.get()) {
                return;
            }
            Collection<MetricData> metrics =  metricProducer.collectAllMetrics();
            metrics.stream().forEach(d -> {
                System.out.println("metric " + d.getName());
                for (PointData p : d.getData().getPoints()) {
                    printDataPoint(p);
                }
            });
        }

        private static void printDataPoint(PointData p) {
            Attributes a = p.getAttributes();
            double value = -1;
            if (p instanceof LongPointData) {
                value = ((LongPointData)p).getValue();
            } else if (p instanceof DoublePointData) {
                value = ((DoublePointData)p).getValue();
            }

            System.out.printf("\tval='%f', attributes=%s\n", value, a.asMap().keySet().stream().map(k -> k.getKey() + "=" + a.asMap().get(k)).collect(Collectors.joining(", ")));
        }

        private static void printJvmMemUsage(MetricData data) {
            double maxG1Eden = 0;
            double maxG1Surv = 0;
            double maxG1Old = 0;
            for (LongPointData p : data.getLongSumData().getPoints()) {
                String pool = p.getAttributes().get(POOL_NAME);

                if (pool.equals("G1 Eden Space")) {
                    if (p.getValue() > maxG1Eden) {
                        maxG1Eden = p.getValue();
                    }
                } else if (pool.equals("G1 Survivor Space")) {
                    if (p.getValue() > maxG1Surv) {
                        maxG1Surv = p.getValue();
                    }
                } else if (pool.equals("G1 Old Gen")) {
                    if (p.getValue() > maxG1Old) {
                        maxG1Old = p.getValue();
                    }
                }
            }

            System.out.printf("|     %28f|     %32f|     %25f|\n", maxG1Eden/MB, maxG1Surv/MB, maxG1Old/MB);
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
            collectAndPrint();
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            isShutdown.set(true);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public String toString() {
            return "InMemoryMetricReader{aggregationTemporality=" + aggregationTemporality + "}";
        }
    }

}
