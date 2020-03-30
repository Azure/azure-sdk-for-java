/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.Provider;
import com.azure.management.resources.ProviderResourceType;
import com.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.management.resources.models.ProviderInner;

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
        return inner().getNamespace();
    }

    @Override
    public String registrationState() {
        return inner().getRegistrationState();
    }

    @Override
    public List<ProviderResourceType> resourceTypes() {
        return inner().getResourceTypes();
    }
}
