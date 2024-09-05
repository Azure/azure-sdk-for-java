// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.O365BreakOutCategoryPolicies;
import com.azure.resourcemanager.network.models.O365Policy;
import com.azure.resourcemanager.network.models.O365PolicyProperties;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/** Implementation for O365Policy properties. */
public class O365PolicyImpl
    extends ChildResourceImpl<O365PolicyProperties, VpnSiteImpl, VpnSite>
    implements O365Policy,
    O365Policy.Definition<VpnSite.DefinitionStages.WithCreate>,
    O365Policy.UpdateDefinition<VpnSite.Update>,
    O365Policy.Update {

    O365PolicyImpl(O365PolicyProperties innerObject, VpnSiteImpl parent) {
        super(innerObject, parent);
    }

    @Override
    public boolean allow() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().breakOutCategories().allow());
    }

    @Override
    public boolean optimize() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().breakOutCategories().optimize());
    }

    @Override
    public boolean defaultProperty() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().breakOutCategories().defaultProperty());
    }

    @Override
    public O365PolicyImpl withAllow(Boolean allow) {
        if (this.innerModel().breakOutCategories() == null) {
            this.innerModel().withBreakOutCategories(new O365BreakOutCategoryPolicies());
        }
        this.innerModel().breakOutCategories().withAllow(allow);
        return this;
    }

    @Override
    public O365PolicyImpl withOptimize(Boolean optimize) {
        if (this.innerModel().breakOutCategories() == null) {
            this.innerModel().withBreakOutCategories(new O365BreakOutCategoryPolicies());
        }
        this.innerModel().breakOutCategories().withOptimize(optimize);
        return this;
    }

    @Override
    public O365PolicyImpl withDefaultProperty(Boolean defaultProperty) {
        if (this.innerModel().breakOutCategories() == null) {
            this.innerModel().withBreakOutCategories(new O365BreakOutCategoryPolicies());
        }
        this.innerModel().breakOutCategories().withDefaultProperty(defaultProperty);
        return this;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public VpnSiteImpl attach() {
        this.parent().innerModel().withO365Policy(this.innerModel());
        return this.parent();
    }
}
