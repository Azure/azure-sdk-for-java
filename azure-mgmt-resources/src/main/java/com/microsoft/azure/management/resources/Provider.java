/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.implementation.api.ProviderResourceType;

import java.util.List;

/**
 * Defines an interface for accessing information of a resource provider in Azure.
 */
public interface Provider extends
        Indexable,
        Wrapper<ProviderInner> {

    /**
     * Get the namespace of the provider.
     *
     * @return the namespace of the provider.
     */
    String namespace();

    /**
     * Get the registration state of the provider.
     *
     * @return the registration state of the provider.
     */
    String registrationState();

    /**
     * Get the collection of provider resource types.
     *
     * @return the collection of provider resource types.
     */
    List<ProviderResourceType> resourceTypes();
}
