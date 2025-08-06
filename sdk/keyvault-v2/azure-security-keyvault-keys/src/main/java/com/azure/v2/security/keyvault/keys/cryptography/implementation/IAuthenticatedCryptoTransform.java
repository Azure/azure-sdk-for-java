// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

interface IAuthenticatedCryptoTransform extends ICryptoTransform {
    byte[] getTag();
}
