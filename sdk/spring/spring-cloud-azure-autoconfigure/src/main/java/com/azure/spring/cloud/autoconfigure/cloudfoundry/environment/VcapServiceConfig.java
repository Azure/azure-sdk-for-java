// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry.environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * config class for VcapService
 */
class VcapServiceConfig {
    private Map<String, String> credentials = new HashMap<>();
    private String label;
    private String name;
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String plan;
    private String provider;

    @JsonProperty("syslog_drain_url")
    private String syslogDrainUrl;
    private String[] tags;

    @JsonProperty("volume_mounts")
    private String[] volumeMounts;

    /**
     * Gets the credentials.
     *
     * @return the credentials
     */
    public Map<String, String> getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials.
     *
     * @param credentials the credentials
     */
    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName the service name
     */
    public void setName(String serviceName) {
        this.name = serviceName;
    }

    /**
     * Gets the plan.
     *
     * @return the plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Sets the plan.
     *
     * @param plan the plan
     */
    public void setPlan(String plan) {
        this.plan = plan;
    }

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider.
     *
     * @param provider the provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Gets the system log drain URL.
     *
     * @return the system log drain URL
     */
    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    /**
     * Sets the system log drain URL.
     *
     * @param syslogDrainUrl the system log drain URL
     */
    public void setSyslogDrainUrl(String syslogDrainUrl) {
        this.syslogDrainUrl = syslogDrainUrl;
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public String[] getTags() {
        return arrayCopy(tags);
    }

    /**
     * Sets the tags.
     *
     * @param tags the tags
     */
    public void setTags(String[] tags) {
        this.tags = arrayCopy(tags);
    }

    /**
     * Gets the volume mounts.
     *
     * @return the volume mounts
     */
    public String[] getVolumeMounts() {
        return arrayCopy(volumeMounts);
    }

    /**
     * Sets the volume mounts.
     *
     * @param volumeMounts the volume mounts
     */
    public void setVolumeMounts(String[] volumeMounts) {
        this.volumeMounts = arrayCopy(volumeMounts);
    }

    private static <T> T[] arrayCopy(T[] origin) {
        return origin == null ? null : Arrays.copyOf(origin, origin.length);
    }

    @Override
    public String toString() {
        return "VcapServiceConfig [credentials=" + credentials + ", label=" + label
                + ", serviceName=" + name + ", plan="
                + plan + ", provider=" + provider + ", syslogDrainUrl="
                + syslogDrainUrl + ", tags=" + Arrays.toString(tags)
                + ", volumeMounts=" + Arrays.toString(volumeMounts) + "]";
    }

}
