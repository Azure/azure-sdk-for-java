/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.utils.WrappedList;

import java.io.IOException;
import java.util.List;

/**
 * This class transforms a list of resource IDs into a list of resources,
 * lazy loaded from the {@link Loader#load} method.
 *
 * @param <ResourceT> the type of the resource
 */
public class ResourceLazyList<ResourceT> extends WrappedList<String, ResourceT> {
    /**
     * Creates an instance of this lazy list from a list of IDs and a lazy loader.
     *
     * @param resourceIds the list of resource IDs
     * @param loader the loader for loading resources
     */
    public ResourceLazyList(List<String> resourceIds, final Loader<ResourceT> loader)  {
        super(resourceIds, new Transformer<String, ResourceT>() {
            @Override
            public ResourceT transform(String id) {
                try {
                    return loader.load(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
    }

    /**
     * Implement this interface to define a lazy loader.
     *
     * @param <ResourceT> the type of the resource.
     */
    public interface Loader<ResourceT> {
        /**
         * Override this method to define how to load a real resource from a resource name.
         *
         * @param resourceGroupName the resource group the resource is in
         * @param resourceName the name of the resource to load
         * @return the "stuffed" resource
         * @throws CloudException exceptions thrown from the cloud.
         * @throws IOException exceptions thrown from serialization/deserialization.
         */
        ResourceT load(String resourceGroupName, String resourceName) throws CloudException, IOException;
    }
}
