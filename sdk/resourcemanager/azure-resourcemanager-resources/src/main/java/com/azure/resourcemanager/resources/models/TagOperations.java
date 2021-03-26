// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Entry point to tag management API.
 */
public interface TagOperations extends HasManager<ResourceManager> {

    /**
     * Updates the tags of the Azure resource.
     *
     * @param resource the Azure resource to have its tags updated
     * @param tags the tags
     * @return the updated tags
     */
    Map<String, String> updateTags(Resource resource, Map<String, String> tags);

    /**
     * Updates the tags of the Azure resource.
     *
     * @param resourceId the ID of the Azure resource to have its tags updated
     * @param tags the tags
     * @return the updated tags
     */
    Map<String, String> updateTags(String resourceId, Map<String, String> tags);

    /**
     * Updates the tags of the Azure resource.
     *
     * @param resource the Azure resource to have its tags updated
     * @param tags the tags
     * @return the updated tags
     */
    Mono<Map<String, String>> updateTagsAsync(Resource resource, Map<String, String> tags);

    /**
     * Updates the tags of the Azure resource.
     *
     * @param resourceId the ID of the Azure resource to have its tags updated
     * @param tags the tags
     * @return the updated tags
     */
    Mono<Map<String, String>> updateTagsAsync(String resourceId, Map<String, String> tags);
}
