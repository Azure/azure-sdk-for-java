// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Context {
    @JsonGetter
    public CurrentFeatures getFeatures() {
        return currentFeatures;
    }

    @JsonSetter
    public Context setCurrentFeatures(CurrentFeatures currentFeatures) {
        this.currentFeatures = currentFeatures;
        return this;
    }

    @JsonProperty
    CurrentFeatures currentFeatures;
}
