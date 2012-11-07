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

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Generic implementation of the list operation, usable by most entities
 * 
 */
public class DefaultListOperation<T> extends EntityOperationBase implements EntityListOperation<T> {
    private final MultivaluedMap<String, String> queryParameters;
    private final GenericType<ListResult<T>> responseType;

    public DefaultListOperation(String entityUri, GenericType<ListResult<T>> responseType) {
        super(entityUri);
        queryParameters = new MultivaluedMapImpl();
        this.responseType = responseType;
    }

    public DefaultListOperation(String entityUri, GenericType<ListResult<T>> responseType,
            MultivaluedMap<String, String> queryParameters) {
        this(entityUri, responseType);
        this.queryParameters.putAll(queryParameters);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityListOperation#getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.entities.EntityListOperation#getResponseGenericType()
     */
    @Override
    public GenericType<ListResult<T>> getResponseGenericType() {
        return responseType;
    }
}
