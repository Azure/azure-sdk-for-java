//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.http.HttpResponse;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.utils.JsonSerializer;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.utils.CodegenUtil;
import io.clientcore.core.utils.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class TestInterfaceClientServiceImpl implements TestInterfaceClientService {

    private static final ClientLogger LOGGER = new ClientLogger(TestInterfaceClientService.class);

    private final HttpPipeline defaultPipeline;

    private final ObjectSerializer serializer;

    public TestInterfaceClientServiceImpl(HttpPipeline defaultPipeline, ObjectSerializer serializer) {
        this.defaultPipeline = defaultPipeline;
        this.serializer = serializer == null ? new JsonSerializer() : serializer;
    }

    public static TestInterfaceClientService getNewInstance(HttpPipeline pipeline, ObjectSerializer serializer) {
        return new TestInterfaceClientServiceImpl(pipeline, serializer);
    }

    public HttpPipeline getPipeline() {
        return defaultPipeline;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testMethod(ByteBuffer request, String contentType, Long contentLength) {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(host);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, contentType);
        //Set the request body
        httpRequest.setBody(BinaryData.fromObject(request, serializer));
        //Send the request through the pipeline
        Response<Void> response = (Response<Void>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testMethod(BinaryData data, String contentType, Long contentLength) {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(host);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, contentType);
        //Set the request body
        BinaryData binaryData = (BinaryData) data;
        if (binaryData.getLength() != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            httpRequest.setBody(binaryData);
        }
        //Send the request through the pipeline
        Response<Void> response = (Response<Void>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testListNext(String nextLink) {
        HttpPipeline pipeline = this.getPipeline();
        String host = nextLink;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<Void> response = (Response<Void>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Void testMethodReturnsVoid() {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void testHeadMethod() {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public boolean testBooleanHeadMethod() {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = (responseCode == 200 || responseCode == 207);
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return expectedResponse;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testMethodReturnsResponseVoid() {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<Void> response = (Response<Void>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<java.io.InputStream> testDownload() {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/my/uri/path";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<java.io.InputStream> response = (Response<java.io.InputStream>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        BinaryData responseBody = response.getBody();
        Object result = responseBody.toStream();
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<io.clientcore.annotation.processor.test.implementation.models.Foo> getFoo(String key, String label, String syncToken) {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/kv/" + key + "?label=" + label;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("Sync-Token"), syncToken);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<io.clientcore.annotation.processor.test.implementation.models.Foo> response = (Response<io.clientcore.annotation.processor.test.implementation.models.Foo>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<io.clientcore.annotation.processor.test.implementation.models.Foo>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> deleteFoo(String key, String label, String syncToken) {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/kv/" + key + "?label=" + label;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(host);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("Sync-Token"), syncToken);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<Void> response = (Response<Void>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = (responseCode == 204 || responseCode == 404);
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult> listFooListResult(String uri, RequestOptions requestOptions, Context context) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "foos";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult> response = (Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult> listNextFooListResult(String nextLink, RequestOptions requestOptions, Context context) {
        HttpPipeline pipeline = this.getPipeline();
        String host = nextLink;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult> response = (Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<io.clientcore.annotation.processor.test.implementation.models.FooListResult>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>> listFoo(String uri, RequestOptions requestOptions, Context context) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "foos";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>> response = (Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>> listNextFoo(String nextLink, RequestOptions requestOptions, Context context) {
        HttpPipeline pipeline = this.getPipeline();
        String host = nextLink;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>> response = (Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<java.util.List<io.clientcore.annotation.processor.test.implementation.models.Foo>>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON> putResponse(String uri, int putBody, RequestOptions options) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "put";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        httpRequest.setBody(BinaryData.fromObject(putBody, serializer));
        //Set the Request Options
        httpRequest.setRequestOptions(options);
        //Send the request through the pipeline
        Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON> response = (Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON> postStreamResponse(String uri, int putBody, RequestOptions options) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "stream";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        httpRequest.setBody(BinaryData.fromObject(putBody, serializer));
        //Set the Request Options
        httpRequest.setRequestOptions(options);
        //Send the request through the pipeline
        Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON> response = (Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON>) pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());
        String returnTypeName = "io.clientcore.core.http.models.Response<io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON>";
        Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result);
        } else {
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result);
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getByteArray(String uri) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "bytes/100";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = response.getBody();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getByteArray(String scheme, String hostName, int numberOfBytes) {
        HttpPipeline pipeline = this.getPipeline();
        String host = "/bytes/" + numberOfBytes;
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = response.getBody();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getNothing(String uri) {
        HttpPipeline pipeline = this.getPipeline();
        String host = uri + "/" + "bytes/100";
        //Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(host);
        //Set the request body
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
        //Send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);
        int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        try {
            response.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
        return;
    }

    private static Object decodeByteArray(byte[] bytes, ObjectSerializer serializer, String returnType) {
        try {
            ParameterizedType type = CodegenUtil.inferTypeNameFromReturnType(returnType);
            Type token = type.getRawType();
            if (Response.class.isAssignableFrom((Class<?>) token)) {
                token = type.getActualTypeArguments()[0];
            }
            return serializer.deserializeFromBytes(bytes, token);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }
}
