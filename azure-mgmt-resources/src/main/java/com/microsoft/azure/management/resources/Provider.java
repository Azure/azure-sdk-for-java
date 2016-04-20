package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.implementation.api.ProviderResourceType;

import java.util.List;

public interface Provider extends
        Indexable,
        Wrapper<ProviderInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String namespace();
    String registrationState();
    List<ProviderResourceType> resourceTypes();
}
