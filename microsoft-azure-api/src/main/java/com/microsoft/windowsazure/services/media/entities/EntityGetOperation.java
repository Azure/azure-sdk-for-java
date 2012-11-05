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
 * 
 *
 */
public interface EntityGetOperation<T> {
    /**
     * Get the URI required to retrieve an entity
     * 
     * @return The URI
     */
    String getUri();

    /**
     * Get the media type for content sent to the server
     * 
     * @return media type
     */
    MediaType getContentType();

    /**
     * Get the media type for the response back from the server
     * 
     * @return media type
     */
    MediaType getAcceptType();

    /**
     * Get the Java class that the returned data
     * will be unmarshalled into
     * 
     * @return Class object
     */
    Class<T> getResponseClass();
}
