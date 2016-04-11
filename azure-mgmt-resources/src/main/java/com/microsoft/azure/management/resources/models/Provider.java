package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.models.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.models.implementation.api.ProviderResourceType;

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
