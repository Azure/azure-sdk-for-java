// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

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
            Callable<CosmosContainerProperties> createCosmosContainerPropertiesCallable = () -> {
                return new CosmosContainerProperties(bodyAsString);
            };
            SerializationDiagnosticsContext serializationDiagnosticsContext = BridgeInternal.getSerializationDiagnosticsContext(this.getCosmosResponseDiagnostics());
            CosmosContainerProperties props = serializationDiagnosticsContext.getResource(createCosmosContainerPropertiesCallable, SerializationDiagnosticsContext.SerializationType.ContainerSerialization);
            super.setProperties(props);
            container = new CosmosAsyncContainer(this.getProperties().getId(), database);
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
