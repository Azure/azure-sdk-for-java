/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import org.joda.time.DateTime;
import java.util.List;

/**
 * Deployment properties with additional details.
 */
public class DeploymentPropertiesExtended {
    /**
     * Gets or sets the state of the provisioning.
     */
    private String provisioningState;

    /**
     * Gets or sets the correlation ID of the deployment.
     */
    private String correlationId;

    /**
     * Gets or sets the timestamp of the template deployment.
     */
    private DateTime timestamp;

    /**
     * Gets or sets key/value pairs that represent deploymentoutput.
     */
    private Object outputs;

    /**
     * Gets the list of resource providers needed for the deployment.
     */
    private List<ProviderInner> providers;

    /**
     * Gets the list of deployment dependencies.
     */
    private List<Dependency> dependencies;

    /**
     * Gets or sets the template content. Use only one of Template or
     * TemplateLink.
     */
    private Object template;

    /**
     * Gets or sets the URI referencing the template. Use only one of Template
     * or TemplateLink.
     */
    private TemplateLink templateLink;

    /**
     * Deployment parameters. Use only one of Parameters or ParametersLink.
     */
    private Object parameters;

    /**
     * Gets or sets the URI referencing the parameters. Use only one of
     * Parameters or ParametersLink.
     */
    private ParametersLink parametersLink;

    /**
     * Gets or sets the deployment mode. Possible values include:
     * 'Incremental', 'Complete'.
     */
    private DeploymentMode mode;

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the correlationId value.
     *
     * @return the correlationId value
     */
    public String correlationId() {
        return this.correlationId;
    }

    /**
     * Set the correlationId value.
     *
     * @param correlationId the correlationId value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public DateTime timestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get the outputs value.
     *
     * @return the outputs value
     */
    public Object outputs() {
        return this.outputs;
    }

    /**
     * Set the outputs value.
     *
     * @param outputs the outputs value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withOutputs(Object outputs) {
        this.outputs = outputs;
        return this;
    }

    /**
     * Get the providers value.
     *
     * @return the providers value
     */
    public List<ProviderInner> providers() {
        return this.providers;
    }

    /**
     * Set the providers value.
     *
     * @param providers the providers value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withProviders(List<ProviderInner> providers) {
        this.providers = providers;
        return this;
    }

    /**
     * Get the dependencies value.
     *
     * @return the dependencies value
     */
    public List<Dependency> dependencies() {
        return this.dependencies;
    }

    /**
     * Set the dependencies value.
     *
     * @param dependencies the dependencies value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    /**
     * Get the template value.
     *
     * @return the template value
     */
    public Object template() {
        return this.template;
    }

    /**
     * Set the template value.
     *
     * @param template the template value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withTemplate(Object template) {
        this.template = template;
        return this;
    }

    /**
     * Get the templateLink value.
     *
     * @return the templateLink value
     */
    public TemplateLink templateLink() {
        return this.templateLink;
    }

    /**
     * Set the templateLink value.
     *
     * @param templateLink the templateLink value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withTemplateLink(TemplateLink templateLink) {
        this.templateLink = templateLink;
        return this;
    }

    /**
     * Get the parameters value.
     *
     * @return the parameters value
     */
    public Object parameters() {
        return this.parameters;
    }

    /**
     * Set the parameters value.
     *
     * @param parameters the parameters value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withParameters(Object parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Get the parametersLink value.
     *
     * @return the parametersLink value
     */
    public ParametersLink parametersLink() {
        return this.parametersLink;
    }

    /**
     * Set the parametersLink value.
     *
     * @param parametersLink the parametersLink value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withParametersLink(ParametersLink parametersLink) {
        this.parametersLink = parametersLink;
        return this;
    }

    /**
     * Get the mode value.
     *
     * @return the mode value
     */
    public DeploymentMode mode() {
        return this.mode;
    }

    /**
     * Set the mode value.
     *
     * @param mode the mode value to set
     * @return the DeploymentPropertiesExtended object itself.
     */
    public DeploymentPropertiesExtended withMode(DeploymentMode mode) {
        this.mode = mode;
        return this;
    }

}
