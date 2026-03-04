// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginGroupInner;
import com.azure.resourcemanager.cdn.models.AfdOriginGroup;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of AFD origin groups associated with a CDN profile.
 */
class AfdOriginGroupsImpl extends
    ExternalChildResourcesNonCachedImpl<AfdOriginGroupImpl, AfdOriginGroup, AfdOriginGroupInner, CdnProfileImpl, CdnProfile> {

    AfdOriginGroupsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "AfdOriginGroup");
    }

    Map<String, AfdOriginGroup> originGroupsAsMap() {
        Map<String, AfdOriginGroup> result = new HashMap<>();
        for (AfdOriginGroupInner inner : this.getParent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .listByProfile(this.getParent().resourceGroupName(), this.getParent().name())) {
            AfdOriginGroupImpl originGroup = new AfdOriginGroupImpl(inner.name(), this.getParent(), inner);
            result.put(originGroup.name(), originGroup);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new AfdOriginGroupImpl(name, getParent(), new AfdOriginGroupInner()));
    }

    void addOriginGroup(AfdOriginGroupImpl originGroup) {
        this.childCollection.put(originGroup.name(), originGroup);
    }

    AfdOriginGroupImpl defineNewOriginGroup(String name) {
        return this.prepareInlineDefine(new AfdOriginGroupImpl(name, this.getParent(), new AfdOriginGroupInner()));
    }

    AfdOriginGroupImpl updateOriginGroup(String name) {
        AfdOriginGroupInner inner = this.getParent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .get(this.getParent().resourceGroupName(), this.getParent().name(), name);
        return this.prepareInlineUpdate(new AfdOriginGroupImpl(name, this.getParent(), inner));
    }
}
