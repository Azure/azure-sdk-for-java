// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.properties.profile.KnownAzureEnvironment;

/**
 * Properties to Azure Active Directory endpoints.
 */
public class AADProfileEnvironmentProperties {

    /**
     * Azure Active Directory endpoint. For example: https://login.microsoftonline.com/
     */
    private String activeDirectoryEndpoint;
    /**
     * Microsoft Graph endpoint. For example: https://graph.microsoft.com/
     */
    private String microsoftGraphEndpoint;

    /**
     * Get active directory endpoint.
     *
     * @return activeDirectoryEndpoint the active directory endpoint
     */
    public String getActiveDirectoryEndpoint() {
        return activeDirectoryEndpoint;
    }

    /**
     * Set active directory endpoint.
     *
     * @param activeDirectoryEndpoint the active directory endpoint
     */
    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        this.activeDirectoryEndpoint = addSlash(activeDirectoryEndpoint);
    }

    /**
     * Get microsoft graph endpoint.
     *
     * @return microsoftGraphEndpoint microsoft graph endpoint
     */
    public String getMicrosoftGraphEndpoint() {
        return microsoftGraphEndpoint;
    }

    /**
     * set microsoft graph endpoint.
     *
     * @param  microsoftGraphEndpoint microsoft graph endpoint
     */
    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        this.microsoftGraphEndpoint = addSlash(microsoftGraphEndpoint);
    }

    /**
     * Update properties by cloud type
     *
     * @param cloudType the cloud type
     */
    public void updatePropertiesByCloudType(AzureProfileAware.CloudType cloudType) {
        KnownAzureEnvironment knownAzureEnvironment = new KnownAzureEnvironment(cloudType);
        if (this.activeDirectoryEndpoint == null) {
            this.activeDirectoryEndpoint = knownAzureEnvironment.getActiveDirectoryEndpoint();
        }
        if (this.microsoftGraphEndpoint == null) {
            this.microsoftGraphEndpoint = knownAzureEnvironment.getMicrosoftGraphEndpoint();
        }
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
