/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.Map;

/**
 * Represents information needed for cloning operation.
 */
public class CloningInfo {
    /**
     * Correlation Id of cloning operation. This id ties multiple cloning
     * operations
     * together to use the same snapshot.
     */
    private String correlationId;

    /**
     * Overwrite destination web app.
     */
    private Boolean overwrite;

    /**
     * If true, clone custom hostnames from source web app.
     */
    private Boolean cloneCustomHostNames;

    /**
     * Clone source control from source web app.
     */
    private Boolean cloneSourceControl;

    /**
     * ARM resource id of the source web app. Web app resource id is of the
     * form
     * /subscriptions/{subId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{siteName}
     * for production slots and
     * /subscriptions/{subId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{siteName}/slots/{slotName}
     * for other slots.
     */
    private String sourceWebAppId;

    /**
     * Hosting environment.
     */
    private String hostingEnvironment;

    /**
     * Application settings overrides for cloned web app. If specified these
     * settings will override the settings cloned
     * from source web app. If not specified, application settings
     * from source web app are retained.
     */
    private Map<String, String> appSettingsOverrides;

    /**
     * If specified configure load balancing for source and clone site.
     */
    private Boolean configureLoadBalancing;

    /**
     * ARM resource id of the traffic manager profile to use if it exists.
     * Traffic manager resource id is of the form
     * /subscriptions/{subId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/trafficManagerProfiles/{profileName}.
     */
    private String trafficManagerProfileId;

    /**
     * Name of traffic manager profile to create. This is only needed if
     * traffic manager profile does not already exist.
     */
    private String trafficManagerProfileName;

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
     * @return the CloningInfo object itself.
     */
    public CloningInfo withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Get the overwrite value.
     *
     * @return the overwrite value
     */
    public Boolean overwrite() {
        return this.overwrite;
    }

    /**
     * Set the overwrite value.
     *
     * @param overwrite the overwrite value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    /**
     * Get the cloneCustomHostNames value.
     *
     * @return the cloneCustomHostNames value
     */
    public Boolean cloneCustomHostNames() {
        return this.cloneCustomHostNames;
    }

    /**
     * Set the cloneCustomHostNames value.
     *
     * @param cloneCustomHostNames the cloneCustomHostNames value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withCloneCustomHostNames(Boolean cloneCustomHostNames) {
        this.cloneCustomHostNames = cloneCustomHostNames;
        return this;
    }

    /**
     * Get the cloneSourceControl value.
     *
     * @return the cloneSourceControl value
     */
    public Boolean cloneSourceControl() {
        return this.cloneSourceControl;
    }

    /**
     * Set the cloneSourceControl value.
     *
     * @param cloneSourceControl the cloneSourceControl value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withCloneSourceControl(Boolean cloneSourceControl) {
        this.cloneSourceControl = cloneSourceControl;
        return this;
    }

    /**
     * Get the sourceWebAppId value.
     *
     * @return the sourceWebAppId value
     */
    public String sourceWebAppId() {
        return this.sourceWebAppId;
    }

    /**
     * Set the sourceWebAppId value.
     *
     * @param sourceWebAppId the sourceWebAppId value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withSourceWebAppId(String sourceWebAppId) {
        this.sourceWebAppId = sourceWebAppId;
        return this;
    }

    /**
     * Get the hostingEnvironment value.
     *
     * @return the hostingEnvironment value
     */
    public String hostingEnvironment() {
        return this.hostingEnvironment;
    }

    /**
     * Set the hostingEnvironment value.
     *
     * @param hostingEnvironment the hostingEnvironment value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withHostingEnvironment(String hostingEnvironment) {
        this.hostingEnvironment = hostingEnvironment;
        return this;
    }

    /**
     * Get the appSettingsOverrides value.
     *
     * @return the appSettingsOverrides value
     */
    public Map<String, String> appSettingsOverrides() {
        return this.appSettingsOverrides;
    }

    /**
     * Set the appSettingsOverrides value.
     *
     * @param appSettingsOverrides the appSettingsOverrides value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withAppSettingsOverrides(Map<String, String> appSettingsOverrides) {
        this.appSettingsOverrides = appSettingsOverrides;
        return this;
    }

    /**
     * Get the configureLoadBalancing value.
     *
     * @return the configureLoadBalancing value
     */
    public Boolean configureLoadBalancing() {
        return this.configureLoadBalancing;
    }

    /**
     * Set the configureLoadBalancing value.
     *
     * @param configureLoadBalancing the configureLoadBalancing value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withConfigureLoadBalancing(Boolean configureLoadBalancing) {
        this.configureLoadBalancing = configureLoadBalancing;
        return this;
    }

    /**
     * Get the trafficManagerProfileId value.
     *
     * @return the trafficManagerProfileId value
     */
    public String trafficManagerProfileId() {
        return this.trafficManagerProfileId;
    }

    /**
     * Set the trafficManagerProfileId value.
     *
     * @param trafficManagerProfileId the trafficManagerProfileId value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withTrafficManagerProfileId(String trafficManagerProfileId) {
        this.trafficManagerProfileId = trafficManagerProfileId;
        return this;
    }

    /**
     * Get the trafficManagerProfileName value.
     *
     * @return the trafficManagerProfileName value
     */
    public String trafficManagerProfileName() {
        return this.trafficManagerProfileName;
    }

    /**
     * Set the trafficManagerProfileName value.
     *
     * @param trafficManagerProfileName the trafficManagerProfileName value to set
     * @return the CloningInfo object itself.
     */
    public CloningInfo withTrafficManagerProfileName(String trafficManagerProfileName) {
        this.trafficManagerProfileName = trafficManagerProfileName;
        return this;
    }

}
