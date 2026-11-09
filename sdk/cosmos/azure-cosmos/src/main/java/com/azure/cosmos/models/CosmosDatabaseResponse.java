// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * The type Cosmos database response.
 */
public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties> {

    CosmosDatabaseResponse(ResourceResponse<Database> response) {
        super(response);
        ObjectNode bodyAsJson = (ObjectNode)response.getBody();
        if (bodyAsJson == null) {
            super.setProperties(null);
        } else {
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getDiagnostics());
            Instant serializationStartTime = Instant.now();
            CosmosDatabaseProperties props =  new CosmosDatabaseProperties(bodyAsJson, null);
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.DATABASE_DESERIALIZATION
            );
            serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
            super.setProperties(props);
        }
    }

    /**
     * Gets the cosmos database properties
     *
     * @return the cosmos database properties
     */
    public CosmosDatabaseProperties getProperties() {
        return super.getProperties();
    }

    /**
     * Gets the Max Quota.
     *
     * @return the getDatabase quota.
     */
    public long getDatabaseQuota() {
        return resourceResponseWrapper.getDatabaseQuota();
    }

    /**
     * Gets the current Usage.
     *
     * @return the current getDatabase usage.
     */
    public long getDatabaseUsage() {
        return resourceResponseWrapper.getDatabaseUsage();
    }

}
