// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import static com.azure.spring.cloud.config.properties.AppConfigurationProperties.LABEL_SEPARATOR;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.config.resource.Connection;

/**
 * Config Store Properties for Requests to an Azure App Configuration Store.
 */
public class ConfigStore {

    private static final String EMPTY_LABEL = "\0";

    private static final String[] EMPTY_LABEL_ARRAY = { EMPTY_LABEL };

    private String endpoint; // Config store endpoint

    private String connectionString;

    // Label values separated by comma in the Azure Config Service, can be empty
    @Nullable
    private String label;

    private boolean failFast = true;

    private FeatureFlagStore featureFlags = new FeatureFlagStore();

    private boolean enabled = true;

    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    public ConfigStore() {
        label = null;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the monitoring
     */
    public AppConfigurationStoreMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * @param monitoring the monitoring to set
     */
    public void setMonitoring(AppConfigurationStoreMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    public FeatureFlagStore getFeatureFlags() {
        return featureFlags;
    }

    public void setFeatureFlags(FeatureFlagStore featureFlags) {
        this.featureFlags = featureFlags;
    }

    /**
     * @throws IllegalStateException Connection String URL endpoint is invalid
     */
    @PostConstruct
    public void validateAndInit() {
        if (StringUtils.hasText(label)) {
            Assert.isTrue(!label.contains("*"), "Label must not contain asterisk(*).");
        }

        if (StringUtils.hasText(connectionString)) {
            String endpoint = (new Connection(connectionString)).getEndpoint();
            try {
                // new URI is used to validate the endpoint as a valid URI
                new URI(endpoint);
                this.endpoint = endpoint;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
            }
        }

        monitoring.validateAndInit();
    }

    /**
     * @param profiles List of current Spring profiles to default to using is null label is set.
     * @return List of reversed label values, which are split by the separator, the latter label has higher priority
     */
    public String[] getLabels(List<String> profiles) {
        if (this.getLabel() == null && profiles.size() > 0) {
            Collections.reverse(profiles);
            return profiles.toArray(new String[profiles.size()]);
        } else if (!StringUtils.hasText(this.getLabel())) {
            return EMPTY_LABEL_ARRAY;
        }

        // The use of trim makes label= dev,prod and label= dev, prod equal.
        List<String> labels = Arrays.stream(this.getLabel().split(LABEL_SEPARATOR))
            .map(label -> mapLabel(label))
            .distinct()
            .collect(Collectors.toList());

        if (this.getLabel().endsWith(",")) {
            labels.add(EMPTY_LABEL);
        }

        Collections.reverse(labels);
        if (labels.isEmpty()) {
            return EMPTY_LABEL_ARRAY;
        } else {
            String[] t = new String[labels.size()];
            return labels.toArray(t);
        }
    }

    private String mapLabel(String label) {
        if (label == null || label.equals("")) {
            return EMPTY_LABEL;
        }
        return label.trim();
    }
}
