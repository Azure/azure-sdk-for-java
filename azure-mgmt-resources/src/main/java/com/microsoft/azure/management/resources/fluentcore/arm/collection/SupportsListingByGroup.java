/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;

import java.io.IOException;

/**
 * Provides access to listing Azure resources of a specific type in a specific resource group
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
*/
public interface SupportsListingByGroup<T> {
	/** Lists resources of the specified type in the specified resource group
	 * @param groupName the name of the resource group to list the resources from
	 * @return list of resources
	 * @throws Exception
	 */
	PagedList<T> list(String groupName) throws CloudException, IOException;
}
