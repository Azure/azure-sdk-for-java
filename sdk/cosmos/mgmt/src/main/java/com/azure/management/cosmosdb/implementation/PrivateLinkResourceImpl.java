// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.cosmosdb.implementation;

import com.azure.management.cosmosdb.PrivateLinkResource;
import com.azure.management.cosmosdb.models.PrivateLinkResourceInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** A private link resource. */
public class PrivateLinkResourceImpl extends WrapperImpl<PrivateLinkResourceInner> implements PrivateLinkResource {

    PrivateLinkResourceImpl(PrivateLinkResourceInner innerObject) {
        super(innerObject);
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public String groupId() {
        return inner().groupId();
    }

    @Override
    public List<String> requiredMembers() {
        return inner().requiredMembers();
    }
}
