package com.microsoft.windowsazure.services.media.entityoperations;

import java.util.concurrent.Callable;

import com.microsoft.windowsazure.services.media.models.Operation;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;

public class OperationThread<T> implements Callable<OperationInfo<T>> {

    private final EntityRestProxy entityRestProxy;
    private final String operationId;
    private final T entity;
    private final int operationCallInterval;

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

    @SuppressWarnings("unchecked")
    @Override
    public OperationInfo<T> call() throws Exception {
        OperationInfo<T> operationInfo = this.entityRestProxy.get(Operation.get(operationId));
        while (operationInfo.getState().equals(OperationState.InProgress)) {
            operationInfo = this.entityRestProxy.get(Operation.get(operationId));
            Thread.sleep(operationCallInterval);
        }
        operationInfo.setEntity(entity);
        return operationInfo;
    }
}
