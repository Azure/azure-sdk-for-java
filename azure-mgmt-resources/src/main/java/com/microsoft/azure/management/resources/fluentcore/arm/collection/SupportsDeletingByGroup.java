/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Completable;

/**
 * Provides access to deleting a resource from Azure, identifying it by its name and its resource group.
 *
 * (Note: this interface is not intended to be implemented by user code)
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = MethodConversion.OnlyMethod)
public interface SupportsDeletingByGroup {
    /**
     * Deletes a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param name The name of the resource
     */
    void deleteByGroup(String groupName, String name);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param name The name of the resource
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceCall<Void> deleteByGroupAsync(String groupName, String name, ServiceCallback<Void> callback);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param name The name of the resource
     * @return a completable indicates completion or exception of the request
     */
    Completable deleteByGroupAsync(String groupName, String name);
}
