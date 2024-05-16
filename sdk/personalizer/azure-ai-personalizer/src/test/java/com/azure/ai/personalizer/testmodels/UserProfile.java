// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class UserProfile {
    @JsonGetter
    public String getProfileType() {
        return profileType;
    }

    @JsonSetter
    public UserProfile setProfileType(String profileType) {
        this.profileType = profileType;
        return this;
    }

    @JsonGetter
    public String getLatLong() {
        return latLong;
    }

    @JsonSetter
    public UserProfile setLatLong(String latLong) {
        this.latLong = latLong;
        return this;
    }

    @JsonProperty
    String profileType;
    @JsonProperty
    String latLong;
}
