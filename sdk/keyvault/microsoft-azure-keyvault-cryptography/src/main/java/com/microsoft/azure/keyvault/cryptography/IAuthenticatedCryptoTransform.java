// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

public interface IAuthenticatedCryptoTransform extends ICryptoTransform {

    byte[] getTag();
}
