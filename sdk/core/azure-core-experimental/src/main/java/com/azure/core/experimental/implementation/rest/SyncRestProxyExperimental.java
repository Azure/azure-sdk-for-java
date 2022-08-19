// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.http.rest.RequestDataConfiguration;
import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SyncRestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SyncRestProxyExperimental extends SyncRestProxy {
    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     */
    public SyncRestProxyExperimental(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    @Override
    public void updateRequest(RequestDataConfiguration requestDataConfiguration,
        SerializerAdapter serializerAdapter) throws IOException {
        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();

        if (bodyContentObject instanceof JsonSerializable<?>) {
            AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            try (JsonWriter jsonWriter = DefaultJsonWriter.toStream(outputStream)) {
                jsonWriter.writeJson((JsonSerializable<?>) bodyContentObject);
            }

            request.setBody(BinaryData.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray(), 0,
                outputStream.count())));
        } else if (isJson) {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject, SerializerEncoding.JSON));
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody((byte[]) bodyContentObject);
        } else if (bodyContentObject instanceof String) {
            final String bodyContentString = (String) bodyContentObject;
            if (!bodyContentString.isEmpty()) {
                request.setBody(bodyContentString);
            }
        } else if (bodyContentObject instanceof ByteBuffer) {
            if (((ByteBuffer) bodyContentObject).hasArray()) {
                request.setBody(((ByteBuffer) bodyContentObject).array());
            } else {
                byte[] array = new byte[((ByteBuffer) bodyContentObject).remaining()];
                ((ByteBuffer) bodyContentObject).get(array);
                request.setBody(array);
            }
        } else {
            byte[] serializedBytes = serializerAdapter
                .serializeToBytes(bodyContentObject, SerializerEncoding.fromHeaders(request.getHeaders()));
            request.setBody(serializedBytes);
        }
    }
}
