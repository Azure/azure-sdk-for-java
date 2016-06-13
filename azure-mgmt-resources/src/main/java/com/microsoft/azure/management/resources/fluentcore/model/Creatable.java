/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

/**
 * The final stage of the resource definition, at which it can be create, using {@link #create()}.
 *
 * @param <T> the fluent type of the resource to be created
 */
public interface Creatable<T> extends Indexable {
    /**
     * Execute the create request.
     *
     * @return the create resource
     * @throws Exception exceptions from Azure
     */
    T create() throws Exception;
}