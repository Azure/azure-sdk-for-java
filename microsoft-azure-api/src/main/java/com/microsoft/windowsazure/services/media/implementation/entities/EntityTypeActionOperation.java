package com.microsoft.windowsazure.services.media.implementation.entities;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.ClientResponse;

public interface EntityTypeActionOperation<T> extends EntityOperation {

    T processTypeResponse(ClientResponse clientResponse);

    /**
     * Gets the query parameters.
     * 
     * @return the query parameters
     */
    MultivaluedMap<String, String> getQueryParameters();

    /**
     * Adds the query parameter.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the entity action operation
     */
    EntityTypeActionOperation<T> addQueryParameter(String key, String value);

    /**
     * Process response.
     * 
     * @param clientResponse
     *            the client response
     * @return the object
     */
    Object processResponse(ClientResponse clientResponse);

    /**
     * Gets the verb.
     * 
     * @return the verb
     */
    String getVerb();

    /**
     * Gets the request contents.
     * 
     * @return the request contents
     */
    Object getRequestContents();

    EntityTypeActionOperation<T> setContentType(MediaType contentType);

}
