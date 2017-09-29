package com.microsoft.windowsazure.services.media.authentication;

public class AzureEnvironments {

    /**
     * Azure Cloud environment.
     */
    public static final AzureEnvironment AzureCloudEnvironment = new AzureEnvironment(
        AzureEnvironmentConstants.AzureCloudActiveDirectoryEndpoint,
        AzureEnvironmentConstants.AzureCloudMediaServicesResource,
        AzureEnvironmentConstants.SdkAadApplicationId,
        AzureEnvironmentConstants.SdkAadApplicationRedirectUri);

    /**
     *  Azure China Cloud environment.
     */
    public static final AzureEnvironment AzureChinaCloudEnvironment = new AzureEnvironment(
        AzureEnvironmentConstants.AzureChinaCloudActiveDirectoryEndpoint,
        AzureEnvironmentConstants.AzureChinaCloudMediaServicesResource,
        AzureEnvironmentConstants.SdkAadApplicationId,
        AzureEnvironmentConstants.SdkAadApplicationRedirectUri);

    /**
     * Azure US Government environment.
     */
    public static final AzureEnvironment AzureUsGovernmentEnvironment = new AzureEnvironment(
        AzureEnvironmentConstants.AzureUsGovernmentActiveDirectoryEndpoint,
        AzureEnvironmentConstants.AzureUsGovernmentMediaServicesResource,
        AzureEnvironmentConstants.AzureUsGovernmentSdkAadAppliationId,
        AzureEnvironmentConstants.SdkAadApplicationRedirectUri);

    /**
     * Azure German Cloud environment.
     */
    public static final AzureEnvironment AzureGermanCloudEnvironment = new AzureEnvironment(
        AzureEnvironmentConstants.AzureGermanCloudActiveDirectoryEndpoint,
        AzureEnvironmentConstants.AzureGermanCloudMediaServicesResource,
        AzureEnvironmentConstants.SdkAadApplicationId,
        AzureEnvironmentConstants.SdkAadApplicationRedirectUri);
}
