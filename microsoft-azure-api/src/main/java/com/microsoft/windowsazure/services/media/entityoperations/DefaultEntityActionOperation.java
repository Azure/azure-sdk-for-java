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
public class DefaultEntityActionOperation implements EntityActionOperation {

    /** The proxy data. */
    private EntityProxyData proxyData;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The query parameters. */
    protected MultivaluedMap<String, String> queryParameters;

    /** The entity name. */
    private String entityName;

    /** The entity id. */
    private String entityId;

    /** The action name. */
    private String actionName;

    /**
     * The default action operation.
     * 
     * @param entityName
     *            the entity name
     * @param entityId
     *            the entity id
     * @param actionName
     *            the action name
     */
    public DefaultEntityActionOperation(String entityName, String entityId, String actionName) {
        this();
        this.entityName = entityName;
        this.entityId = entityId;
        this.actionName = actionName;
    }

    /**
     * Instantiates a new default action operation.
     */
    public DefaultEntityActionOperation() {
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

    /**
     * Gets the entity name.
     * 
     * @return the entity name
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * Gets the entity id.
     * 
     * @return the entity id
     */
    public String getEntityId() {
        return this.entityId;
    }

    /**
     * Gets the action name.
     * 
     * @return the action name
     */
    public String getActionName() {
        return this.actionName;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entityoperations.EntityOperation#getUri()
     */
    @Override
    public String getUri() {
        return String.format("%s(%s)/%s", this.entityName, this.entityId, this.actionName);
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
    public DefaultEntityActionOperation addQueryParameter(String key, String value) {
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
    public DefaultEntityActionOperation setContentType(MediaType contentType) {
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
    public DefaultEntityActionOperation setAcceptType(MediaType acceptType) {
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
