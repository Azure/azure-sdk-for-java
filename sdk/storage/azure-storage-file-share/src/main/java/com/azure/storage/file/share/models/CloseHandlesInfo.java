// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.storage.file.share.ShareFileAsyncClient;

/**
 * Contains close handles information from the storage File service.
 *
 * @see ShareFileAsyncClient#forceCloseHandle(String)
 * @see ShareFileAsyncClient#forceCloseAllHandles()
 */
public class CloseHandlesInfo {

    private final Integer closedHandles;
    private final Integer failedToCloseHandles;

    /**
     * Creates an instance of information about close handles.
     *
     * @param closedHandles The numbers of handles closed.
     * Note : Failed handles was added as a parameter, default value for failed handles is 0
     */
    public CloseHandlesInfo(Integer closedHandles) {
        this.closedHandles = closedHandles;
        this.failedToCloseHandles = 0;
    }

    /**
     * Creates an instance of information about close handles.
     *
     * @param closedHandles The numbers of handles closed.
     * @param failedToCloseHandles The numbers of handles that failed to close.
     */
    public CloseHandlesInfo(Integer closedHandles, Integer failedToCloseHandles) {
        this.closedHandles = closedHandles;
        this.failedToCloseHandles = failedToCloseHandles;
    }

    /**
     *
     * @return The number of handles closed.
     */
    public int getClosedHandles() {
        return this.closedHandles;
    }

    /**
     *
     * @return The number of handles that failed to close.
     */
    public int getFailedToCloseHandles() {
        return this.failedToCloseHandles;
    }
}
