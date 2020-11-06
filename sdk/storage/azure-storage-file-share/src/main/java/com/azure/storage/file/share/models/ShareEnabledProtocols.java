// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;

/**
 * Represents protocols that can be set on a share.
 */
public class ShareEnabledProtocols {
    private final ClientLogger logger = new ClientLogger(ShareEnabledProtocols.class);

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
    public ShareEnabledProtocols setSmb(boolean smb) {
        this.smb = smb;
        return this;
    }

    /**
     * @param nfs Enable NFS
     * @return The updated object
     */
    public ShareEnabledProtocols setNfs(boolean nfs) {
        this.nfs = nfs;
        return this;
    }

    /**
     * Converts the given protocols to a {@code String}.
     *
     * @return A {@code String} which represents the enabled protocols.
     * @throws IllegalArgumentException If both SMB and NFS are set.
     */
    public String toString() {
        if (this.smb) {
            if (this.nfs) {
                throw logger.logExceptionAsError(new IllegalArgumentException("SMB and NFS cannot both be set."));
            }
            return Constants.HeaderConstants.SMB_PROTOCOL;
        }
        if (this.nfs) {
            return Constants.HeaderConstants.NFS_PROTOCOL;
        }
        return null;
    }

    /**
     * Parses a {@code String} into a {@code SharEnabledProtocol}.
     *
     * @param str The string to parse.
     * @return A {@code ShareEnabledProtocol} represented by the string.
     * @throws IllegalArgumentException If the String is not a recognized protocol.
     */
    public static ShareEnabledProtocols parse(String str) {
        if (str == null) {
            return null;
        }

        if (str.equals(Constants.HeaderConstants.SMB_PROTOCOL)) {
            return new ShareEnabledProtocols().setSmb(true);
        } else if (str.equals(Constants.HeaderConstants.NFS_PROTOCOL)) {
            return new ShareEnabledProtocols().setNfs(true);
        }

        throw new IllegalArgumentException("String is not an understood protocol: " + str);
    }
}
