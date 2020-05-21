// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.client;

import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

/**
 * Interface that defines operation for registering and fetching schemas and schema information to and from a
 * schema registry store.
 */
public interface SchemaRegistryClient {

    /**
     * Encoding used by registry client implementation.
     * @return encoding for registry client implementation
     */
    Charset getEncoding();

    /**
     * Loads function for a given serialization format that can parse the registry-stored schema string into
     * usable schema object.
     *
     * @param serializationType tag used by schema registry store to identify schema serialization type, e.g. "avro"
     * @param parseMethod function to parse string into usable schema object
     */
    void loadSchemaParser(String serializationType, Function<String, Object> parseMethod);

    /**
     * Registers a schema against backing schema registry store.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType string representation of serialization format type
     * @return SchemaRegistryObject containing information regarding registered schema.
     * @throws SchemaRegistryClientException if registration operation fails
     */
    SchemaRegistryObject register(String schemaGroup, String schemaName, String schemaString, String serializationType)
        throws SchemaRegistryClientException;

    /**
     * Fetches schema specified by the GUID.
     * <p>
     * GUID can be assumed to be unique within a schema registry store.
     *
     * @param schemaGuid GUID reference to specific schema within configured schema registry store.
     * @return SchemaRegistryObject containing information regarding matching schema.
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    SchemaRegistryObject getSchemaByGuid(String schemaGuid) throws SchemaRegistryClientException;

    /**
     * Fetches schema GUID given schema group, name, string representation, and serialization type
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString String representation of schema
     * @param serializationType String representation of serialization format type
     * @return SchemaRegistryObject containing information regarding requested schema.
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    String getSchemaId(String schemaGroup, String schemaName, String schemaString, String serializationType)
        throws SchemaRegistryClientException;

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param version schema version
     * @return GUID of delete schema
     * @throws SchemaRegistryClientException deletion operation failed
     */
    String deleteSchemaVersion(String schemaGroup, String schemaName, int version)
        throws SchemaRegistryClientException;

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return GUID of deleted schema
     * @throws SchemaRegistryClientException deletion operation failed
     */
    String deleteLatestSchemaVersion(String schemaGroup, String schemaName) throws SchemaRegistryClientException;

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return list of GUID references to deleted schemas
     * @throws SchemaRegistryClientException deletion operation failed
     */
    List<String> deleteSchema(String schemaGroup, String schemaName) throws SchemaRegistryClientException;
}
