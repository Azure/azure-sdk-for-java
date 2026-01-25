// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

/**
 * Defines supported authentication strategies for blob storage clients.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public enum AuthenticationStrategy {
    /**
     * No authentication (anonymous access).
     */
    ANONYMOUS,

    /**
     * Storage shared key credential only.
     */
    SHARED_KEY,

    /**
     * Token credential only.
     */
    TOKEN,

    /**
     * SAS token or Azure SAS credential only.
     */
    SAS,

    /**
     * Token credential combined with SAS token/credential (for delegated user scenarios).
     */
    TOKEN_WITH_SAS
}
