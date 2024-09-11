// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.core.util.MetricsOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosMicrometerMetricsConfig;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Micrometer-specific Azure Cosmos DB SDK metrics options
 */
public final class CosmosMicrometerMetricsOptions extends MetricsOptions {
    private MeterRegistry clientMetricRegistry = Metrics.globalRegistry;
    private EnumSet<MetricCategory> metricCategories;
    private EnumSet<TagName> defaultTagNames;
    private double[] defaultPercentiles;
    private boolean defaultShouldPublishHistograms;

    private boolean defaultApplyDiagnosticThresholdsForTransportLevelMeters;
    private final ConcurrentHashMap<CosmosMetricName, CosmosMeterOptions> effectiveOptions = new ConcurrentHashMap<>();

    /**
     * Instantiates new Micrometer-specific Azure Cosmos DB SDK metrics options
     */
    public CosmosMicrometerMetricsOptions() {
        CosmosMicrometerMetricsConfig metricsConfig = Configs.getMetricsConfig();

        this.setMetricCategoriesFromConfig(metricsConfig.getMetricCategories());
        this.setTagNamesFromConfig(metricsConfig.getTagNames());
        this.configureDefaultPercentiles(metricsConfig.getPercentiles());
        this.defaultShouldPublishHistograms = metricsConfig.getEnableHistograms();
        this.defaultApplyDiagnosticThresholdsForTransportLevelMeters = metricsConfig.getApplyDiagnosticThresholdsForTransportLevelMeters();
    }

    MeterRegistry getClientMetricRegistry() {
        return this.clientMetricRegistry;
    }

    /**
     * Sets MetricRegistry to be used to emit client metrics
     *
     * @param clientMetricMeterRegistry - the MetricRegistry to be used to emit client metrics
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions meterRegistry(MeterRegistry clientMetricMeterRegistry) {
        if (clientMetricMeterRegistry == null) {
            this.clientMetricRegistry = Metrics.globalRegistry;
        } else {
            this.clientMetricRegistry = clientMetricMeterRegistry;
        }

        return this;
    }

    /**
     * Sets the default tags that should be used for metrics (where applicable) unless overridden for a specific
     * meter in its {@link CosmosMicrometerMeterOptions}
     * By default all applicable tags are added for each metric. Adding tags/dimensions especially with high
     * cardinality has some overhead - so, this method allows modifying the set of tags to be applied when some are
     * not relevant in a certain use case.
     *
     * @param tags - the default tags to be used (when they are applicable to a specific meter and there is no
     *     override in {@link CosmosMicrometerMeterOptions} for that meter.
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions configureDefaultTagNames(CosmosMetricTagName... tags) {
        if (tags == null || tags.length == 0) {
            this.defaultTagNames = TagName.DEFAULT_TAGS.clone();
        } else {
            EnumSet<TagName> newTagNames = TagName.MINIMUM_TAGS.clone();
            for (CosmosMetricTagName t: tags) {
                for (TagName tagName: t.getTagNames()) {
                    newTagNames.add(tagName);
                }
            }

            this.defaultTagNames = newTagNames;
        }

        return this;
    }

    CosmosMicrometerMetricsOptions setTagNamesFromConfig(EnumSet<TagName> tags) {
        if (tags == null || tags.size() == 0) {
            this.defaultTagNames = TagName.DEFAULT_TAGS.clone();
        } else {
            EnumSet<TagName> newTagNames = TagName.MINIMUM_TAGS.clone();
            newTagNames.addAll(tags);

            this.defaultTagNames = newTagNames;
        }

        return this;
    }

    /**
     * Sets the default percentiles that should be captured for metrics (where applicable) unless overridden for a
     * specific meter in its {@link CosmosMicrometerMeterOptions}
     * By default percentiles 0.95 and 0.99 are captured. If percentiles is null or empty no percentiles will be
     * captured.
     *
     * @param percentiles - the default percentiles to be captured (when they are applicable to a specific meter and
     *     there is no override in {@link CosmosMicrometerMeterOptions} for that meter.
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions configureDefaultPercentiles(double... percentiles) {
        if (percentiles == null || percentiles.length == 0) {
            this.defaultPercentiles = null;
        } else {
            for (double p: percentiles) {
                if (p < 0 || p > 1) {
                    throw new IllegalArgumentException(
                        String.format("Percentile '%f' is outside of valid range.", p));
                }
            }

            this.defaultPercentiles = percentiles.clone();
        }

        return this;
    }

    /**
     * Sets a flag indicating whether by default histograms should be published for metrics (where applicable) unless
     * overridden for a specific meter in its {@link CosmosMicrometerMeterOptions}
     * By default histograms are published. Publishing histograms has its overhead - so, this method allows disabling
     * histograms by default.
     *
     * @param publishHistograms -  a flag indicating whether by default histograms should be published for metrics
     *     (when they are applicable to a specific meter and there is no override in {@link CosmosMicrometerMeterOptions} for
     *     that meter.
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions enableHistogramsByDefault(boolean publishHistograms) {
        this.defaultShouldPublishHistograms = publishHistograms;

        return this;
    }

    /**
     * Sets a flag indicating whether for transport level (rntbd) meters should only be emitted when diagnostic
     * thresholds are violated. This can be enabled to reduce the cardinality of dimensions (and the number of time
     * series being stored) especially when the partition/replica-level dimensions are enabled and the workload
     * is dealing with containers having a high number of physical partitions.
     * Request-level metrics usually are used to capture metrics per backend endpoint/replica - a high cardinality
     * dimension. Filtering by diagnostic thresholds reduces the overhead - but also means request-level metrics
     * can only be used for debugging purposes - not for monitoring purposes. So, it is important to use the unfiltered
     * operation-level metrics for health monitoring in this case.
     * By default, no filtering happens and diagnostic thresholds are not applied.
     *
     * @param isEnabled -  a flag indicating whether for transport level (rntbd) meters should only be emitted when
     * diagnostic thresholds are violated. (when they are applicable to a specific meter and there is no
     * override in {@link CosmosMicrometerMeterOptions#applyDiagnosticThresholds(boolean)} for that meter.
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions applyDiagnosticThresholdsForTransportLevelMeters(boolean isEnabled) {
        this.defaultApplyDiagnosticThresholdsForTransportLevelMeters = isEnabled;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CosmosMicrometerMetricsOptions setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Sets the categories of metrics that should be emitted. By default the following categories will be enabled:
     * OperationSummary (required), RequestSummary, DirectChannels, DirectRequests, System (required)
     * (the System and OperationSummary metrics are always collected and can't be disabled when enabling Cosmos metrics)
     * For most use-cases that should be sufficient. An overview of the different metric categories can be found here:
     * https://aka.ms/azure-cosmos-metrics
     * NOTE: metric categories are mutable. You can safely modify the categories on the CosmosClientTelemetryConfig
     * instance passed into the CosmosClientBuilder after the CosmosClient was created - and changes to the config
     * instance will be reflected at runtime by the client.
     *
     * @param categories - a comma-separated list of metric categories that should be emitted
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions setMetricCategories(CosmosMetricCategory... categories) {
        if (categories == null || categories.length == 0) {
            this.metricCategories = MetricCategory.DEFAULT_CATEGORIES.clone();
        } else {
            EnumSet<MetricCategory> newMetricCategories = MetricCategory.MINIMAL_CATEGORIES.clone();
            for (CosmosMetricCategory c: categories) {
                for (MetricCategory metricCategory: c.getCategories()) {
                    newMetricCategories.add(metricCategory);
                }
            }

            this.metricCategories = newMetricCategories;
        }

        return this;
    }

    CosmosMicrometerMetricsOptions setMetricCategoriesFromConfig(EnumSet<MetricCategory> categories) {
        if (categories == null || categories.size() == 0) {
            this.metricCategories = MetricCategory.DEFAULT_CATEGORIES.clone();
        } else {
            EnumSet<MetricCategory> newMetricCategories = MetricCategory.MINIMAL_CATEGORIES.clone();
            newMetricCategories.addAll(categories);

            this.metricCategories = newMetricCategories;
        }

        return this;
    }

    /**
     * Adds categories of metrics that should be emitted. By default the following categories will be enabled:
     * OperationSummary (required), RequestSummary, DirectChannels, DirectRequests, System (required)
     * (the System and OperationSummary metrics are always collected and can't be disabled when enabling Cosmos metrics)
     * An overview of the different metric categories can be found here:
     * https://aka.ms/azure-cosmos-metrics
     * NOTE: metric categories are mutable. You can safely modify the categories on the CosmosClientTelemetryConfig
     * instance passed into the CosmosClientBuilder after the CosmosClient was created - and changes to the config
     * instance will be reflected at runtime by the client.
     *
     * @param categories - a comma-separated list of metric categories that should be emitted
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions addMetricCategories(CosmosMetricCategory... categories) {
        if (categories == null || categories.length == 0) {
            return this;
        }

        EnumSet<MetricCategory> newMetricCategories = this.metricCategories.clone();
        for (CosmosMetricCategory c: categories) {
            for (MetricCategory metricCategory: c.getCategories()) {
                newMetricCategories.add(metricCategory);
            }
        }

        this.metricCategories = newMetricCategories;
        return this;
    }

    /**
     * Removes categories of metrics that should be emitted. By default the following categories will be enabled:
     * OperationSummary (required), RequestSummary, DirectChannels, DirectRequests, System (required)
     * (the System and OperationSummary metrics are always collected and can't be disabled when enabling Cosmos metrics)
     * An overview of the different metric categories can be found here:
     * https://aka.ms/azure-cosmos-metrics
     * NOTE: metric categories are mutable. You can safely modify the categories on the CosmosClientTelemetryConfig
     * instance passed into the CosmosClientBuilder after the CosmosClient was created - and changes to the config
     * instance will be reflected at runtime by the client.
     *
     * @param categories - a comma-separated list of metric categories that should be emitted
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions removeMetricCategories(CosmosMetricCategory... categories) {
        if (categories == null || categories.length == 0) {
            return this;
        }

        EnumSet<MetricCategory> newMetricCategories = this.metricCategories.clone();
        for (CosmosMetricCategory c: categories) {
            for (MetricCategory metricCategory: c.getCategories()) {
                newMetricCategories.remove(metricCategory);
            }
        }

        for (MetricCategory metricCategory: CosmosMetricCategory.MINIMUM.getCategories()) {
            newMetricCategories.add(metricCategory);
        }

        this.metricCategories = newMetricCategories;

        return this;
    }


    /**
     * Allows overriding meter-specific options.
     * @param meterName the meter name
     * @param options the options to be overridden
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions configureMeter(
        CosmosMetricName meterName,
        CosmosMicrometerMeterOptions options) {

        this
            .effectiveOptions
            .compute(
                meterName,
                (name, valueBeforeUpdate) -> {
                    if (valueBeforeUpdate == null) {
                        return new CosmosMeterOptions(
                            name,
                            options.getIsEnabled() != null ? options.getIsEnabled().booleanValue() : true,
                            options.getPercentiles() != null ? options.getPercentiles() : this.defaultPercentiles,
                            options.getIsHistogramPublishingEnabled() != null ?
                                options.getIsHistogramPublishingEnabled().booleanValue() :
                                this.defaultShouldPublishHistograms,
                            options.getSuppressedTagNames() != null ?
                                options.getSuppressedTagNames() :
                                EnumSet.noneOf(TagName.class),
                            options.getApplyDiagnosticThresholdsEnabled() != null ?
                                options.getApplyDiagnosticThresholdsEnabled() :
                                this.defaultApplyDiagnosticThresholdsForTransportLevelMeters && (
                                    meterName.getCategory() == CosmosMetricCategory.REQUEST_SUMMARY ||
                                        meterName.getCategory() == CosmosMetricCategory.REQUEST_DETAILS)
                        );
                    }

                    return new CosmosMeterOptions(
                        name,
                        options.getIsEnabled() != null ?
                            options.getIsEnabled().booleanValue() :
                            valueBeforeUpdate.isEnabled(),
                        options.getPercentiles() != null ?
                            options.getPercentiles() :
                            valueBeforeUpdate.getPercentiles(),
                        options.getIsHistogramPublishingEnabled() != null ?
                            options.getIsHistogramPublishingEnabled().booleanValue() :
                            valueBeforeUpdate.isHistogramPublishingEnabled(),
                        options.getSuppressedTagNames() != null ?
                            options.getSuppressedTagNames() :
                            valueBeforeUpdate.getSuppressedTagNames(),
                        valueBeforeUpdate.isDiagnosticThresholdsFilteringEnabled()
                    );
                });

        return this;
    }

    /**
     * Gets the meter options for a certain meter. The returned meter options can be used to modify the settings for
     * this meter. Changes can be applied even after building the Cosmos Client instance at runtime.
     * @param meterName - the meter name
     * @return the current meter options
     */
    CosmosMeterOptions getMeterOptions(CosmosMetricName meterName) {
        checkNotNull(meterName, "Argument 'meterName' must not be null.");

        return this
            .effectiveOptions
            .computeIfAbsent(meterName, name -> new CosmosMeterOptions(
                name,
                true,
                this.defaultPercentiles,
                this.defaultShouldPublishHistograms,
                EnumSet.noneOf(TagName.class),
                this.defaultApplyDiagnosticThresholdsForTransportLevelMeters
            ));
    }

    EnumSet<MetricCategory> getMetricCategories() {
        return this.metricCategories;
    }

    EnumSet<TagName> getDefaultTagNames() {
        return this.defaultTagNames;
    }

    void configureDefaultTagNames(EnumSet<TagName> newTagNames) {
        this.defaultTagNames = newTagNames;
    }

    double[] getDefaultPercentiles() {
        return defaultPercentiles;
    }

    boolean shouldPublishHistograms() {
        return defaultShouldPublishHistograms;
    }

    boolean shouldApplyDiagnosticThresholdsForTransportLevelMeters() {
        return defaultApplyDiagnosticThresholdsForTransportLevelMeters;
    }
}
