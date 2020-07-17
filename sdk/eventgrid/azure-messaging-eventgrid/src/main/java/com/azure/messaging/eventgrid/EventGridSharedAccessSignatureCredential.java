package com.azure.messaging.eventgrid;

import com.azure.core.util.CoreUtils;

/**
 * A way to use a generated access token as a credential to publish events to a topic through a client.
 */
public class EventGridSharedAccessSignatureCredential {

    private final String accessToken;

    /**
     * Create an instance of this object to authenticate calls to the EventGrid service.
     * @param accessToken the access token to use.
     */
    public EventGridSharedAccessSignatureCredential(String accessToken) {
        if (CoreUtils.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("the access token cannot be null or empty");
        }
        this.accessToken = accessToken;
    }

    /**
     * Get the token string to authenticate service calls
     * @return the SharedAccessSignature token as a string
     */
    public String getToken() {
        return accessToken;
    }
}
