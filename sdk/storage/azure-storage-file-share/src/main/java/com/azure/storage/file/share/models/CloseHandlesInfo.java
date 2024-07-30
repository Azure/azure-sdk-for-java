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
    private final Integer failedHandles;

    /**
     * Creates an instance of information about close handles.
     *
     * @param closedHandles The numbers of handles closed.
     * Note : Failed handles was added as a parameter, default value for failed handles is 0
     */
    public CloseHandlesInfo(Integer closedHandles) {
        this.closedHandles = closedHandles;
        this.failedHandles = 0;
    }

    /**
     * Creates an instance of information about close handles.
     *
     * @param closedHandles The numbers of handles closed.
     * @param failedHandles The numbers of handles that failed to close.
     */
    public CloseHandlesInfo(Integer closedHandles, Integer failedHandles) {
        this.closedHandles = closedHandles;
        this.failedHandles = failedHandles;
    }

    /**
     * Gets the number of handles closed.
     *
     * @return The number of handles closed.
     */
    public int getClosedHandles() {
        return this.closedHandles;
    }

    /**
     * Gets the number of handles that failed to close.
     *
     * @return The number of handles that failed to close.
     */
    public int getFailedHandles() {
        return this.failedHandles;
    }
}
