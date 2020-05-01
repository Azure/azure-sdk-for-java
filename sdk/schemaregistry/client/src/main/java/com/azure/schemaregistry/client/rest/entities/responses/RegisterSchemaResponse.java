/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client.rest.entities.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class RegisterSchemaResponse {

    private String schemaGuid;

    public static RegisterSchemaResponse fromJson(String json) throws IOException {
        return new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .readValue(json, RegisterSchemaResponse.class);
    }
    
    public String toJson() throws IOException {
        return new ObjectMapper().writeValueAsString(this);
    }

    @JsonProperty("Id")
    public String getId() {
        return this.schemaGuid;
    }

    @JsonProperty("Id")
    public void setId(String schemaGuid) {
        this.schemaGuid = schemaGuid;
    }
}
