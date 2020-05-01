/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class MockSchemaRegistryClient implements SchemaRegistryClient {
    
    public final HashMap<String, Function<String, ?>> typeParserDictionary;
    
    public final HashMap<String, SRObject<?>> guidCache;
    public final HashMap<String, SRObject<?>> schemaStringCache;
    
    public MockSchemaRegistryClient() {
        this.guidCache = new HashMap<String, SRObject<?>>();
        this.schemaStringCache = new HashMap<String, SRObject<?>>();
        this.typeParserDictionary = new HashMap<String, Function<String, ?>>();
    }

    public void loadSchemaParser(String serializationFormat, Function<String, ?> f) {}

    @Override
    public SRObject<?> register(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            return schemaStringCache.get(schemaString);
        } 
        
        return null;
    }
    
    @Override
    public SRObject<?> getSchemaByGuid(String schemaGuid)
            throws IOException, SchemaRegistryClientException {
        if (guidCache.containsKey(schemaGuid)) {
            return guidCache.get(schemaGuid);
        }
        return null;
    }

    @Override
    public String getGuid(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            return schemaStringCache.get(schemaString).schemaGuid;
        }
        
        return null;
    }

    @Override
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version)
            throws IOException, SchemaRegistryClientException {
        return null;
    }

    @Override
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName)
            throws IOException, SchemaRegistryClientException {
        return null;
    }

    @Override
    public List<String> deleteSchema(String schemaGroup, String schemaName)
            throws IOException, SchemaRegistryClientException {
        return new ArrayList<String>();
    }    
}