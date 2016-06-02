/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Describes the source control configuration for web app.
 */
@JsonFlatten
public class SiteSourceControlInner extends Resource {
    /**
     * Repository or source control url.
     */
    @JsonProperty(value = "properties.repoUrl")
    private String repoUrl;

    /**
     * Name of branch to use for deployment.
     */
    @JsonProperty(value = "properties.branch")
    private String branch;

    /**
     * Whether to manual or continuous integration.
     */
    @JsonProperty(value = "properties.isManualIntegration")
    private Boolean isManualIntegration;

    /**
     * Whether to manual or continuous integration.
     */
    @JsonProperty(value = "properties.deploymentRollbackEnabled")
    private Boolean deploymentRollbackEnabled;

    /**
     * Mercurial or Git repository type.
     */
    @JsonProperty(value = "properties.isMercurial")
    private Boolean isMercurial;

    /**
     * Get the repoUrl value.
     *
     * @return the repoUrl value
     */
    public String repoUrl() {
        return this.repoUrl;
    }

    /**
     * Set the repoUrl value.
     *
     * @param repoUrl the repoUrl value to set
     * @return the SiteSourceControlInner object itself.
     */
    public SiteSourceControlInner withRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }

    /**
     * Get the branch value.
     *
     * @return the branch value
     */
    public String branch() {
        return this.branch;
    }

    /**
     * Set the branch value.
     *
     * @param branch the branch value to set
     * @return the SiteSourceControlInner object itself.
     */
    public SiteSourceControlInner withBranch(String branch) {
        this.branch = branch;
        return this;
    }

    /**
     * Get the isManualIntegration value.
     *
     * @return the isManualIntegration value
     */
    public Boolean isManualIntegration() {
        return this.isManualIntegration;
    }

    /**
     * Set the isManualIntegration value.
     *
     * @param isManualIntegration the isManualIntegration value to set
     * @return the SiteSourceControlInner object itself.
     */
    public SiteSourceControlInner withIsManualIntegration(Boolean isManualIntegration) {
        this.isManualIntegration = isManualIntegration;
        return this;
    }

    /**
     * Get the deploymentRollbackEnabled value.
     *
     * @return the deploymentRollbackEnabled value
     */
    public Boolean deploymentRollbackEnabled() {
        return this.deploymentRollbackEnabled;
    }

    /**
     * Set the deploymentRollbackEnabled value.
     *
     * @param deploymentRollbackEnabled the deploymentRollbackEnabled value to set
     * @return the SiteSourceControlInner object itself.
     */
    public SiteSourceControlInner withDeploymentRollbackEnabled(Boolean deploymentRollbackEnabled) {
        this.deploymentRollbackEnabled = deploymentRollbackEnabled;
        return this;
    }

    /**
     * Get the isMercurial value.
     *
     * @return the isMercurial value
     */
    public Boolean isMercurial() {
        return this.isMercurial;
    }

    /**
     * Set the isMercurial value.
     *
     * @param isMercurial the isMercurial value to set
     * @return the SiteSourceControlInner object itself.
     */
    public SiteSourceControlInner withIsMercurial(Boolean isMercurial) {
        this.isMercurial = isMercurial;
        return this;
    }

}
