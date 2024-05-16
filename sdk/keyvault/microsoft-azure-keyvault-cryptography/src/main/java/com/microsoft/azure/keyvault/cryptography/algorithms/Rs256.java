// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.microsoft.azure.keyvault.cryptography.ByteExtensions;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;

/**
 *
 */
public class Rs256 extends RsaSignature {

    static final String RSA_NONE = "RSA/ECB/PKCS1Padding";

    class Rs256SignatureTransform implements ISignatureTransform {

        private final KeyPair  keyPair;
        private final int      emLen;

        Rs256SignatureTransform(KeyPair keyPair) {
            this.keyPair = keyPair;

            BigInteger modulus = ((RSAPublicKey) keyPair.getPublic()).getModulus();

            this.emLen = getOctetLength(modulus.bitLength());
        }

        @Override
        public byte[] sign(byte[] digest) throws NoSuchAlgorithmException {
            // Signing isn't just a case of encrypting the digest, there is much more to do.
            // For details of the algorithm, see https://tools.ietf.org/html/rfc3447#section-8.2

            // TODO
            // if (keyPair.getPrivate() == null) {
            // }

            // Construct the encoded message
            byte[] em = EMSA_PKCS1_V1_5_ENCODE_HASH(digest, emLen, "SHA-256");

            // Convert to integer message
            BigInteger s = OS2IP(em);

            // RSASP1(s)
            s = RSASP1((RSAPrivateKey) keyPair.getPrivate(), s);

            // Convert to octet sequence
            return I2OSP(s, emLen);
        }

        @Override
        public boolean verify(byte[] digest, byte[] signature) throws NoSuchAlgorithmException {

            if (signature.length != emLen) {
                throw new IllegalArgumentException("invalid signature length");
            }

            // Convert to integer signature
            BigInteger s = OS2IP(signature);

            // Convert integer message
            BigInteger m = RSAVP1((RSAPublicKey) keyPair.getPublic(), s);

            byte[] em  = I2OSP(m, emLen);
            byte[] em2 = EMSA_PKCS1_V1_5_ENCODE_HASH(digest, emLen, "SHA-256");

            // Use constant time compare
            return ByteExtensions.sequenceEqualConstantTime(em, em2);
        }

    }

    public static final String ALGORITHM_NAME = "RS256";

    public Rs256() {
        super(ALGORITHM_NAME);
    }

    @Override
    public ISignatureTransform createSignatureTransform(KeyPair keyPair) {

        return new Rs256SignatureTransform(keyPair);
    }
}
