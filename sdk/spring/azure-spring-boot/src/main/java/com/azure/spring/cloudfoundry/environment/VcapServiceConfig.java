// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * config class for VcapService
 */
public class VcapServiceConfig {
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

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String serviceName) {
        this.name = serviceName;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public void setSyslogDrainUrl(String syslogDrainUrl) {
        this.syslogDrainUrl = syslogDrainUrl;
    }

    public String[] getTags() {
        return arrayCopy(tags);
    }

    public void setTags(String[] tags) {
        this.tags = arrayCopy(tags);
    }

    public String[] getVolumeMounts() {
        return arrayCopy(volumeMounts);
    }

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
