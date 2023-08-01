// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

    /**
     * Creates an instance of {@link BuildReport}.
     */
    public BuildReport() {
        this.errors = new ArrayList<>();
    }

    /**
     * Returns the list of build errors.
     * @return The list of build errors.
     */
    public List<BuildError> getErrors() {
        return errors;
    }

    /**
     * Returns the version of the BOM used to build the project.
     * @return The version of the BOM used to build the project.
     */
    public String getBomVersion() {
        return this.bomVersion;
    }

    /**
     * The list of Azure dependencis used by the project.
     * @return The list of Azure dependencies used by the project.
     */
    public List<String> getAzureDependencies() {
        return this.azureDependencies;
    }

    /**
     * Adds a build error to the report.
     * @param error The build error to add.
     */
    public void addError(BuildError error) {
        errors.add(error);
    }

    /**
     * Sets the list of service method calls.
     * @param serviceMethodCalls the serviceMethodCalls to set
     */
    public void setServiceMethodCalls(List<MethodCallDetails> serviceMethodCalls) {
        this.serviceMethodCalls = serviceMethodCalls;
    }

    /**
     * Sets the list of beta method calls.
     * @param betaMethodCalls the betaMethodCalls to set.
     */
    public void setBetaMethodCalls(List<MethodCallDetails> betaMethodCalls) {
        this.betaMethodCalls = betaMethodCalls;
    }

    /**
     * Sets the list of outdated direct dependencies.
     * @param outdatedDirectDependencies the outdatedDirectDependencies to set
     */
    public void setOutdatedDirectDependencies(List<OutdatedDependency> outdatedDirectDependencies) {
        this.outdatedDirectDependencies = outdatedDirectDependencies;
    }

    /**
     * Returns the outdated direct dependencies.
     * @return the outdated direct dependencies.
     */
    public List<OutdatedDependency> getOutdatedDirectDependencies() {
        return outdatedDirectDependencies;
    }

    /**
     * Sets the list of outdated transitive dependencies.
     * @param outdatedTransitiveDependencies the outdated transitive dependencies to set
     */
    public void setOutdatedTransitiveDependencies(List<OutdatedDependency> outdatedTransitiveDependencies) {
        this.outdatedTransitiveDependencies = outdatedTransitiveDependencies;
    }

    /**
     * Returns the outdated transitive dependencies.
     * @return the outdated transitive dependencies.
     */
    public List<OutdatedDependency> getOutdatedTransitiveDependencies() {
        return outdatedTransitiveDependencies;
    }

    /**
     * Sets the version of the BOM used to build the project.
     * @param bomVersion the bomVersion to set
     */
    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }

    /**
     * Sets the list of Azure dependencies used by the project.
     * @param azureDependencies the azureDependencies to set
     */
    public void setAzureDependencies(List<String> azureDependencies) {
        this.azureDependencies = azureDependencies;
    }

    /**
     * Returns the list of service method calls.
     * @return the serviceMethodCalls
     */
    public List<MethodCallDetails> getServiceMethodCalls() {
        return this.serviceMethodCalls;
    }

    /**
     * Returns the list of beta method calls.
     * @return the betaMethodCalls
     */
    public List<MethodCallDetails> getBetaMethodCalls() {
        return this.betaMethodCalls;
    }

    /**
     * Sets the groupId of the project.
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the groupId of the project.
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the artifactId of the project.
     * @param artifactId the artifactId to set
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Returns the artifactId of the project.
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the version of the project.
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the version of the project.
     * @return the version
     */
    public String getVersion() {
        return version;
    }
}
