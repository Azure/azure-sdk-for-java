// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Contains a file's properties and the paths that link to the file.
 */
@Immutable
public final class ShareFileLinks {
    private final ShareFileProperties properties;
    private final List<ShareFileLink> links;

    /**
     * Creates a file links result.
     *
     * @param properties The file's properties.
     * @param links The paths that link to the file.
     */
    public ShareFileLinks(ShareFileProperties properties, List<ShareFileLink> links) {
        this.properties = Objects.requireNonNull(properties, "'properties' cannot be null.");
        this.links
            = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(links, "'links' cannot be null.")));
    }

    /**
     * Gets the file's properties.
     *
     * @return The file's properties.
     */
    public ShareFileProperties getProperties() {
        return properties;
    }

    /**
     * Gets the paths that link to the file.
     *
     * @return The file links.
     */
    public List<ShareFileLink> getLinks() {
        return links;
    }
}
