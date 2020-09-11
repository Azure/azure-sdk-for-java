// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

/**
 * Refs: https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
 */
public class AADAccessTokenClaim {

    /**
     * Provides a human-readable value that identifies the subject of the token. The value is not guaranteed to be
     * unique, it is mutable, and it's designed to be used only for display purposes. The profile scope is required in
     * order to receive this claim.
     */
    public static final String NAME = "name";
}
