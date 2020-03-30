/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import com.azure.management.graphrbac.models.PermissionInner;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;

/**
 * An immutable client-side representation of a permission.
 */
public interface Permission extends HasInner<PermissionInner> {
    /**
     * @return allowed actions
     */
    List<String> actions();

    /**
     * @return denied actions
     */
    List<String> notActions();

    /**
     * @return allowed Data actions
     */
    List<String> dataActions();

    /**
     * @return denied Data actions
     */
    List<String> notDataActions();
}
