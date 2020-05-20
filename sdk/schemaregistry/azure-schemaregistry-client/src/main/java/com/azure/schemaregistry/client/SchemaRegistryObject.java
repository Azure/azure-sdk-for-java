/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.core.util.logging.ClientLogger;
import java.util.function.Function;

/**
 * Stores all relevant information returned from SchemaRegistryClient layer.
 */
public class SchemaRegistryObject {
    private final ClientLogger log;

    public final String schemaId;
    public final String serializationType;

    private Function<String, Object> parseMethod;

    public byte[] schemaByteArray;
    private Object deserialized;

    public SchemaRegistryObject(
        String schemaId,
        String serializationType,
        byte[] schemaByteArray,
        Function<String, Object> parseMethod) {
        this.schemaId = schemaId;
        this.serializationType = serializationType;
        this.schemaByteArray = schemaByteArray;
        this.deserialized = null;
        this.parseMethod = parseMethod;
        this.log = new ClientLogger(this.schemaId);
    }

    /**
     *  @return schema object of type T, deserialized using stored schema parser method.
     */
    public Object deserialize() {
        if (parseMethod == null) {
            log.warning(String.format("No loaded parser for %s format. Schema guid: %s", this.serializationType, this.schemaId));
            return null;
        }

        if (this.deserialized == null) {
            log.verbose(String.format("Deserializing schema %s", new String(this.schemaByteArray)));
            this.deserialized = parseMethod.apply(new String(this.schemaByteArray));
        }
        return deserialized;
    }
}
