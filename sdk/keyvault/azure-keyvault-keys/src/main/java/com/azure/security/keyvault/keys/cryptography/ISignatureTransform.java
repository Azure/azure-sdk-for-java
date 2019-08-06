// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.security.GeneralSecurityException;

interface ISignatureTransform {

    byte[] sign(byte[] digest) throws GeneralSecurityException;

    boolean verify(byte[] digest, byte[] signature) throws GeneralSecurityException;
}
