// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = NonEmptyAnimalWithTypeIdContainingDot.class)
@JsonTypeName("NonEmptyAnimalWithTypeIdContainingDot")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Favourite.Pet.TurtleWithTypeIdContainingDot",
        value = TurtleWithTypeIdContainingDot.class)
})
public class NonEmptyAnimalWithTypeIdContainingDot {
    @JsonProperty(value = "age")
    private Integer age;

    public Integer age() {
        return this.age;
    }

    public NonEmptyAnimalWithTypeIdContainingDot withAge(Integer age) {
        this.age = age;
        return this;
    }
}
