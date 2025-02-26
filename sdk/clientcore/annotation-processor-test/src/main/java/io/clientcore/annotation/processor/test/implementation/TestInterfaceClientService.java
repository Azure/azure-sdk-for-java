// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

@ServiceInterface(name = "myService")
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

    @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
    Response<Void> testMethod(@BodyParam("application/octet-stream") ByteBuffer request,
        @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

    @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
    Response<Void> testMethod(@BodyParam("application/octet-stream") BinaryData data,
        @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

    @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    Void testMethodReturnsVoid();

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200 })
    void testHeadMethod();


    @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200, 207 })
    boolean testBooleanHeadMethod();

    @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    Response<Void> testMethodReturnsResponseVoid();

    @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
    Response<InputStream> testDownload();

    @HttpRequestInformation(method = HttpMethod.GET, path = "/kv/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Foo> getFoo(@PathParam("key") String key, @QueryParam("label") String label,
        @HeaderParam("Sync-Token") String syncToken);

    @HttpRequestInformation(method = HttpMethod.DELETE, path = "/kv/{key}", expectedStatusCodes = { 204, 404 })
    Response<Void> deleteFoo(@PathParam("key") String key, @QueryParam("label") String label,
        @HeaderParam("Sync-Token") String syncToken);

    // HttpClientTests
    // Need to add RequestOptions to specify ResponseBodyMode, which is otherwise provided by convenience methods
    @SuppressWarnings({ "unchecked", "cast" })
    @HttpRequestInformation(method = HttpMethod.PUT, path = "put", expectedStatusCodes = {200})
    default HttpBinJSON putConvenience(String uri, int putBody, RequestOptions options) {
        return putResponse(uri, putBody, options).getValue();
    }

    @HttpRequestInformation(method = HttpMethod.PUT, path = "put", expectedStatusCodes = { 200 })
    Response<HttpBinJSON> putResponse(@HostParam("uri") String uri,
        @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestOptions options);

    @HttpRequestInformation(method = HttpMethod.POST, path = "stream", expectedStatusCodes = { 200 })
    default HttpBinJSON postStreamConvenience(@HostParam("uri") String uri,
                           @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestOptions options) {
        return postStreamResponse(uri, putBody, options).getValue();
    }

    @HttpRequestInformation(method = HttpMethod.POST, path = "stream", expectedStatusCodes = { 200 })
    Response<HttpBinJSON> postStreamResponse(@HostParam("uri") String uri,
                                             @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestOptions options);

    // Service 1
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/100", expectedStatusCodes = {200})
    byte[] getByteArray(@HostParam("uri") String uri);

    // Service 2
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/{numberOfBytes}", expectedStatusCodes = { 200 })
    byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String hostName,
        @PathParam("numberOfBytes") int numberOfBytes);

    // Service 3
    @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/100", expectedStatusCodes = { 200 })
    void getNothing(@HostParam("uri") String uri);
}
