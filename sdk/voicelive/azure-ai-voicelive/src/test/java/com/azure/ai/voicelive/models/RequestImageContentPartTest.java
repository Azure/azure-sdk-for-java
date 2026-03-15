// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link RequestImageContentPart}.
 */
class RequestImageContentPartTest {

    @Test
    void testConstructor() {
        // Act
        RequestImageContentPart imagePart = new RequestImageContentPart();

        // Assert
        assertNotNull(imagePart);
        assertEquals(ContentPartType.INPUT_IMAGE, imagePart.getType());
    }

    @Test
    void testSetAndGetUrl() {
        // Arrange
        RequestImageContentPart imagePart = new RequestImageContentPart();
        String imageUrl = "https://example.com/image.jpg";

        // Act
        RequestImageContentPart result = imagePart.setUrl(imageUrl);

        // Assert
        assertEquals(imageUrl, imagePart.getUrl());
        assertEquals(imagePart, result); // Fluent API
    }

    @Test
    void testSetAndGetDetail() {
        // Arrange
        RequestImageContentPart imagePart = new RequestImageContentPart();

        // Act
        RequestImageContentPart result = imagePart.setDetail(RequestImageContentPartDetail.HIGH);

        // Assert
        assertEquals(RequestImageContentPartDetail.HIGH, imagePart.getDetail());
        assertEquals(imagePart, result); // Fluent API
    }

    @Test
    void testFromJsonWithUrl() {
        // Arrange
        String json = "{\"type\":\"input_image\",\"url\":\"https://example.com/test.png\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        RequestImageContentPart imagePart = data.toObject(RequestImageContentPart.class);

        // Assert
        assertNotNull(imagePart);
        assertEquals(ContentPartType.INPUT_IMAGE, imagePart.getType());
        assertEquals("https://example.com/test.png", imagePart.getUrl());
        assertNull(imagePart.getDetail());
    }

    @Test
    void testFromJsonWithAllFields() {
        // Arrange
        String json = "{\"type\":\"input_image\",\"url\":\"https://example.com/image.jpg\",\"detail\":\"high\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        RequestImageContentPart imagePart = data.toObject(RequestImageContentPart.class);

        // Assert
        assertNotNull(imagePart);
        assertEquals(ContentPartType.INPUT_IMAGE, imagePart.getType());
        assertEquals("https://example.com/image.jpg", imagePart.getUrl());
        assertEquals(RequestImageContentPartDetail.HIGH, imagePart.getDetail());
    }

    @Test
    void testJsonRoundTrip() {
        // Arrange
        RequestImageContentPart original = new RequestImageContentPart().setUrl("https://example.com/photo.png")
            .setDetail(RequestImageContentPartDetail.AUTO);

        // Act
        BinaryData data = BinaryData.fromObject(original);
        RequestImageContentPart deserialized = data.toObject(RequestImageContentPart.class);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getUrl(), deserialized.getUrl());
        assertEquals(original.getDetail(), deserialized.getDetail());
    }

    @Test
    void testWithLowDetail() {
        // Arrange
        String json = "{\"type\":\"input_image\",\"url\":\"https://example.com/low-res.jpg\",\"detail\":\"low\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        RequestImageContentPart imagePart = data.toObject(RequestImageContentPart.class);

        // Assert
        assertEquals(RequestImageContentPartDetail.LOW, imagePart.getDetail());
    }

    @Test
    void testWithAutoDetail() {
        // Arrange
        String json = "{\"type\":\"input_image\",\"url\":\"https://example.com/auto.jpg\",\"detail\":\"auto\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        RequestImageContentPart imagePart = data.toObject(RequestImageContentPart.class);

        // Assert
        assertEquals(RequestImageContentPartDetail.AUTO, imagePart.getDetail());
    }

    @Test
    void testFluentApi() {
        // Act
        RequestImageContentPart imagePart = new RequestImageContentPart().setUrl("https://example.com/fluent.png")
            .setDetail(RequestImageContentPartDetail.HIGH);

        // Assert
        assertNotNull(imagePart);
        assertEquals("https://example.com/fluent.png", imagePart.getUrl());
        assertEquals(RequestImageContentPartDetail.HIGH, imagePart.getDetail());
    }
}
