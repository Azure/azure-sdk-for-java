// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Initializes a new instance of the TestInterfaceClient type.
 */
public final class TestInterfaceClientImpl {

    @ServiceInterface(name = "myService")
    public interface TestInterfaceClientService {
        static TestInterfaceClientService getNewInstance(HttpPipeline pipeline) {
            if (pipeline == null) {
                throw new IllegalArgumentException("pipeline cannot be null");
            }
            try {
                Class<?> clazz = Class.forName("io.clientcore.annotation.processor.test.implementation.TestInterfaceClientServiceImpl");
                return (TestInterfaceClientService) clazz
                    .getMethod("getNewInstance", HttpPipeline.class)
                    .invoke(null, pipeline);
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


        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<FooListResult> listFooListResult(@HostParam("uri") String uri, RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<FooListResult> listNextFooListResult(@PathParam(value = "nextLink", encoded = true) String nextLink,
                                                      RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<List<Foo>> listFoo(@HostParam("uri") String uri, @QueryParam(value = "tags", multipleQueryParams =
            true) List<String> tags, @QueryParam(value = "tags2", multipleQueryParams = true) List<String> tags2,
            RequestOptions requestOptions);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<List<Foo>> listNextFoo(@PathParam(value = "nextLink", encoded = true) String nextLink,
                                        RequestOptions requestOptions);
    }
}
