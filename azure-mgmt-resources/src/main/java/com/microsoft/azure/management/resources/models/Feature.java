package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.FeatureResultInner;

public interface Feature extends
        Indexable,
        Wrapper<FeatureResultInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String name();
    String type();
    String state();
}
