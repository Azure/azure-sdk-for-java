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

/**
 * The Interface EntityCreateOperation.
 * 
 * @param <T>
 *            the generic type
 */
public interface EntityCreateOperation<T> extends
        EntityOperationSingleResult<T> {

    /**
     * Get the object to be sent to the server containing the request data for
     * entity creation.
     * 
     * @return The payload to be marshalled and sent to the server.
     * @throws ServiceException
     *             the service exception
     */
    Object getRequestContents() throws ServiceException;

}
