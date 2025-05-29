// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.FilteringConfiguration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKs;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.LiveMetricsRestAPIsForClientSDKsBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.MonitoringDataPoint;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.HostName;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.IKeyMasker;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.Strings;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.ThreadPoolUtils;
import reactor.util.annotation.Nullable;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.Objects;

public class QuickPulse {

    static final int QP_INVARIANT_VERSION = 5;

    private volatile QuickPulseDataCollector collector;

    private static final ClientLogger LOGGER = new ClientLogger(QuickPulse.class);

    public static QuickPulse create(HttpPipeline httpPipeline, Supplier<URL> endpointUrl,
        Supplier<String> instrumentationKey, @Nullable String roleName, @Nullable String roleInstance,
        String sdkVersion) {

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
            quickPulse.initialize(httpPipeline, endpointUrl, instrumentationKey, roleName, roleInstance, sdkVersion);
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
        if (collector == null) {
            // Because QuickPulse is initialized with a 5 s delay
            return false;
        }
        return collector.isEnabled();
    }

    public void add(TelemetryItem telemetryItem) {
        if (collector != null) {
            collector.add(telemetryItem);
        }
    }

    private void initialize(HttpPipeline httpPipeline, Supplier<URL> endpointUrl, Supplier<String> instrumentationKey,
        @Nullable String roleName, @Nullable String roleInstance, String sdkVersion) {
        if (LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
            LOGGER.verbose(
                "Initializing QuickPulse with instrumentation key: {} , URL {}, rolename {}, role instance {}, sdk version {}",
                Objects.toString(IKeyMasker.mask(instrumentationKey.get())), Objects.toString(endpointUrl.get()),
                roleName, roleInstance, sdkVersion);
        }

        String quickPulseId = UUID.randomUUID().toString().replace("-", "");
        ArrayBlockingQueue<MonitoringDataPoint> sendQueue = new ArrayBlockingQueue<>(256, true);

        LiveMetricsRestAPIsForClientSDKsBuilder builder = new LiveMetricsRestAPIsForClientSDKsBuilder();
        LiveMetricsRestAPIsForClientSDKs liveMetricsRestAPIsForClientSDKs
            = builder.pipeline(httpPipeline).buildClient();

        String instanceName = roleInstance;
        String machineName = HostName.get();

        if (Strings.isNullOrEmpty(instanceName)) {
            instanceName = machineName;
        }
        if (Strings.isNullOrEmpty(instanceName)) {
            instanceName = "Unknown host";
        }

        FilteringConfiguration configuration = new FilteringConfiguration();
        AtomicReference<FilteringConfiguration> atomicConfig = new AtomicReference<>(configuration);

        QuickPulseDataCollector collector = new QuickPulseDataCollector(atomicConfig);

        QuickPulsePingSender quickPulsePingSender
            = new QuickPulsePingSender(liveMetricsRestAPIsForClientSDKs, endpointUrl, instrumentationKey, roleName,
                instanceName, machineName, quickPulseId, sdkVersion, atomicConfig);
        QuickPulseDataSender quickPulseDataSender = new QuickPulseDataSender(liveMetricsRestAPIsForClientSDKs,
            sendQueue, endpointUrl, instrumentationKey, atomicConfig);
        QuickPulseDataFetcher quickPulseDataFetcher = new QuickPulseDataFetcher(collector, sendQueue, roleName,
            instanceName, machineName, quickPulseId, sdkVersion);

        QuickPulseCoordinatorInitData coordinatorInitData
            = new QuickPulseCoordinatorInitDataBuilder().withPingSender(quickPulsePingSender)
                .withDataFetcher(quickPulseDataFetcher)
                .withDataSender(quickPulseDataSender)
                .withCollector(collector)
                .build();

        QuickPulseCoordinator coordinator = new QuickPulseCoordinator(coordinatorInitData);

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
