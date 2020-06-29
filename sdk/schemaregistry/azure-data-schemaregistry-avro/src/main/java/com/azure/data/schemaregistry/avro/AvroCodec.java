// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.Codec;
import org.apache.avro.Schema;

/**
 * Base Codec class for Avro encoder and decoder implementations
 */
abstract class AvroCodec implements Codec {
    @Override
    public String schemaType() {
        return "avro";
    }

    /**
     * @param schemaString string representation of schema
     * @return avro schema
     */
    @Override
    public Schema parseSchemaString(String schemaString) {
        return (new Schema.Parser()).parse(schemaString);
    }
}
