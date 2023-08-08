/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.util.ListResourceBundle;


/**
 * Represents a simple resource bundle containing the strings for localizing.
 *
 */
public final class MicrosoftDataEncryptionExceptionResource extends ListResourceBundle {

    /**
     * Fetches an error message from the package.
     *
     * @param key
     *        identifier of the message
     * @return error message
     */
    public static String getResource(String key) {
        return MicrosoftDataEncryptionExceptionResource.getBundle("com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionExceptionResource").getString(key);
    }

    protected Object[][] getContents() {
        return CONTENTS;
    }

    static final Object[][] CONTENTS = {{"R_EncryptionFailed", "Encryption failed. The last 10 bytes of the encrypted data encryption key are: '{0}'. "},
            {"R_DecryptionFailed", "Decryption failed. The last 10 bytes of the encrypted data encryption key are: '{0}'. The first 10 bytes of ciphertext are: '{1}'"},
            {"R_NullEncryptedDataEncryptionKey", "Value cannot be null. (Parameter 'encryptedDataEncryptionKey [java.lang.Byte[]]')"},
            {"R_EmptyEncryptedDataEncryptionKey", "encryptedDataEncryptionKey cannot be empty. "},
            {"R_NullEncryptionSettings", "Value cannot be null. (Parameter 'encryptionSettings [Com.Microsoft.Data.Encryption.Cryptography.EncryptionSettings]') "},
            {"R_PlaintextEncryptionSettings", "The {0} EncryptionType cannot be Plaintext in this context. "},
            {"R_InvalidCipherTextSize",
                    "Specified ciphertext has an invalid size of {0} bytes, which is below the minimum {1} bytes required for decryption."},
            {"R_InvalidAlgorithmVersion",
                    "The specified ciphertext's encryption algorithm version {0} does not match the expected encryption algorithm version '01'. "},
            {"R_InvalidDataEncryptionKey",
                    "name cannot be null or empty or consist of only whitespace. "},
            {"R_InvalidKeySize", "rootKey must contain {0} elements. "}, {"R_IllegalOffset", "Illegal offset minutes."},
            {"R_InvalidDataEncryptionKeySize", "The data encryption key has been successfully decrypted but its length: {0} does not match the length: 32 for algorithm 'AEAD_AES_256_CBC_HMAC_SHA256'. Verify the encrypted value of the data encryption key. "},
            {"R_IllegalNanos", "Illegal nanosecond values."},
            {"R_NullDataEncryptionKey", "Value cannot be null. (Parameter 'dataEncryptionKey [Com.Microsoft.Data.Encryption.Cryptography.DataEncryptionKey]') "},
            {"R_EmptyDataEncryptionKey", "Empty data encryption key specified. "},
            {"R_NullKeyEncryptionAlgorithm", "Key encryption algorithm cannot be null."},
            {"R_InvalidKeyEncryptionAlgorithm",
                    "Invalid key encryption algorithm specified: {0}. Expected value: {1}."},
            {"R_InvalidKeyEncryptionKeyDetails", "Invalid key encryption key details specified."},
            {"R_DEKSignatureNotMatchKEK",
                    "The specified encrypted data encryption key signature does not match the signature computed with the key encryption key in \"{0}\". The encrypted data encryption key may be corrupt, or the specified path may be incorrect."},
            {"R_ByteToShortConversion", "Error occurred while decrypting data encryption key."},
            {"R_InvalidValueToType", "The given value from the data source cannot be converted to type {0}."},
            {"R_InvalidSerializerName", "There is no serializer that maps to the class name {0}. "},
            {"R_InvalidDataType", "Invalid data type provided : {0}"},
            {"R_InvalidType", "Encryption and decryption of data type {0} is not supported."},
            {"R_InvalidEncoding", "The encoding {0} is not supported."},
            {"R_NullString", "Null strings are not accepted"},
            {"R_InvalidTimestampFormat", "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]"},
            {"R_InvalidTemporalValue", "Invalid temporal value provided."},
            {"R_UnexpectedSourceType", "Unexpected source data type: {0}."},
            {"R_UnexpectedTargetType", "Unexpected target data type: {0}."},
            {"R_InvalidTemporalValue", "Invalid temporal value provided."},
            {"R_KeystoreProviderError", "An error has occured while getting keystore provider."},
            {"R_CannotBeNull", "{0} cannot be null."}, {"R_CannotBeNullOrWhiteSpace", "{0} cannot be null or empty or consist of only whitespace. "},
            {"R_InvalidPSLength",
                    "Value ''{0}'' of type {1} is invalid for the expected precision of {2} and scale of {3}."},
            {"R_valueOutOfRange", "One or more values is out of range of values for the {0} data type."},
            {"R_parameterOutOfRange", "Parameter value {0} is out of range."},
            {"R_HashNull", "Hash should not be null while decrypting encrypted data encryption key."},
            {"R_SignedHashLengthError", "Signed hash length does not match the RSA key size."},
            {"R_NoSHA256Algorithm", "SHA-256 Algorithm is not supported."},
            {"R_AKVPathNull", "Azure Key Vault key path cannot be null."},
            {"R_AKVURLInvalid", "Invalid URL specified: {0}."},
            {"R_AKVKeyEncryptionKeyPathInvalid", "Invalid Azure Key Vault key path specified: {0}."},
            {"R_AKVKeyNotFound", "The key with identifier {0} was not found."}, {"R_NonRSAKey", "Cannot use a non-RSA key: {0}."},
            {"R_AKVKeyLengthError",
                    "The specified encrypted data encryption key''s ciphertext length: {0} does not match the ciphertext length: {1} when using key encryption key (Azure Key Vault key) in {2}. "
                            + "The encrypted data encryption key may be corrupt, or the specified Azure Key Vault key path may be incorrect."},
            {"R_AKVSignatureLengthError",
                    "The specified encrypted data encryption key''s signature length: {0} does not match the signature length: {1} when using key encryption key (Azure Key Vault key) in {2}. "
                            + "The encrypted data encryption key may be corrupt, or the specified Azure Key Vault key path may be incorrect."},
            {"R_InvalidSignatureComputed", "Invalid signature of the encrypted data encryption key computed."},};
};
