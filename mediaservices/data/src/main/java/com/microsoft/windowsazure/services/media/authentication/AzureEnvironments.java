package com.microsoft.windowsazure.services.media.authentication;

public final class AzureEnvironments {

    // Utility classes should not have a public or default constructor.
    private AzureEnvironments() {
    }

    /**
     * Azure Cloud environment.
     */
    public static final AzureEnvironment AZURE_CLOUD_ENVIRONMENT = new AzureEnvironment(
        AzureEnvironmentConstants.AZURE_CLOUD_ACTIVE_DIRECTORY_ENDPOINT,
        AzureEnvironmentConstants.AZURE_CLOUD_MEDIA_SERVICES_RESOURCE,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_ID,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_REDIRECT_URI);

    /**
     *  Azure China Cloud environment.
     */
    public static final AzureEnvironment AZURE_CHINA_CLOUD_ENVIRONMENT = new AzureEnvironment(
        AzureEnvironmentConstants.AZURE_CHINA_CLOUD_ACTIVE_DIRECTORY_ENDPOINT,
        AzureEnvironmentConstants.AZURE_CHINA_CLOUD_MEDIA_SERVICES_RESOURCE,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_ID,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_REDIRECT_URI);

    /**
     * Azure US Government environment.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT_ENVIRONMENT = new AzureEnvironment(
        AzureEnvironmentConstants.AZURE_US_GOVERNMENT_ACTIVE_DIRECTORY_ENDPOINT,
        AzureEnvironmentConstants.AZURE_US_GOVERNMENT_MEDIA_SERVICES_RESOURCE,
        AzureEnvironmentConstants.AZURE_US_GOVERNMENT_SDK_AAD_APPLIATION_ID,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_REDIRECT_URI);

    /**
     * Azure German Cloud environment.
     */
    public static final AzureEnvironment AZURE_GERMAN_CLOUD_ENVIRONMENT = new AzureEnvironment(
        AzureEnvironmentConstants.AZURE_GERMAN_CLOUD_ACTIVE_DIRECTORY_ENDPOINT,
        AzureEnvironmentConstants.AZURE_GERMAN_CLOUD_MEDIA_SERVICES_RESOURCE,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_ID,
        AzureEnvironmentConstants.SDK_AAD_APPLICATION_REDIRECT_URI);
}
