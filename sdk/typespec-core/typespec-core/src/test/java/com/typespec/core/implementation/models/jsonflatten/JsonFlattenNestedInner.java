// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.models.jsonflatten;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
@JsonFlatten
public class JsonFlattenNestedInner {

    @JsonFlatten
    @JsonProperty(value = "identity")
    private VirtualMachineIdentity identity;

    public VirtualMachineIdentity getIdentity() {
        return identity;
    }

    public JsonFlattenNestedInner setIdentity(VirtualMachineIdentity identity) {
        this.identity = identity;
        return this;
    }
}
