// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.models.PrivateLinkResource;
import com.azure.resourcemanager.cosmos.fluent.models.PrivateLinkResourceInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** A private link resource. */
public class PrivateLinkResourceImpl extends WrapperImpl<PrivateLinkResourceInner> implements PrivateLinkResource {

    PrivateLinkResourceImpl(PrivateLinkResourceInner innerObject) {
        super(innerObject);
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public String type() {
        return innerModel().type();
    }

    @Override
    public String groupId() {
        return innerModel().groupId();
    }

    @Override
    public List<String> requiredMembers() {
        return innerModel().requiredMembers();
    }
}
