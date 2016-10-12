/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.FeatureResultInner;

/**
 * An immutable client-side representation of an Azure feature.
 */
@Fluent
public interface Feature extends
        Indexable,
        Wrapper<FeatureResultInner>,
        HasName {

    /**
     * @return the type of the feature
     */
    String type();

    /**
     * @return the state of the previewed feature
     */
    String state();
}
