/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.azurekeyvaultprovider;



import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionExceptionResource;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;


/**
 *
 * This class holds information about the certificate
 *
 */
class CertificateDetails {
    X509Certificate certificate;
    Key privateKey;

    CertificateDetails(X509Certificate certificate, Key privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }
}


/**
 * Utility class used internally for keystore providers.
 */
class KeyStoreProviderCommon {

    static final String rsaEncryptionAlgorithmWithOAEP = "RSA_OAEP";
    static byte[] version = new byte[] {0x01};

    /**
     * Utility method for validating encryption algorithm.
     *
     * @param encryptionAlgorithm
     *        name of algorithm
     * @param isEncrypt
     *        whether it is encrypted
     * @throws MicrosoftDataEncryptionException
     *         if an exception occurs
     */
    static void validateEncryptionAlgorithm(String encryptionAlgorithm,
                                            boolean isEncrypt) throws MicrosoftDataEncryptionException {
        if (null == encryptionAlgorithm) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullKeyEncryptionAlgorithm"));
        }

        if (!rsaEncryptionAlgorithmWithOAEP.equalsIgnoreCase(encryptionAlgorithm.trim())) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidKeyEncryptionAlgorithm"));
            Object[] msgArgs = {encryptionAlgorithm, rsaEncryptionAlgorithmWithOAEP};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Utility method for validating non empty path
     *
     * @param keyEncryptionKeyPath
     *        path
     * @throws MicrosoftDataEncryptionException
     *         if an exception occurs
     */
    static void validateNonEmptyKeyEncryptionKeyPath(
            String keyEncryptionKeyPath) throws MicrosoftDataEncryptionException {
        if (null == keyEncryptionKeyPath || keyEncryptionKeyPath.trim().length() == 0) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidKeyEncryptionKeyDetails"));
        }
    }

    /**
     * Utility method for decrypting
     *
     * @param keyEncryptionKeyPath
     *        path
     * @param encryptionAlgorithm
     *        algorithm
     * @param encryptedDataEncryptionKey
     *        DEK
     * @param certificateDetails
     *        certificate details
     * @return decrypted key
     * @throws MicrosoftDataEncryptionException
     *         if an exception occurs
     */
    static byte[] decryptDataEncryptionKey(String keyEncryptionKeyPath, String encryptionAlgorithm,
                                           byte[] encryptedDataEncryptionKey,
                                           CertificateDetails certificateDetails) throws MicrosoftDataEncryptionException {
        if (null == encryptedDataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullDataEncryptionKey"));
        } else if (0 == encryptedDataEncryptionKey.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_EmptyDataEncryptionKey"));
        }

        validateEncryptionAlgorithm(encryptionAlgorithm, false);

        int currentIndex = version.length;
        int keyPathLength = convertTwoBytesToShort(encryptedDataEncryptionKey, currentIndex);
        // We just read 2 bytes
        currentIndex += 2;

        // Get ciphertext length
        int cipherTextLength = convertTwoBytesToShort(encryptedDataEncryptionKey, currentIndex);
        currentIndex += 2;

        currentIndex += keyPathLength;

        int signatureLength = encryptedDataEncryptionKey.length - currentIndex - cipherTextLength;

        // Get ciphertext
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(encryptedDataEncryptionKey, currentIndex, cipherText, 0, cipherTextLength);
        currentIndex += cipherTextLength;

        byte[] signature = new byte[signatureLength];
        System.arraycopy(encryptedDataEncryptionKey, currentIndex, signature, 0, signatureLength);

        byte[] hash = new byte[encryptedDataEncryptionKey.length - signature.length];

        System.arraycopy(encryptedDataEncryptionKey, 0, hash, 0, encryptedDataEncryptionKey.length - signature.length);

        if (!verifyRSASignature(hash, signature, certificateDetails.certificate, keyEncryptionKeyPath)) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_DEKSignatureNotMatchKEK"));
            Object[] msgArgs = {keyEncryptionKeyPath};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        byte[] plainDEK = decryptRSAOAEP(cipherText, certificateDetails);

        return plainDEK;
    }

    private static byte[] decryptRSAOAEP(byte[] cipherText,
            CertificateDetails certificateDetails) throws MicrosoftDataEncryptionException {
        byte[] plainDEK = null;
        try {
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, certificateDetails.privateKey);
            rsa.update(cipherText);
            plainDEK = rsa.doFinal();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_ByteToShortConversion"));
            Object[] msgArgs = {e.getMessage()};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        return plainDEK;

    }

    /**
     *
     * @param hash
     *        hash value
     * @param signature
     *        signature value
     * @param certificate
     *        certificate value
     * @param keyEncryptionKeyPath
     *        path
     * @return boolean value indicating its legitimacy
     * @throws MicrosoftDataEncryptionException
     *         if an exception occurs
     */
    static boolean verifyRSASignature(byte[] hash, byte[] signature, X509Certificate certificate,
            String keyEncryptionKeyPath) throws MicrosoftDataEncryptionException {
        Signature signVerify;
        boolean verificationSuccess = false;
        try {
            signVerify = Signature.getInstance("SHA256withRSA");
            signVerify.initVerify(certificate.getPublicKey());
            signVerify.update(hash);
            verificationSuccess = signVerify.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_DEKSignatureNotMatchKEK"));
            Object[] msgArgs = {keyEncryptionKeyPath};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        return verificationSuccess;

    }

    private static short convertTwoBytesToShort(byte[] input, int index) throws MicrosoftDataEncryptionException {

        short shortVal;
        if (index + 1 >= input.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_ByteToShortConversion"));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(input[index]);
        byteBuffer.put(input[index + 1]);
        shortVal = byteBuffer.getShort(0);
        return shortVal;

    }
}
