// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

public class AadProfileEnvironmentProperties {

    /**
     * Azure Active Directory endpoint. For example: https://login.microsoftonline.com/
     */
    private String activeDirectoryEndpoint;
    /**
     * Microsoft Graph endpoint. For example: https://graph.microsoft.com/
     */
    private String microsoftGraphEndpoint;

    public String getActiveDirectoryEndpoint() {
        return activeDirectoryEndpoint;
    }

    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        this.activeDirectoryEndpoint = addSlash(activeDirectoryEndpoint);
    }

    public String getMicrosoftGraphEndpoint() {
        return microsoftGraphEndpoint;
    }

    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        this.microsoftGraphEndpoint = addSlash(microsoftGraphEndpoint);
    }

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
