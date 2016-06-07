/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Deployment properties.
 */
public class DeploymentProperties {
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
     * @return the DeploymentProperties object itself.
     */
    public DeploymentProperties withTemplate(Object template) {
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
     * @return the DeploymentProperties object itself.
     */
    public DeploymentProperties withTemplateLink(TemplateLink templateLink) {
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
     * @return the DeploymentProperties object itself.
     */
    public DeploymentProperties withParameters(Object parameters) {
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
     * @return the DeploymentProperties object itself.
     */
    public DeploymentProperties withParametersLink(ParametersLink parametersLink) {
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
     * @return the DeploymentProperties object itself.
     */
    public DeploymentProperties withMode(DeploymentMode mode) {
        this.mode = mode;
        return this;
    }

}
