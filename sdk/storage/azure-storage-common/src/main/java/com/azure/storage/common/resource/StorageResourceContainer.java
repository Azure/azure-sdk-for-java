// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.resource;

import java.util.List;

/**
 * Represents a container of storage resources. E.g. local directory, blob container, file share, file share directory,
 * s3 bucket. A container can contain other container, e.g. a directory can contain subdirectory.
 */
public interface StorageResourceContainer {

    /**
     * Lists storage resources in this container.
     * @return A list of storage resources in this container.
     */
    Iterable<StorageResource> listResources();

    /**
     * Gets an abstract path segments of the resource container.
     * <p>
     *     The path segments don't carry path separator which is specific to storage resource provider.
     * </p>
     * @return An abstract path segments of the resource.
     */
    List<String> getPath();

    /**
     * Gets reference to a storage resource within this container.
     * @param path The path of storage resource. Relative to this container.
     * @return A storage resource.
     */
    StorageResource getStorageResource(List<String> path);

    /**
     * Gets reference to a child storage resource container within this container.
     * @param path The path of storage resource container. Relative to this container.
     * @return A storage resource container.
     * @throws UnsupportedOperationException If the storage resource provider doesn't support nested containers.
     */
    StorageResourceContainer getStorageResourceContainer(List<String> path);
}
