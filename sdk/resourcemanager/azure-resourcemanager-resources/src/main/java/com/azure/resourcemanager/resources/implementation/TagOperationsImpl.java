// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.models.TagOperations;
import com.azure.resourcemanager.resources.models.Tags;
import com.azure.resourcemanager.resources.models.TagsPatchOperation;
import com.azure.resourcemanager.resources.models.TagsPatchResource;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TagOperationsImpl implements TagOperations {

    private final ResourceManager myManager;

    public TagOperationsImpl(ResourceManager resourceManager) {
        this.myManager = resourceManager;
    }

    @Override
    public Map<String, String> updateTags(Resource resource, Map<String, String> tags) {
        return this.updateTagsAsync(resource, tags).block();
    }

    @Override
    public Map<String, String> updateTags(String resourceId, Map<String, String> tags) {
        return this.updateTagsAsync(resourceId, tags).block();
    }

    @Override
    public Mono<Map<String, String>> updateTagsAsync(Resource resource, Map<String, String> tags) {
        return this.updateTagsAsync(Objects.requireNonNull(resource).id(), tags);
    }

    @Override
    public Mono<Map<String, String>> updateTagsAsync(String resourceId, Map<String, String> tags) {
        TagsPatchResource parameters = new TagsPatchResource()
            .withOperation(TagsPatchOperation.REPLACE)
            .withProperties(new Tags().withTags(new TreeMap<>(tags)));
        return this.manager().serviceClient().getTagOperations()
            .updateAtScopeAsync(resourceId, parameters)
            .map(inner -> inner.properties() == null ? null : inner.properties().tags());
    }

    @Override
    public ResourceManager manager() {
        return this.myManager;
    }
}
