// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

/**
 * The options for OneDeploy.
 */
public class DeployOptions {

    private String path;
    private Boolean restartSite;
    private Boolean cleanDeployment;

    /**
     * @return the path for deploy
     */
    public String path() {
        return path;
    }

    /**
     * Specifies the path for deploy. Some some deploy type, path is required.
     *
     * @param path the path for deploy
     * @return the DeployOptions object
     */
    public DeployOptions withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * @return whether to restart site after deployment
     */
    public Boolean restartSite() {
        return restartSite;
    }

    /**
     * Specifies whether to restart site after deployment.
     *
     * By default, any OneDeploy call will restart the site. This behavior can be altered by this option.
     *
     * @param restartSite whether to restart side after deployment
     * @return the DeployOptions object
     */
    public DeployOptions withRestartSite(Boolean restartSite) {
        this.restartSite = restartSite;
        return this;
    }

    /**
     * @return whether to perform clean deployment
     */
    public Boolean cleanDeployment() {
        return cleanDeployment;
    }

    /**
     * Specifies whether to perform clean deployment.
     *
     * By default {@code type=zip} and {@code type=war&path=webapps/<appname>} performs clean deployment.
     * All other types of artifacts will be deployed incrementally.
     * The default behavior for any artifact type can be changed by this option.
     * A clean deployment nukes the default directory associated with the type of artifact being deployed.
     *
     * @param cleanDeployment whether to perform clean deployment
     * @return the DeployOptions object
     */
    public DeployOptions withCleanDeployment(Boolean cleanDeployment) {
        this.cleanDeployment = cleanDeployment;
        return this;
    }
}
