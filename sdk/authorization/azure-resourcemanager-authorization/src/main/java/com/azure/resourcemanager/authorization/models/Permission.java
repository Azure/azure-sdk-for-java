// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.resourcemanager.authorization.fluent.models.PermissionInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** An immutable client-side representation of a permission. */
public interface Permission extends HasInnerModel<PermissionInner> {
    /**
     * Gets allowed actions.
     *
     * @return allowed actions
     */
    List<String> actions();

    /**
     * Gets denied actions.
     *
     * @return denied actions
     */
    List<String> notActions();

    /**
     * Gets allowed Data actions.
     *
     * @return allowed Data actions
     */
    List<String> dataActions();

    /**
     * Gets denied Data actions.
     *
     * @return denied Data actions
     */
    List<String> notDataActions();
}
