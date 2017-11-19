/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The MovieTheater model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("MovieTheater")
public class MovieTheater extends EntertainmentBusiness {
    /**
     * The screenCount property.
     */
    @JsonProperty(value = "screenCount", access = JsonProperty.Access.WRITE_ONLY)
    private Integer screenCount;

    /**
     * Get the screenCount value.
     *
     * @return the screenCount value
     */
    public Integer screenCount() {
        return this.screenCount;
    }

}
