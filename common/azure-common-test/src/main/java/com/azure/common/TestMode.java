package com.azure.common;

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
