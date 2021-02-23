// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

/**
 * The data part of a {@link CloudEvent} can be serialized to base64 bytes, JSON, or String when a CloudEvent
 * is serialized.
 * Use {@link #BYTES} if you'd like to serialize the CloudEvent data to base64 bytes. Use {@link #JSON} otherwise.
 */
public enum CloudEventDataFormat {
    BYTES,
    JSON,
}
