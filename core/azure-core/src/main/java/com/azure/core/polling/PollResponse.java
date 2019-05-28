package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import com.microsoft.azure.eventhubs.impl.Operation;

import java.io.Serializable;

/** The life cycle of an operation cdepicted below
 * NOT-STARTED ---> IN-PROGRESS
 *                              ------> Successfully Complete
 *                              ------> Cancelled
 *                              ------> Failed
 **/
public final class PollResponse<T>{

    private OperationStatus status;
    private T result;
    public enum OperationStatus{
        SUCCESSFULLY_COMPLETED,
        IN_PROGRESS,
        FAILED,
        CANCELLED,
        STARTED
    }

    public PollResponse( OperationStatus status, T result){
        this.status=status;
        this.result = result;
    }
    /**@return  OperationStatus**/
    public OperationStatus status(){
        return status;
    }

    public void setStatus(OperationStatus status){
        this.status =status;
    }
    /** An operation will be done if it is
     *          a. Successfully Complete
     *          b. Cancelled
     *          c. Failed
     @return  true if operation is done.
     **/
    public boolean isDone(){
        return status == OperationStatus.SUCCESSFULLY_COMPLETED
            || status == OperationStatus.FAILED
            || status == OperationStatus.CANCELLED;
    }

    public T getResult(){
        return result;
    }
}
