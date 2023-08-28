// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.CoreUtils;

import java.security.NoSuchAlgorithmException;

final class SignatureEncoding {
    // SignatureEncoding is intended to be a static class
    private SignatureEncoding() { }


    /*
     * Converts an ASN.1 DER encoded ECDSA signature to a raw signature in the form R|S
     * @param asn1DerSignature An ASN.1 DER encoded signature
     * @param algorithm The algorithm used to produce the given ASN.1 DER encoded signature
     * @return The raw format of the given ASN.1 DER encoded signature in the form R|S
     */
    static byte[] fromAsn1Der(byte[] asn1DerSignature, String algorithm) throws NoSuchAlgorithmException {
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm);

        // verify the given algorithm could be resolved
        if (baseAlgorithm == null) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        // verify the given algorithm is an Ecdsa signature algorithm
        if (!(baseAlgorithm instanceof Ecdsa)) {
            throw new IllegalArgumentException("Invalid algorithm; must be an instance of ECDSA.");
        }

        return SignatureEncoding.fromAsn1Der(asn1DerSignature, (Ecdsa) baseAlgorithm);
    }

    /*
     * Converts an ASN.1 DER encoded ECDSA signature to a raw signature in the form R|S
     * @param asn1DerSignature An ASN.1 DER encoded signature
     * @param algorithm The algorithm used to produce the given ASN.1 DER encoded signature
     * @return The raw format of the given ASN.1 DER encoded signature in the form R|S
     */
    static byte[] fromAsn1Der(byte[] asn1DerSignature, Ecdsa algorithm) {

        try {
            return Asn1DerSignatureEncoding.decode(asn1DerSignature, algorithm);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex.getMessage() + " " + CoreUtils.bytesToHexString(asn1DerSignature),
                ex);
        }
    }

    /*
     * Converts a raw ECDSA signature in the form R|S to an ASN.1 DER encoded signature.
     * @param signature A raw ECDSA signature in the form R|S.
     * @param algorithm The algorithm used to produce the given signature.
     * @return The ASN.1 DER encoded signature of the given signature.
     */
    static byte[] toAsn1Der(byte[] signature, String algorithm) throws NoSuchAlgorithmException {
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm);

        // verify the given algorithm could be resolved
        if (baseAlgorithm == null) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        // verify the given algorithm is an Ecdsa signature algorithm
        if (!(baseAlgorithm instanceof Ecdsa)) {
            throw new IllegalArgumentException("Invalid algorithm; must be an instance of ECDSA.");
        }

        return SignatureEncoding.toAsn1Der(signature, (Ecdsa) baseAlgorithm);
    }

    /*
     * Converts a raw ECDSA signature in the form R|S to an ASN.1 DER encoded signature.
     * @param signature A raw ECDSA signature in the form R|S.
     * @param algorithm The algorithm used to produce the given signature.
     * @return The ASN.1 DER encoded signature of the given signature.
     */
    static byte[] toAsn1Der(byte[] signature, Ecdsa algorithm) {
        try {
            return Asn1DerSignatureEncoding.encode(signature, algorithm);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex.getMessage() + " " + CoreUtils.bytesToHexString(signature), ex);
        }
    }
}
