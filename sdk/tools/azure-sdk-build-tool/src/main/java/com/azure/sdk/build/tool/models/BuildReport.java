package com.azure.sdk.build.tool.models;

import com.azure.sdk.build.tool.util.AnnotatedMethodCallerResult;
import com.azure.sdk.build.tool.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The build report that contains detailed information about the build including failure messages, recommended
 * changes and Azure SDK usage.
 */
public class BuildReport {
    private final List<String> warningMessages;
    private final List<String> errorMessages;
    private final List<String> failureMessages;

    private List<String> azureDependencies;
    private Set<AnnotatedMethodCallerResult> serviceMethodCalls;
    private Set<AnnotatedMethodCallerResult> betaMethodCalls;
    private Set<OutdatedDependency> outdatedDirectDependencies;
    private Set<OutdatedDependency> outdatedTransitiveDependencies;
    private String bomVersion;
    private String jsonReport;

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public BuildReport() {
        this.warningMessages = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.failureMessages = new ArrayList<>();
    }

    public String getBomVersion() {
        return this.bomVersion;
    }

    public List<String> getAzureDependencies() {
        return this.azureDependencies;
    }

    public void addWarningMessage(String message) {
        warningMessages.add(message);
    }

    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    public void addFailureMessage(String message) {
        failureMessages.add(message);
    }

    public void setServiceMethodCalls(Set<AnnotatedMethodCallerResult> serviceMethodCalls) {
        this.serviceMethodCalls = serviceMethodCalls;
    }

    public void setBetaMethodCalls(Set<AnnotatedMethodCallerResult> betaMethodCalls) {
        this.betaMethodCalls = betaMethodCalls;
    }

    public void setOutdatedDirectDependencies(Set<OutdatedDependency> outdatedDirectDependencies) {
        this.outdatedDirectDependencies = outdatedDirectDependencies;
    }

    public void setOutdatedTransitiveDependencies(Set<OutdatedDependency> outdatedTransitiveDependencies) {
        this.outdatedTransitiveDependencies = outdatedTransitiveDependencies;
    }

    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }

    public void setAzureDependencies(List<String> azureDependencies) {
        this.azureDependencies = azureDependencies;
    }

    public void setJsonReport(String jsonReport) {
        this.jsonReport = jsonReport;
    }

    public String getJsonReport() {
        return jsonReport;
    }

    public Set<AnnotatedMethodCallerResult> getServiceMethodCalls() {
        return this.serviceMethodCalls;
    }

    public Set<AnnotatedMethodCallerResult> getBetaMethodCalls() {
        return this.betaMethodCalls;
    }
}
