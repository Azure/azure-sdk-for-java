// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

/**
 * Properties on what Selects are checked before loading configurations.
 */
public final class AppConfigurationKeyValueSelector {

    /**
     * Label for requesting all configurations with (No Label)
     */
    private static final String[] EMPTY_LABEL_ARRAY = { EMPTY_LABEL };

    private static final String APPLICATION_SETTING_DEFAULT_KEY_FILTER = "/application/";

    /**
     * Separator for multiple labels
     */
    public static final String LABEL_SEPARATOR = ",";

    @NotNull
    private String keyFilter = "";

    private String labelFilter;

    private String snapshotName = "";
    /**
     * @return the keyFilter
     */
    public String getKeyFilter() {
        return StringUtils.hasText(keyFilter) ? keyFilter : APPLICATION_SETTING_DEFAULT_KEY_FILTER;
    }

    /**
     * @param keyFilter the keyFilter to set
     * @return AppConfigurationStoreSelects
     */
    public AppConfigurationKeyValueSelector setKeyFilter(String keyFilter) {
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
        } else if (StringUtils.hasText(snapshotName)) {
            return new String[0];
        } else if (!StringUtils.hasText(labelFilter)) {
            return EMPTY_LABEL_ARRAY;
        }

        // The use of trim makes label= dev,prod and label= dev, prod equal.
        List<String> labels = Arrays.stream(labelFilter.split(LABEL_SEPARATOR)).map(this::mapLabel).distinct()
            .collect(Collectors.toList());

        if (labelFilter.endsWith(",")) {
            labels.add(EMPTY_LABEL);
        }

        Collections.reverse(labels);
        String[] t = new String[labels.size()];
        return labels.toArray(t);
    }

    /**
     * @param labelFilter the labelFilter to set
     * @return AppConfigurationStoreSelects
     */
    public AppConfigurationKeyValueSelector setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
        return this;
    }

    /**
     * @return the snapshot
     */
    public String getSnapshotName() {
        return snapshotName;
    }

    /**
     * @param snapshot the snapshot to set
     */
    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }
    
    /**
     * Validates key-filter and label-filter are valid.
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.isTrue(!keyFilter.contains("*"), "KeyFilter must not contain asterisk(*)");
        if (labelFilter != null) {
            Assert.isTrue(!labelFilter.contains("*"), "LabelFilter must not contain asterisk(*)");
        }
        Assert.isTrue(!(StringUtils.hasText(keyFilter) && StringUtils.hasText(snapshotName)),
            "Snapshots can't use key filters");
        Assert.isTrue(!(StringUtils.hasText(labelFilter) && StringUtils.hasText(snapshotName)),
            "Snapshots can't use label filters");
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label) || EMPTY_LABEL.equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
