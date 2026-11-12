// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

/**
 * Describes one path that links to a file.
 */
@Immutable
public final class ShareFileLink {
    private final String parentId;
    private final String name;

    /**
     * Creates a file link.
     *
     * @param parentId The file ID of the parent directory.
     * @param name The name of the file link.
     */
    public ShareFileLink(String parentId, String name) {
        this.parentId = parentId;
        this.name = name;
    }

    /**
     * Gets the file ID of the parent directory.
     *
     * @return The parent directory ID.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Gets the name of the file link.
     *
     * @return The file link name.
     */
    public String getName() {
        return name;
    }
}
