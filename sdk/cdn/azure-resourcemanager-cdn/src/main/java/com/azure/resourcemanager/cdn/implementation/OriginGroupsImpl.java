// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginGroupInner;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.OriginGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of origin groups associated with a CDN profile.
 */
class OriginGroupsImpl extends
    ExternalChildResourcesNonCachedImpl<OriginGroupImpl, OriginGroup, AfdOriginGroupInner, CdnProfileImpl, CdnProfile> {

    OriginGroupsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "OriginGroup");
    }

    Map<String, OriginGroup> originGroupsAsMap() {
        Map<String, OriginGroup> result = new HashMap<>();
        for (AfdOriginGroupInner inner : this.getParent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .listByProfile(this.getParent().resourceGroupName(), this.getParent().name())) {
            OriginGroupImpl originGroup = new OriginGroupImpl(inner.name(), this.getParent(), inner);
            result.put(originGroup.name(), originGroup);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new OriginGroupImpl(name, getParent(), new AfdOriginGroupInner()));
    }

    void addOriginGroup(OriginGroupImpl originGroup) {
        this.childCollection.put(originGroup.name(), originGroup);
    }

    OriginGroupImpl defineNewOriginGroup(String name) {
        return this.prepareInlineDefine(new OriginGroupImpl(name, this.getParent(), new AfdOriginGroupInner()));
    }

    OriginGroupImpl updateOriginGroup(String name) {
        AfdOriginGroupInner inner = this.getParent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .get(this.getParent().resourceGroupName(), this.getParent().name(), name);
        return this.prepareInlineUpdate(new OriginGroupImpl(name, this.getParent(), inner));
    }
}
