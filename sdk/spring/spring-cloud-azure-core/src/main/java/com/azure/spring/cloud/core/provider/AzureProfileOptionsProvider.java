// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface to be implemented by classes that wish to provide the Azure profile options.
 */
public interface AzureProfileOptionsProvider {

    /**
     * Get the profile
     * @return the profile
     */
    ProfileOptions getProfile();

    /**
     * Interface to be implemented by classes that wish to describe an Azure cloud profile.
     */
    interface ProfileOptions {

        /**
         * Get the tenant id.
         * @return the tenant id.
         */
        String getTenantId();

        /**
         * Get the subscription id.
         * @return the subscription id.
         */
        String getSubscriptionId();

        /**
         * Get the cloud type.
         * @return the cloud type.
         */
        CloudType getCloudType();

        /**
         * Get the AzureEnvironment implementation.
         * @return the AzureEnvironment implementation.
         */
        AzureEnvironmentOptions getEnvironment();

    }

    /**
     * Define the cloud environment type, with four known Azure cloud types and the types don't fall in the four known
     * types will be OTHER.
     */
    enum CloudType {
        /**
         * Azure
         */
        AZURE,

        /**
         * Azure China
         */
        AZURE_CHINA,

        /**
         * Azure Germany
         * @deprecated AZURE_GERMANY is deprecated. Please use other CloudTypes.
         */
        @Deprecated
        AZURE_GERMANY,

        /**
         * Azure US government
         */
        AZURE_US_GOVERNMENT,

        /**
         * Other
         */
        OTHER;

        private static final Map<String, CloudType> CLOUD_TYPE_MAP = initMap();

        private static Map<String, CloudType> initMap() {
            return Collections.unmodifiableMap(Arrays.stream(CloudType.values())
                         .collect(Collectors.toMap(c -> c.name(), Function.identity())));
        }

        /**
         * Get the {@link CloudType} from {@link String} value.
         * @param cloudType the cloud type string value
         * @return the {@link CloudType}
         */
        public static CloudType fromString(String cloudType) {
            return CLOUD_TYPE_MAP.get(cloudType.toUpperCase(Locale.ROOT));
        }
    }

    /**
     * Interface to be implemented by classes that wish to describe an Azure cloud environment options.
     */
    interface AzureEnvironmentOptions {

        /**
         * Return the management portal URL.
         * @return The management portal URL.
         */
        String getPortal();
        /**
         * Return the publishing settings file URL.
         * @return the publishing settings file URL.
         */
        String getPublishingProfile();
        /**
         * Return the management service endpoint.
         * @return the management service endpoint.
         */
        String getManagementEndpoint();
        /**
         * Return the resource management endpoint.
         * @return the resource management endpoint.
         */
        String getResourceManagerEndpoint();
        /**
         * Return the sql server management endpoint for mobile commands.
         * @return the sql server management endpoint for mobile commands.
         */
        String getSqlManagementEndpoint();
        /**
         * Return the dns suffix for sql servers.
         * @return the dns suffix for sql servers.
         */
        String getSqlServerHostnameSuffix();
        /**
         * Return the template gallery endpoint.
         * @return the template gallery endpoint.
         */
        String getGalleryEndpoint();
        /**
         * Return the Active Directory login endpoint.
         * @return the Active Directory login endpoint.
         */
        String getActiveDirectoryEndpoint();
        /**
         * Return the resource ID to obtain AD tokens for.
         * @return The resource ID to obtain AD tokens for.
         */
        String getActiveDirectoryResourceId();
        /**
         * Return the Active Directory Graph endpoint.
         * @return the Active Directory Graph endpoint.
         */
        String getActiveDirectoryGraphEndpoint();
        /**
         * Return the Microsoft Graph endpoint.
         * @return the Microsoft Graph endpoint.
         */
        String getMicrosoftGraphEndpoint();
        /**
         * Return the Data Lake resource ID.
         * @return the Data Lake resource ID.
         */
        String getDataLakeEndpointResourceId();
        /**
         * Return the Active Directory api version.
         * @return the Active Directory api version.
         */
        String getActiveDirectoryGraphApiVersion();
        /**
         * The endpoint suffix for storage accounts.
         * @return the endpoint suffix for storage accounts.
         */
        String getStorageEndpointSuffix();
        /**
         * Return the key vault service dns suffix.
         * @return the key vault service dns suffix.
         */
        String getKeyVaultDnsSuffix();
        /**
         * Return the data lake store filesystem service dns suffix.
         * @return the data lake store filesystem service dns suffix.
         */
        String getAzureDataLakeStoreFileSystemEndpointSuffix();
        /**
         * Return the data lake analytics job and catalog service dns suffix.
         * @return the data lake analytics job and catalog service dns suffix.
         */
        String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix();
        /**
         * Return the log analytics endpoint.
         * @return the log analytics endpoint.
         */
        String getAzureLogAnalyticsEndpoint();
        /**
         * Return the application insights endpoint.
         * @return the application insights endpoint.
         */
        String getAzureApplicationInsightsEndpoint();

        /**
         * Return the domain name of Service Bus.
         * @return the domain name of Service Bus.
         */
        String getServiceBusDomainName();

        /**
         * Get the AzureEnvironment from {@link com.azure.core.management.AzureEnvironment}.
         * @param environment the azure core AzureEnvironment.
         * @return the AzureEnvironment implementation.
         * @deprecated deprecate the dependency on {@link com.azure.core.management.AzureEnvironment}.
         */
        @Deprecated
        AzureEnvironmentOptions fromAzureManagementEnvironment(com.azure.core.management.AzureEnvironment environment);

        /**
         * Return the AzureEnvironment
         * @return the azure core {@link com.azure.core.management.AzureEnvironment}.
         */
        default com.azure.core.management.AzureEnvironment toAzureManagementEnvironment() {
            Map<String, String> endpointsMap = new HashMap<>();
            endpointsMap.put("portalUrl", getPortal());
            endpointsMap.put("publishingProfileUrl", getPublishingProfile());
            endpointsMap.put("managementEndpointUrl", getManagementEndpoint());
            endpointsMap.put("resourceManagerEndpointUrl", getResourceManagerEndpoint());
            endpointsMap.put("sqlManagementEndpointUrl", getSqlManagementEndpoint());
            endpointsMap.put("sqlServerHostnameSuffix", getSqlServerHostnameSuffix());
            endpointsMap.put("galleryEndpointUrl", getGalleryEndpoint());
            endpointsMap.put("activeDirectoryEndpointUrl", getActiveDirectoryEndpoint());
            endpointsMap.put("activeDirectoryResourceId", getActiveDirectoryResourceId());
            endpointsMap.put("activeDirectoryGraphResourceId", getActiveDirectoryGraphEndpoint());
            endpointsMap.put("microsoftGraphResourceId", getMicrosoftGraphEndpoint());
            endpointsMap.put("dataLakeEndpointResourceId", getDataLakeEndpointResourceId());
            endpointsMap.put("activeDirectoryGraphApiVersion", getActiveDirectoryGraphApiVersion());
            endpointsMap.put("storageEndpointSuffix", getStorageEndpointSuffix());
            endpointsMap.put("keyVaultDnsSuffix", getKeyVaultDnsSuffix());
            endpointsMap.put("azureDataLakeStoreFileSystemEndpointSuffix", getAzureDataLakeStoreFileSystemEndpointSuffix());
            endpointsMap.put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix());
            endpointsMap.put("azureLogAnalyticsResourceId", getAzureLogAnalyticsEndpoint());
            endpointsMap.put("azureApplicationInsightsResourceId", getAzureApplicationInsightsEndpoint());

            return new com.azure.core.management.AzureEnvironment(endpointsMap);
        }

    }

}
