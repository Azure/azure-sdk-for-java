package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;

import java.io.Serializable;

/** The life cycle of an operation cdepicted below
 * NOT-STARTED  --> IN-PROGRESS
 *                              ------> Successfully Complete
 *                              ------> Cancelled
 *                              ------> Failed
 **/
public interface PollResponse extends Serializable {

    /**@return  true if operation successfully completes.**/
    boolean isOperationSuccessfullyComplete();

    /**@return  true if operation is cancelled.**/
    boolean isOperationCancelled();

    /**@return  true if operation failed.**/
    boolean isOperationFailed();

    /**@return  true if operation is in progress.**/
    boolean isOperationInProgress();

    /**@return  true if operation is started.**/
    boolean isOperationStarted();


    /** An operation will be done if it is
     *          a. Successfully Complete
     *          b. Cancelled
     *          c. Failed
     @return  true if operation is done.
     **/
    default boolean isDone(){
        return isOperationSuccessfullyComplete() || isOperationFailed() || isOperationCancelled();
    }

    /**
     * If the long running operation failed, get the error that occurred. If the operation is not
     * done or did not fail, then return null.
     * @return The error of the operation, or null if the operation isn't done or didn't fail.
     */
    public HttpResponseException error();

}
