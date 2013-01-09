package com.microsoft.windowsazure.services.media.implementation.entities;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * The Class DefaultTypeActionOperation.
 * 
 * @param <T>
 *            the generic type
 */
public class DefaultTypeActionOperation<T> implements EntityTypeActionOperation<T> {

    /** The name. */
    private String name;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The query parameters. */
    private MultivaluedMapImpl queryParameters;

    /** The proxy data. */
    private EntityProxyData proxyData;

    /**
     * Instantiates a new default type action operation.
     * 
     * @param name
     *            the name
     */
    public DefaultTypeActionOperation(String name) {
        this();
        this.name = name;
    }

    /**
     * Instantiates a new default type action operation.
     */
    public DefaultTypeActionOperation() {
        this.queryParameters = new MultivaluedMapImpl();
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#processTypeResponse(com.sun.jersey.api.client.ClientResponse)
     */
    @Override
    public T processTypeResponse(ClientResponse clientResponse) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "GET";
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#setProxyData(com.microsoft.windowsazure.services.media.implementation.entities.EntityProxyData)
     */
    @Override
    public void setProxyData(EntityProxyData proxyData) {
        this.proxyData = proxyData;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#getUri()
     */
    @Override
    public String getUri() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#getContentType()
     */
    @Override
    public MediaType getContentType() {
        return this.contentType;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#getAcceptType()
     */
    @Override
    public MediaType getAcceptType() {
        return this.acceptType;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#processResponse(java.lang.Object)
     */
    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#addQueryParameter(java.lang.String, java.lang.String)
     */
    @Override
    public DefaultTypeActionOperation<T> addQueryParameter(String key, String value) {
        this.queryParameters.add(key, value);
        return this;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#setContentType(javax.ws.rs.core.MediaType)
     */
    @Override
    public EntityTypeActionOperation<T> setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the accept type.
     * 
     * @param acceptType
     *            the accept type
     * @return the entity type action operation
     */
    public EntityTypeActionOperation<T> setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityTypeActionOperation#processResponse(com.sun.jersey.api.client.ClientResponse)
     */
    @Override
    public Object processResponse(ClientResponse clientResponse) {
        return null;
    }

}
