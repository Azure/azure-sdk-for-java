// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.storage.common.implementation.Constants;

/**
 * Represents protocols that can be set on a share.
 */
public class ShareEnabledProtocol {

    private boolean smb;

    private boolean nfs;

    /**
     * @return Enable SMB
     */
    public boolean isSmb() {
        return smb;
    }

    /**
     * @return Enable NFS
     */
    public boolean isNfs() {
        return nfs;
    }

    /**
     * @param smb Enable SMB
     * @return The updated object
     */
    public ShareEnabledProtocol setSmb(boolean smb) {
        this.smb = smb;
        return this;
    }

    /**
     * @param nfs Enable NFS
     * @return The updated object
     */
    public ShareEnabledProtocol setNfs(boolean nfs) {
        this.nfs = nfs;
        return this;
    }

    public String toString() {
        if (this.smb) {
            if (this.nfs) {
                throw new IllegalArgumentException("SMB and NFS cannot both be set.");
            }
            return Constants.HeaderConstants.SMB_PROTOCOL;
        }
        if (this.nfs) {
            return Constants.HeaderConstants.NFS_PROTOCOL;
        }
        return null;
    }
}
