// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.incubating.EnduserIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class EnduserIncubatingAttributes {
    /**
     * Unique identifier of an end user in the system. It maybe a username, email address, or other
     * identifier.
     *
     * <p>Notes:
     *
     * <p>Unique identifier of an end user in the system.
     *
     * <blockquote>
     *
     * [!Warning] This field contains sensitive (PII) information.
     *
     * </blockquote>
     */
    public static final AttributeKey<String> ENDUSER_ID = stringKey("enduser.id");

    /**
     * Pseudonymous identifier of an end user. This identifier should be a random value that is not
     * directly linked or associated with the end user's actual identity.
     *
     * <p>Notes:
     *
     * <p>Pseudonymous identifier of an end user.
     *
     * <blockquote>
     *
     * [!Warning] This field contains sensitive (linkable PII) information.
     *
     * </blockquote>
     */
    public static final AttributeKey<String> ENDUSER_PSEUDO_ID = stringKey("enduser.pseudo.id");

    /**
     * Deprecated, use {@code user.roles} instead.
     *
     * @deprecated Replaced by {@code user.roles}.
     */
    @Deprecated
    public static final AttributeKey<String> ENDUSER_ROLE = stringKey("enduser.role");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> ENDUSER_SCOPE = stringKey("enduser.scope");

    // Enum definitions

    private EnduserIncubatingAttributes() {
    }
}
