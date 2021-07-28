// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.security.SignatureSpi;

/**
 * KeyVault Signature to key less sign
 */
public abstract class AbstractKeyVaultKeyLessSignature extends SignatureSpi {

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineInitVerify.
    @Override
    protected void engineInitVerify(PublicKey publicKey) {
        throw new UnsupportedOperationException("engineInitVerify() not supported");
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineVerify.
    @Override
    protected boolean engineVerify(byte[] signature) {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as ECDSASignature#engineGetParameter.
    @Override
    @Deprecated
    protected Object engineGetParameter(String param)
        throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineSetParameter.
    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value)
        throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

}
