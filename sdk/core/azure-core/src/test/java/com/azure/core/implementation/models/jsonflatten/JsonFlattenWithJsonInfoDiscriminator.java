// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = JsonFlattenWithJsonInfoDiscriminator.class)
@JsonTypeName("JsonFlattenWithJsonInfoDiscriminator")
@Fluent
public final class JsonFlattenWithJsonInfoDiscriminator {
    @JsonFlatten
    @JsonProperty("jsonflatten.discriminator")
    private String jsonFlattenDiscriminator;

    public JsonFlattenWithJsonInfoDiscriminator setJsonFlattenDiscriminator(String jsonFlattenDiscriminator) {
        this.jsonFlattenDiscriminator = jsonFlattenDiscriminator;
        return this;
    }

    public String getJsonFlattenDiscriminator() {
        return jsonFlattenDiscriminator;
    }
}
