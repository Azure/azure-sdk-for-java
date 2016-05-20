package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.FeatureResultInner;

/**
 * Defines an interface for accessing a feature in Azure.
 */
public interface Feature extends
        Indexable,
        Wrapper<FeatureResultInner> {
    /**
     * Get the name of the feature.
     *
     * @return the name of the feature.
     */
    String name();

    /**
     * Get the type of the feature.
     *
     * @return the type of the feature.
     */
    String type();

    /**
     * Get the state of the previewed feature.
     *
     * @return the state of the previewed feature.
     */
    String state();
}
