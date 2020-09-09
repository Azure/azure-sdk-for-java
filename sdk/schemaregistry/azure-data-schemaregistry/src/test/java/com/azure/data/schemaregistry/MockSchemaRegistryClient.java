// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.SchemaRegistryClient;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;
import com.azure.data.schemaregistry.client.SchemaRegistryObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class MockSchemaRegistryClient implements SchemaRegistryClient {
    private final HashMap<String, Function<String, Object>> typeParserDictionary;
    private final HashMap<String, SchemaRegistryObject> guidCache;
    private final HashMap<String, SchemaRegistryObject> schemaStringCache;

    public MockSchemaRegistryClient() {
        this.guidCache = new HashMap<String, SchemaRegistryObject>();
        this.schemaStringCache = new HashMap<String, SchemaRegistryObject>();
        this.typeParserDictionary = new HashMap<String, Function<String, Object>>();
    }

    @Override
    public Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }

    public void addSchemaParser(Codec codec) { }

    @Override
    public SchemaRegistryObject register(String schemaGroup, String schemaName, String schemaString, String schemaType)
            throws SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            return schemaStringCache.get(schemaString);
        }

        return null;
    }

    @Override
    public SchemaRegistryObject getSchemaByGuid(String schemaGuid)
            throws SchemaRegistryClientException {
        if (guidCache.containsKey(schemaGuid)) {
            return guidCache.get(schemaGuid);
        }
        return null;
    }

    @Override
    public String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType)
            throws SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            return schemaStringCache.get(schemaString).getSchemaId();
        }

        return null;
    }

    @Override
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version)
            throws SchemaRegistryClientException {
        return null;
    }

    @Override
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName)
            throws SchemaRegistryClientException {
        return null;
    }

    @Override
    public List<String> deleteSchema(String schemaGroup, String schemaName)
            throws SchemaRegistryClientException {
        return new ArrayList<String>();
    }

    public HashMap<String, Function<String, Object>> getTypeParserDictionary() {
        return typeParserDictionary;
    }

    public HashMap<String, SchemaRegistryObject> getGuidCache() {
        return guidCache;
    }

    public HashMap<String, SchemaRegistryObject> getSchemaStringCache() {
        return schemaStringCache;
    }
}
