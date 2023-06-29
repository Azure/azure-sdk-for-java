package com.azure.sdk.build.tool.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The build report that contains detailed information about the build including failure messages, recommended
 * changes and Azure SDK usage.
 */
public class BuildReport {
    @JsonProperty
    private String groupId;
    @JsonProperty
    private String artifactId;
    @JsonProperty
    private String version;
    @JsonProperty
    private String bomVersion;
    @JsonProperty
    private List<String> azureDependencies;
    @JsonProperty
    private List<OutdatedDependency> outdatedDirectDependencies;
    @JsonProperty
    private List<OutdatedDependency> outdatedTransitiveDependencies;
    @JsonProperty
    private List<MethodCallDetails> serviceMethodCalls;
    @JsonProperty
    private List<MethodCallDetails> betaMethodCalls;
    @JsonProperty
    private final List<BuildError> errors;

    public BuildReport() {
        this.errors = new ArrayList<>();
    }

    public List<BuildError> getErrors() {
        return errors;
    }

    public String getBomVersion() {
        return this.bomVersion;
    }

    public List<String> getAzureDependencies() {
        return this.azureDependencies;
    }

    public void addError(BuildError error) {
        errors.add(error);
    }

    public void setServiceMethodCalls(List<MethodCallDetails> serviceMethodCalls) {
        this.serviceMethodCalls = serviceMethodCalls;
    }

    public void setBetaMethodCalls(List<MethodCallDetails> betaMethodCalls) {
        this.betaMethodCalls = betaMethodCalls;
    }

    public void setOutdatedDirectDependencies(List<OutdatedDependency> outdatedDirectDependencies) {
        this.outdatedDirectDependencies = outdatedDirectDependencies;
    }

    public List<OutdatedDependency> getOutdatedDirectDependencies() {
        return outdatedDirectDependencies;
    }

    public void setOutdatedTransitiveDependencies(List<OutdatedDependency> outdatedTransitiveDependencies) {
        this.outdatedTransitiveDependencies = outdatedTransitiveDependencies;
    }

    public List<OutdatedDependency> getOutdatedTransitiveDependencies() {
        return outdatedTransitiveDependencies;
    }

    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }

    public void setAzureDependencies(List<String> azureDependencies) {
        this.azureDependencies = azureDependencies;
    }

    public List<MethodCallDetails> getServiceMethodCalls() {
        return this.serviceMethodCalls;
    }

    public List<MethodCallDetails> getBetaMethodCalls() {
        return this.betaMethodCalls;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
