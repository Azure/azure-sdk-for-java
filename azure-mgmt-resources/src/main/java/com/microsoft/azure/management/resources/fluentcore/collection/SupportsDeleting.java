/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

/**
 * Provides access to deleting a resource from Azure, identifying it by its resource ID.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsDeleting {
    /**
     * Deletes a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @throws Exception exceptions thrown from Azure
     */
    void delete(String id) throws Exception;
}
