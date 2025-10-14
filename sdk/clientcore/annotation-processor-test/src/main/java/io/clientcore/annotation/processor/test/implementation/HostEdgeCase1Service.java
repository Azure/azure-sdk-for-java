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
 * Tests that there isn't a variable name conflict if the parameterized host segment is called {@code url}.
 */
@ServiceInterface(name = "HostEdgeCase1", host = "{url}")
public interface HostEdgeCase1Service {
    /**
     * Creates a new instance of {@link HostEdgeCase1Service}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of HostEdgeCase1.
     */
    static HostEdgeCase1Service getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.HostEdgeCase1ServiceImpl");
            return (HostEdgeCase1Service) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the byte array from the specified URL.
     *
     * @param url The URL.
     * @param numberOfBytes The number of bytes to retrieve.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/{numberOfBytes}", expectedStatusCodes = { 200 })
    byte[] getByteArray(@HostParam("url") String url, @PathParam("numberOfBytes") int numberOfBytes);
}
