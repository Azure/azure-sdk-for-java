// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.FAIL_TO_SEND_STATSBEAT_ERROR;

public class StatsbeatModule {

    private static final Logger logger = LoggerFactory.getLogger(BaseStatsbeat.class);

    private final ScheduledExecutorService scheduledExecutor
        = Executors.newSingleThreadScheduledExecutor(ThreadPoolUtils.createDaemonThreadFactory(BaseStatsbeat.class));

    private final CustomDimensions customDimensions;
    private final NetworkStatsbeat networkStatsbeat;
    private final AttachStatsbeat attachStatsbeat;
    private final FeatureStatsbeat featureStatsbeat;
    private final FeatureStatsbeat instrumentationStatsbeat;
    private final NonessentialStatsbeat nonessentialStatsbeat;
    private final AzureMetadataService azureMetadataService;
    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean shutdown = new AtomicBoolean();

    public StatsbeatModule(Consumer<MetadataInstanceResponse> vmMetadataServiceCallback) {
        customDimensions = new CustomDimensions();
        attachStatsbeat = new AttachStatsbeat(customDimensions);
        featureStatsbeat = new FeatureStatsbeat(customDimensions, FeatureType.FEATURE);
        instrumentationStatsbeat = new FeatureStatsbeat(customDimensions, FeatureType.INSTRUMENTATION);
        azureMetadataService = new AzureMetadataService(attachStatsbeat, customDimensions, vmMetadataServiceCallback);
        // TODO (heya) will update this when we have a consensus from other languages on what other telemetry to be collected
        if (RpAttachType.getRpAttachType() != RpAttachType.MANUAL) {
            networkStatsbeat = new NetworkStatsbeat(customDimensions);
            nonessentialStatsbeat = new NonessentialStatsbeat(customDimensions);
        } else {
            networkStatsbeat = null;
            nonessentialStatsbeat = null;
        }
    }

    public void start(TelemetryItemExporter telemetryItemExporter, Supplier<StatsbeatConnectionString> connectionString,
        Supplier<String> instrumentationKey, boolean disabledAll, long shortIntervalSeconds, long longIntervalSeconds,
        boolean disabled, Set<Feature> featureSet) {
        if (connectionString.get() == null) {
            logger.debug("Don't start StatsbeatModule when statsbeat connection string is null.");
            return;
        }

        if (started.getAndSet(true)) {
            throw new IllegalStateException("initialize already called");
        }

        if (disabledAll) {
            // disabledAll is an internal emergency kill-switch to turn off Statsbeat completely when
            // something goes wrong.
            // this happens rarely.
            return;
        }

        updateConnectionString(connectionString.get());
        updateInstrumentationKey(instrumentationKey.get());

        if (RpAttachType.getRpAttachType() != RpAttachType.MANUAL) {
            scheduledExecutor.scheduleWithFixedDelay(new StatsbeatSender(networkStatsbeat, telemetryItemExporter),
                shortIntervalSeconds, shortIntervalSeconds, TimeUnit.SECONDS);
        }
        scheduledExecutor.scheduleWithFixedDelay(new StatsbeatSender(attachStatsbeat, telemetryItemExporter),
            Math.min(60, longIntervalSeconds), longIntervalSeconds, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(new StatsbeatSender(featureStatsbeat, telemetryItemExporter),
            Math.min(60, longIntervalSeconds), longIntervalSeconds, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(new StatsbeatSender(instrumentationStatsbeat, telemetryItemExporter),
            Math.min(60, longIntervalSeconds), longIntervalSeconds, TimeUnit.SECONDS);

        ResourceProvider rp = customDimensions.getResourceProvider();
        // only turn on AzureMetadataService when the resource provider is VM or UNKNOWN.
        if (rp == ResourceProvider.RP_VM || rp == ResourceProvider.UNKNOWN) {
            // will only reach here the first time, after instance has been instantiated
            azureMetadataService.scheduleWithFixedDelay(longIntervalSeconds);
        }

        featureStatsbeat.trackConfigurationOptions(featureSet);

        if (!disabled && RpAttachType.getRpAttachType() != RpAttachType.MANUAL) {
            nonessentialStatsbeat.setConnectionString(connectionString.get());
            nonessentialStatsbeat.setInstrumentationKey(instrumentationKey.get());
            scheduledExecutor.scheduleWithFixedDelay(new StatsbeatSender(nonessentialStatsbeat, telemetryItemExporter),
                longIntervalSeconds, longIntervalSeconds, TimeUnit.SECONDS);
        } else {
            logger.debug("Non-essential Statsbeat is disabled.");
        }
    }

    public void shutdown() {
        // guarding against multiple shutdown calls because this can get called if statsbeat shuts down
        // early because it cannot reach breeze and later on real shut down (when running not as agent)
        if (!shutdown.getAndSet(true)) {
            logger.debug("Shutting down Statsbeat scheduler.");
            scheduledExecutor.shutdown();
            azureMetadataService.shutdown();
        }
    }

    public NetworkStatsbeat getNetworkStatsbeat() {
        return networkStatsbeat;
    }

    public FeatureStatsbeat getFeatureStatsbeat() {
        return featureStatsbeat;
    }

    public FeatureStatsbeat getInstrumentationStatsbeat() {
        return instrumentationStatsbeat;
    }

    public NonessentialStatsbeat getNonessentialStatsbeat() {
        return nonessentialStatsbeat;
    }

    private void updateConnectionString(StatsbeatConnectionString connectionString) {
        if (connectionString != null) {
            if (RpAttachType.getRpAttachType() != RpAttachType.MANUAL) {
                networkStatsbeat.setConnectionString(connectionString);
            }
            attachStatsbeat.setConnectionString(connectionString);
            featureStatsbeat.setConnectionString(connectionString);
            instrumentationStatsbeat.setConnectionString(connectionString);
        }
    }

    private void updateInstrumentationKey(String instrumentationKey) {
        if (instrumentationKey != null && !instrumentationKey.isEmpty()) {
            if (RpAttachType.getRpAttachType() != RpAttachType.MANUAL) {
                networkStatsbeat.setInstrumentationKey(instrumentationKey);
            }
            attachStatsbeat.setInstrumentationKey(instrumentationKey);
            featureStatsbeat.setInstrumentationKey(instrumentationKey);
            instrumentationStatsbeat.setInstrumentationKey(instrumentationKey);
        }
    }

    /**
     * Runnable which is responsible for calling the send method to transmit Statsbeat telemetry.
     */
    private static class StatsbeatSender implements Runnable {

        private final BaseStatsbeat statsbeat;
        private final TelemetryItemExporter telemetryItemExporter;

        private StatsbeatSender(BaseStatsbeat statsbeat, TelemetryItemExporter telemetryItemExporter) {
            this.statsbeat = statsbeat;
            this.telemetryItemExporter = telemetryItemExporter;
        }

        @SuppressWarnings("try")
        @Override
        public void run() {
            try {
                // For Linux Consumption Plan, connection string is lazily set.
                // There is no need to send statsbeat when cikey is empty.
                if (statsbeat.getInstrumentationKey() == null || statsbeat.getInstrumentationKey().isEmpty()) {
                    return;
                }
                statsbeat.send(telemetryItemExporter);
            } catch (RuntimeException e) {
                try (MDC.MDCCloseable ignored = FAIL_TO_SEND_STATSBEAT_ERROR.makeActive()) {
                    logger.error("Error occurred while sending statsbeat", e);
                }
            }
        }
    }
}
