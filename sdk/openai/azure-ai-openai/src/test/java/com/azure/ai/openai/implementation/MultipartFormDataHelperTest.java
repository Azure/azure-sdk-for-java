// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MultipartFormDataHelper}
 */
public class MultipartFormDataHelperTest {
    @Test
    public void testSerializeJsonField() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();

        // Act
        MultipartFormDataHelper multipartRequest
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", "sunny")
                .serializeJsonField("temperatureCelsius", 24);

        BinaryData requestBody = multipartRequest.end().getRequestBody();

        // Assert
        assertTrue(requestBody.toString().contains("Content-Disposition: form-data; name=\"weather\""));
        assertTrue(requestBody.toString().contains("Content-Disposition: form-data; name=\"temperatureCelsius\""));
        assertTrue(requestBody.toString().contains("Content-Type: application/json"));
        assertTrue(requestBody.toString().contains("sunny"));
        assertTrue(requestBody.toString().contains("24"));
    }

    @Test
    public void testSerializeJsonFieldNull() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();

        // Act
        MultipartFormDataHelper multipartRequest
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", null);

        BinaryData requestBody = multipartRequest.end().getRequestBody();

        // Assert
        assertFalse(requestBody.toString().contains("Content-Disposition: form-data; name=\"weather\""));
        assertFalse(requestBody.toString().contains("Content-Type: application/json"));
    }

    @Test
    public void testSerializeFileFields() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();
        MultipartFormDataHelper multipartRequestOne
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", "sunny");
        MultipartFormDataHelper multipartRequestTwo
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", "rainy");
        MultipartFormDataHelper multipartRequestThree
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", "cloudy");
        BinaryData binaryDataOne = multipartRequestOne.end().getRequestBody();
        BinaryData binaryDataTwo = multipartRequestTwo.end().getRequestBody();
        BinaryData binaryDataThree = multipartRequestThree.end().getRequestBody();

        List<BinaryData> files = Arrays.asList(binaryDataOne, binaryDataTwo, binaryDataThree);

        List<String> contentTypes = Arrays.asList(null, "", "application/mocktype");

        List<String> filenames = Arrays.asList("Sunny", "Rainy", "Cloudy");

        // Act
        MultipartFormDataHelper multipartFormDataHelper = new MultipartFormDataHelper(multipartRequestOptions)
            .serializeFileFields("weather", files, contentTypes, filenames);

        BinaryData requestBody = multipartFormDataHelper.end().getRequestBody();

        // Assert
        assertTrue(requestBody.toString().contains("name=\"weather\"; filename=\"Sunny\""));
        assertTrue(requestBody.toString().contains("name=\"weather\"; filename=\"Rainy\""));
        assertTrue(requestBody.toString().contains("name=\"weather\"; filename=\"Cloudy\""));
        assertTrue(requestBody.toString().contains("application/octet-stream"));
        assertTrue(requestBody.toString().contains("application/mocktype"));
    }

    @Test
    public void testSerializeFileFieldsNullFile() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();

        List<String> contentTypes = Arrays.asList(null, "", "application/mocktype");

        List<String> filenames = Arrays.asList("Sunny", "Rainy", "Cloudy");

        // Act
        MultipartFormDataHelper multipartFormDataHelper = new MultipartFormDataHelper(multipartRequestOptions)
            .serializeFileFields("weather", null, contentTypes, filenames);

        BinaryData requestBody = multipartFormDataHelper.end().getRequestBody();

        // Assert
        assertFalse(requestBody.toString().contains("name=\"weather\"; filename=\"Sunny\""));
        assertFalse(requestBody.toString().contains("name=\"weather\"; filename=\"Rainy\""));
        assertFalse(requestBody.toString().contains("name=\"weather\"; filename=\"Cloudy\""));
        assertFalse(requestBody.toString().contains("application/octet-stream"));
        assertFalse(requestBody.toString().contains("application/mocktype"));
    }

    @Test
    public void testSerializeFileFieldNullContentType() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();
        MultipartFormDataHelper multipartRequestSunny
            = new MultipartFormDataHelper(multipartRequestOptions).serializeJsonField("weather", "sunny");
        BinaryData file = multipartRequestSunny.end().getRequestBody();

        // Act
        MultipartFormDataHelper multipartFormDataHelper
            = new MultipartFormDataHelper(multipartRequestOptions).serializeFileField("weather", file, null, "Sunny");

        BinaryData requestBody = multipartFormDataHelper.end().getRequestBody();

        // Assert
        assertTrue(requestBody.toString().contains("name=\"weather\"; filename=\"Sunny\""));
        assertTrue(requestBody.toString().contains("application/octet-stream"));
    }

    @Test
    public void testSerializeFileFieldNullFile() {
        // Arrange
        RequestOptions multipartRequestOptions = new RequestOptions();

        // Act
        MultipartFormDataHelper multipartFormDataHelper = new MultipartFormDataHelper(multipartRequestOptions)
            .serializeFileField("weather", null, "application/mocktype", "Sunny");

        BinaryData requestBody = multipartFormDataHelper.end().getRequestBody();

        // Assert
        assertFalse(requestBody.toString().contains("name=\"weather\"; filename=\"Sunny\""));
        assertFalse(requestBody.toString().contains("application/mocktype"));
    }
}
