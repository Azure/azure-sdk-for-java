/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static com.microsoft.azure.spring.cloud.config.AppConfigurationProperties.LABEL_SEPARATOR;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.config.resource.Connection;

/**
 * Config Store Properties for Requests to an Azure App Configuration Store.
 */
public class ConfigStore {
    private static final String EMPTY_LABEL = "\0";
    private static final String[] EMPTY_LABEL_ARRAY = {EMPTY_LABEL};
    private String endpoint; // Config store endpoint

    @Nullable
    @Pattern(regexp = "(/[a-zA-Z0-9.\\-_]+)*")
    private String prefix;

    private String connectionString;

    // Label values separated by comma in the Azure Config Service, can be empty
    @Nullable
    private String label;

    // The keys to be watched, won't take effect if watch not enabled
    @NotEmpty
    private String watchedKey = "*";

    private boolean failFast = true;

    /**
     * Creates a {@link ConfigStore}.
     */
    public ConfigStore() {
        label = null;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the connectionString
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @param connectionString the connectionString to set
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the watchedKey
     */
    public String getWatchedKey() {
        return watchedKey;
    }

    /**
     * @param watchedKey the watchedKey to set
     */
    public void setWatchedKey(String watchedKey) {
        this.watchedKey = watchedKey;
    }

    /**
     * @return the failFast
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * @param failFast the failFast to set
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
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

        Assert.isTrue(watchedKeyValid(this.watchedKey), "Watched key can only be a single asterisk(*) or " +
                "a specific key without asterisk(*)");
    }

    private boolean watchedKeyValid(String watchedKey) {
        if (!StringUtils.hasText(watchedKey)) {
            return false;
        }

        String trimmedKey = watchedKey.trim();
        // Watched key can either be single asterisk(*) or a specific key without asterisk(*)
        return trimmedKey.equals("*") || !trimmedKey.contains("*");
    }

    /**
     * @return List of reversed label values, which are split by the separator, the latter label has higher priority
     */
    public String[] getLabels() {
        if (!StringUtils.hasText(this.getLabel())) {
            return EMPTY_LABEL_ARRAY;
        }

        // The use of trim makes label= dev,prod and label= dev, prod equal.
        List<String> labels =  Arrays.stream(this.getLabel().split(LABEL_SEPARATOR))
                .map(this::mapLabel)
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
