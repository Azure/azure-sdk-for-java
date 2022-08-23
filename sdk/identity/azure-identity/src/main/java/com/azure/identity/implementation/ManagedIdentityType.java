// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

/**
 * Enum used to represent different Managed Identity platforms.
 */
public enum ManagedIdentityType {
    VM,
    APP_SERVICE,
    SERVICE_FABRIC,
    ARC,
    AKS;
}
