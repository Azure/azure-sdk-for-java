// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

public interface MultipartBoundaryGenerator {
    String generateBoundary();
}
