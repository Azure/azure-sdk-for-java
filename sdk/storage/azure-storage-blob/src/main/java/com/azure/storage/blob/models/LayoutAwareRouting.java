// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the routing behavior the SDK should use when download hint metadata indicates blob layout is available.
 * <p>
 * This is a performance optimization only. The bytes returned are identical regardless of the mode used.
 */
public enum LayoutAwareRouting {
    /**
     * The default routing behavior. This currently behaves the same as {@link #ENABLED}, but the client library may
     * change this behavior in a future release.
     */
    AUTO,

    /**
     * Never use layout-aware routing. The client always uses its configured endpoint.
     */
    DISABLED,

    /**
     * Opt in to layout-aware routing. The client fetches blob layout on demand, caches it, and refreshes it in the
     * background when needed.
     */
    ENABLED
}
