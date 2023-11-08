// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuickPulseEnvelope {
    @JsonProperty(value = "Documents")
    private List<QuickPulseDocument> documents;

    @JsonProperty(value = "InstrumentationKey")
    private String instrumentationKey;

    @JsonProperty(value = "Metrics")
    private List<QuickPulseMetrics> metrics;

    @JsonProperty(value = "InvariantVersion")
    private int invariantVersion;

    @JsonProperty(value = "Timestamp")
    private String timeStamp;

    @JsonProperty(value = "Version")
    private String version;

    @JsonProperty(value = "StreamId")
    private String streamId;

    @JsonProperty(value = "MachineName")
    private String machineName;

    @JsonProperty(value = "Instance")
    private String instance;

    @JsonProperty(value = "RoleName")
    private String roleName;

    public List<QuickPulseDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<QuickPulseDocument> documents) {
        this.documents = documents;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    public List<QuickPulseMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<QuickPulseMetrics> metrics) {
        this.metrics = metrics;
    }

    public int getInvariantVersion() {
        return invariantVersion;
    }

    public void setInvariantVersion(int invariantVersion) {
        this.invariantVersion = invariantVersion;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
