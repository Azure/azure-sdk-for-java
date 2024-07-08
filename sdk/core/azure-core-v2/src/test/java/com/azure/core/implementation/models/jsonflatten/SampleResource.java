// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.v2.annotation.Fluent;
import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
@JsonFlatten
public class SampleResource {

    @JsonProperty(value = "properties.name")
    private String namePropertiesName;

    @JsonProperty(value = "properties.registrationTtl")
    private String registrationTtl;

    public SampleResource withNamePropertiesName(String namePropertiesName) {
        this.namePropertiesName = namePropertiesName;
        return this;
    }

    public SampleResource withRegistrationTtl(String registrationTtl) {
        this.registrationTtl = registrationTtl;
        return this;
    }

    public String getNamePropertiesName() {
        return namePropertiesName;
    }

    public String getRegistrationTtl() {
        return registrationTtl;
    }
}
