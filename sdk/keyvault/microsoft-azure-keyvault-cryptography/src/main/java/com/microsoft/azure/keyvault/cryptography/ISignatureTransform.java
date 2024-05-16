// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

import java.security.GeneralSecurityException;

public interface ISignatureTransform {

    byte[] sign(final byte[] digest) throws GeneralSecurityException;

    boolean verify(final byte[] digest, final byte[] signature) throws GeneralSecurityException;
}
