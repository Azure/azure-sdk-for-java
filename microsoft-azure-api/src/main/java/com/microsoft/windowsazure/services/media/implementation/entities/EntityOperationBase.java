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
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import javax.ws.rs.core.MediaType;

/**
 * Default implementation of EntityOperation<T> to provide
 * default values for common methods.
 * 
 */
public abstract class EntityOperationBase implements EntityOperation {
    private final EntityUriBuilder uriBuilder;

    /**
     * 
     */
    protected EntityOperationBase(final String uri) {
        this.uriBuilder = new EntityUriBuilder() {
            @Override
            public String getUri() {
                return uri;
            }
        };
    }

    protected EntityOperationBase(EntityUriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getUri()
     */
    @Override
    public String getUri() {
        return uriBuilder.getUri();
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getContentType()
     */
    @Override
    public MediaType getContentType() {
        return MediaType.APPLICATION_ATOM_XML_TYPE;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getAcceptType()
     */
    @Override
    public MediaType getAcceptType() {
        return MediaType.APPLICATION_ATOM_XML_TYPE;
    }

    public interface EntityUriBuilder {
        String getUri();
    }

    public static class EntityIdUriBuilder implements EntityUriBuilder {
        private final String entityType;
        private final String entityId;

        public EntityIdUriBuilder(String entityName, String entityId) {
            super();
            this.entityType = entityName;
            this.entityId = entityId;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.entities.EntityOperationBase.EntityUriBuilder#getUri()
         */
        @Override
        public String getUri() {
            String escapedEntityId;
            try {
                escapedEntityId = URLEncoder.encode(entityId, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new InvalidParameterException(entityId);
            }
            return String.format("%s('%s')", entityType, escapedEntityId);
        }
    }
}
