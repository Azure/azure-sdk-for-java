/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import rx.Observable;

/**
 * Provides access to getting a specific resource based on its name.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 * @param <T> the type of the resource collection
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = LangDefinition.MethodConversion.OnlyMethod)
public interface SupportsGettingByNameAsync<T> extends SupportsGettingByName<T> {
    /**
     * Gets the information about a resource based on the resource name.
     *
     * @param name the name of the resource. (Note, this is not the resource ID.)
     * @return an immutable representation of the resource
     */
    Observable<T> getByNameAsync(String name);
}