// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * The type Cosmos async container response.
 */
@SuppressWarnings("enforcefinalfields")
public class CosmosAsyncContainerResponse extends CosmosResponse<CosmosContainerProperties> {

    private final CosmosAsyncContainer container;

    CosmosAsyncContainerResponse(ResourceResponse<DocumentCollection> response, CosmosAsyncDatabase database) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            container = null;
        } else {
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getResponseDiagnostics());
            ZonedDateTime serializationStartTime = ZonedDateTime.now(ZoneOffset.UTC);
            CosmosContainerProperties props =  new CosmosContainerProperties(bodyAsString);
            ZonedDateTime serializationEndTime = ZonedDateTime.now(ZoneOffset.UTC);
            SerializationDiagnosticsContext.SerializationDiagnostics diagnostics = new SerializationDiagnosticsContext.SerializationDiagnostics(
                serializationStartTime,
                serializationEndTime,
                SerializationDiagnosticsContext.SerializationType.CONTAINER_DESERIALIZATION
            );
            serializationDiagnosticsContext.addSerializationDiagnostics(diagnostics);
            super.setProperties(props);
            container = BridgeInternal.createCosmosAsyncContainer(ModelBridgeInternal.getResourceFromResourceWrapper(this.getProperties()).getId(), database);
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

    /**
     * Gets the Container object
     *
     * @return the Cosmos container object
     */
    public CosmosAsyncContainer getContainer() {
        return container;
    }
}
