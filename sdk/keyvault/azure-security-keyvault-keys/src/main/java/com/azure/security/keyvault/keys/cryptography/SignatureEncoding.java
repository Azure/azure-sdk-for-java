// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

final class SignatureEncoding {
    // SignatureEncoding is intended to be a static class
    private SignatureEncoding() { }

    private static final char[] HEX_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /*
     * Converts an ASN.1 DER encoded ECDSA signature to a raw signature in the form R|S
     * @param asn1DerSignature An ASN.1 DER encoded signature
     * @param algorithm The algorithm used to produce the given ASN.1 DER encoded signature
     * @return The raw format of the given ASN.1 DER encoded signature in the form R|S
     */
    static byte[] fromAsn1Der(byte[] asn1DerSignature, String algorithm) throws NoSuchAlgorithmException {
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        // verify the given algoritm could be resolved
        if (baseAlgorithm == null) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        // verify the given algoritm is an Ecdsa signature algorithm
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
            throw (IllegalArgumentException) new IllegalArgumentException(
                ex.getMessage() + " " + Arrays.toString(encodeHex(asn1DerSignature, HEX_LOWER))).initCause(ex);
        }
    }

    /*
     * Converts a raw ECDSA signature in the form R|S to an ASN.1 DER encoded signature.
     * @param signature A raw ECDSA signature in the form R|S.
     * @param algorithm The algorithm used to produce the given signature.
     * @return The ASN.1 DER encoded signature of the given signature.
     */
    static byte[] toAsn1Der(byte[] signature, String algorithm) throws NoSuchAlgorithmException {
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        // verify the given algoritm could be resolved
        if (baseAlgorithm == null) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        // verify the given algoritm is an Ecdsa signature algorithm
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
            throw (IllegalArgumentException) new IllegalArgumentException(
                ex.getMessage() + " " + Arrays.toString(encodeHex(signature, HEX_LOWER))).initCause(ex);
        }
    }

    private static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int j = 0; i < l; ++i) {
            out[j++] = toDigits[(240 & data[i]) >>> 4];
            out[j++] = toDigits[15 & data[i]];
        }

        return out;
    }
}


final class Asn1DerSignatureEncoding {
    // the EDCSA ASN.1 DER signature is in the format:
    // 0x30 b1 0x02 b2 (vr) 0x02 b3 (vs)
    // where:
    //      * b1 one or more bytes equal to the length, in bytes, of the remaining list of bytes (from the first 0x02
    //      to the end of the encoding)
    //      * b2 one or more bytes equal to the length, in bytes, of (vr)
    //      * b3 one or more bytes equal to the length, in bytes, of (vs)
    //     (vr) is the signed big-endian encoding of the value "r", of minimal length
    //     (vs) is the signed big-endian encoding of the value "s", of minimal length
    //
    //      * lengths which are less than 0x80 can be expressed in one byte.  For lengths greater then 0x80 the first
    //      byte denotes the
    //        length in bytes of the length with the most significant bit masked off, i.e. 0x81 denotes the length is
    //        one byte long.

    private Asn1DerSignatureEncoding() {

    }

    static byte[] encode(byte[] signature, Ecdsa algorithm) {
        int coordLength = algorithm.getCoordLength();

        // verify that the signature is the correct length for the given algorithm
        if (signature.length != (coordLength * 2)) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        // r is the first half of the signature
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, signature.length / 2));

        // s is the second half of the signature
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, signature.length / 2, signature.length));

        // vr and vs are the compacted ASN.1 integer encoding, same as BigInteger encoding
        byte[] rfield = encodeIntField(r);

        byte[] sfield = encodeIntField(s);

        ByteArrayOutputStream asn1DerSignature = new ByteArrayOutputStream();

        asn1DerSignature.write(0x30);

        // add the length of the fields
        writeFieldLength(asn1DerSignature, rfield.length + sfield.length);

        // write the fields
        asn1DerSignature.write(rfield, 0, rfield.length);

        asn1DerSignature.write(sfield, 0, sfield.length);

        return asn1DerSignature.toByteArray();
    }

    static byte[] decode(byte[] bytes, Ecdsa algorithm) {
        int coordLength = algorithm.getCoordLength();

        ByteArrayInputStream asn1DerSignature = new ByteArrayInputStream(bytes);

        // verify byte 0 is 0x30
        if (asn1DerSignature.read() != 0x30) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        int objLen = readFieldLength(asn1DerSignature);

        // verify the object lenth is equal to the remaining length of the
        // _asn1DerSignature
        if (objLen != asn1DerSignature.available()) {
            throw new IllegalArgumentException(String.format("Invalid signature; invalid field len %d", objLen));
        }

        byte[] rawSignature = new byte[coordLength * 2];

        // decode the r feild to the first half of _rawSignature
        decodeIntField(asn1DerSignature, rawSignature, 0, coordLength);

        // decode the s feild to the second half of _rawSignature
        decodeIntField(asn1DerSignature, rawSignature, rawSignature.length / 2, coordLength);

        return rawSignature;
    }

    static byte[] encodeIntField(BigInteger i) {
        ByteArrayOutputStream field = new ByteArrayOutputStream();

        field.write(0x02);

        // get this byte array for the asn1 encoded integer
        byte[] vi = i.toByteArray();

        // write the length of the field
        writeFieldLength(field, vi.length);

        // write the field value
        field.write(vi, 0, vi.length);

        return field.toByteArray();
    }

    static void writeFieldLength(ByteArrayOutputStream field, int len) {
        // if the length of vi is less then 0x80 we can fit the length in one byte
        if (len < 0x80) {
            field.write(len);
        } else {
            // get the len as a byte array
            byte[] blen = BigInteger.valueOf(len).toByteArray();

            int lenlen = blen.length;

            // the byte array might have a leading zero byte if so we need to discard this
            if (blen[0] == 0) {
                lenlen--;
            }

            // write the continuation byte containing the length length in bytes
            field.write(0x80 | lenlen);

            // write the field lenth bytes
            field.write(blen, blen.length - lenlen, lenlen);
        }
    }

    static void decodeIntField(ByteArrayInputStream bytes, byte[] dest, int index, int intlen) {
        // verify the first byte of field is 0x02
        if (bytes.read() != 0x02) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        //get the length of the field
        int len = readFieldLength(bytes);

        // if the most significant bit of the raw int was set an extra zero byte will be prepended to
        // the asn1der encoded value so len can have a max value of intlen + 1

        // validate that that len is within the max range and doesn't run past the end of bytes
        if (len > intlen + 1 || len > bytes.available()) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        // if len is greater than intlen increment _bytesRead and decrement len
        if (len > intlen) {
            bytes.skip(1);
            len--;
        }

        bytes.read(dest, index + (intlen - len), len);
    }

    static int readFieldLength(ByteArrayInputStream bytes) {
        int firstLenByte = bytes.read();

        // if the high order bit of len is not set it is a single byte length so return
        if ((firstLenByte & 0x80) == 0x00) {
            return firstLenByte;
        }

        // otherwise mask off the high order bit to get the number of bytes to read
        int numLenBytes = firstLenByte ^ 0x80;

        // if the number of len bytes is greater than the remaining signature the signature is invalid
        if (numLenBytes > bytes.available()) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        byte[] lenBytes = new byte[numLenBytes];

        bytes.read(lenBytes, 0, numLenBytes);

        BigInteger bigLen = new BigInteger(1, lenBytes);

        // for DSA signatures no feilds should be longer than can be expressed in an integer
        // this means that the bitLength must be 31 or less to account for the leading zero of
        // a positive integer
        if (bigLen.bitLength() >= 31) {
            throw new IllegalArgumentException("Invalid signature.");
        }

        return bigLen.intValue();
    }
}

