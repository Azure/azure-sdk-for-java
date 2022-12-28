// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * Keeps track of different sanitizers that redact the sensitive information when recording
 */
public interface TestProxySanitizer {

    TestProxySanitizerType getType();

    String getRedactedValue();

    String getRegex();
}
