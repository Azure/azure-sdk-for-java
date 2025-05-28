// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Base64Uri;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Service that tests special return bodies for methods.
 * <p>
 * The following types are handled in a special way when they are the return type or are wrapped in {@link Response} as
 * the return type:
 * <ul>
 *     <li>{@link BinaryData}</li>
 *     <li>{@code byte[]}</li>
 *     <li>{@link InputStream}</li>
 * </ul>
 */
@ServiceInterface(name = "SpecialReturnBodiesService", host = "{url}")
public interface SpecialReturnBodiesService {
    /**
     * Creates a new instance of {@link SpecialReturnBodiesService}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of SpecialReturnBodiesService.
     */
    static SpecialReturnBodiesService getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class
                .forName("io.clientcore.annotation.processor.test.SpecialReturnBodiesServiceImpl");
            return (SpecialReturnBodiesService) clazz.getMethod("getNewInstance", HttpPipeline.class)
                .invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    BinaryData getBinaryData(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return A response containing the byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    Response<BinaryData> getBinaryDataWithResponse(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    byte[] getByteArray(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return A response containing the byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    Response<byte[]> getByteArrayWithResponse(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return The byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    InputStream getInputStream(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param url The URL.
     * @return A response containing the byte array result.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes", expectedStatusCodes = { 200 })
    Response<InputStream> getInputStreamWithResponse(@HostParam("url") String url);

    /**
     * Gets a binary payload from the specified URL.
     *
     * @param endpoint The URL.
     * @return A response containing the list of binary data.
     */
    @HttpRequestInformation(method = HttpMethod.GET, path = "/type/array/unknown", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail
    Response<List<BinaryData>> getListOfBinaryData(@HostParam("url") String endpoint);


    /**
     * Gets Base64 encoded binary data from the specified URL.
     * @param endpoint The URL.
     * @return A response containing the Base64 encoded binary data.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/bytes/body/response/base64url",
        expectedStatusCodes = { 200 },
        returnValueWireType = Base64Uri.class)
    @UnexpectedResponseExceptionDetail
    Response<byte[]> base64url(@HostParam("url") String endpoint);
}
