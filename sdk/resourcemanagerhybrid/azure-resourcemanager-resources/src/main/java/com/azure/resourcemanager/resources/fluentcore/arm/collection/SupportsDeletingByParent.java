// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to getting a specific Azure resource based on its resource group and parent.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 */
public interface SupportsDeletingByParent {
    /**
     * Deletes a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param parentName the name of parent resource.
     * @param name The name of the resource
     */
    void deleteByParent(String groupName, String parentName, String name);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param parentName the name of parent resource.
     * @param name The name of the resource
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteByParentAsync(String groupName, String parentName, String name);
}
