// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    static final String INVALID_MODEL_ID = "a0a3998a-4c4affe66b7";
    static final String INVALID_RECEIPT_URL = "https://invalid.blob.core.windows.net/fr/contoso-allinone.jpg";
    static final String INVALID_KEY = "invalid key";
    static final String INVALID_SOURCE_URL_ERROR = "Status code 400, \"{\"error\":{\"code\":\"1003\","
        + "\"message\":\"Parameter 'Source' is not a valid Uri.\"}}\"";
    static final String INVALID_MODEL_ID_ERROR = "Invalid UUID string: " + INVALID_MODEL_ID;
    static final String NULL_SOURCE_URL_ERROR = "'fileSourceUrl' cannot be null.";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String RECEIPT_LOCAL_URL = "src/test/resources/sample_files/Test/contoso-allinone.jpg";
    static final String LAYOUT_LOCAL_URL = "src/test/resources/sample_files/Test/layout1.jpg";
    static final String FORM_LOCAL_URL = "src/test/resources/sample_files/Test/Invoice_6.pdf";
    static final long RECEIPT_FILE_LENGTH = new File(RECEIPT_LOCAL_URL).length();
    static final long LAYOUT_FILE_LENGTH = new File(LAYOUT_LOCAL_URL).length();
    static final long CUSTOM_FORM_FILE_LENGTH = new File(FORM_LOCAL_URL).length();
    static final String VALID_URL = "https://resources/contoso-allinone.jpg";

    private TestUtils() {
    }

    static AccountProperties getExpectedAccountProperties() {
        return new AccountProperties(14, 5000);
    }

    static InputStream getFileData(String fileName) {
        final HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        final HttpResponse httpResponse =
            httpClient.send(new HttpRequest(HttpMethod.GET, fileName)).block();
        return new ByteArrayInputStream(httpResponse.getBodyAsByteArray().block());
    }

    static InputStream getContentDetectionFileData(String localFileUrl) {
        try {
            return new FileInputStream(localFileUrl);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
        }
    }

    static Flux<ByteBuffer> getReplayableBufferData(String localFileUrl) {
        Mono<InputStream> dataMono = Mono.defer(() -> {
            try {
                return Mono.just(new FileInputStream(localFileUrl));
            } catch (FileNotFoundException e) {
                return Mono.error(new RuntimeException("Local file not found.", e));
            }
        });
        return dataMono.flatMapMany(new Function<InputStream, Flux<ByteBuffer>>() {
            @Override
            public Flux<ByteBuffer> apply(InputStream inputStream) {
                return Utility.toFluxByteBuffer(inputStream);
            }
        });
    }

    static SerializerAdapter getSerializerAdapter() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }
}

