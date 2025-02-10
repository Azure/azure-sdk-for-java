// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosMicrometerMetricsConfig {
    public static final CosmosMicrometerMetricsConfig DEFAULT = new CosmosMicrometerMetricsConfig();

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private String metricCategories = MetricCategory.DEFAULT_CATEGORIES.clone().toString();
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private String tagNames = TagName.DEFAULT_TAGS.clone().toString();
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private Double sampleRate = 1.0;
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private double[] percentiles = { 0.95, 0.99 };
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private Boolean enableHistograms = true;
    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private Boolean applyDiagnosticThresholdsForTransportLevelMeters = false;

    public CosmosMicrometerMetricsConfig() {}

    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert to Json String", e);
        }
    }

    @JsonIgnore
    public EnumSet<MetricCategory> getMetricCategories() {
        List<String> metricsCategoryList = convertToList(this.metricCategories);
        return EnumSet.copyOf(
            metricsCategoryList
                .stream()
                .map(categoryValue -> MetricCategory.fromValue(categoryValue))
                .collect(Collectors.toList())
        );
    }

    @JsonIgnore
    public EnumSet<TagName> getTagNames() {
        List<String> tagNames = convertToList(this.tagNames);
        return EnumSet.copyOf(
            tagNames
                .stream()
                .map(tagName -> TagName.fromValue(tagName))
                .collect(Collectors.toList())
        );
    }

    public double[] getPercentiles() {
        return this.percentiles;
    }

    public double getSampleRate() {
        return this.sampleRate;
    }

    public Boolean getEnableHistograms() {
        return this.enableHistograms;
    }

    public Boolean getApplyDiagnosticThresholdsForTransportLevelMeters() {
        return applyDiagnosticThresholdsForTransportLevelMeters;
    }

    private static List<String> convertToList(String value) {
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toList());
    }

    public static CosmosMicrometerMetricsConfig fromJsonString(String jsonString) {
        try {
            return Utils.getSimpleObjectMapper().readValue(jsonString, CosmosMicrometerMetricsConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert from Json String", e);
        }
    }
}
