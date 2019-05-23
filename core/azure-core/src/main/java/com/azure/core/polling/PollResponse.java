package com.azure.core.polling;

import java.io.Serializable;

public interface PollResponse extends Serializable {

    /**@return  true if operation successfully compelete.**/
    boolean isOperationSuccessfullyComplete();

    /**@return  true if operation is cancelled.**/
    boolean isOperationCancelled();

    /**@return  true if operation failed.**/
    boolean isOperationFailed();

    /**@return  true if operation is in progress.**/
    boolean isOperationInProgress();

    /**
     * If the long running operation failed, get the error that occurred. If the operation is not
     * done or did not fail, then return null.
     * @return The error of the operation, or null if the operation isn't done or didn't fail.
     */
    public HttpResponseException error() {
        return error;
    }

}
