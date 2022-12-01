// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuickPulseDependencyDocument extends QuickPulseDocument {

    @JsonProperty(value = "Name")
    private String name;

    @JsonProperty(value = "Target")
    private String target;

    @JsonProperty(value = "Success")
    private boolean success;

    @JsonProperty(value = "Duration")
    private String duration;

    @JsonProperty(value = "ResultCode")
    private String resultCode;

    @JsonProperty(value = "CommandName")
    private String commandName;

    @JsonProperty(value = "DependencyTypeName")
    private String dependencyTypeName;

    @JsonProperty(value = "OperationName")
    private String operationName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getDependencyTypeName() {
        return dependencyTypeName;
    }

    public void setDependencyTypeName(String dependencyTypeName) {
        this.dependencyTypeName = dependencyTypeName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
