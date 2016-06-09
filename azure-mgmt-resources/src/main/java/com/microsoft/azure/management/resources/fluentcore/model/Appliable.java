/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

/**
 * The base interface for all template interfaces that support update operations.
 *
 * @param <T> the type of the resource returned from the update.
 */
public interface Appliable<T> extends Indexable {
    /**
     * Execute the update request.
     *
     * @return the updated resource
     * @throws Exception exceptions from Azure
     */
	T apply() throws Exception;
}
