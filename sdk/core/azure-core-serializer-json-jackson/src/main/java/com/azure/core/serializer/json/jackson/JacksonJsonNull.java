// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNull;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Jackson specific implementation of {@link JsonNull}.
 */
public final class JacksonJsonNull implements JsonNull {
    private final NullNode nullNode;

    public JacksonJsonNull() {
        this.nullNode = NullNode.getInstance();
    }

    public JacksonJsonNull(NullNode nullNode) {
        this.nullNode = nullNode;
    }

    NullNode getNullNode() {
        return nullNode;
    }
}
