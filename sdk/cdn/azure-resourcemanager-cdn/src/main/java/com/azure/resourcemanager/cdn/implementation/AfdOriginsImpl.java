// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginInner;
import com.azure.resourcemanager.cdn.models.AfdOrigin;
import com.azure.resourcemanager.cdn.models.AfdOriginGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of AFD origins associated with an AFD origin group.
 */
class AfdOriginsImpl extends
    ExternalChildResourcesNonCachedImpl<AfdOriginImpl, AfdOrigin, AfdOriginInner, AfdOriginGroupImpl, AfdOriginGroup> {

    AfdOriginsImpl(AfdOriginGroupImpl parent) {
        super(parent, parent.taskGroup(), "AfdOrigin");
    }

    Map<String, AfdOrigin> originsAsMap() {
        Map<String, AfdOrigin> result = new HashMap<>();
        for (AfdOriginInner originInner : this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .listByOriginGroup(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name())) {
            AfdOriginImpl origin = new AfdOriginImpl(originInner.name(), this.getParent(), originInner);
            result.put(origin.name(), origin);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new AfdOriginImpl(name, getParent(), new AfdOriginInner()));
    }

    void addOrigin(AfdOriginImpl origin) {
        this.childCollection.put(origin.name(), origin);
    }

    AfdOriginImpl defineNewOrigin(String name) {
        return this.prepareInlineDefine(new AfdOriginImpl(name, this.getParent(), new AfdOriginInner()));
    }

    AfdOriginImpl updateOrigin(String name) {
        AfdOriginInner originInner = this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .get(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name(), name);
        return this.prepareInlineUpdate(new AfdOriginImpl(name, this.getParent(), originInner));
    }
}
