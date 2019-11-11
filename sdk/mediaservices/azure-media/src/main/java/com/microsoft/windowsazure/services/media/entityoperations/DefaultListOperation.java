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

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Generic implementation of the list operation, usable by most entities
 * 
 */
public class DefaultListOperation<T> extends EntityOperationBase implements
        EntityListOperation<T> {
    private final MultivaluedMap<String, String> queryParameters;
    private final GenericType<ListResult<T>> responseType;

    public DefaultListOperation(String entityUri,
            GenericType<ListResult<T>> responseType) {
        super(entityUri);
        queryParameters = new MultivaluedMapImpl();
        this.responseType = responseType;
    }

    public DefaultListOperation(String entityUri,
            GenericType<ListResult<T>> responseType,
            MultivaluedMap<String, String> queryParameters) {
        this(entityUri, responseType);
        this.queryParameters.putAll(queryParameters);
    }

    /**
     * Add a "$top" query parameter to set the number of values to return
     * 
     * @param topValue
     *            number of values to return
     * @return this
     */
    public DefaultListOperation<T> setTop(int topValue) {
        queryParameters.add("$top", Integer.toString(topValue));
        return this;
    }

    /**
     * Add a "$skip" query parameter to set the number of values to skip
     * 
     * @param skipValue
     *            the number of values to skip
     * @return this
     */
    public DefaultListOperation<T> setSkip(int skipValue) {
        queryParameters.add("$skip", Integer.toString(skipValue));
        return this;
    }

    /**
     * Add an arbitrary query parameter
     * 
     * @param parameterName
     *            name of query parameter
     * @param parameterValue
     *            value for query parameter
     * @return this
     */
    public DefaultListOperation<T> set(String parameterName,
            String parameterValue) {
        queryParameters.add(parameterName, parameterValue);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entities.EntityListOperation
     * #getQueryParameters()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.microsoft.windowsazure.services.media.entities.EntityListOperation
     * #getResponseGenericType()
     */
    @Override
    public GenericType<ListResult<T>> getResponseGenericType() {
        return responseType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entityoperations.
     * EntityOperationBase#processResponse(java.lang.Object)
     */
    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        return rawResponse;
    }
}
