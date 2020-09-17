// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ProviderResourceType;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluent.inner.ProviderInner;

import java.util.List;

/**
 * The implementation of {@link Provider}.
 */
final class ProviderImpl extends
        IndexableWrapperImpl<ProviderInner>
        implements
        Provider {

    ProviderImpl(ProviderInner provider) {
        super(provider);
    }

    @Override
    public String namespace() {
        return inner().namespace();
    }

    @Override
    public String registrationState() {
        return inner().registrationState();
    }

    @Override
    public List<ProviderResourceType> resourceTypes() {
        return inner().resourceTypes();
    }
}
