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
 * An immutable client-side representation of an Azure resource provider.
 */
public interface Provider extends
        Indexable,
        Wrapper<ProviderInner> {

    /**
     * @return the namespace of the provider
     */
    String namespace();

    /**
     * @return the registration state of the provider, indicating whether this
     *         resource provider is registered in the current subscription
     */
    String registrationState();

    /**
     * @return the list of provider resource types
     */
    List<ProviderResourceType> resourceTypes();
}
