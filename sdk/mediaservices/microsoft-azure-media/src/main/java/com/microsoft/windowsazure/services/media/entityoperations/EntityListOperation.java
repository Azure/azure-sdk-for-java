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

import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;

/**
 * Operation class to retrieve a list of entities
 * 
 */
public interface EntityListOperation<T> extends EntityOperation {

    /**
     * Get query parameters to add to the uri
     * 
     * @return The query parameters collection
     */
    MultivaluedMap<String, String> getQueryParameters();

    /**
     * Get a GenericType object representing the result list type
     * 
     * @return the type of the operation's result
     */
    GenericType<ListResult<T>> getResponseGenericType();
}
