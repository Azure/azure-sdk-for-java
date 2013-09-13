package com.microsoft.windowsazure.services.media.entityoperations;

import java.util.concurrent.Callable;

import com.microsoft.windowsazure.services.media.models.Operation;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;

public class OperationThread<T> implements Callable<OperationInfo<T>> {

    private final EntityRestProxy entityRestProxy;
    private final String operationId;
    private final T entity;

    public OperationThread(EntityRestProxy entityRestProxy, String operationId, T entity) {
        if (entityRestProxy == null) {
            throw new IllegalArgumentException("The entity rest proxy cannot be null.");
        }

        if ((operationId == null) || (operationId.isEmpty())) {
            throw new IllegalArgumentException("The operation ID cannot be null or empty.");
        }
        this.entityRestProxy = entityRestProxy;
        this.operationId = operationId;
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OperationInfo<T> call() throws Exception {
        OperationInfo<T> operationInfo = this.entityRestProxy.get(Operation.get(operationId));
        while (operationInfo.getState().equals(OperationState.InProgress)) {
            operationInfo = this.entityRestProxy.get(Operation.get(operationId));
        }
        operationInfo.setEntity(entity);
        return operationInfo;
    }
}
