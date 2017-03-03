/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import rx.Observable;

/**
 * Base class for resources that can be refreshed to get the latest state.
 *
 * @param <T> the fluent type of the resource
 */
@LangDefinition(ContainerName = "ResourceActions")
public interface Refreshable<T> {
    /**
     * Refreshes the resource to sync with Azure.
     *
     * @return the refreshed resource
     */
    @Method
    T refresh();

    /**
     * Refreshes the resource to sync with Azure.
     *
     * @return the Observable to refreshed resource
     */
    @Method
    Observable<T> refreshAsync();
}
