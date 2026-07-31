// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginInner;
import com.azure.resourcemanager.cdn.models.Origin;
import com.azure.resourcemanager.cdn.models.OriginGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of origins associated with an origin group.
 */
class OriginsImpl
    extends ExternalChildResourcesNonCachedImpl<OriginImpl, Origin, AfdOriginInner, OriginGroupImpl, OriginGroup> {

    OriginsImpl(OriginGroupImpl parent) {
        super(parent, parent.taskGroup(), "Origin");
    }

    Map<String, Origin> originsAsMap() {
        Map<String, Origin> result = new HashMap<>();
        for (AfdOriginInner originInner : this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .listByOriginGroup(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name())) {
            OriginImpl origin = new OriginImpl(originInner.name(), this.getParent(), originInner);
            result.put(origin.name(), origin);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new OriginImpl(name, getParent(), new AfdOriginInner()));
    }

    void addOrigin(OriginImpl origin) {
        this.childCollection.put(origin.name(), origin);
    }

    OriginImpl defineNewOrigin(String name) {
        return this.prepareInlineDefine(new OriginImpl(name, this.getParent(), new AfdOriginInner()));
    }

    OriginImpl updateOrigin(String name) {
        AfdOriginInner originInner = this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .get(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name(), name);
        return this.prepareInlineUpdate(new OriginImpl(name, this.getParent(), originInner));
    }
}
