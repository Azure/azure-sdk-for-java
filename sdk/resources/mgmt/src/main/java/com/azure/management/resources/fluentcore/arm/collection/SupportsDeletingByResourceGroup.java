/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.collection;

import reactor.core.publisher.Mono;

/**
 * Provides access to deleting a resource from Azure, identifying it by its name and its resource group.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 */
public interface SupportsDeletingByResourceGroup {
    /**
     * Deletes a resource from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name              the name of the resource
     */
    void deleteByResourceGroup(String resourceGroupName, String name);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name              the name of the resource
     * @return a representation of the deferred computation of this call
     */
    Mono<?> deleteByResourceGroupAsync(String resourceGroupName, String name);
}
