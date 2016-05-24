/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.CloudException;

import java.io.IOException;

/**
 * Provides access to getting a specific Azure resource based on its ID
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsGetting<T> {
	/**
	 * Gets the information about a resource from Azure based on the resource ID
	 * <p>
	 * @param id the full ID of the resource
	 * @return an immutable representation of the resource
	 * @throws CloudException
	 * @throws IOException
	 */
	T get(String id) throws CloudException, IOException;
}
