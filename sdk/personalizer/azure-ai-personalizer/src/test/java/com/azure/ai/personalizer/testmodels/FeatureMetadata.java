// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class FeatureMetadata {
    @JsonGetter
    public String getFeatureType() {
        return featureType;
    }

    @JsonSetter
    public FeatureMetadata setFeatureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    @JsonProperty
    String featureType;
}
