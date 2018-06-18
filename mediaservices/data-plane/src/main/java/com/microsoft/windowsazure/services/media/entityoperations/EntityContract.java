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

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.ListResult;

/**
 * Contract for interacting with the back end service providing various odata
 * entities.
 * 
 */
public interface EntityContract {

    /**
     * Create a new instance of an entity.
     * 
     * @param <T>
     *            the generic type
     * @param creator
     *            Object providing the details of the entity to be created
     * @return the t
     * @throws ServiceException
     *             the service exception The created entity
     */
    <T> T create(EntityCreateOperation<T> creator) throws ServiceException;

    /**
     * Retrieve an existing entity by id.
     * 
     * @param <T>
     *            the generic type
     * @param getter
     *            object providing the details of the entity to be retrieved
     * @return The retrieved entity
     * @throws ServiceException
     *             the service exception
     */
    <T> T get(EntityGetOperation<T> getter) throws ServiceException;

    /**
     * Retrieve a list of entities.
     * 
     * @param <T>
     *            the generic type
     * @param lister
     *            object providing details of entities to list
     * @return The resulting list
     * @throws ServiceException
     *             the service exception
     */
    <T> ListResult<T> list(EntityListOperation<T> lister)
            throws ServiceException;

    /**
     * Update an existing entity.
     * 
     * @param updater
     *            Object providing details of the update
     * @throws ServiceException
     *             the service exception
     */
    String update(EntityUpdateOperation updater) throws ServiceException;

    /**
     * Delete an entity.
     * 
     * @param deleter
     *            Object providing details of the delete
     * @return 
     * @throws ServiceException
     *             the service exception
     */
    String delete(EntityDeleteOperation deleter) throws ServiceException;

    /**
     * Perform an action on an entity.
     * 
     * @param action
     *            Object providing details of the action
     * @return 
     * @throws ServiceException
     *             the service exception
     */
    String action(EntityActionOperation action) throws ServiceException;

    /**
     * Action.
     * 
     * @param <T>
     *            the generic type
     * @param entityActionOperation
     *            the entity action operation
     * @return the t
     * @throws ServiceException
     *             the service exception
     */
    <T> T action(EntityTypeActionOperation<T> entityActionOperation)
            throws ServiceException;

}
