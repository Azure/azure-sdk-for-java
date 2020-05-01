/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 *  Interface that defines operation for registering and fetching schemas and schema information to and from a
 *  schema registry store.
 */
public interface SchemaRegistryClient {
    /**
     * Loads function for a given serialization format that can parse the registry-stored schema string into
     * usable schema object.
     * @param serializationType tag used by schema registry store to identify schema serialization type, e.g. "avro"
     * @param parseMethod function to parse string into usable schema object
     */
    public void loadSchemaParser(String serializationType, Function<String, ?> parseMethod);

    /**
     * Registers a schema against backing schema registry store.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType string representation of serialization format type
     * @return SRObject containing information regarding registered schema.
     * @throws IOException
     * @throws SchemaRegistryClientException if registration operation fails
     */
    public SRObject<?> register(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException;

    /**
     * Fetches schema specified by the GUID.
     *
     * GUID can be assumed to be unique within a schema registry store.
     *
     * @param schemaGuid GUID reference to specific schema within configured schema registry store.
     * @return SRObject containing information regarding matching schema.
     * @throws IOException
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    public SRObject<?> getSchemaByGuid(String schemaGuid) throws IOException, SchemaRegistryClientException;

    /**
     * Fetches schema GUID given schema group, name, string representation, and serialization type
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString String representation of schema
     * @param serializationType String representation of serialization format type
     * @return SRObject containing information regarding requested schema.
     * @throws IOException
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    public String getGuid(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException;

    /**
     * Not currently implemented.
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param version
     * @return GUID of delete schema
     * @throws IOException
     * @throws SchemaRegistryClientException
     */
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version)
            throws IOException, SchemaRegistryClientException;

    /**
     * Not currently implemented.
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return GUID of deleted schema
     * @throws IOException
     * @throws SchemaRegistryClientException
     */
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName)
            throws IOException, SchemaRegistryClientException;

    /**
     * Not currently implemented.
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return list of GUID references to deleted schemas
     * @throws IOException
     * @throws SchemaRegistryClientException
     */
    public List<String> deleteSchema(String schemaGroup, String schemaName) throws IOException, SchemaRegistryClientException;
}
