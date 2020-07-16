package com.azure.messaging.eventgrid;

/**
 * A way to use a generated access token as a credential to publish events to a topic through a client.
 */
public class EventGridSasCredential {

    /**
     * Create an instance of this object to authenticate calls to the EventGrid service.
     * @param sasCredential the access token to use.
     */
    public EventGridSasCredential(String sasCredential) {
        // TODO: implement method
    }

    /**
     * Get the token string to authenticate service calls
     * @return the SAS token as a string
     */
    public String getToken() {
        // TODO: implement method
        return null;
    }
}
