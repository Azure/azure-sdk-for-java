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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.exception.ServiceException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Generic implementation of Delete operation usable by most entities.
 */
public class DefaultEntityActionOperation implements EntityActionOperation {

    /** The proxy data. */
    private EntityProxyData proxyData;

    /** The uri builder. */
    private final EntityOperationBase.EntityUriBuilder uriBuilder;

    /** The content type. */
    private MediaType contentType = MediaType.APPLICATION_JSON_TYPE;

    /** The accept type. */
    private MediaType acceptType = MediaType.APPLICATION_ATOM_XML_TYPE;

    /** The query parameters. */
    private MultivaluedMap<String, String> queryParameters;

    /** The entity name. */
    private final String entityName;

    /** The entity id. */
    private final String entityId;

    /** The action name. */
    private final String actionName;

    /** The body parameters. */
    private Map<String, Object> bodyParameters;

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
    public DefaultEntityActionOperation(String entityName, String entityId,
            String actionName) {
        this.queryParameters = new MultivaluedMapImpl();
        this.bodyParameters = new HashMap<String, Object>();
        this.entityName = entityName;
        this.entityId = entityId;
        this.actionName = actionName;
        this.uriBuilder = new EntityOperationBase.EntityIdUriBuilder(
                entityName, entityId).setActionName(actionName);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entityoperations.EntityOperation
     * #getUri()
     */
    @Override
    public String getUri() {
        return uriBuilder.getUri();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#addQueryParameter(java.lang.String,
     * java.lang.String)
     */
    @Override
    public DefaultEntityActionOperation addQueryParameter(String key,
            String value) {
        this.queryParameters.add(key, value);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#getBodyParameters()
     */
    @Override
    public Map<String, Object> getBodyParameters() {
        return this.bodyParameters;
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
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#setContentType(javax.ws.rs.core.MediaType)
     */
    @Override
    public DefaultEntityActionOperation setContentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "POST";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        if (this.bodyParameters.size() == 0) {
            return "{}";
        } else {
            String jsonString = "";
            EntityActionBodyParameterMapper mapper = new EntityActionBodyParameterMapper();
            jsonString = mapper.toString(this.bodyParameters);
            return jsonString;
        }
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
        PipelineHelpers.throwIfNotSuccess((ClientResponse) rawResponse);
        return rawResponse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityActionOperation#addBodyParameter(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public EntityActionOperation addBodyParameter(String key, Object value) {
        this.bodyParameters.put(key, value);
        return this;
    }

}
