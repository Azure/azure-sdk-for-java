/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

/**
 * Defines the base interface for all resources that support deleting a resource
 * from the resource group it belongs to.
 */
public interface SupportsDeletingByGroup {
    /**
     * Delete a resource from a specific group.
	 *
     * @param groupName The group the resource is part of
     * @param name The name of the resource within that group
     * @throws Exception error to throw
     */
    void delete(String groupName, String name) throws Exception;
}
