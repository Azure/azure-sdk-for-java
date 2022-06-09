package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

public enum DataEncryptionKeyAlgorithm
{
    /// <summary>
    /// Represents the authenticated encryption algorithm with associated data as described in
    /// http://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05.
    /// </summary>
    AEAD_AES_256_CBC_HMAC_SHA256
}
