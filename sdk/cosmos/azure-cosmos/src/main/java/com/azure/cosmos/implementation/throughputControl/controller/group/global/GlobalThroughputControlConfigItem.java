// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class GlobalThroughputControlConfigItem extends GlobalThroughputControlItem {

    @JsonProperty(value = "targetThroughput")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String targetThroughput;

    @JsonProperty(value = "targetThroughputThreshold")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String targetThroughputThreshold;

    @JsonProperty(value = "isDefault", required = true)
    private boolean isDefault;

    /**
     * Constructor used for Json deserialization
     */
    public GlobalThroughputControlConfigItem() {

    }

    public GlobalThroughputControlConfigItem(
        String id,
        String partitionKeyValue,
        Integer targetThroughput,
        Double targetThroughputThreshold,
        boolean isDefault) {

        super(id, partitionKeyValue);
        this.targetThroughput = targetThroughput != null ? targetThroughput.toString() : StringUtils.EMPTY;
        this.targetThroughputThreshold = targetThroughputThreshold != null ? targetThroughputThreshold.toString() : StringUtils.EMPTY;
        this.isDefault = isDefault;
    }

    public String getTargetThroughput() {
        return targetThroughput;
    }

    public void setTargetThroughput(String targetThroughput) {
        this.targetThroughput = targetThroughput;
    }

    public String getTargetThroughputThreshold() {
        return targetThroughputThreshold;
    }

    public void setTargetThroughputThreshold(String targetThroughputThreshold) {
        this.targetThroughputThreshold = targetThroughputThreshold;
    }

    @JsonIgnore
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GlobalThroughputControlConfigItem that = (GlobalThroughputControlConfigItem) other;

        return StringUtils.equals(this.getId(), that.getId())
            && StringUtils.equals(this.getGroupId(), that.getGroupId())
            && StringUtils.equals(this.targetThroughput, that.targetThroughput)
            && StringUtils.equals(this.targetThroughputThreshold, that.targetThroughputThreshold)
            && this.isDefault == that.isDefault;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.getId(), this.getGroupId(), targetThroughput, targetThroughputThreshold);
        result = 31 * result + Boolean.hashCode(this.isDefault);
        return result;
    }

    @Override
    public String toString() {
        return "ThroughputGlobalControlConfigItem{" +
            "id='" + this.getId() + '\'' +
            "groupId='" + this.getGroupId() + '\'' +
            "targetThroughput='" + this.targetThroughput + '\'' +
            ", targetThroughputThreshold='" + this.targetThroughputThreshold + '\'' +
            ", isDefault=" + this.isDefault +
            '}';
    }
}
