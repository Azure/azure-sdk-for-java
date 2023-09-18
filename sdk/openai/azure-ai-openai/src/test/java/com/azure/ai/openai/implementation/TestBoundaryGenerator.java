// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

/**
 * Stub implementation for the sake of testing for {@link MultipartDataHelper} constructor argument
 * to test multipart/form-data payloads
 */
public class TestBoundaryGenerator implements MultipartBoundaryGenerator {

    @Override
    public String generateBoundary() {
        return "test-boundary";
    }
}
