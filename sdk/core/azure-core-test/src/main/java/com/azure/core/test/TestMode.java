// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

/**
 * The possible testing modes.
 */
public enum TestMode {
    /**
     * Record data from a live test.
     */
    RECORD,
    /**
     * Run a live test without recording.
     */
    LIVE,
    /**
     * Playback data from an existing test session.
     */
    PLAYBACK,
}
