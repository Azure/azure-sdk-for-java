// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;

import com.azure.spring.cloud.appconfiguration.config.implementation.ValidationUtil;

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
    private static final String LABEL_SEPARATOR = ",";


    /**
     * Filters configurations by key prefix. Defaults to {@code /application/} when
     * not explicitly set. Must not be {@code null} or contain asterisks ({@code *}).
     */
    @NotNull
    private String keyFilter = "";

    /**
     * Filters configurations by label. When unset, defaults to the active Spring
     * profiles; if no profiles are active, only configurations with no label are
     * loaded. Multiple labels can be specified as a comma-separated string. Must
     * not contain asterisks ({@code *}).
     */
    private String labelFilter;

    /**
     * Filters configurations by tags. Each entry is interpreted as a tag-based filter,
     * typically in the {@code tagName=tagValue} format. When multiple entries are
     * provided, they are combined using AND logic.
     */
    private List<String> tagsFilter;

    /**
     * Loads configurations from a named snapshot. Cannot be used together with
     * key, label, or tag filters.
     */
    private String snapshotName = "";

    /**
     * Returns the key filter, defaulting to {@code /application/} when not explicitly set.
     *
     * @return the key filter string
     */
    public String getKeyFilter() {
        return StringUtils.hasText(keyFilter) ? keyFilter : APPLICATION_SETTING_DEFAULT_KEY_FILTER;
    }

    /**
     * Sets the key filter used to select configurations by key prefix.
     *
     * @param keyFilter the key prefix to filter by
     * @return this {@link AppConfigurationKeyValueSelector} for chaining
     */
    public AppConfigurationKeyValueSelector setKeyFilter(String keyFilter) {
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
     * Resolves the label filter into an array of labels. When no label filter is set,
     * falls back to the active Spring profiles (in reverse priority order). If neither
     * is available, returns the empty-label sentinel. Returns an empty array when a
     * snapshot is configured.
     *
     * @param profiles the active Spring profiles to use as a fallback
     * @return an array of resolved labels, ordered from lowest to highest priority
     */
    public String[] getLabelFilter(List<String> profiles) {
        if (StringUtils.hasText(snapshotName)) {
            return new String[0];
        }
        if (labelFilter == null && !profiles.isEmpty()) {
            List<String> mutableProfiles = new ArrayList<>(profiles);
            // Defensive copy: profiles may be immutable when provided by certain Spring Boot contexts,
            // such as when obtained from Environment.getActiveProfiles(). See
            // https://github.com/Azure/azure-sdk-for-java/issues/32708 for details.
            Collections.reverse(mutableProfiles);
            return mutableProfiles.toArray(new String[mutableProfiles.size()]);
        } 
        if (!StringUtils.hasText(labelFilter)) {
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
     * Sets the label filter used to select configurations by label.
     *
     * @param labelFilter a comma-separated string of labels to filter by
     * @return this {@link AppConfigurationKeyValueSelector} for chaining
     */
    public AppConfigurationKeyValueSelector setLabelFilter(String labelFilter) {
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
     * Sets the tag filters used to select configurations by tags. Each entry is
     * interpreted as a tag-based filter, typically in the {@code tagName=tagValue}
     * format. When multiple entries are provided, they are combined using AND logic.
     *
     * @param tagsFilter list of tag expressions, typically in {@code tagName=tagValue} format
     * @return this {@link AppConfigurationKeyValueSelector} for chaining
     */
    public AppConfigurationKeyValueSelector setTagsFilter(List<String> tagsFilter) {
        this.tagsFilter = tagsFilter;
        return this;
    }

    /**
     * Returns the snapshot name, or an empty string if not set.
     *
     * @return the snapshot name
     */
    public String getSnapshotName() {
        return snapshotName;
    }

    /**
     * Sets the snapshot name to load configurations from.
     *
     * @param snapshotName the snapshot name
     */
    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }
    
    /**
     * Validates that key, label, tag, and snapshot filters are well-formed and
     * mutually compatible. Asterisks are not allowed in key or label filters,
     * and snapshots cannot be combined with any other filter type.
     */
    @PostConstruct
    void validateAndInit() {
        Assert.isTrue(!keyFilter.contains("*"), "KeyFilter must not contain asterisk(*)");
        if (labelFilter != null) {
            Assert.isTrue(!labelFilter.contains("*"), "LabelFilter must not contain asterisk(*)");
        }
        Assert.isTrue(!(StringUtils.hasText(keyFilter) && StringUtils.hasText(snapshotName)),
            "Snapshots can't use key filters");
        Assert.isTrue(!(StringUtils.hasText(labelFilter) && StringUtils.hasText(snapshotName)),
            "Snapshots can't use label filters");
        Assert.isTrue(!(tagsFilter != null && !tagsFilter.isEmpty() && StringUtils.hasText(snapshotName)),
            "Snapshots can't use tag filters");
        ValidationUtil.validateTagsFilter(tagsFilter);
    }

    private String mapLabel(String label) {
        if (label == null || "".equals(label) || EMPTY_LABEL.equals(label)) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
