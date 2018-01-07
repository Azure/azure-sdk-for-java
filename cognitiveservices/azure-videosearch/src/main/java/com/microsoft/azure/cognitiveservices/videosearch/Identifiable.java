/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Defines the identity of a resource.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Identifiable.class)
@JsonTypeName("Identifiable")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Response", value = Response.class)
})
public class Identifiable extends ResponseBase {
    /**
     * A String identifier.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

}
