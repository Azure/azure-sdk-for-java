package com.microsoft.windowsazure.services.core.utils.pipeline;

import com.sun.jersey.api.client.ClientRequest;

public interface EntityStreamingListener {
    /**
     * This method is called just before the entity is streamed to the underlying connection. This is the last chance
     * for filters to inspect and modify the headers of the client request if necessary.
     * 
     * @param clientRequest
     *            The client request
     */
    void onBeforeStreamingEntity(ClientRequest clientRequest);
}
