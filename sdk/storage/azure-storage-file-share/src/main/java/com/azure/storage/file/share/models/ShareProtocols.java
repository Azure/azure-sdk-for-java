// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

/**
 * Represents protocols that can be set on a share.
 */
public class ShareProtocols {
    private final ClientLogger logger = new ClientLogger(ShareProtocols.class);

    private boolean smbEnabled;

    private boolean nfsEnabled;

    /**
     * @return Enable SMB
     */
    public boolean isSmbEnabled() {
        return smbEnabled;
    }

    /**
     * @return Enable NFS
     */
    public boolean isNfsEnabled() {
        return nfsEnabled;
    }

    /**
     * @param smb Enable SMB
     * @return The updated object
     */
    public ShareProtocols setSmbEnabled(boolean smb) {
        this.smbEnabled = smb;
        return this;
    }

    /**
     * @param nfs Enable NFS
     * @return The updated object
     */
    public ShareProtocols setNfsEnabled(boolean nfs) {
        this.nfsEnabled = nfs;
        return this;
    }

    /**
     * Converts the given protocols to a {@code String}.
     *
     * @return A {@code String} which represents the enabled protocols.
     * @throws IllegalArgumentException If both SMB and NFS are set.
     */
    public String toString() {
        if (this.smbEnabled) {
            if (this.nfsEnabled) {
                throw logger.logExceptionAsError(new IllegalArgumentException("SMB and NFS cannot both be set."));
            }
            return Constants.HeaderConstants.SMB_PROTOCOL;
        }
        if (this.nfsEnabled) {
            return Constants.HeaderConstants.NFS_PROTOCOL;
        }
        return "";
    }
}
