// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@odata\\.type",
    defaultImpl = AnimalWithTypeIdContainingDot.class)
@JsonTypeName("AnimalWithTypeIdContainingDot")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "#Favourite.Pet.DogWithTypeIdContainingDot",
        value = DogWithTypeIdContainingDot.class),
    @JsonSubTypes.Type(name = "#Favourite.Pet.CatWithTypeIdContainingDot",
        value = CatWithTypeIdContainingDot.class),
    @JsonSubTypes.Type(name = "#Favourite.Pet.RabbitWithTypeIdContainingDot",
        value = RabbitWithTypeIdContainingDot.class)
})
public class AnimalWithTypeIdContainingDot {
}
