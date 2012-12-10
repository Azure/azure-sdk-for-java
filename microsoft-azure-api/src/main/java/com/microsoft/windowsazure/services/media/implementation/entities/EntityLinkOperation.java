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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Generic implementation of Delete operation usable by most entities.
 */
public class EntityLinkOperation extends DefaultActionOperation {

    /** The master entity set. */
    private final String masterEntitySet;

    /** The master entity id. */
    private final String masterEntityId;

    /** The slave entity set. */
    private final String slaveEntitySet;

    /** The slave entity uri. */
    private final URI slaveEntityUri;

    /**
     * Instantiates a new entity link operation.
     * 
     * @param masterEntitySet
     *            the master entity set
     * @param masterEntityId
     *            the master entity id
     * @param slaveEntitySet
     *            the slave entity set
     * @param slaveEntityUri
     *            the slave entity uri
     */
    public EntityLinkOperation(String masterEntitySet, String masterEntityId, String slaveEntitySet, URI slaveEntityUri) {
        super();
        this.masterEntitySet = masterEntitySet;
        this.masterEntityId = masterEntityId;
        this.slaveEntitySet = slaveEntitySet;
        this.slaveEntityUri = slaveEntityUri;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityDeleteOperation#getUri()
     */
    @Override
    public String getUri() {
        String escapedEntityId;
        try {
            escapedEntityId = URLEncoder.encode(masterEntityId, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new InvalidParameterException(masterEntityId);
        }
        return String.format("%s('%s')/$links/%s", masterEntitySet, escapedEntityId, slaveEntitySet);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.DefaultActionOperation#getVerb()
     */
    @Override
    public String getVerb() {
        return "POST";
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation#processResponse(com.sun.jersey.api.client.ClientResponse)
     */
    @Override
    public Object processResponse(ClientResponse clientResponse) {
        PipelineHelpers.ThrowIfNotSuccess(clientResponse);
        return null;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.entities.DefaultActionOperation#getRequestContents()
     */
    @Override
    public Object getRequestContents() {
        String result = String
                .format("<?xml version=\"1.0\" encoding=\"utf-8\"?><uri xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">%s</uri>",
                        this.slaveEntityUri);

        return result;
    }
}
