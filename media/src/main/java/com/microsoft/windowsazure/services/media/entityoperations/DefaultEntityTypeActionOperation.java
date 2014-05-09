/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.entityoperations;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.exception.ServiceException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * The Class DefaultTypeActionOperation.
 * 
 * @param <T>
 *            the generic type
 */
public class DefaultEntityTypeActionOperation<T> implements
        EntityTypeActionOperation<T> {

    /** The name. */
    private String name;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_XML_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The query parameters. */
    private final MultivaluedMap<String, String> queryParameters;

    /** The proxy data. */
    private EntityProxyData proxyData;

    /**
     * Instantiates a new default type action operation.
     * 
     * @param name
     *            the name
     */
    public DefaultEntityTypeActionOperation(String name) {
        this();
        this.name = name;
    }

    /**
     * Instantiates a new default type action operation.
     */
    public DefaultEntityTypeActionOperation() {
        this.queryParameters = new MultivaluedMapImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation
     * #processTypeResponse(com.sun.jersey.api.client.ClientResponse)
     */
    @Override
    public T processTypeResponse(ClientResponse clientResponse) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "GET";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #setProxyData(com.microsoft.windowsazure.services.media.entityoperations.
     * EntityProxyData)
     */
    @Override
    public void setProxyData(EntityProxyData proxyData) {
        this.proxyData = proxyData;
    }

    public EntityProxyData getProxyData() {
        return this.proxyData;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #getUri()
     */
    @Override
    public String getUri() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #getContentType()
     */
    @Override
    public MediaType getContentType() {
        return this.contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #getAcceptType()
     */
    @Override
    public MediaType getAcceptType() {
        return this.acceptType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #processResponse(java.lang.Object)
     */
    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#addQueryParameter(java.lang.String,
     * java.lang.String)
     */
    @Override
    public DefaultEntityTypeActionOperation<T> addQueryParameter(String key,
            String value) {
        this.queryParameters.add(key, value);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#setContentType(javax.ws.rs.core.MediaType)
     */
    @Override
    public EntityTypeActionOperation<T> setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityTypeActionOperation#setAcceptType(javax.ws.rs.core.MediaType)
     */
    @Override
    public EntityTypeActionOperation<T> setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
        return this;
    }

}
