// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.graphrbac.implementation;

import com.azure.management.graphrbac.Permission;
import com.azure.management.graphrbac.models.PermissionInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.List;

/**
 * Implementation for Permission and its parent interfaces.
 */
class PermissionImpl
        extends WrapperImpl<PermissionInner>
        implements Permission {
    protected PermissionImpl(PermissionInner innerObject) {
        super(innerObject);
    }

    @Override
    public List<String> actions() {
        return inner().actions();
    }

    @Override
    public List<String> notActions() {
        return inner().notActions();
    }

    @Override
    public List<String> dataActions() {
        return inner().dataActions();
    }

    @Override
    public List<String> notDataActions() {
        return inner().notDataActions();
    }
}
