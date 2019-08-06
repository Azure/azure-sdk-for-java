// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Signature;

abstract class Ecdsa extends AsymmetricSignatureAlgorithm {

    protected Ecdsa() {
        super("NONEwithECDSA");
    }

    public ISignatureTransform createSignatureTransform(KeyPair key, Provider provider) {
        return new EcdsaSignatureTransform(key, provider, this);
    }

    public abstract int getDigestLength();
    public abstract int getCoordLength();

    class EcdsaSignatureTransform implements ISignatureTransform {
        private static final String ALGORITHM = "NONEwithECDSA";
        private final KeyPair keyPair;
        private final Provider provider;
        private final Ecdsa algorithm;

        EcdsaSignatureTransform(KeyPair keyPair, Provider provider, Ecdsa algorithm) {
            this.keyPair = keyPair;
            this.provider = provider;
            this.algorithm = algorithm;
        }

        @Override
        public byte[] sign(byte[] digest) throws GeneralSecurityException {
            checkDigestLength(digest);
            Signature signature = Signature.getInstance(ALGORITHM, provider);
            signature.initSign(keyPair.getPrivate());
            signature.update(digest);
            return SignatureEncoding.fromAsn1Der(signature.sign(), algorithm);
        }

        @Override
        public boolean verify(byte[] digest, byte[] signature) throws GeneralSecurityException {
            Signature verify = Signature.getInstance(ALGORITHM, provider);
            checkDigestLength(digest);
            signature = SignatureEncoding.toAsn1Der(signature, algorithm);
            verify.initVerify(keyPair.getPublic());
            verify.update(digest);
            return verify.verify(signature);
        }

        private void checkDigestLength(byte[] digest) {
            if (digest.length != getDigestLength()) {
                throw new IllegalArgumentException("Invalid digest length.");
            }
        }
    }
}
