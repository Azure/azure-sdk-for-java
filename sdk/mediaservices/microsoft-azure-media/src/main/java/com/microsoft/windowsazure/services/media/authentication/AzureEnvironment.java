package com.microsoft.windowsazure.services.media.authentication;

import java.net.URI;

/**
 * Represents an Azure Environment
 */
public class AzureEnvironment {

    private URI activeDirectoryEndpoint;

    private String mediaServicesResource;

    private String mediaServicesSdkClientId;

    private URI mediaServicesSdkRedirectUri;

    /**
     * Gets the Active Directory endpoint.
     * @return Active Directory endpoint.
     */
    public URI getActiveDirectoryEndpoint() {
        return this.activeDirectoryEndpoint;
    }

    /**
     * Gets the Media Services resource.
     * @return Media Services resource
     */
    public String getMediaServicesResource() {
        return this.mediaServicesResource;
    }

    /**
     * Gets the Media Services SDK client ID.
     * @return Media Services SDK client ID
     */
    public String getMediaServicesSdkClientId() {
        return this.mediaServicesSdkClientId;
    }

    /**
     * Gets Media Services SDK application redirect URI.
     * @return Media Services SDK application redirect URI.
     */
    public URI getMediaServicesSdkRedirectUri() {
        return this.mediaServicesSdkRedirectUri;
    }

    /**
     * Initializes a new instance of the AzureEnvironment class.
     * @param activeDirectoryEndpoint The Active Directory endpoint.
     * @param mediaServicesResource The Media Services resource.
     * @param mediaServicesSdkClientId The Media Services SDK client ID.
     * @param mediaServicesSdkRedirectUri The Media Services SDK redirect URI.
     */
    public AzureEnvironment(
        URI activeDirectoryEndpoint,
        String mediaServicesResource,
        String mediaServicesSdkClientId,
        URI mediaServicesSdkRedirectUri) {
        if (activeDirectoryEndpoint == null) {
            throw new NullPointerException("activeDirectoryEndpoint");
        }

        if (mediaServicesResource == null || mediaServicesResource.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaServicesResource");
        }

        if (mediaServicesSdkClientId == null || mediaServicesSdkClientId.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaServicesSdkClientId");
        }

        if (mediaServicesSdkRedirectUri == null) {
            throw new NullPointerException("mediaServicesSdkRedirectUri");
        }

        this.activeDirectoryEndpoint = activeDirectoryEndpoint;
        this.mediaServicesResource = mediaServicesResource;
        this.mediaServicesSdkClientId = mediaServicesSdkClientId;
        this.mediaServicesSdkRedirectUri = mediaServicesSdkRedirectUri;
    }
}
