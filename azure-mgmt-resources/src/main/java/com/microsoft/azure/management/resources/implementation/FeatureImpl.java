package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.Feature;
import com.microsoft.azure.management.resources.implementation.api.FeatureResultInner;

public class FeatureImpl extends
        IndexableWrapperImpl<FeatureResultInner>
        implements
        Feature {

    public FeatureImpl(FeatureResultInner innerModel) {
        super(innerModel.id(), innerModel);
    }

    /***********************************************************
     * Getters
     ***********************************************************/

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public String state() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().state();
    }
}
