// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * Configurations that allow callers to specify details about how to execute a custom entities recognition action in
 * a set of documents.
 *
 * See the service documentation for regional support of custom entities recognition:
 * https://aka.ms/azsdk/textanalytics/customentityrecognition
 */
@Fluent
public final class RecognizeCustomEntitiesAction {
    private String actionName;
    private final String projectName;
    private final String deploymentName;
    private boolean disableServiceLogs;

    /**
     * Configurations that allow callers to specify details about how to execute a custom entities recognition action in
     * a set of documents.
     *
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     */
    public RecognizeCustomEntitiesAction(String projectName, String deploymentName) {
        this.projectName = projectName;
        this.deploymentName = deploymentName;
    }

    /**
     * Get the name of action.
     *
     * @return the name of action.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Set the custom name for the action.
     *
     * @param actionName the custom name for the action.
     *
     * @return The {@link RecognizeCustomEntitiesAction} object itself.
     */
    public RecognizeCustomEntitiesAction setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    /**
     * Gets the name of the project which owns the model being consumed.
     *
     * @return The name of the project which owns the model being consumed.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the name of the deployment being consumed.
     *
     * @return The name of the deployment being consumed.
     */
    public String getDeploymentName() {
        return deploymentName;
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
     * @return The {@link RecognizeCustomEntitiesAction} object itself.
     */
    public RecognizeCustomEntitiesAction setServiceLogsDisabled(boolean disableServiceLogs) {
        this.disableServiceLogs = disableServiceLogs;
        return this;
    }
}
