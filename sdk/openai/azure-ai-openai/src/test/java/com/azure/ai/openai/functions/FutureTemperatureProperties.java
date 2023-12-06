// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.functions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FutureTemperatureProperties {
    @JsonProperty
    private StringField date = new StringField("The date of the weather forecast.");

    @JsonProperty
    private StringField locationName = new StringField("The name of the location to forecast the weather for.");

    @JsonProperty
    private StringField unit = new StringField("The unit of measurement for the temperature.");
}
