// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;

/**
 * This class contains static helper methods to validate types for changefeed models.
 */
class ChangefeedTypeValidator {

    /**
     * Determines whether or not the object is null in the Avro sense.
     */
    static boolean isNull(Object o) {
        return o == null || o instanceof AvroNullSchema.Null;
    }

    /**
     * Casts the object to the specified class.
     */
    static <T> T nullOr(String name, Object o, Class<T> clazz) {
        if (isNull(o)) {
            return null;
        }
        AvroSchema.checkType(name, o, clazz);
        return clazz.cast(o);
    }
}
