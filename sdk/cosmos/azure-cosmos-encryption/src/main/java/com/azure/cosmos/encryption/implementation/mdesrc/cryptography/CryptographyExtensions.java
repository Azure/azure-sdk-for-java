/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * Utility class for cryptography operations.
 *
 */
@SuppressWarnings({"unchecked", "cast"})
public final class CryptographyExtensions {

    private final static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F'};
    private static final StandardSerializerFactory STANDARD_SERIALIZER_FACTORY = new StandardSerializerFactory();

    private CryptographyExtensions() {}

    /**
     * Encrypts the given plaintext according to the specified type, using the provided encryption key.
     *
     * @param <T>
     *        Type of the class
     * @param plaintext
     *        plaintext data
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return encrypted byte array
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> byte[] encrypt(T plaintext, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws MicrosoftDataEncryptionException {
        if (null == encryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = (Serializer<T>) STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);
        byte[] serializedData = serializer.serialize(plaintext);
        return encryptionAlgorithm.encrypt(serializedData);
    }

    /**
     * Decrypts the given ciphertext according to the specified type, using the provided encryption key.
     *
     * @param <T>
     *        Type of the class
     * @param ciphertext
     *        encrypted data
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return plaintext data
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> T decrypt(byte[] ciphertext, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws MicrosoftDataEncryptionException {
        if (null == encryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);
        byte[] plaintextData = encryptionAlgorithm.decrypt(ciphertext);
        return serializer.deserialize(plaintextData);
    }

    /**
     * Encrypts the given plaintext according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param plaintext
     *        plaintext data
     * @param encryptionSettings
     *        encryption settings
     * @return encrypted byte array
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> byte[] encrypt(T plaintext,
            EncryptionSettingsImpl<T> encryptionSettings) throws MicrosoftDataEncryptionException {
        if (null == encryptionSettings) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_PlaintextEncryptionSettings"));
            Object[] msgArgs = {"encryptionSettings"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), encryptionSettings.getEncryptionType());
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();
        byte[] serializedData = serializer.serialize(plaintext);
        return encryptionAlgorithm.encrypt(serializedData);
    }

    /**
     * Decrypts the given ciphertext according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param ciphertext
     *        encrypted data
     * @param encryptionSettings
     *        encryption settings
     * @return plaintext data
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> T decrypt(byte[] ciphertext,
            EncryptionSettingsImpl<T> encryptionSettings) throws MicrosoftDataEncryptionException {
        if (null == encryptionSettings) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_PlaintextEncryptionSettings"));
            Object[] msgArgs = {"encryptionSettings"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), EncryptionType.Plaintext);
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();
        byte[] plaintextData = encryptionAlgorithm.decrypt(ciphertext);
        return serializer.deserialize(plaintextData);
    }

    /**
     * Encrypts the given Iterable plaintext source according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param source
     *        iterable plaintext data source
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return Iterable containing encrypted byte array
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> Iterable<byte[]> encrypt(Iterable<T> source, DataEncryptionKey encryptionKey,
                                               Class<T> typeParameter) throws MicrosoftDataEncryptionException {
        if (null == encryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);

        List<byte[]> r = new ArrayList<byte[]>();

        for (T item : source) {
            byte[] serializedData = serializer.serialize(item);
            r.add(encryptionAlgorithm.encrypt(serializedData));
        }

        return r;
    }

    /**
     * Decrypts the given Iterable ciphertext source according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param source
     *        iterable ciphertext data source
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return Iterable containing decrypted data
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> Iterable<T> decrypt(Iterable<byte[]> source, DataEncryptionKey encryptionKey,
                                          Class<T> typeParameter) throws MicrosoftDataEncryptionException {
        if (null == encryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);

        List<T> r = new ArrayList<T>();

        for (byte[] item : source) {
            byte[] plaintextData = encryptionAlgorithm.decrypt(item);
            r.add(serializer.deserialize(plaintextData));
        }

        return r;
    }

    /**
     * Encrypts the given object according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param source
     *        iterable data source
     * @param encryptionSettings
     *        encryption settings
     * @return Iterable containing encrypted byte array
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> Iterable<byte[]> encrypt(Iterable<T> source,
                                               EncryptionSettingsImpl<T> encryptionSettings) throws MicrosoftDataEncryptionException {
        if (null == encryptionSettings) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_PlaintextEncryptionSettings"));
            Object[] msgArgs = {"encryptionSettings"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), encryptionSettings.getEncryptionType());
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();

        List<byte[]> r = new ArrayList<byte[]>();

        for (T item : source) {
            byte[] serializedData = serializer.serialize(item);
            r.add(encryptionAlgorithm.encrypt(serializedData));
        }

        return r;
    }

    /**
     * Decrypts the given ciphertext according to the specified type.
     *
     * @param <T>
     *        Type of the class
     * @param source
     *        iterable ciphertext data source
     * @param encryptionSettings
     *        encryption settings
     * @return Iterable containing decrypted data
     * @throws MicrosoftDataEncryptionException
     *         if encryptionKey is null
     */
    public static <T> Iterable<T> decrypt(Iterable<byte[]> source,
                                          EncryptionSettingsImpl<T> encryptionSettings) throws MicrosoftDataEncryptionException {
        if (null == encryptionSettings) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_PlaintextEncryptionSettings"));
            Object[] msgArgs = {"encryptionSettings"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), EncryptionType.Plaintext);
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();

        List<T> r = new ArrayList<T>();

        for (byte[] item : source) {
            byte[] plaintextData = encryptionAlgorithm.decrypt(item);
            r.add(serializer.deserialize(plaintextData));
        }

        return r;
    }

    /**
     * Converts an array of bytes to its equivalent string representation that is encoded with base-64 digits.
     *
     * @param source
     *        An array of bytes.
     * @return The string representation, in base 64, of the contents of source.
     */
    public static String toBase64String(byte[] source) {
        byte[] encoded = Base64.getEncoder().encode(source);
        return new String(encoded);
    }

    /**
     * Converts the specified string, which encodes binary data as base-64 digits, to an equivalent byte array.
     *
     * @param source
     *        The string to convert.
     * @return An array of bytes that is equivalent to source.
     */
    public static byte[] fromBase64String(String source) {
        return Base64.getDecoder().decode(source);
    }

    /**
     * Converts each byte array in the source sequence to its equivalent string representation that is encoded with
     * base-64 digits.
     *
     * @param source
     *        A sequence of byte arrays to convert.
     * @return An Iterable of String whose elements are the result of being encoded with base-64 digits.
     */
    public static Iterable<String> toBase64String(Iterable<byte[]> source) {
        List<String> r = new ArrayList<String>();
        for (byte[] item : source) {
            r.add(toBase64String(item));
        }
        return r;
    }

    /**
     * Converts each String element of source, which encodes binary data as base-64 digits, to an equivalent byte array.
     *
     * @param source
     *        A sequence of strings to convert.
     * @return An Iterable of byte arrays that is equivalent to source.
     */
    public static Iterable<byte[]> fromBase64String(Iterable<String> source) {
        List<byte[]> r = new ArrayList<byte[]>();
        for (String item : source) {
            r.add(fromBase64String(item));
        }

        return r;
    }

    /**
     * Converts the numeric value of each element of a specified array of bytes to its equivalent hexadecimal string
     * representation.
     *
     * @param source
     *        An array of bytes to convert.
     * @return A string of hexadecimal characters
     */
    public static String toHexString(byte[] source) {
        if (null == source) {
            return null;
        }

        StringBuilder sb = new StringBuilder(source.length * 2);
        for (int i = 0; i < source.length; i++) {
            int hexVal = source[i] & 0xFF;
            sb.append(hexChars[(hexVal & 0xF0) >> 4]);
            sb.append(hexChars[(hexVal & 0x0F)]);
        }
        return sb.toString();
    }

    /**
     * Converts the numeric value of each element of a specified array of bytes to its equivalent hexadecimal string
     * representation, with dashes in between.
     *
     * @param source
     *        An array of bytes to convert.
     * @return A string of hexadecimal characters
     */
    static String toHexStringWithDashes(byte[] source) {
        if (null == source) {
            return null;
        }

        StringBuilder sb = new StringBuilder(source.length * 2);
        for (int i = 0; i < source.length; i++) {
            int hexVal = source[i] & 0xFF;
            sb.append(hexChars[(hexVal & 0xF0) >> 4]);
            sb.append(hexChars[(hexVal & 0x0F)]);
            if (i < source.length - 1) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    /**
     * Converts the string representation of a number in hexidecimal to an equivalent array of bytes.
     *
     * @param source
     *        The string to convert.
     * @return An array of bytes that is equivalent to source.
     */
    public static byte[] fromHexString(String source) {
        if (null == source) {
            return null;
        }

        int len = source.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(source.charAt(i), 16) << 4)
                    + Character.digit(source.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts each byte array in the source sequence to its equivalent string representation that is encoded with
     * hexidecimal digits.
     *
     * @param source
     *        A sequence of byte arrays to convert.
     * @return An Iterable of String whose elements are the result of being encoded with hexidecimal digits.
     */
    public static Iterable<String> toHexString(Iterable<byte[]> source) {
        List<String> r = new ArrayList<String>();
        for (byte[] item : source) {
            r.add(toHexString(item));
        }
        return r;
    }

    /**
     * Converts each String element of source, which encodes binary data as hexidecimal digits, to an equivalent byte
     * array.
     *
     * @param source
     *        A sequence of strings to convert.
     * @return An Iterable of byte arrays that is equivalent to source.
     */
    public static Iterable<byte[]> fromHexString(Iterable<String> source) {
        List<byte[]> r = new ArrayList<byte[]>();
        for (String item : source) {
            r.add(fromHexString(item));
        }

        return r;
    }
}
