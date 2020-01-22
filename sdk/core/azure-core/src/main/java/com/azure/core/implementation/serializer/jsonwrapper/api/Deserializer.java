// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.api;

import com.azure.core.implementation.serializer.jsonwrapper.JsonWrapper;
/**
 * A custom deserialization API for JSON content that will return a specific Java type. A Deserializer is registered on
 * a specific instance of a {@link JsonApi} after it is retrieved from the {@link JsonWrapper}
 * root class.
 *
 * For example, here is a custom deserializer that converts the JSON string {@code {"valueInt":7,"valueString":"seven"}}
 * into an instance of 'Foo':
 *
 * <pre>
 * jsonDeserializer.registerCustomDeserializer(new Deserializer&lt;Foo&gt;(Foo.class) {
 *     {@literal @}Override public Foo deserialize(Node node) {
 *         int intValue = node.get("valueInt").asInt();
 *         String stringValue = node.get("valueString").asString();
 *         return new Foo(intValue, stringValue);
 *     }
 * });
 * Foo targetObject = jsonDeserializer.readString(json, Foo.class);
 * </pre>
 *
 * @param <T> The type of the element that should be return by the {@link #deserialize(Node)} method following a
 * successful deserialization.
 */
public abstract class Deserializer<T> {
    private final Class<T> rawType;

    /**
     * Constructor
     * @param rawType class type
     */
    protected Deserializer(Class<T> rawType) {
        this.rawType = rawType;
    }

    /**
     * Get raw type
     * @return class type
     */
    public Class<T> getRawType() {
        return rawType;
    }

    /**
     * Deserialize body into object of type T
     * @param node node
     * @return object of type T
     */
    public abstract T deserialize(Node node);
}

