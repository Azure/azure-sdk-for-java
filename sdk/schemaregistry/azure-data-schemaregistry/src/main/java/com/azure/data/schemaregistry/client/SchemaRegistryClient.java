// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client;

import com.azure.data.schemaregistry.Codec;

import java.nio.charset.Charset;
import java.util.List;

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
     * Loads function for a given schema type that can parse the registry-stored schema string into
     * usable schema object.
     *
     * Any com.azure.data.schemaregistry.ByteEncoder or com.azure.data.schemaregistry.ByteDecoder class will implement
     *  - schemaType(), which specifies schema type, and
     *  - parseSchemaString(), which parses schemas of the specified schema type from String to Object.
     *
     * @param codec Codec class implementation
     */
    void addSchemaParser(Codec codec);

    /**
     * Registers a schema against backing schema registry store.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param schemaType string representation of schema type
     * @return SchemaRegistryObject containing information regarding registered schema.
     * @throws SchemaRegistryClientException if registration operation fails
     */
    SchemaRegistryObject register(String schemaGroup, String schemaName, String schemaString, String schemaType);

    /**
     * Fetches schema specified by the GUID.
     * <p>
     * GUID can be assumed to be unique within a schema registry store.
     *
     * @param schemaGuid GUID reference to specific schema within configured schema registry store.
     * @return SchemaRegistryObject containing information regarding matching schema.
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    SchemaRegistryObject getSchemaByGuid(String schemaGuid);

    /**
     * Fetches schema GUID given schema group, name, string representation, and serialization type
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param schemaString String representation of schema
     * @param schemaType String representation of schema type
     * @return SchemaRegistryObject containing information regarding requested schema.
     * @throws SchemaRegistryClientException if fetch operation fails
     */
    String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType);

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @param version schema version
     * @return GUID of delete schema
     * @throws SchemaRegistryClientException deletion operation failed
     */
    String deleteSchemaVersion(String schemaGroup, String schemaName, int version);

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return GUID of deleted schema
     * @throws SchemaRegistryClientException deletion operation failed
     */
    String deleteLatestSchemaVersion(String schemaGroup, String schemaName);

    /**
     * Not currently implemented.
     *
     * @param schemaGroup schema group name
     * @param schemaName schema name
     * @return list of GUID references to deleted schemas
     * @throws SchemaRegistryClientException deletion operation failed
     */
    List<String> deleteSchema(String schemaGroup, String schemaName);
}
