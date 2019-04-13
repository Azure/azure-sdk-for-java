// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.test;

/**
 * The possible testing modes.
 */
public enum TestMode {
    /**
     * Record data from a live test.
     */
    RECORD,
    /**
     * Playback data from an existing test session.
     */
    PLAYBACK,
}
