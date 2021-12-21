// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Base interface used by child resources that do not immediately have their parent
 * attached to them but are instead available directly off other entry points.
 *
 * @param <ParentT> parent interface
 */
@Fluent
public interface ParentlessChildResource<ParentT> extends
        Indexable,
        HasName {

    /**
     * @return the parent of this child object
     */
    ParentT getParent();
}
