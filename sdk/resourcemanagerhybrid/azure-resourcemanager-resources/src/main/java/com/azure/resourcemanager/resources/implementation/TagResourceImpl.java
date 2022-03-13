// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.fluent.models.TagsResourceInner;
import com.azure.resourcemanager.resources.models.TagResource;

import java.util.Collections;
import java.util.Map;

final class TagResourceImpl implements TagResource {

    private final TagsResourceInner innerObject;

    TagResourceImpl(TagsResourceInner inner) {
        this.innerObject = inner;
    }

    @Override
    public TagsResourceInner innerModel() {
        return this.innerObject;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public Map<String, String> tags() {
        return this.innerModel().properties() == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(this.innerModel().properties().tags());
    }
}
