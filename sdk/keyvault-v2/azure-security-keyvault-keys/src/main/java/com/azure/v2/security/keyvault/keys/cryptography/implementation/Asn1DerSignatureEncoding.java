// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static io.clientcore.core.utils.CoreUtils.bytesToHexString;

final class Asn1DerSignatureEncoding {
    private static final ClientLogger LOGGER = new ClientLogger(Asn1DerSignatureEncoding.class);
    // The EDCSA ASN.1 DER signature is in the format:
    // 0x30 b1 0x02 b2 (vr) 0x02 b3 (vs)
    // where:
    //      * b1 one or more bytes equal to the length, in bytes, of the remaining list of bytes (from the first 0x02
    //      to the end of the encoding)
    //      * b2 one or more bytes equal to the length, in bytes, of (vr)
    //      * b3 one or more bytes equal to the length, in bytes, of (vs)
    //     (vr) is the signed big-endian encoding of the value "r", of minimal length
    //     (vs) is the signed big-endian encoding of the value "s", of minimal length
    //
    //      * lengths which are less than 0x80 can be expressed in one byte. For lengths greater than 0x80 the first
    //      byte denotes the length in bytes of the length with the most significant bit masked off, i.e. 0x81 denotes
    //      the length is one byte long.

    private Asn1DerSignatureEncoding() {
    }

    static byte[] encode(byte[] signature, Ecdsa algorithm) {
        int coordLength = algorithm.getCoordLength();

        // Verify that the signature is the correct length for the given algorithm.
        if (signature.length != (coordLength * 2)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("signature", bytesToHexString(signature))
                .addKeyValue("len", signature.length)
                .log("Invalid signature; invalid length.", CoreException::from);
        }

        // r is the first half of the signature.
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, signature.length / 2));
        // s is the second half of the signature.
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, signature.length / 2, signature.length));
        // vr and vs are the compacted ASN.1 integer encoding, same as BigInteger encoding.
        byte[] rfield = encodeIntField(r);
        byte[] sfield = encodeIntField(s);

        ByteArrayOutputStream asn1DerSignature = new ByteArrayOutputStream();

        asn1DerSignature.write(0x30);
        // Add the length of the fields.
        writeFieldLength(asn1DerSignature, rfield.length + sfield.length);
        // Write the fields.
        asn1DerSignature.write(rfield, 0, rfield.length);
        asn1DerSignature.write(sfield, 0, sfield.length);

        return asn1DerSignature.toByteArray();
    }

    static byte[] decode(byte[] bytes, Ecdsa algorithm) {
        int coordLength = algorithm.getCoordLength();

        ByteArrayInputStream asn1DerSignature = new ByteArrayInputStream(bytes);

        // Verify byte 0 is 0x30.
        if (asn1DerSignature.read() != 0x30) {
            throw LOGGER.throwableAtError()
                .addKeyValue("signature", bytesToHexString(bytes))
                .log("Invalid signature; First byte of field is not 0x30.", CoreException::from);
        }

        int objLen = readFieldLength(asn1DerSignature);

        // Verify the object length is equal to the remaining length of the _asn1DerSignature.
        if (objLen != asn1DerSignature.available()) {
            throw LOGGER.throwableAtError()
                .addKeyValue("signature", bytesToHexString(bytes))
                .addKeyValue("len", objLen)
                .log("Invalid signature; invalid field len.", CoreException::from);
        }

        byte[] rawSignature = new byte[coordLength * 2];

        // Decode the r field to the first half of _rawSignature.
        decodeIntField(asn1DerSignature, rawSignature, 0, coordLength);
        // Decode the s field to the second half of _rawSignature.
        decodeIntField(asn1DerSignature, rawSignature, rawSignature.length / 2, coordLength);

        return rawSignature;
    }

    static byte[] encodeIntField(BigInteger i) {
        ByteArrayOutputStream field = new ByteArrayOutputStream();

        field.write(0x02);

        // Get this byte array for the asn1 encoded integer.
        byte[] vi = i.toByteArray();

        // Write the length of the field.
        writeFieldLength(field, vi.length);
        // Write the field value.
        field.write(vi, 0, vi.length);

        return field.toByteArray();
    }

    static void writeFieldLength(ByteArrayOutputStream field, int len) {
        // If the length of vi is less than 0x80 we can fit the length in one byte.
        if (len < 0x80) {
            field.write(len);
        } else {
            // Get the len as a byte array
            byte[] blen = BigInteger.valueOf(len).toByteArray();
            int lenlen = blen.length;

            // The byte array might have a leading zero byte if so we need to discard this.
            if (blen[0] == 0) {
                lenlen--;
            }

            // Write the continuation byte containing the length in bytes.
            field.write(0x80 | lenlen);
            // Write the field length bytes.
            field.write(blen, blen.length - lenlen, lenlen);
        }
    }

    static void decodeIntField(ByteArrayInputStream bytes, byte[] dest, int index, int intlen) {
        // Verify the first byte of field is 0x02.
        if (bytes.read() != 0x02) {
            throw LOGGER.throwableAtError()
                .log("Invalid signature; First byte of field is not 0x02.", CoreException::from);
        }

        // Get the length of the field.
        int len = readFieldLength(bytes);

        // If the most significant bit of the raw int was set an extra zero byte will be prepended to the asn1der
        // encoded value so len can have a max value of intlen + 1.

        // Validate that len is within the max range and doesn't run past the end of bytes.
        if (len > intlen + 1 || len > bytes.available()) {
            throw LOGGER.throwableAtError()
                .addKeyValue("len", len)
                .addKeyValue("intlen", intlen)
                .addKeyValue("bytesAvailable", bytes.available())
                .log("Invalid signature; invalid field len.", CoreException::from);
        }

        // If len is greater than intlen increment _bytesRead and decrement len.
        if (len > intlen) {
            bytes.skip(1);
            len--;
        }

        bytes.read(dest, index + (intlen - len), len);
    }

    static int readFieldLength(ByteArrayInputStream bytes) {
        int firstLenByte = bytes.read();

        // If the high order bit of len is not set it is a single byte length so return.
        if ((firstLenByte & 0x80) == 0x00) {
            return firstLenByte;
        }

        // Otherwise mask off the high order bit to get the number of bytes to read.
        int numLenBytes = firstLenByte ^ 0x80;

        // If the number of len bytes is greater than the remaining signature the signature is invalid.
        if (numLenBytes > bytes.available()) {
            throw LOGGER.throwableAtError()
                .addKeyValue("numLenBytes", numLenBytes)
                .addKeyValue("bytesAvailable", bytes.available())
                .log("Invalid signature; invalid field len.", CoreException::from);
        }

        byte[] lenBytes = new byte[numLenBytes];

        bytes.read(lenBytes, 0, numLenBytes);

        BigInteger bigLen = new BigInteger(1, lenBytes);

        // For DSA signatures no fields should be longer than can be expressed in an integer this means that the
        // bitLength must be 31 or less to account for the leading zero of a positive integer.
        if (bigLen.bitLength() >= 31) {
            throw LOGGER.throwableAtError()
                .addKeyValue("bigLen", bigLen.toString())
                .log("Invalid signature; invalid field len.", CoreException::from);
        }

        return bigLen.intValue();
    }
}
