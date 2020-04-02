/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.models.FeatureResultInner;

/**
 * An immutable client-side representation of an Azure feature.
 */
@Fluent
public interface Feature extends
        Indexable,
        HasInner<FeatureResultInner>,
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
