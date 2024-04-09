// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.azure.core.CoreTestUtils.readStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Tests for {@link MultipartFormData} and {@link MultipartFormDataBuilder}.
 */
public class MultipartFormDataTests {
    @Test
    public void boundaryValueMatch() {
        // Arrange
        String boundaryValue = "boundaryValue";
        String contentType = "multipart/form-data; boundary=" + boundaryValue;
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder(boundaryValue).build();
        // Assert
        assertEquals(contentType, formData.getContentType());
    }

    @Test
    public void appendTextContentLengthMatch() throws IOException {
        // Arrange
        String fieldName = "fieldName";
        String value = "value";
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder().appendText(fieldName, value).build();
        // Assert
        assertEquals(99, readStream(formData.getRequestBody()).length);
        assertEquals(99, formData.getContentLength());
    }

    @Test
    public void appendFileContentLengthMatch() throws IOException {
        // Arrange
        String fieldName = "fieldName";
        Path filePath = Paths.get("src/test/resources/upload.txt");
        byte[] fileBytes = BinaryData.fromFile(filePath).toBytes();
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder()
            .appendFile(fieldName, BinaryData.fromBytes(fileBytes), ContentType.APPLICATION_OCTET_STREAM, "upload.txt")
            .build();
        // Assert
        assertEquals(200, readStream(formData.getRequestBody()).length);
        assertEquals(200, formData.getContentLength());
    }

    @Test
    public void appendMultipleFiles() throws IOException {
        // Arrange
        String fieldName = "fieldName";

        Path filePath1 = Paths.get("src/test/resources/upload.txt");
        byte[] fileBytes1 = BinaryData.fromFile(filePath1).toBytes();
        Path filePath2 = Paths.get("src/test/resources/CloudEvent/CloudEventBinaryData.json");
        byte[] fileBytes2 = BinaryData.fromFile(filePath2).toBytes();

        List<BinaryData> files = new ArrayList<>();
        files.add(BinaryData.fromBytes(fileBytes1));
        files.add(BinaryData.fromBytes(fileBytes2));

        List<String> contentTypes = new ArrayList<>();
        contentTypes.add(ContentType.APPLICATION_OCTET_STREAM);
        contentTypes.add(ContentType.APPLICATION_JSON);

        List<String> filenames = new ArrayList<>();
        filenames.add("upload.txt");
        filenames.add("CloudEventBinaryData.json");
        // Act
        MultipartFormData formData
            = new MultipartFormDataBuilder().appendFiles(fieldName, files, contentTypes, filenames).build();
        // Assert
        assertEquals(644, readStream(formData.getRequestBody()).length);
        assertEquals(644, formData.getContentLength());
    }

    @Test
    public void appendJson() throws IOException {
        // Arrange
        String fieldName = "fieldName";
        String jsonString = "{\"name\": \"John Doe\", \"age\":\"40\"}";
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder().appendJson(fieldName, jsonString).build();
        // Assert
        assertEquals(168, readStream(formData.getRequestBody()).length);
        assertEquals(168, formData.getContentLength());
    }

    @Test
    public void appendMultipleParts() throws IOException {
        // Arrange
        String jsonString = "{\"name\": \"John Doe\", \"age\":\"40\"}";
        Path filePath = Paths.get("src/test/resources/upload.txt");
        byte[] fileBytes = BinaryData.fromFile(filePath).toBytes();
        // Act
        MultipartFormData formData = new MultipartFormDataBuilder()
            .appendFile("file", BinaryData.fromBytes(fileBytes), ContentType.APPLICATION_OCTET_STREAM, "upload.txt")
            .appendJson("JSON", jsonString)
            .appendText("text", "value")
            .build();
        // Assert
        assertEquals(412, readStream(formData.getRequestBody()).length);
        assertEquals(412, formData.getContentLength());
    }
}
