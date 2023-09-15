// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

/**
 * Interface implemented by classes that would generate a boundary string to use in multipart type of requests.
 * The main purpose of this class is to allow to mock behaviour for tests
 */
public interface MultipartBoundaryGenerator {

    /**
     * Generates a new multipart boundary value each time the method is called
     * @return a {@link String} value containing a boundary to be used in HTTP multipart requests
     */
    String generateBoundary();
}
