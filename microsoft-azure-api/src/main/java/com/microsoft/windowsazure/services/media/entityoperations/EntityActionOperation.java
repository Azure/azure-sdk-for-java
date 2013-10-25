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

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Action operation for Entities.
 */
public interface EntityActionOperation extends EntityOperation {

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
    EntityActionOperation addQueryParameter(String key, String value);

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
     * @return the default action operation
     */
    EntityActionOperation setContentType(MediaType contentType);

    /**
     * Gets the body parameters.
     * 
     * @return the body parameters
     */
    Map<String, Object> getBodyParameters();

    /**
     * Adds the body parameter.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the entity action operation
     */
    EntityActionOperation addBodyParameter(String key, Object value);
}
