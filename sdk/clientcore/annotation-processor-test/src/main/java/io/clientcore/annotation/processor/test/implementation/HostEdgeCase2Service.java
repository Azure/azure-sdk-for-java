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

/**
 * Tests that there isn't a variable name conflict if the parameterized host segment is called {@code uri}.
 */
@ServiceInterface(name = "HostEdgeCase2", host = "{uri}")
public interface HostEdgeCase2Service {
    /**
     * Creates a new instance of {@link HostEdgeCase2Service}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of HostEdgeCase2.
     */
    static HostEdgeCase2Service getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.HostEdgeCase2ServiceImpl");
            return (HostEdgeCase2Service) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the byte array from the specified URI.
     *
     * @param uri The URI.
     * @param numberOfBytes The number of bytes to retrieve.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/{numberOfBytes}", expectedStatusCodes = { 200 })
    byte[] getByteArray(@HostParam("uri") String uri, @PathParam("numberOfBytes") int numberOfBytes);
}
