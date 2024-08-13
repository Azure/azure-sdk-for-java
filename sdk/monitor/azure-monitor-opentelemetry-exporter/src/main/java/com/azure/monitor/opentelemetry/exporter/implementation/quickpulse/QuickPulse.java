// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.HostName;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class QuickPulse {

    // 6 represents filtering support for Otel metrics only is enabled
    static final int QP_INVARIANT_VERSION = 6;

    private volatile QuickPulseDataCollector collector;

    public static QuickPulse create(HttpPipeline httpPipeline, Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey, @Nullable String roleName, @Nullable String roleInstance,
        boolean useNormalizedValueForNonNormalizedCpuPercentage, QuickPulseMetricReader quickPulseMetricReader,
        MetricDataMapper metricDataMapper, String sdkVersion) {

        QuickPulse quickPulse = new QuickPulse();

        // initialization is delayed and performed in the background because initializing the random
        // seed via UUID.randomUUID() below can cause slowness during startup in some environments
        ExecutorService executor
            = Executors.newSingleThreadExecutor(ThreadPoolUtils.createDaemonThreadFactory(QuickPulse.class));
        executor.execute(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            quickPulse.initialize(httpPipeline, endpointUrl, instrumentationKey, roleName, roleInstance,
                useNormalizedValueForNonNormalizedCpuPercentage, quickPulseMetricReader, metricDataMapper, sdkVersion);
        });
        // the condition below will always be false, but by referencing the executor it ensures the
        // executor can't become unreachable in the middle of the execute() method execution above
        // (and prior to the task being registered), which can lead to the executor being terminated and
        // scheduleAtFixedRate throwing a RejectedExecutionException
        // (see https://bugs.openjdk.org/browse/JDK-8145304)
        if (executor.isTerminated()) {
            throw new AssertionError();
        }
        return quickPulse;
    }

    public boolean isEnabled() {
        return collector.isEnabled();
    }

    public void add(TelemetryItem telemetryItem) {
        if (collector != null) {
            collector.add(telemetryItem);
        }
    }

    private void initialize(HttpPipeline httpPipeline, Supplier<URL> endpointUrl, Supplier<String> instrumentationKey,
        @Nullable String roleName, @Nullable String roleInstance,
        boolean useNormalizedValueForNonNormalizedCpuPercentage, QuickPulseMetricReader quickPulseMetricReader,
        MetricDataMapper metricDataMapper, String sdkVersion) {

        String quickPulseId = UUID.randomUUID().toString().replace("-", "");
        ArrayBlockingQueue<HttpRequest> sendQueue = new ArrayBlockingQueue<>(256, true);
        QuickPulseConfiguration quickPulseConfiguration = new QuickPulseConfiguration();

        QuickPulseDataSender quickPulseDataSender
            = new QuickPulseDataSender(httpPipeline, sendQueue, quickPulseConfiguration);

        String instanceName = roleInstance;
        String machineName = HostName.get();

        if (Strings.isNullOrEmpty(instanceName)) {
            instanceName = machineName;
        }
        if (Strings.isNullOrEmpty(instanceName)) {
            instanceName = "Unknown host";
        }

        QuickPulseDataCollector collector
            = new QuickPulseDataCollector(useNormalizedValueForNonNormalizedCpuPercentage, quickPulseConfiguration);

        QuickPulsePingSender quickPulsePingSender = new QuickPulsePingSender(httpPipeline, endpointUrl,
            instrumentationKey, roleName, instanceName, machineName, quickPulseId, sdkVersion, quickPulseConfiguration);
        QuickPulseDataFetcher quickPulseDataFetcher = new QuickPulseDataFetcher(collector, sendQueue, endpointUrl,
            instrumentationKey, roleName, instanceName, machineName, quickPulseId, quickPulseConfiguration);

        QuickPulseCoordinatorInitData coordinatorInitData
            = new QuickPulseCoordinatorInitDataBuilder().withPingSender(quickPulsePingSender)
                .withDataFetcher(quickPulseDataFetcher)
                .withDataSender(quickPulseDataSender)
                .withCollector(collector)
                .build();

        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(coordinatorInitData);

        QuickPulseMetricReceiver quickPulseMetricReceiver
            = new QuickPulseMetricReceiver(quickPulseMetricReader, metricDataMapper, collector);

        Thread metricReceiverThread
            = new Thread(quickPulseMetricReceiver, QuickPulseMetricReceiver.class.getSimpleName());
        metricReceiverThread.setDaemon(true);
        metricReceiverThread.start();

        Thread senderThread = new Thread(quickPulseDataSender, QuickPulseDataSender.class.getSimpleName());
        senderThread.setDaemon(true);
        senderThread.start();

        Thread thread = new Thread(coordinator, QuickPulseCoordinator.class.getSimpleName());
        thread.setDaemon(true);
        thread.start();

        collector.enable(instrumentationKey);

        this.collector = collector;
    }
}
