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

import java.util.concurrent.Callable;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.Operation;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;

/**
 * The Class OperationThread.
 * 
 * @param <T>
 *            the generic type
 */
public class OperationThread<T> implements Callable<OperationInfo<T>> {

    /** The entity rest proxy. */
    private final EntityRestProxy entityRestProxy;

    /** The operation id. */
    private final String operationId;

    /** The entity. */
    private final T entity;

    /** The operation call interval. */
    private final int operationCallInterval;

    /**
     * Instantiates a new operation thread.
     * 
     * @param entityRestProxy
     *            the entity rest proxy
     * @param operationId
     *            the operation id
     * @param operationCallInterval
     *            the operation call interval
     * @param entity
     *            the entity
     */
    public OperationThread(EntityRestProxy entityRestProxy, String operationId, int operationCallInterval, T entity) {
        if (entityRestProxy == null) {
            throw new IllegalArgumentException("The entity rest proxy cannot be null.");
        }

        if ((operationId == null) || (operationId.isEmpty())) {
            throw new IllegalArgumentException("The operation ID cannot be null or empty.");
        }

        if (operationCallInterval < 0) {
            throw new IllegalArgumentException("The operation call interval must be positive.");
        }
        this.entityRestProxy = entityRestProxy;
        this.operationId = operationId;
        this.entity = entity;
        this.operationCallInterval = operationCallInterval;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    @Override
    public OperationInfo<T> call() throws ServiceException, InterruptedException {
        OperationInfo<T> operationInfo = this.entityRestProxy.get(Operation.get(operationId));

        while (operationInfo.getState().equals(OperationState.InProgress)) {
            operationInfo = this.entityRestProxy.get(Operation.get(operationId));
            Thread.sleep(operationCallInterval);
        }
        operationInfo.setEntity(entity);
        return operationInfo;
    }
}
