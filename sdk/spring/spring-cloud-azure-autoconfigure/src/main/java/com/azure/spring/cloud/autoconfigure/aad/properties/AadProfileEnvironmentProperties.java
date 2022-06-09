// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;

/**
 * Properties to Azure Active Directory endpoints.
 */
public class AadProfileEnvironmentProperties {

    /**
     * Azure Active Directory endpoint. For example: https://login.microsoftonline.com/
     */
    private String activeDirectoryEndpoint;
    /**
     * Microsoft Graph endpoint. For example: https://graph.microsoft.com/
     */
    private String microsoftGraphEndpoint;

    /**
     *
     * @return The Active Directory Endpoint.
     */
    public String getActiveDirectoryEndpoint() {
        return activeDirectoryEndpoint;
    }

    /**
     *
     * @param activeDirectoryEndpoint The Active Directory Endpoint.
     */
    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        this.activeDirectoryEndpoint = addSlash(activeDirectoryEndpoint);
    }

    /**
     *
     * @return The Microsoft Graph endpoint.
     */
    public String getMicrosoftGraphEndpoint() {
        return microsoftGraphEndpoint;
    }

    /**
     *
     * @param microsoftGraphEndpoint The Microsoft Graph endpoint.
     */
    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        this.microsoftGraphEndpoint = addSlash(microsoftGraphEndpoint);
    }

    /**
     *
     * @param cloudType The cloud type.
     */
    public void updatePropertiesByCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        AzureEnvironment knownAzureEnvironment = AzureProfileOptionsAdapter.decideAzureManagementEnvironment(cloudType, AzureEnvironment.AZURE);
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
