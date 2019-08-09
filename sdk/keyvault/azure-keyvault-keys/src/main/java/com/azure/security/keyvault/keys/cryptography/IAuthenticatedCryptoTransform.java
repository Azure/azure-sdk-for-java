// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

interface IAuthenticatedCryptoTransform extends ICryptoTransform {

    byte[] getTag();
}
