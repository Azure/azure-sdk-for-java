// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import java.lang.reflect.InvocationTargetException;

/**
 * Tests that there isn't a variable name conflict if the parameterized host segment is called {@code endpoint}.
 */
@ServiceInterface(name = "HostPathEdgeCase3", host = "{endpoint}/server/path/multiple/{apiVersion}")
public interface HostPathEdgeCase3Service {
    /**
     * Creates a new instance of {@link HostPathEdgeCase3Service}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of HostEdgeCase2.
     */
    static HostPathEdgeCase3Service getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.HostPathEdgeCase3ServiceImpl");
            return (HostPathEdgeCase3Service) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Performs a no operation.
     * @param endpoint The endpoint to connect to.
     * @param apiVersion The API version to use.
     * @param requestContext The request context to use.
     * @return A response with no content.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "/", expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> noOperationParams(@HostParam("endpoint") String endpoint,
        @HostParam("apiVersion") String apiVersion, RequestContext requestContext);

}
