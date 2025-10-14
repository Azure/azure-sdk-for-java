// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.models.ShareFileSymbolicLinkInfo;

import java.time.OffsetDateTime;

/**
 * Helper class to access private constructor of {@link ShareFileSymbolicLinkInfo} across package boundaries.
 */
public final class ShareFileSymbolicLinkInfoHelper {

    private static ShareFileSymbolicLinkInfoAccessor accessor;

    private ShareFileSymbolicLinkInfoHelper() {
    }

    /**
     * Type defining the constructor of a {@link ShareFileSymbolicLinkInfo} instance.
     */
    public interface ShareFileSymbolicLinkInfoAccessor {
        /**
         * Creates a new instance of {@link ShareFileSymbolicLinkInfo}.
         *
         * @param eTag Entity tag that corresponds to the directory.
         * @param lastModified Last time the directory was modified.
         * @param linkText The absolute or relative path of the symbolic link file.
         * @return A new instance of {@link ShareFileSymbolicLinkInfo}.
         */
        ShareFileSymbolicLinkInfo create(String eTag, OffsetDateTime lastModified, String linkText);
    }

    /**
     * The method called from {@link ShareFileSymbolicLinkInfo} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(ShareFileSymbolicLinkInfoAccessor accessor) {
        ShareFileSymbolicLinkInfoHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ShareFileSymbolicLinkInfo}.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param linkText The absolute or relative path of the symbolic link file.
     * @return A new instance of {@link ShareFileSymbolicLinkInfo}.
     */
    public static ShareFileSymbolicLinkInfo create(String eTag, OffsetDateTime lastModified, String linkText) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareFileSymbolicLinkInfo which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileSymbolicLinkInfo();
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, linkText);
    }

}
