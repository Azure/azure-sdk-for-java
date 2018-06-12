package com.microsoft.windowsazure.services.media.authentication;

import java.net.URI;
import java.net.URISyntaxException;

public final class AzureEnvironmentConstants {

    // Utility classes should not have a public or default constructor.
    private AzureEnvironmentConstants() {
    }

    /**
     * The Active Directory endpoint for Azure Cloud environment.
     */
    public static final URI AZURE_CLOUD_ACTIVE_DIRECTORY_ENDPOINT = makeURI("https://login.microsoftonline.com/");

    /**
     * The Media Services resource for Azure Cloud environment.
     */
    public static final String AZURE_CLOUD_MEDIA_SERVICES_RESOURCE = "https://rest.media.azure.net";

    /**
     * The Active Directory endpoint for Azure China Cloud environment.
     */
    public static final URI AZURE_CHINA_CLOUD_ACTIVE_DIRECTORY_ENDPOINT = makeURI("https://login.chinacloudapi.cn/");

    /**
     * The Media Services resource for Azure China Cloud environment.
     */
    public static final String AZURE_CHINA_CLOUD_MEDIA_SERVICES_RESOURCE = "https://rest.media.chinacloudapi.cn";

    /**
     * The Active Directory endpoint for Azure US Government environment.
     */
    public static final URI AZURE_US_GOVERNMENT_ACTIVE_DIRECTORY_ENDPOINT = makeURI("https://login-us.microsoftonline.com/");

    /**
     * The Media Services resource for Azure US Government environment.
     */
    public static final String AZURE_US_GOVERNMENT_MEDIA_SERVICES_RESOURCE = "https://rest.media.usgovcloudapi.net";

    /**
     * The native SDK AAD application ID for Azure US Government environment.
     */
    public static final String AZURE_US_GOVERNMENT_SDK_AAD_APPLIATION_ID = "68dac91e-cab5-461b-ab4a-ec7dcff0bd67";

    /**
     * The Active Directory endpoint for Azure German cloud environment.
     */
    public static final URI AZURE_GERMAN_CLOUD_ACTIVE_DIRECTORY_ENDPOINT = makeURI("https://login.microsoftonline.de/");

    /**
     * The Media Services resource for Azure German Cloud environment.
     */
    public static final String AZURE_GERMAN_CLOUD_MEDIA_SERVICES_RESOURCE = "https://rest.media.cloudapi.de";

    /**
     * The native SDK AAD application ID for Azure Cloud, Azure China Cloud and Azure German Cloud environment.
     */
    public static final String SDK_AAD_APPLICATION_ID = "d476653d-842c-4f52-862d-397463ada5e7";

    /**
     * The native SDK AAD application's redirect URL for all environments.
     */
    public static final URI SDK_AAD_APPLICATION_REDIRECT_URI = makeURI("https://AzureMediaServicesNativeSDK");

    private static URI makeURI(String urlString) {
        try {
            return new URI(urlString);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
