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
 * 
 * 
 */
public class EntityOperationSingleResultBase<T> extends EntityOperationBase
        implements EntityOperationSingleResult<T> {
    private final Class<T> responseClass;

    /**
     * 
     */
    public EntityOperationSingleResultBase(String uri, Class<T> responseClass) {
        super(uri);
        this.responseClass = responseClass;
    }

    public EntityOperationSingleResultBase(
            EntityOperationBase.EntityUriBuilder uriBuilder,
            Class<T> responseClass) {
        super(uriBuilder);
        this.responseClass = responseClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.microsoft.windowsazure.services.media.entities.
     * EntityOperationSingleResult#getResponseClass()
     */
    @Override
    public Class<?> getResponseClass() {
        return responseClass;
    }

    @Override
    public Object processResponse(Object rawResponse) throws ServiceException {
        return rawResponse;
    }
}
