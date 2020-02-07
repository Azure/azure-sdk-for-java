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

    /**
     * Creates an instance of information about close handles.
     *
     * @param closedHandles The numbers of handles closed.
     */
    public CloseHandlesInfo(Integer closedHandles) {
        this.closedHandles = closedHandles;
    }

    /**
     *
     * @return The number of handles closed.
     */
    public int getClosedHandles() {
        return this.closedHandles;
    }
}
