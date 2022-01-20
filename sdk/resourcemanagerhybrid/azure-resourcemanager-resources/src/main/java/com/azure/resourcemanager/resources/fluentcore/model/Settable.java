// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * The base interface for all template interfaces for child resources that support
 * update operations.
 *
 * @param <ParentT> the parent definition {@link Settable#parent()} returns to
 */
public interface Settable<ParentT> {
    /**
     * Begins an update for a child resource.
     * <p>
     * This is the beginning of the builder pattern used to update child resources
     * The final method completing the update and continue
     * the actual parent resource update process in Azure is {@link Settable#parent()}.
     *
     * @return the stage of parent resource update
     */
    ParentT parent();
}
