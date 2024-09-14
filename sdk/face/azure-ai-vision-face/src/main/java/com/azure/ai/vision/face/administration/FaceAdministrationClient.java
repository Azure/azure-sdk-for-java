// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.administration;

import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.implementation.FaceAdministrationClientImpl;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;

import java.util.Objects;

/**
 * FaceAdministrationClient class.
 */
@ServiceClient(builder = FaceAdministrationClientBuilder.class)
public final class FaceAdministrationClient {

    private final HttpPipeline pipeline;
    private final SerializerAdapter serializerAdapter;
    private final String endpoint;
    private final FaceServiceVersion serviceVersion;

    FaceAdministrationClient(HttpPipeline pipeline, SerializerAdapter serializerAdapter,
                             String endpoint, FaceServiceVersion serviceVersion) {
        this.pipeline = pipeline;
        this.serializerAdapter = serializerAdapter;
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Creates a new instance of LargeFaceListClient.
     *
     * @param largeFaceListId the ID of LargeFaceList.
     * @return a new instance of LargeFaceListClient.
     */
    public LargeFaceListClient getLargeFaceListClient(String largeFaceListId) {
        Objects.requireNonNull(largeFaceListId, "'largeFaceListId' cannot be null.");

        FaceAdministrationClientImpl client
                = new FaceAdministrationClientImpl(this.pipeline, this.serializerAdapter,
                this.endpoint, largeFaceListId, null, serviceVersion);
        return new LargeFaceListClient(client.getLargeFaceLists());
    }

    /**
     * Creates a new instance of LargePersonGroupClient.
     *
     * @param largePersonGroupId the ID of LargePersonGroup.
     * @return a new instance of LargePersonGroupClient.
     */
    public LargePersonGroupClient getLargePersonGroupClient(String largePersonGroupId) {
        Objects.requireNonNull(largePersonGroupId, "'largePersonGroupId' cannot be null.");

        FaceAdministrationClientImpl client
                = new FaceAdministrationClientImpl(this.pipeline, this.serializerAdapter,
                this.endpoint, null, largePersonGroupId, serviceVersion);
        return new LargePersonGroupClient(client.getLargePersonGroups());
    }
}
