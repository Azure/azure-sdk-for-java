package com.microsoft.windowsazure.services.media.implementation.entities;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DefaultTypeActionOperation<T> implements EntityTypeActionOperation<T> {

    private String name;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    private MultivaluedMapImpl queryParameters;

    private EntityProxyData proxyData;

    public DefaultTypeActionOperation(String name) {
        this();
        this.name = name;
    }

    public DefaultTypeActionOperation() {
        this.queryParameters = new MultivaluedMapImpl();
    }

    @Override
    public T processTypeResponse(ClientResponse clientResponse) {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    @Override
    public String getVerb() {
        return "GET";
    }

    @Override
    public Object getRequestContents() {
        return null;
    }

    @Override
    public void setProxyData(EntityProxyData proxyData) {
        this.proxyData = proxyData;
    }

    @Override
    public String getUri() {
        return this.name;
    }

    @Override
    public MediaType getContentType() {
        return this.contentType;
    }

    @Override
    public MediaType getAcceptType() {
        return this.acceptType;
    }

    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        return null;
    }

    @Override
    public DefaultTypeActionOperation<T> addQueryParameter(String key, String value) {
        this.queryParameters.add(key, value);
        return this;
    }

    @Override
    public void processResponse(ClientResponse clientResponse) {
    }

    @Override
    public EntityTypeActionOperation<T> setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public EntityTypeActionOperation<T> setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
        return this;
    }

}
