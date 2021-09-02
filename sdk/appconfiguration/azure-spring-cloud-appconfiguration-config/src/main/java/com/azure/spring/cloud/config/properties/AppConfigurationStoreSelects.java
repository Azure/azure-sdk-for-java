// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Properties on what Selects are checked before loading configurations.
 */
public final class AppConfigurationStoreSelects {

    private static final String EMPTY_LABEL = "\0";

    private static final String[] EMPTY_LABEL_ARRAY = { EMPTY_LABEL };

    public static final String LABEL_SEPARATOR = ",";

    @NotNull
    private String keyFilter = "/application/";

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
    public AppConfigurationStoreSelects setKeyFilter(String keyFilter) {
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
            .map(label -> mapLabel(label))
            .distinct()
            .collect(Collectors.toList());

        if (labelFilter.endsWith(",")) {
            labels.add(EMPTY_LABEL);
        }

        Collections.reverse(labels);
        String[] t = new String[labels.size()];
        return labels.toArray(t);
    }
    
    public String getLabelFilterText(List<String> profiles) {
        String[] labels = getLabelFilter(profiles);
        String labelText = "";
        for(String label: labels) {
            if(!labelText.isEmpty()) {
                labelText += ",";
            }
            labelText += label;
        }
        
        return labelText;
    }

    /**
     * Used for Generating Property Source name only.
     * 
     * @return String all labels combined.
     */
    public String getLabel() {
        return labelFilter;
    }

    /**
     * @param labelFilter the labelFilter to set
     * @return AppConfigurationStoreSelects
     */
    public AppConfigurationStoreSelects setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
        return this;
    }

    @PostConstruct
    public void validateAndInit() {
        Assert.isTrue(!keyFilter.contains("*"), "KeyFilter must not contain asterisk(*)");
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
