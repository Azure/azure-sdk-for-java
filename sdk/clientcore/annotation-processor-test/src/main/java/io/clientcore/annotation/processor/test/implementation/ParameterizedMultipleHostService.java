// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;
import java.lang.reflect.InvocationTargetException;

@ServiceInterface(name = "ParameterizedMultipleHostService", host = "{scheme}://{hostPart1}{hostPart2}")
public interface ParameterizedMultipleHostService {

    /**
     * Creates a new instance of {@link ParameterizedMultipleHostService}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of ParameterizedMultipleHostService.
     */   static ParameterizedMultipleHostService getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.ParameterizedMultipleHostServiceImpl");
            return (ParameterizedMultipleHostService) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a JSON object from the specified URL.
     * @param scheme The scheme to use (e.g., "http" or "https").
     * @param hostPart1 The first part of the host (e.g., "localhost").
     * @param hostPart2 The second part of the host (e.g., ":8000").
     * @return The JSON object retrieved from the URL.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "get", expectedStatusCodes = { 200 })
    HttpBinJSON get(@HostParam("scheme") String scheme, @HostParam("hostPart1") String hostPart1,
        @HostParam("hostPart2") String hostPart2);
}
