// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import java.util.Objects;


/**
 * A transformer interface that is used to serialize/deserialize objects from the way they are stored in our persistent
 * storage to a representation which is used in the application.
 */
public interface DocumentTransformer<T, V> {

    /**
     * Convert an object from its application representation (e.g. RecordTemplate, SpecificRecord etc.) to
     * its storage representation (e.g. JSON String, Avro Binary etc.)
     *
     * @param object Application layer object representation
     * @return serialized storage layer representation of the application object
     */
    V serialize(T object);

    /**
     * Convert an object from its storage representation (e.g. JSON String, Avro Binary etc.) to
     * its application representation (e.g. RecordTemplate, SpecificRecord etc.)
     *
     * @param serializedObject storage representation object
     * @return Application layer object representation
     */
    T deserialize(V serializedObject);

    /**
     * Returns a composed transformer that first applies this transformer to
     * the input object, and then applies the {@code after} transformer
     * to the result. If evaluation of either transformer throws an exception,
     * it is relayed to the caller of the composed transformer.
     *
     * @param <W> the type of output of the {@code after} transformer, and of the
     *           composed transformer
     * @param after the transformer to apply after this transformer is applied
     * @return a composed transformer that first applies this transformer and then
     *         applies the {@code after} transformer
     * @throws NullPointerException if after is null
     *
     */
    default <W> DocumentTransformer<T, W> andThen(final DocumentTransformer<V, W> after) {
        Objects.requireNonNull(after);

        return new DocumentTransformer<T, W>() {
            @Override
            public W serialize(T object) {
                V serialized = DocumentTransformer.this.serialize(object);
                return after.serialize(serialized);
            }

            @Override
            public T deserialize(W serializedObject) {
                V deserialized = after.deserialize(serializedObject);
                return DocumentTransformer.this.deserialize(deserialized);
            }
        };
    }
}
