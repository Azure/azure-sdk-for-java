/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

/**
 * Base class for resources that can be refreshed to get the latest state.
 *
 * @param <T> the fluent type of the resource
 */
public interface Refreshable<T> {
    /**
     * Refreshes the resource to sync with Azure.
     *
     * @return the refreshed resource
     * @throws Exception exceptions thrown from Azure
     */
    T refresh() throws Exception;
}
