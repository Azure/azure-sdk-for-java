/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;


/**
 * Provides access to updating a specific Azure resource, based on its resource ID
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
public interface SupportsUpdating<T> {
	
	/**
	 * Begins an update definition for an existing resource.
	 * <p>
	 * This is the beginning of the builder pattern used to modify top level resources 
	 * in Azure. The final method completing the update definition and starting the actual resource update 
	 * process in Azure is {@link Appliable#apply()}.
	 * <p>
	 * Note that the {@link Appliable#apply()} method is available at any stage of the update definition 
	 * because all the updatable settings are generally optional. 
	 * <p>Settings typically begin 
	 * with the word "with", for example: <code>.withRegion()</code> and return the update definition itself, to enable chaining 
	 * in the fluent interface style.
	 * @param id the resource id of the resource to update. Remember to call {@see Appliable#apply()} for the changes
	 * to go into effect on Azure.
	 * @return the update definition itself
	 */
	T update(String id);
}
