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

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Generic implementation of Delete operation usable by most entities.
 */
public class DefaultActionOperation implements EntityActionOperation {

    /** The proxy data. */
    private EntityProxyData proxyData;

    /** The name. */
    protected String name;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The query parameters. */
    protected MultivaluedMap<String, String> queryParameters;

    /**
     * The default action operation.
     * 
     * @param name
     *            the name
     */
    public DefaultActionOperation(String name) {
        this();
        this.name = name;
    }

    /**
     * Instantiates a new default action operation.
     */
    public DefaultActionOperation() {
        this.queryParameters = new MultivaluedMapImpl();
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#setProxyData(com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData)
     */
    @Override
    public void setProxyData(EntityProxyData proxyData) {
        this.proxyData = proxyData;
    }

    /**
     * Get the current proxy data.
     * 
     * @return the proxy data
     */
    protected EntityProxyData getProxyData() {
        return proxyData;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation#getUri()
     */
    @Override
    public String getUri() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation#addQueryParameter(java.lang.String, java.lang.String)
     */
    @Override
    public DefaultActionOperation addQueryParameter(String key, String value) {
        this.queryParameters.add(key, value);
        return this;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#getContentType()
     */
    @Override
    public MediaType getContentType() {
        return this.contentType;
    }

    /**
     * Sets the content type.
     * 
     * @param contentType
     *            the content type
     * @return the default action operation
     */
    @Override
    public DefaultActionOperation setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#getAcceptType()
     */
    @Override
    public MediaType getAcceptType() {
        return this.acceptType;
    }

    /**
     * Sets the accept type.
     * 
     * @param acceptType
     *            the accept type
     * @return the default action operation
     */
    public DefaultActionOperation setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "GET";
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#processResponse(java.lang.Object)
     */
    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        PipelineHelpers.ThrowIfNotSuccess((ClientResponse) rawResponse);
        return rawResponse;
    }

}
