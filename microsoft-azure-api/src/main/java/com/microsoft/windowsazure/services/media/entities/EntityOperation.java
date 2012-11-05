package com.microsoft.windowsazure.services.media.entities;

import javax.ws.rs.core.MediaType;

public interface EntityOperation<T> {

    /**
     * Get the URI the creation request should be sent to.
     * 
     * @return The uri
     */
    public abstract String getUri();

    /**
     * Get the MIME type for the content that's being sent to the server.
     * 
     * @return The MIME type
     */
    public abstract MediaType getContentType();

    /**
     * Get the MIME type that we're expecting the server to send back.
     */
    public abstract MediaType getAcceptType();

    /**
     * Get the Java class object for the type that the response should
     * be unmarshalled into.
     * 
     * @return Class object for response.
     */
    public abstract Class<T> getResponseClass();

}