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

package com.microsoft.windowsazure.services.media.entities;

import javax.ws.rs.core.MediaType;

/**
 * Default implementation of EntityOperation<T> to provide
 * default values for common methods.
 * 
 */
public abstract class EntityOperationBase implements EntityOperation {
    private final String uri;

    /**
     * 
     */
    protected EntityOperationBase(String uri) {
        this.uri = uri;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityOperation#getUri()
     */
    @Override
    public String getUri() {
        return uri;
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
}
