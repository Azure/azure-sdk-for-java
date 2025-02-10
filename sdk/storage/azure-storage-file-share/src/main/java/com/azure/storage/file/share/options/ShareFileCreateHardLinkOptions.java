// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Options that may be passed when creating a hard link for a file.
 */
public final class ShareFileCreateHardLinkOptions {
    private final String targetFile;
    private ShareRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link ShareFileCreateHardLinkOptions}.
     * @param targetFile Path of the file to create the hard link to, not including the share. For example,
     * {@code targetDirectory/targetSubDirectory/.../targetFile}
     */
    public ShareFileCreateHardLinkOptions(String targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * Gets the path of the target file to create the hard link to, not including the share.
     *
     * @return the path of the target file to create the hard link to, not including the share. For example,
     * {@code targetDirectory/targetSubDirectory/.../targetFile}
     */
    public String getTargetFile() {
        return targetFile;
    }

    /**
     * Optional {@link ShareRequestConditions} to add conditions on creating the hard link.
     *
     * @return the {@link ShareRequestConditions} for the creation of the hard link.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Optional {@link ShareRequestConditions} to add conditions on creating the hard link.
     *
     * @param requestConditions {@link ShareRequestConditions}
     * @return the updated options.
     */
    public ShareFileCreateHardLinkOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
