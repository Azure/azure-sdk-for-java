// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;

/**
 * Represents an external child resource.
 *
 * @param <FluentModelT> fluent type of the external child resource
 * @param <ParentT> parent interface
 */
public interface ExternalChildResource<FluentModelT, ParentT>
        extends ChildResource<ParentT>, Refreshable<FluentModelT> {
    /**
     * @return the id of the external child resource
     */
    String id();
}
