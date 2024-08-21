// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests cases where the service returns a response that would result in a {@link DataLakeStorageException} being thrown
 * with a response body that needs to be deserialized.
 */
public class DataLakeErrorDeserializationTests {
    @Test
    public void errorResponseBody() {
        String errorResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Error><Code>ContainerAlreadyExists</Code>"
            + "<Message>The specified container already exists.</Message></Error>";
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, 409, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"),
                errorResponse.getBytes(StandardCharsets.UTF_8)))).build();
        DataLakeFileClient fileClient = new DataLakePathClientBuilder().endpoint(
                "https://account.blob.core.windows.net/container")
            .fileSystemName("filesystem")
            .pathName("path")
            .credential(new MockTokenCredential())
            .pipeline(httpPipeline)
            .buildFileClient();

        DataLakeStorageException exception = assertThrows(DataLakeStorageException.class, fileClient::create);
        assertTrue(exception.getMessage().contains("The specified container already exists."));
        // assertEquals(BlobErrorCode.CONTAINER_ALREADY_EXISTS, exception.getErrorCode());
    }
}
