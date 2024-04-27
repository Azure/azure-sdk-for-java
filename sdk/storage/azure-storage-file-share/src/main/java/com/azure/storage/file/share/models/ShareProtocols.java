// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

/**
 * Represents protocols that can be set on a share.
 */
public class ShareProtocols {
    private static final ClientLogger LOGGER = new ClientLogger(ShareProtocols.class);

    private boolean smbEnabled;
    private boolean nfsEnabled;

    /**
     * Creates a new instance of {@link ShareProtocols}.
     */
    public ShareProtocols() {
    }

    /**
     * Whether SMB is enabled.
     *
     * @return Enable SMB
     */
    public boolean isSmbEnabled() {
        return smbEnabled;
    }

    /**
     * Whether NFS is enabled.
     *
     * @return Enable NFS
     */
    public boolean isNfsEnabled() {
        return nfsEnabled;
    }

    /**
     * Sets whether SMB is enabled.
     *
     * @param smb Enable SMB
     * @return The updated object
     */
    public ShareProtocols setSmbEnabled(boolean smb) {
        this.smbEnabled = smb;
        return this;
    }

    /**
     * Sets whether NFS is enabled.
     *
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
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("SMB and NFS cannot both be set."));
            }
            return Constants.HeaderConstants.SMB_PROTOCOL;
        }
        if (this.nfsEnabled) {
            return Constants.HeaderConstants.NFS_PROTOCOL;
        }
        return "";
    }
}
