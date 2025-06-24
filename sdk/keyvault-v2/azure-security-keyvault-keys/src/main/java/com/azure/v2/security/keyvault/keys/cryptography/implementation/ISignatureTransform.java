// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import java.security.GeneralSecurityException;

interface ISignatureTransform {

    byte[] sign(byte[] digest) throws GeneralSecurityException;

    boolean verify(byte[] digest, byte[] signature) throws GeneralSecurityException;
}
