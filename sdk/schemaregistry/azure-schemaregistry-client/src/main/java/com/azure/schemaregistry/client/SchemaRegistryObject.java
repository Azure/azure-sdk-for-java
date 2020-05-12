/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.schemaregistry.client.rest.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Stores all relevant information returned from SchemaRegistryClient layer.
 *
 * @param <T> is derived from the parser function that is passed in the constructor.
 */
public class SchemaRegistryObject<T> {
    private final Logger log;

    public final String schemaGuid;
    public final String serializationType;
    private final Charset encoding = RestService.SERVICE_CHARSET;

    private Function<String, T> parseMethod;

    public byte[] schemaByteArray;
    private T deserialized;

    public SchemaRegistryObject(
        String schemaGuid,
        String serializationType,
        byte[] schemaByteArray,
        Function<String, T> parseMethod) {
        this.schemaGuid = schemaGuid;
        this.serializationType = serializationType;
        this.schemaByteArray = schemaByteArray;
        this.deserialized = null;
        this.parseMethod = parseMethod;
        this.log = LoggerFactory.getLogger(this.schemaGuid);
    }

    /**
     *  @return schema object of type T, deserialized using stored schema parser method.
     */
    public T deserialize() {
        if (parseMethod == null) {
            log.warn(String.format("No loaded parser for %s format. Schema guid: %s", this.serializationType, this.schemaGuid));
            return null;
        }

        if (this.deserialized == null) {
            log.debug(String.format("Deserializing schema %s", new String(this.schemaByteArray, encoding)));
            this.deserialized = parseMethod.apply(new String(this.schemaByteArray, encoding));
        }
        return deserialized;
    }
}
