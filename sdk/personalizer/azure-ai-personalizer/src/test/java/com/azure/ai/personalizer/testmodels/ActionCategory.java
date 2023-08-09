// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ActionCategory {
    @JsonGetter
    public String getMostWatchedByAge() {
        return mostWatchedByAge;
    }

    @JsonSetter
    public ActionCategory setMostWatchedByAge(String mostWatchedByAge) {
        this.mostWatchedByAge = mostWatchedByAge;
        return this;
    }

    @JsonProperty
    String mostWatchedByAge;
}
