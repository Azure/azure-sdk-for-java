package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;

import java.io.Serializable;

/** The life cycle of an operation cdepicted below
 * NOT-STARTED ---> IN-PROGRESS
 *                              ------> Successfully Complete
 *                              ------> Cancelled
 *                              ------> Failed
 **/
public final class PollResponse{

    private OperationStatus status;

    public enum OperationStatus{
        SUCCESSFULLY_COMPLETED,
        IN_PROGRESS,
        FAILED,
        CANCELLED,
        STARTED
    }

    public PollResponse( OperationStatus status){
        this.status=status;
    }
    /**@return  OperationStatus**/
    OperationStatus status(){
        return status;
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
}
