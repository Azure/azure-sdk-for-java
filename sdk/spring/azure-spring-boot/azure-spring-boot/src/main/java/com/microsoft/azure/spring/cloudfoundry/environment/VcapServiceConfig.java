/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloudfoundry.environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public String[] getTags() {
        return tags;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public String[] getVolumeMounts() {
        return volumeMounts;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setVolumeMounts(String[] volumeMounts) {
        this.volumeMounts = volumeMounts;
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
