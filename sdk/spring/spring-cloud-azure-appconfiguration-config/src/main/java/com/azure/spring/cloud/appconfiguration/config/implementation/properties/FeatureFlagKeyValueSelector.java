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

import jakarta.annotation.PostConstruct;

/**
 * Properties on what Selects are checked before loading configurations.
 */
public final class FeatureFlagKeyValueSelector {

    /**
     * Label for requesting all configurations with (No Label)
     */
    private static final String[] EMPTY_LABEL_ARRAY = { EMPTY_LABEL };

    /**
     * Key filter to use when loading feature flags. The provided key filter is
     * appended after the feature flag prefix, ".appconfig.featureflag/". By
     * default, all feature flags are loaded.
     */
    private String keyFilter = "";

    /**
     * Label filter to use when loading feature flags. By default, all feature flags
     * with no label are loaded. The label filter must be a non-null string that
     * does not contain an asterisk.
     */
    private String labelFilter;

    /**
     * @return the keyFilter
     */
    public String getKeyFilter() {
        return keyFilter;
    }

    /**
     * @param keyFilter the keyFilter to set
     * @return AppConfigurationStoreSelects
     */
    public FeatureFlagKeyValueSelector setKeyFilter(String keyFilter) {
        this.keyFilter = keyFilter;
        return this;
    }

    /**
     * @param profiles List of current Spring profiles to default to using is null label is set.
     * @return List of reversed label values, which are split by the separator, the latter label has higher priority
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
     * Get all labels as a single String
     * 
     * @param profiles current user profiles
     * @return comma separated list of labels
     */
    public String getLabelFilterText(List<String> profiles) {
        return String.join(",", getLabelFilter(profiles));
    }

    /**
     * @param labelFilter the labelFilter to set
     * @return AppConfigurationStoreSelects
     */
    public FeatureFlagKeyValueSelector setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
        return this;
    }

    /**
     * Validates key-filter and label-filter are valid.
     */
    @PostConstruct
    public void validateAndInit() {
        if (labelFilter != null) {
            Assert.isTrue(!labelFilter.contains("*"), "LabelFilter must not contain asterisk(*)");
        }
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label) || EMPTY_LABEL.equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
