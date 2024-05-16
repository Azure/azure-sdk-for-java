// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

// Not sure why but the ordering of serialization changes between Java 8 and 17
// Using azure-json will fix this as it strongly enforces order.
@JsonPropertyOrder({"isMobile", "isWindows", "windows", "mobile"})
public class Device {
    @JsonGetter
    public boolean isMobile() {
        return isMobile;
    }

    @JsonSetter
    public Device setMobile(boolean mobile) {
        isMobile = mobile;
        return this;
    }

    @JsonGetter
    public boolean isWindows() {
        return isWindows;
    }

    @JsonSetter
    public Device setWindows(boolean windows) {
        isWindows = windows;
        return this;
    }

    @JsonProperty
    boolean isMobile;
    @JsonProperty
    boolean isWindows;
}
