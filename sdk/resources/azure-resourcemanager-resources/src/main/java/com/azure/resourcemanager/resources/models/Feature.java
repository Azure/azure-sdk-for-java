// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluent.models.FeatureResultInner;

/**
 * An immutable client-side representation of an Azure feature.
 */
@Fluent
public interface Feature extends Indexable, HasId, HasInnerModel<FeatureResultInner>, HasName {

    /**
     * Gets the type of the feature.
     *
     * @return the type of the feature
     */
    String type();

    /**
     * Gets the state of the previewed feature.
     *
     * @return the state of the previewed feature
     */
    String state();

    /**
     * Gets the resource provider namespace of the feature.
     *
     * @return the resource provider namespace of the feature
     */
    String resourceProviderName();

    /**
     * Gets the name of the feature.
     *
     * @return the name of the feature
     */
    String featureName();
}
