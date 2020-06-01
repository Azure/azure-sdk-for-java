// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNull;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Objects;

/**
 * Jackson specific implementation of {@link JsonNull}.
 */
public final class JacksonJsonNull implements JsonNull {
    private final NullNode nullNode;

    /**
     * Constructs a {@link JsonNull} backed by Jackson {@link NullNode#getInstance()}.
     */
    public JacksonJsonNull() {
        this.nullNode = NullNode.getInstance();
    }

    /**
     * Constructs a {@link JsonNull} backed by the passed Jackson {@link NullNode}.
     *
     * @param nullNode The backing Jackson {@link NullNode}.
     */
    public JacksonJsonNull(NullNode nullNode) {
        this.nullNode = nullNode;
    }

    NullNode getNullNode() {
        return nullNode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof JacksonJsonNull)) {
            return false;
        }

        return Objects.equals(nullNode, ((JacksonJsonNull) obj).nullNode);
    }

    @Override
    public int hashCode() {
        return nullNode.hashCode();
    }
}
