// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute a single-label classification action
 * in a set of documents.
 */
@Fluent
public final class ClassifySingleCategoryAction {
    private String projectName;
    private String deploymentName;
    private boolean disableServiceLogs;

    /**
     * Gets the name of the project which owns the model being consumed.
     *
     * @return The name of the project which owns the model being consumed.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the name of the project which owns the model being consumed.
     *
     * @param projectName The name of the project which owns the model being consumed.
     *
     * @return The {@link ClassifySingleCategoryAction} object itself.
     */
    public ClassifySingleCategoryAction setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Gets the name of the deployment (model version) being consumed.
     *
     * @return The name of the deployment (model version) being consumed.
     */
    public String getDeploymentName() {
        return deploymentName;
    }

    /**
     * Sets the name of the deployment (model version) being consumed.
     *
     * @param deploymentName The name of the deployment (model version) being consumed.
     *
     * @return The {@link ClassifySingleCategoryAction} object itself.
     */
    public ClassifySingleCategoryAction setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
        return this;
    }

    /**
     * Gets the value of {@code disableServiceLogs}.
     *
     * @return The value of {@code disableServiceLogs}. The default value of this property is 'false'. This means,
     * Text Analytics service logs your input text for 48 hours, solely to allow for troubleshooting issues. Setting
     * this property to true, disables input logging and may limit our ability to investigate issues that occur.
     */
    public boolean isServiceLogsDisabled() {
        return disableServiceLogs;
    }

    /**
     * Sets the value of {@code disableServiceLogs}.
     *
     * @param disableServiceLogs The default value of this property is 'false'. This means, Text Analytics service logs
     * your input text for 48 hours, solely to allow for troubleshooting issues. Setting this property to true,
     * disables input logging and may limit our ability to investigate issues that occur.
     *
     * @return The {@link ClassifySingleCategoryAction} object itself.
     */
    public ClassifySingleCategoryAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }
}
