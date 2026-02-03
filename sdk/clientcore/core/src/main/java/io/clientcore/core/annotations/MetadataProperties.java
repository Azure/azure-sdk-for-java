// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.annotations;

/**
 * Enum that defines the conditions that can be applied to a class.
 */
public enum MetadataProperties {
    /**
     * Indicates that a class is expected to provide a fluent API to end users. If a class is marked as this, checks can
     * be made to ensure all APIs meet this expectation. Similarly, classes that are not marked as fluent should not
     * have fluent APIs.
     */
    FLUENT,

    /**
     * Indicates that a class is immutable. If a class is marked as this, checks can be made to ensure all fields in
     * the class are final.
     */
    IMMUTABLE,

    /**
     * Indicates that a class is generated and will be overwritten by the code generator if modified.
     */
    GENERATED
}
