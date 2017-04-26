/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import java.util.Collection;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;
import rx.Observable;

/**
 * Provides access to deleting multiple resource from Azure, identifying them by their IDs.
 * <p>
 * (Note this interface is not intended to be implemented by user code.)
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true, MethodConversionType = MethodConversion.OnlyMethod)
@Beta
public interface SupportsBatchDeletion {
    /**
     * Deletes the specified resources from Azure asynchronously and in parallel.
     * @param ids resource IDs of the resources to be deleted
     * @return an observable from which all of the successfully deleted resources can be observed
     */
    @Beta
    Observable<String> deleteByIdsAsync(Collection<String> ids);

    /**
     * Deletes the specified resources from Azure asynchronously and in parallel.
     * @param ids resource IDs of the resources to be deleted
     * @return an observable from which all of the successfully deleted resources can be observed
     */
    @Beta
    Observable<String> deleteByIdsAsync(String...ids);

    /**
     * Deletes the specified resources from Azure.
     * @param ids resource IDs of the resources to be deleted
     */
    @Beta
    void deleteByIds(Collection<String> ids);

    /**
     * Deletes the specified resources from Azure.
     * @param ids resource IDs of the resources to be deleted
     */
    @Beta
    void deleteByIds(String...ids);
}
