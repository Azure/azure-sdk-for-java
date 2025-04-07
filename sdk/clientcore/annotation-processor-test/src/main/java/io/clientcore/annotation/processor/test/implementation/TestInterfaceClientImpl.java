// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.models.binarydata.BinaryData;
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
        Response<Void> testMethod(@HostParam("uri") String uri,
            @BodyParam("application/octet-stream") ByteBuffer request,
                                  @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.POST, path = "my/uri/path", expectedStatusCodes = { 200 })
        Response<Void> testMethod(@HostParam("uri") String uri, @BodyParam("application/octet-stream") BinaryData data,
                                  @HeaderParam("Content-Type") String contentType, @HeaderParam("Content-Length") Long contentLength);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<Void> testListNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = { 200 })
        Void testMethodReturnsVoid(@HostParam("uri") String uri);

        @HttpRequestInformation(method = HttpMethod.GET, path = "kv/{key}", expectedStatusCodes = { 200 })
        @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
        Response<Foo> getFoo(@PathParam("key") String key, @QueryParam("label") String label,
                             @HeaderParam("Sync-Token") String syncToken);

        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<FooListResult> listFooListResult(@HostParam("uri") String uri, RequestContext requestContext);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<FooListResult> listNextFooListResult(@PathParam(value = "nextLink", encoded = true) String nextLink,
                                                      RequestContext requestContext);

        @HttpRequestInformation(method = HttpMethod.GET, path = "foos", expectedStatusCodes = { 200 })
        Response<List<Foo>> listFoo(@HostParam("uri") String uri, @QueryParam(value = "tags", multipleQueryParams =
            true) List<String> tags, @QueryParam(value = "tags2", multipleQueryParams = true) List<String> tags2,
                                    RequestContext requestContext);

        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
        Response<List<Foo>> listNextFoo(@PathParam(value = "nextLink", encoded = true) String nextLink,
                                        RequestContext requestContext);
        // HttpClientTests
        // Need to add RequestContext to specify ResponseBodyMode, which is otherwise provided by convenience methods
        @SuppressWarnings({ "unchecked", "cast" })
        @HttpRequestInformation(method = HttpMethod.PUT, path = "put", expectedStatusCodes = {200})
        default HttpBinJSON putConvenience(String uri, int putBody, RequestContext requestContext) {
            return putResponse(uri, putBody, requestContext).getValue();
        }

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put", expectedStatusCodes = { 200 })
        Response<HttpBinJSON> putResponse(@HostParam("uri") String uri,
                                          @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestContext requestContext);

        @HttpRequestInformation(method = HttpMethod.POST, path = "stream", expectedStatusCodes = { 200 })
        default HttpBinJSON postStreamConvenience(@HostParam("uri") String uri,
                                                  @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestContext requestContext) {
            return postStreamResponse(uri, putBody, requestContext).getValue();
        }

        @HttpRequestInformation(method = HttpMethod.POST, path = "stream", expectedStatusCodes = { 200 })
        Response<HttpBinJSON> postStreamResponse(@HostParam("uri") String uri,
                                                 @BodyParam(ContentType.APPLICATION_OCTET_STREAM) int putBody, RequestContext requestContext);

        // Service 1
        @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/100", expectedStatusCodes = {200})
        byte[] getByteArray(@HostParam("uri") String uri);

        // Service 2
        @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/{numberOfBytes}", expectedStatusCodes = { 200 })
        byte[] getByteArray(@HostParam("scheme") String scheme, @HostParam("hostName") String hostName,
                            @PathParam(value = "numberOfBytes", encoded = true) int numberOfBytes);

        // Service 3
        @HttpRequestInformation(method = HttpMethod.GET, path = "bytes/100", expectedStatusCodes = { 200 })
        void getNothing(@HostParam("uri") String uri);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything", expectedStatusCodes = { 200 })
        HttpBinJSON getAnything(@HostParam("uri") String uri);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything/with+plus", expectedStatusCodes = { 200 })
        HttpBinJSON getAnythingWithPlus(@HostParam("uri") String uri);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything/{path}", expectedStatusCodes = { 200 })
        HttpBinJSON getAnythingWithPathParam(@HostParam("uri") String uri, @PathParam("path") String pathParam);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything/{path}", expectedStatusCodes = { 200 })
        HttpBinJSON getAnythingWithEncodedPathParam(@HostParam("uri") String uri,
            @PathParam(value = "path", encoded = true) String pathParam);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything", expectedStatusCodes = { 200 })
        HttpBinJSON getAnything(@HostParam("uri") String uri, @QueryParam("a") String a, @QueryParam("b") int b);

        @HttpRequestInformation(method = HttpMethod.GET, path = "anything", expectedStatusCodes = { 200 })
        HttpBinJSON getAnythingWithEncoded(@HostParam("uri") String uri,
            @QueryParam(value = "a", encoded = true) String a, @QueryParam("b") int b);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithNoContentTypeAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithNoContentTypeAndByteArrayBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put", headers = { "Content-Type: application/json" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @HttpRequestInformation(
            method = HttpMethod.PUT,
            path = "put",
            headers = { "Content-Type: application/json; charset=utf-8" })
        HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @HttpRequestInformation(
            method = HttpMethod.PUT,
            path = "put",
            headers = { "Content-Type: application/octet-stream" }, expectedStatusCodes = { 200 })
        Response<HttpBinJSON> putWithHeaderApplicationOctetStreamContentTypeAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @HttpRequestInformation(
            method = HttpMethod.PUT,
            path = "put",
            headers = { "Content-Type: application/octet-stream" })
        HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put", expectedStatusCodes = { 200 })
        Response<HttpBinJSON> putWithBodyParamApplicationJsonContentTypeAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_JSON) String body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_JSON + "; charset=utf-8") String body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_JSON) byte[] body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String body);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "put")
        HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(@HostParam("uri") String uri,
            @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] body);

    }
}
