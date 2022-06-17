// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.accesshelpers.CosmosDatabaseResponseHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Instant;

/**
 * The type Cosmos database response.
 */
public class CosmosDatabaseResponse extends CosmosResponse<CosmosDatabaseProperties> {

    CosmosDatabaseResponse(ResourceResponse<Database> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
        } else {
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getDiagnostics());
            Instant serializationStartTime = Instant.now();
            CosmosDatabaseProperties props =  new CosmosDatabaseProperties(bodyAsString, null);
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

    // Static initializer to set up the helper.
    static {
        CosmosDatabaseResponseHelper.setAccessor(new CosmosDatabaseResponseHelper.CosmosDatabaseResponseAccessor() {
            @Override
            public CosmosDatabaseResponse createCosmosDatabaseResponse(ResourceResponse<Database> response) {
                return new CosmosDatabaseResponse(response);
            }
        });
    }
}
