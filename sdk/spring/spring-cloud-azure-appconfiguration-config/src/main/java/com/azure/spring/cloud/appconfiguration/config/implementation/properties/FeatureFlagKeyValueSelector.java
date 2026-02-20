// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.LABEL_SEPARATOR;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.ValidationUtil;

import jakarta.annotation.PostConstruct;

/**
 * Selector properties that control which feature flags are loaded from an
 * Azure App Configuration store.
 */
public final class FeatureFlagKeyValueSelector {

    /**
     * Sentinel array representing the empty (no label) value.
     */
    private static final String[] EMPTY_LABEL_ARRAY = { EMPTY_LABEL };

    /**
     * Filters feature flags by key suffix, appended after the
     * {@code .appconfig.featureflag/} prefix. When empty, all feature flags
     * are loaded.
     */
    private String keyFilter = "";

    /**
     * Filters feature flags by label. When unset, only feature flags with no
     * label are loaded. Multiple labels can be specified as a comma-separated
     * string. Must not contain asterisks ({@code *}).
     */
    private String labelFilter;

    /**
     * Filters feature flags by tags. Each entry is interpreted as a tag-based filter,
     * typically in the {@code tagName=tagValue} format. When multiple entries are
     * provided, they are combined using AND logic.
     */
    private List<String> tagsFilter;

    /**
     * Returns the key filter for feature flags.
     *
     * @return the key filter string
     */
    public String getKeyFilter() {
        return keyFilter;
    }

    /**
     * Sets the key filter for feature flags.
     *
     * @param keyFilter the key suffix to filter by
     * @return this {@link FeatureFlagKeyValueSelector} for chaining
     */
    public FeatureFlagKeyValueSelector setKeyFilter(String keyFilter) {
        this.keyFilter = keyFilter;
        return this;
    }

    /**
     * Returns the raw label filter string, or {@code null} if not set.
     *
     * @return the label filter
     */
    public String getLabelFilter() {
        return labelFilter;
    }

    /**
     * Resolves the label filter into an array of labels. When no label filter is
     * set, falls back to the active Spring profiles (in reverse priority order).
     * If neither is available, returns the empty-label sentinel.
     *
     * @param profiles the active Spring profiles to use as a fallback
     * @return an array of resolved labels, ordered from lowest to highest priority
     */
    public String[] getLabelFilter(List<String> profiles) {
        if (labelFilter == null && profiles.size() > 0) {
            Collections.reverse(profiles);
            return profiles.toArray(new String[profiles.size()]);
        } else if (!StringUtils.hasText(labelFilter)) {
            return EMPTY_LABEL_ARRAY;
        }

        // The use of trim makes label= dev,prod and label= dev, prod equal.
        List<String> labels = Arrays.stream(labelFilter.split(LABEL_SEPARATOR))
            .map(this::mapLabel)
            .distinct()
            .collect(Collectors.toList());

        if (labelFilter.endsWith(",")) {
            labels.add(EMPTY_LABEL);
        }

        Collections.reverse(labels);
        String[] t = new String[labels.size()];
        return labels.toArray(t);
    }

    /**
     * Returns resolved labels as a single comma-separated string.
     *
     * @param profiles the active Spring profiles to use as a fallback
     * @return comma-separated label string
     */
    public String getLabelFilterText(List<String> profiles) {
        return String.join(",", getLabelFilter(profiles));
    }

    /**
     * Sets the label filter for feature flags.
     *
     * @param labelFilter a comma-separated string of labels to filter by
     * @return this {@link FeatureFlagKeyValueSelector} for chaining
     */
    public FeatureFlagKeyValueSelector setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
        return this;
    }

    /**
     * Returns the list of tag filters, or {@code null} if not set.
     *
     * @return the tag filter list
     */
    public List<String> getTagsFilter() {
        return tagsFilter;
    }

    /**
     * Sets the tag filters used to select feature flags by tags. Each entry is
     * interpreted as a tag-based filter, typically in the {@code tagName=tagValue}
     * format. When multiple entries are provided, they are combined using AND logic.
     *
     * @param tagsFilter list of tag expressions, typically in {@code tagName=tagValue} format
     * @return this {@link FeatureFlagKeyValueSelector} for chaining
     */
    public FeatureFlagKeyValueSelector setTagsFilter(List<String> tagsFilter) {
        this.tagsFilter = tagsFilter;
        return this;
    }

    /**
     * Validates that the label filter does not contain asterisks and that tag filters
     * follow the expected {@code tagName=tagValue} format.
     */
    @PostConstruct
    void validateAndInit() {
        if (labelFilter != null) {
            Assert.isTrue(!labelFilter.contains("*"), "LabelFilter must not contain asterisk(*)");
        }
        ValidationUtil.validateTagsFilter(tagsFilter);
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label) || EMPTY_LABEL.equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
