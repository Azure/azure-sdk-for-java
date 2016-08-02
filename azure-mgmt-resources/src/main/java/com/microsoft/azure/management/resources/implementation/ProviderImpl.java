/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ProviderResourceType;

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
