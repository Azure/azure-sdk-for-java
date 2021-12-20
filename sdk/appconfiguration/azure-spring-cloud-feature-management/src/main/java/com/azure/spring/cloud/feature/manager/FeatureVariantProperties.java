// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds information on Feature Management properties and can check if a given feature is enabled.
 */
@ConfigurationProperties(prefix = "feature-variants")
public class FeatureVariantProperties extends HashMap<String, Object> {

    private static final long serialVersionUID = -1132599128116263392L;

    /**
     * Returns the names of all feature variants
     *
     * @return a set of all feature variant names
     */
    public Set<String> getAllVariantNames() {
        return Collections.unmodifiableSet(this.keySet());
    }
    

}
