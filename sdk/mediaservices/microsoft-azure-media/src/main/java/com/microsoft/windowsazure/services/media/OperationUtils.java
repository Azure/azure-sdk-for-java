package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityWithOperationIdentifier;
import com.microsoft.windowsazure.services.media.models.Operation;
import com.microsoft.windowsazure.services.media.models.OperationInfo;
import com.microsoft.windowsazure.services.media.models.OperationState;

public final class OperationUtils {
    
    private OperationUtils() {
        // do nothing
    }
    
    /**
     * Awaits for an operation to be completed.
     * 
     * @param service 
     *          the media contract
     * @param operationId
     *          the operation id to wait for.
     * @return the final state of the operation. If operationId is null, returns OperationState.Succeeded.
     * @throws ServiceException 
     */
    public static OperationState await(MediaContract service, String operationId) throws ServiceException {
        if (operationId == null) {
            return OperationState.Succeeded;
        }
        OperationInfo opinfo;
        do {
            opinfo = service.get(Operation.get(operationId));            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // intentionally do nothing
            }
        } while (opinfo.getState().equals(OperationState.InProgress));
        return opinfo.getState();
    }
    
    /**
     * Awaits for an operation to be completed.
     * 
     * @param service 
     *          the media contract
     * @param operation
     *          the operation id to wait for.
     * @return the final state of the operation. If the entity has not operationId, returns OperationState.Succeeded.
     * @throws ServiceException 
     */
    public static OperationState await(MediaContract service, EntityWithOperationIdentifier entity) throws ServiceException {
        if (entity.hasOperationIdentifier()) {
            return await(service, entity.getOperationId());
        }
        return OperationState.Succeeded;
    }
}
