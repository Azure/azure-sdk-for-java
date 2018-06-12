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

/**
 * Generic implementation of the get operation usable for most entities
 * 
 */
public class DefaultGetOperation<T> extends EntityOperationSingleResultBase<T>
        implements EntityGetOperation<T> {

    /**
     * Construct a new DefaultGetOperation to return the given entity id
     * 
     * @param entityTypeUri
     *            Entity set URI
     * @param entityId
     *            id of entity
     * @param responseClass
     *            class to return from the get operation
     */
    public DefaultGetOperation(String entityTypeUri, String entityId,
            Class<T> responseClass) {
        super(new EntityOperationBase.EntityIdUriBuilder(entityTypeUri,
                entityId), responseClass);
    }

    /**
     * Construct a new DefaultGetOperation to return the entity from the given
     * uri
     * 
     * @param uri
     *            Uri for entity
     * @param responseClass
     *            class to return from the get operation
     */
    public DefaultGetOperation(String uri, Class<T> responseClass) {
        super(uri, responseClass);
    }
}
