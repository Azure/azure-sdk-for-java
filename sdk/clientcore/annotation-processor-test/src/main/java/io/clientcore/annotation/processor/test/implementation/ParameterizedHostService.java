// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;
import java.lang.reflect.InvocationTargetException;

@ServiceInterface(name = "ParameterizedHostService", host = "{scheme}://{hostName}")
public interface ParameterizedHostService {
    /**
     * Creates a new instance of {@link ParameterizedHostService}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of ParameterizedHostService.
     */
    static ParameterizedHostService getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.ParameterizedHostServiceImpl");
            return (ParameterizedHostService) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the byte array from the specified host and scheme.
     *
     * @param scheme The URI scheme (e.g., "https").
     * @param host The host name.
     * @param numberOfBytes The number of bytes to retrieve.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/{numberOfBytes}", expectedStatusCodes = { 200 })
    byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String host,
        @PathParam("numberOfBytes") int numberOfBytes);
}
