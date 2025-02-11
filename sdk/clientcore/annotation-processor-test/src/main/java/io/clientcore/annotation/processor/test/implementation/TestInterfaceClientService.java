// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

@ServiceInterface(name = "myService", host = "https://somecloud.com")
public interface TestInterfaceClientService {
    static TestInterfaceClientService getNewInstance(HttpPipeline pipeline, ObjectSerializer serializer) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class.forName("io.clientcore.annotation.processor.test.implementation.TestInterfaceClientServiceImpl");
            return (TestInterfaceClientService) clazz
                .getMethod("getNewInstance", HttpPipeline.class, ObjectSerializer.class)
                .invoke(null, pipeline, serializer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    //@HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
    //Response<Void> testMethod(@BodyParam("application/octet-stream") ByteBuffer request,
    //    @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);
    //
    //@HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
    //Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
    //    @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);
    //
    //@HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    //Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);
    //
    //@HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    //Void testMethodReturnsVoid();
    //
    //@HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200 })
    //void testHeadMethod();
    //
    //@HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    //Response<Void> testMethodReturnsResponseVoid();
    //
    //@HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    //Response<InputStream> testDownload();

    @HttpRequestInformation(method = HttpMethod.GET, path = "/kv/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Foo> getFoo(@PathParam("key") String key, @QueryParam("label") String label,
        @HeaderParam("Sync-Token") String syncToken);

    @HttpRequestInformation(method = HttpMethod.DELETE, path = "/kv/{key}", expectedStatusCodes = { 204, 404 })
    Response<Void> deleteFoo(@PathParam("key") String key, @QueryParam("label") String label,
        @HeaderParam("Sync-Token") String syncToken);
}
