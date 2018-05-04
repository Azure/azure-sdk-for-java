package com.microsoft.azure.storage.blob;

public interface IProgressReceiver {

    /**
     * The callback function invoked as progress is reported.
     *
     * @param bytesTransferred
     *      The total number of bytes transferred during this transaction.
     */
    public void reportProgress(long bytesTransferred);
}
