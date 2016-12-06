/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * Provides access to getting a specific Azure resource based on its resource group and parent.
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = LangDefinition.MethodConversion.OnlyMethod)
public interface SupportsDeletingByParent {
    /**
     * Deletes a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param parentName the name of parent resource.
     * @param name The name of the resource
     */
    void deleteByParent(String groupName, String parentName, String name);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param parentName the name of parent resource.
     * @param name The name of the resource
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceCall<Void> deleteByParentAsync(String groupName, String parentName, String name, ServiceCallback<Void> callback);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its name and its resource group.
     *
     * @param groupName The group the resource is part of
     * @param parentName the name of parent resource.
     * @param name The name of the resource
     * @return an observable to the request
     */
    Observable<Void> deleteByParentAsync(String groupName, String parentName, String name);
}
