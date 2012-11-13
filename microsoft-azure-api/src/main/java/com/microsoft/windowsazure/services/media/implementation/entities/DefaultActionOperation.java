/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.implementation.entities;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

// TODO: Auto-generated Javadoc
/**
 * Generic implementation of Delete operation usable by most entities.
 */
public class DefaultActionOperation implements EntityActionOperation {

    /** The entity set. */
    private final String entitySet;

    /** The verb. */
    private final String verb;

    /** The query parameters. */
    private final MultivaluedMap queryParameters;

    /**
     * The default action operation.
     * 
     * @param entitySet
     *            the entity set
     * @param verb
     *            the verb
     */
    public DefaultActionOperation(String entitySet, String verb) {
        this.entitySet = entitySet;
        this.verb = verb;
        this.queryParameters = new MultivaluedMapImpl();
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation#getUri()
     */
    @Override
    public String getUri() {
        return String.format("%s/%s", entitySet, verb);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap getQueryParameters() {
        return this.queryParameters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#getContentType()
     */
    @Override
    public MediaType getContentType() {
        return MediaType.APPLICATION_ATOM_XML_TYPE;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperation#getAcceptType()
     */
    @Override
    public MediaType getAcceptType() {
        return MediaType.APPLICATION_ATOM_XML_TYPE;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation#addQueryParameter(java.lang.String, java.lang.String)
     */
    @Override
    public EntityActionOperation addQueryParameter(String key, String value) {
        this.queryParameters.add(key, value);
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation#processResponse(com.sun.jersey.api.client.ClientResponse)
     */
    @Override
    public void processResponse(ClientResponse clientResponse) {
        PipelineHelpers.ThrowIfNotSuccess(clientResponse);
    }
}
