package com.azure.messaging.eventgrid;

/**
 * This class provides the recommended way to authorize the {@link EventGridPublisherClientBuilder} to produce
 * clients by providing a shared access key from the topic. This can be obtained from Azure CLI, Azure portal,
 * or the EventGrid ARM SDK.
 */
public class EventGridSharedKeyCredential {

    /**
     * Create an instance of the EventGridSharedKeyCredential using the given policy name
     * and its shared access key.
     * @param accessKey the access key for the topic or domain to send to.
     */
    public EventGridSharedKeyCredential(String accessKey) {
        // TODO: implement method
    }

    /**
     * Give the stored shared access key.
     * @return the shared access key.
     */
    public String getKey() {
        // TODO: implement method
        return null;
    }
}
