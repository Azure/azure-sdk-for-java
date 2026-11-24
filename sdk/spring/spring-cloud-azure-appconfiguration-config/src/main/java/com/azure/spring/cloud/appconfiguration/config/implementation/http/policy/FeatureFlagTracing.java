// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import java.util.Arrays;
import java.util.List;

public class FeatureFlagTracing {

    private static final String CUSTOM_FILTER = "CSTM";

    private static final String PERCENTAGE_FILTER = "PRCNT";

    private static final String TIME_WINDOW_FILTER = "TIME";

    private static final String TARGETING_FILTER = "TRGT";

    private static final String FILTER_TYPE_DELIMITER = "+";

    private static final String FEATURE_FLAG_USES_TELEMETRY_TAG = "Telemetry";

    private static final String FEATURE_FLAG_USES_SEED_TAG = "Seed";

    private static final List<String> PERCENTAGE_FILTER_NAMES = Arrays.asList("Percentage", "Microsoft.Percentage",
        "PercentageFilter", "Microsoft.PercentageFilter");

    private static final List<String> TIME_WINDOW_FILTER_NAMES = Arrays.asList("TimeWindow", "Microsoft.TimeWindow",
        "TimeWindowFilter", "Microsoft.TimeWindowFilter");

    private static final List<String> TARGETING_FILTER_NAMES = Arrays.asList("Targeting", "Microsoft.Targeting",
        "TargetingFilter", "Microsoft.TargetingFilter");

    private Boolean usesCustomFilter = false;

    private Boolean usesPercentageFilter = false;

    private Boolean usesTimeWindowFilter = false;

    private Boolean usesTargetingFilter = false;

    private Boolean usesTelemetry = false;

    private Boolean usesSeed = false;

    private Integer maxVariants = null;

    boolean usesAnyFilter() {
        return usesCustomFilter || usesPercentageFilter || usesTimeWindowFilter || usesTargetingFilter;
    }

    public void resetFeatureFilterTelemetry() {
        usesCustomFilter = false;
        usesPercentageFilter = false;
        usesTimeWindowFilter = false;
        usesTargetingFilter = false;
        usesTelemetry = false;
        usesSeed = false;
        maxVariants = null;
    }

    public void updateFeatureFilterTelemetry(String filterName) {
        if (PERCENTAGE_FILTER_NAMES.stream().anyMatch(name -> name.equalsIgnoreCase(filterName))) {
            usesPercentageFilter = true;
        } else if (TIME_WINDOW_FILTER_NAMES.stream().anyMatch(name -> name.equalsIgnoreCase(filterName))) {
            usesTimeWindowFilter = true;
        } else if (TARGETING_FILTER_NAMES.stream().anyMatch(name -> name.equalsIgnoreCase(filterName))) {
            usesTargetingFilter = true;
        } else {
            usesCustomFilter = true;
        }
    }

    /**
     * Marks telemetry as used.
     */
    public void setUsesTelemetry() {
        this.usesTelemetry = true;
    }

    /**
     * Marks seed as used.
     */
    public void setUsesSeed() {
        this.usesSeed = true;
    }

    /**
     * Updates max variants if the new size is larger.
     * @param size size of variants
     */
    public void updateMaxVariants(int size) {
        if (maxVariants == null || size > maxVariants) {
            maxVariants = size;
        }
    }

    /**
     * Gets the max variants value.
     * @return max variants or null if not set
     */
    public Integer getMaxVariants() {
        return maxVariants;
    }

    /**
     * Creates the FFFeatures string for correlation context.
     * @return FFFeatures string with telemetry and seed tags
     */
    public String createFFFeaturesString() {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(usesSeed)) {
            sb.append(FEATURE_FLAG_USES_SEED_TAG);
        }
        if (Boolean.TRUE.equals(usesTelemetry)) {
            if (sb.length() > 0) {
                sb.append(FILTER_TYPE_DELIMITER);
            }
            sb.append(FEATURE_FLAG_USES_TELEMETRY_TAG);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (usesCustomFilter) {
            sb.append(CUSTOM_FILTER);
        }
        if (usesPercentageFilter) {
            sb.append(sb.length() > 0 ? FILTER_TYPE_DELIMITER : "").append(PERCENTAGE_FILTER);
        }
        if (usesTimeWindowFilter) {
            sb.append(sb.length() > 0 ? FILTER_TYPE_DELIMITER : "").append(TIME_WINDOW_FILTER);
        }
        if (usesTargetingFilter) {
            sb.append(sb.length() > 0 ? FILTER_TYPE_DELIMITER : "").append(TARGETING_FILTER);
        }
        return sb.toString();
    }

}
