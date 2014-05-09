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

import com.sun.jersey.api.client.ClientResponse;

/**
 * The Interface EntityTypeActionOperation.
 * 
 * @param <T>
 *            the generic type
 */
public interface EntityTypeActionOperation<T> extends EntityOperation {

    /**
     * Process type response.
     * 
     * @param clientResponse
     *            the client response
     * @return the t
     */
    T processTypeResponse(ClientResponse clientResponse);

    /**
     * Gets the query parameters.
     * 
     * @return the query parameters
     */
    MultivaluedMap<String, String> getQueryParameters();

    /**
     * Adds the query parameter.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the entity action operation
     */
    EntityTypeActionOperation<T> addQueryParameter(String key, String value);

    /**
     * Gets the verb.
     * 
     * @return the verb
     */
    String getVerb();

    /**
     * Gets the request contents.
     * 
     * @return the request contents
     */
    Object getRequestContents();

    /**
     * Sets the content type.
     * 
     * @param contentType
     *            the content type
     * @return the entity type action operation
     */
    EntityTypeActionOperation<T> setContentType(MediaType contentType);

    /**
     * Sets the accept type.
     * 
     * @param acceptType
     *            the accept type
     * @return the entity type action operation
     */
    EntityTypeActionOperation<T> setAcceptType(MediaType acceptType);

}
