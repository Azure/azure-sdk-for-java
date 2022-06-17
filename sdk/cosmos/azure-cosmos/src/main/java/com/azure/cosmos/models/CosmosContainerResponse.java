// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.accesshelpers.CosmosContainerResponseHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Instant;

/**
 * The type Cosmos container response.
 */
@SuppressWarnings("enforcefinalfields")
public class CosmosContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    CosmosContainerResponse(ResourceResponse<DocumentCollection> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            super.setProperties(null);
        } else {
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getDiagnostics());
            Instant serializationStartTime = Instant.now();
            CosmosContainerProperties props =  new CosmosContainerProperties(bodyAsString);
            Instant serializationEndTime = Instant.now();
            SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.CONTAINER_DESERIALIZATION
            );
            serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
            super.setProperties(props);
        }
    }

    /**
     * Gets the progress of an index transformation, if one is underway.
     *
     * @return the progress of an index transformation.
     */
    public long getIndexTransformationProgress() {
        return resourceResponseWrapper.getIndexTransformationProgress();
    }

    /**
     * Gets the progress of lazy indexing.
     *
     * @return the progress of lazy indexing.
     */
    long getLazyIndexingProgress() {
        return resourceResponseWrapper.getLazyIndexingProgress();
    }

    /**
     * Gets the container properties
     *
     * @return the cosmos container properties
     */
    public CosmosContainerProperties getProperties() {
        return super.getProperties();
    }

    // Static initializer to set up the helper.
    static {
        CosmosContainerResponseHelper.setAccessor(new CosmosContainerResponseHelper.CosmosContainerResponseAccessor() {
            @Override
            public CosmosContainerResponse createCosmosContainerResponse(ResourceResponse<DocumentCollection> response) {
                return new CosmosContainerResponse(response);
            }
        });
    }
}
