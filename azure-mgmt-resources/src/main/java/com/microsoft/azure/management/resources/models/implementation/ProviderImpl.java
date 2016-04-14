package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.models.Provider;
import com.microsoft.azure.management.resources.models.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.models.implementation.api.ProviderResourceType;

import java.util.List;

public class ProviderImpl extends
        IndexableWrapperImpl<ProviderInner>
        implements
        Provider {

    public ProviderImpl(ProviderInner provider) {
        super(provider.id(), provider);
    }

    /***********************************************************
     * Getters
     ***********************************************************/

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
