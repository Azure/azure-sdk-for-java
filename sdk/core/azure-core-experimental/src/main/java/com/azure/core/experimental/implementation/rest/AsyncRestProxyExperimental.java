// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.http.rest.AsyncRestProxy;
import com.azure.core.implementation.http.rest.RequestDataConfiguration;
import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SwaggerMethodParser;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AsyncRestProxyExperimental extends AsyncRestProxy {
    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     */
    public AsyncRestProxyExperimental(HttpPipeline httpPipeline, SerializerAdapter serializer,
        SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateRequest(RequestDataConfiguration requestDataConfiguration,
        SerializerAdapter serializerAdapter) throws IOException {
        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();
        SwaggerMethodParser methodParser = requestDataConfiguration.getMethodParser();

        if (bodyContentObject instanceof JsonSerializable<?>) {
            AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            try (JsonWriter jsonWriter = DefaultJsonWriter.toStream(outputStream)) {
                jsonWriter.writeJson((JsonSerializable<?>) bodyContentObject).flush();
            }

            request.setBody(BinaryData.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray(), 0,
                outputStream.count())));
        } else if (isJson) {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject, SerializerEncoding.JSON));
        } else if (FluxUtil.isFluxByteBuffer(methodParser.getBodyJavaType())) {
            // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a
            // Flowable<byte[]> is given for the body.
            request.setBody((Flux<ByteBuffer>) bodyContentObject);
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody((byte[]) bodyContentObject);
        } else if (bodyContentObject instanceof String) {
            final String bodyContentString = (String) bodyContentObject;
            if (!bodyContentString.isEmpty()) {
                request.setBody(bodyContentString);
            }
        } else if (bodyContentObject instanceof ByteBuffer) {
            request.setBody(Flux.just((ByteBuffer) bodyContentObject));
        } else {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject,
                SerializerEncoding.fromHeaders(request.getHeaders())));
        }
    }
}
