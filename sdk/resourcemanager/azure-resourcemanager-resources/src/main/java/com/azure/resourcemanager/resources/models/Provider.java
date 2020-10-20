// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluent.models.ProviderInner;

import java.util.List;

/**
 * An immutable client-side representation of an Azure resource provider.
 */
@Fluent
public interface Provider extends
        Indexable,
        HasInnerModel<ProviderInner> {

    /**
     * @return the namespace of the provider
     */
    String namespace();

    /**
     * @return the registration state of the provider, indicating whether this
     * resource provider is registered in the current subscription
     */
    String registrationState();

    /**
     * @return the list of provider resource types
     */
    List<ProviderResourceType> resourceTypes();
}
