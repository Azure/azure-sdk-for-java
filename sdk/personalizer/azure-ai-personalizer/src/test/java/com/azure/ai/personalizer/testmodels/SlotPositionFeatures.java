// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class SlotPositionFeatures {
    @JsonGetter
    public String getSize() {
        return size;
    }

    @JsonSetter
    public SlotPositionFeatures setSize(String size) {
        this.size = size;
        return this;
    }

    @JsonGetter
    public String getPosition() {
        return position;
    }

    @JsonSetter
    public SlotPositionFeatures setPosition(String position) {
        this.position = position;
        return this;
    }

    @JsonProperty
    String size;
    @JsonProperty
    String position;
}
