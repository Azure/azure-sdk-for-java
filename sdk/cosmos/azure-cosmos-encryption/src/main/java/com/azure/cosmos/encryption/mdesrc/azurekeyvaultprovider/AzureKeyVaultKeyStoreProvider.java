/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.mdesrc.azurekeyvaultprovider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.cosmos.encryption.mdesrc.cryptography.EncryptionKeyStoreProvider;
import com.azure.cosmos.encryption.mdesrc.cryptography.KeyEncryptionKeyAlgorithm;
import com.azure.cosmos.encryption.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_16LE;


/**
 * Provides an implementation for an Azure key store provider. A DEK encrypted with a key store provider
 * should be decryptable by this provider and vice versa.
 *
 * Envelope Format for the encrypted data encryption key: version + keyPathLength + ciphertextLength + keyPath +
 * ciphertext + signature.
 *
 * version: A single byte indicating the format version.
 *
 * keyPathLength: Length of the keyPath.
 *
 * ciphertextLength: ciphertext length.
 *
 * keyPath: keyPath used to encrypt the data encryption key. This is only used for troubleshooting purposes,
 * and is not verified during decryption.
 *
 * ciphertext: Encrypted data encryption key.
 *
 * signature: Signature of the entire byte array. Signature is validated before decrypting the data encryption key.
 */
public class AzureKeyVaultKeyStoreProvider extends EncryptionKeyStoreProvider {

    private static final int KEY_NAME_INDEX = 4;
    private static final int KEY_URL_SPLIT_LENGTH_WITH_VERSION = 6;
    private static final String KEY_URL_DELIMITER = "/";
    private HttpPipeline keyVaultPipeline;

    /**
     * Data Encryption Key Store Provider string
     */
    String providerName = "AZURE_KEY_VAULT";

    private static final String RSA_ENCRYPTION_ALGORITHM_WITH_OAEP_FOR_AKV = "RSA-OAEP";
    private static List<String> akvTrustedEndpoints = getTrustedEndpoints();

    /**
     * Algorithm version
     */
    private final byte[] firstVersion = new byte[] {0x01};

    private Map<String, KeyClient> cachedKeyClients = new ConcurrentHashMap<>();
    private Map<String, CryptographyClient> cachedCryptographyClients = new ConcurrentHashMap<>();
    private TokenCredential credential;

    /**
     * Setter for name of the key vault.
     *
     * @param name
     *        String value of name
     */
    public void setName(String name) {
        this.providerName = name;
    }

    /**
     * Getter for name of the key vault.
     *
     * @return provider name
     */
    public String getName() {
        return this.providerName;
    }

    /**
     * Getter for name of the key vault provider.
     *
     * @return provider name
     */
    @Override
    public String getProviderName() {
        return this.providerName;
    }

    /**
     * Constructs an AzureKeyVaultKeyStoreProvider using the provided TokenCredential to authenticate to Azure AD. This
     * is used by the KeyVault client at runtime to authenticate to Azure Key Vault.
     *
     * @param tokenCredential
     *        The TokenCredential to use to authenticate to Azure Key Vault.
     *
     * @throws MicrosoftDataEncryptionException
     *         when an error occurs
     */
    public AzureKeyVaultKeyStoreProvider(TokenCredential tokenCredential) throws MicrosoftDataEncryptionException {
        if (null == tokenCredential) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Token Credential"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        setCredential(tokenCredential);
    }

    /**
     * Constructs an AzureKeyVaultKeyStoreProvider using the provided TokenCredential to authenticate to Azure AD. This
     * is used by the KeyVault client at runtime to authenticate to Azure Key Vault.
     *
     * @param tokenCredential
     *        The TokenCredential to use to authenticate to Azure Key Vault.
     * @param endpoints
     *        String array of trusted Azure Key Vault endpoints.
     * @throws MicrosoftDataEncryptionException
     *         when an error occurs
     */
    public AzureKeyVaultKeyStoreProvider(TokenCredential tokenCredential,
                                         String[] endpoints) throws MicrosoftDataEncryptionException {
        if (null == tokenCredential) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Token Credential"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        setCredential(tokenCredential);
        akvTrustedEndpoints = Arrays.asList(endpoints);
    }

    /**
     * Sets the credential that will be used for authenticating requests to the Key Vault service.
     *
     * @param credential
     *        A credential of type {@link TokenCredential}.
     * @throws MicrosoftDataEncryptionException
     *         If the credential is null.
     */
    private void setCredential(TokenCredential credential) throws MicrosoftDataEncryptionException {
        if (null == credential) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Credential"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        this.credential = credential;
    }

    /**
     * Decrypts an encrypted DEK with the RSA encryption algorithm using the asymmetric key specified by the key path
     *
     * @param encryptionKeyId
     *        - Complete path of an asymmetric key in Azure Key Vault
     * @param encryptionAlgorithm
     *        - Asymmetric Key Encryption Algorithm
     * @param encryptedDataEncryptionKey
     *        - Encrypted Data Encryption Key
     * @return Plain text data encryption key
     */
    @Override
    public byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm encryptionAlgorithm,
                            byte[] encryptedDataEncryptionKey) throws MicrosoftDataEncryptionException {

        // Validate the input parameters
        this.validateNonEmptyAKVPath(encryptionKeyId);

        if (null == encryptedDataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NullEncryptedDataEncryptionKey"));
        }

        if (0 == encryptedDataEncryptionKey.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_EmptyEncryptedDataEncryptionKey"));
        }

        // Validate encryptionAlgorithm
        KeyWrapAlgorithm keyWrapAlgorithm = this.validateEncryptionAlgorithm(encryptionAlgorithm.toString());

        // Validate whether the key is RSA one or not and then get the key size
        int keySizeInBytes = getAKVKeySize(encryptionKeyId);

        // Validate and decrypt the EncryptedDataEncryptionKey
        // Format is
        // version + keyPathLength + ciphertextLength + keyPath + ciphertext + signature
        //
        // keyPath is present in the encrypted data encryption key for identifying the
        // original source of the
        // asymmetric key pair and
        // we will not validate it against the data contained in the KEK metadata
        // (encryptionKeyId).

        // Validate the version byte
        if (encryptedDataEncryptionKey[0] != firstVersion[0]) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_InvalidAlgorithmVersion"));
            Object[] msgArgs = {String.format("%02X ", encryptedDataEncryptionKey[0]),
                    String.format("%02X ", firstVersion[0])};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        // Get key path length
        int currentIndex = firstVersion.length;
        short keyPathLength = convertTwoBytesToShort(encryptedDataEncryptionKey, currentIndex);
        // We just read 2 bytes
        currentIndex += 2;

        // Get ciphertext length
        short cipherTextLength = convertTwoBytesToShort(encryptedDataEncryptionKey, currentIndex);
        currentIndex += 2;

        // Skip KeyPath
        // KeyPath exists only for troubleshooting purposes and doesnt need validation.
        currentIndex += keyPathLength;

        // validate the ciphertext length
        if (cipherTextLength != keySizeInBytes) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_AKVKeyLengthError"));
            Object[] msgArgs = {cipherTextLength, keySizeInBytes, encryptionKeyId};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        // Validate the signature length
        int signatureLength = encryptedDataEncryptionKey.length - currentIndex - cipherTextLength;

        if (signatureLength != keySizeInBytes) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_AKVSignatureLengthError"));
            Object[] msgArgs = {signatureLength, keySizeInBytes, encryptionKeyId};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        // Get ciphertext
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(encryptedDataEncryptionKey, currentIndex, cipherText, 0, cipherTextLength);
        currentIndex += cipherTextLength;

        // Get signature
        byte[] signature = new byte[signatureLength];
        System.arraycopy(encryptedDataEncryptionKey, currentIndex, signature, 0, signatureLength);

        // Compute the hash to validate the signature
        byte[] hash = new byte[encryptedDataEncryptionKey.length - signature.length];

        System.arraycopy(encryptedDataEncryptionKey, 0, hash, 0, encryptedDataEncryptionKey.length - signature.length);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NoSHA256Algorithm"));
        }
        md.update(hash);
        byte dataToVerify[] = md.digest();

        if (null == dataToVerify) {
            throw new MicrosoftDataEncryptionException(MicrosoftDataEncryptionException.getErrString("R_HashNull"));
        }

        // Validate the signature
        if (!azureKeyVaultVerifySignature(dataToVerify, signature, encryptionKeyId)) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_DEKSignatureNotMatchKEK"));
            Object[] msgArgs = {encryptionKeyId};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        // Decrypt the DEK
        byte[] decryptedDEK = this.azureKeyVaultUnWrap(encryptionKeyId, keyWrapAlgorithm, cipherText);

        return decryptedDEK;
    }

    private short convertTwoBytesToShort(byte[] input, int index) throws MicrosoftDataEncryptionException {

        short shortVal;
        if (index + 1 >= input.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_ByteToShortConversion"));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(input[index]);
        byteBuffer.put(input[index + 1]);
        shortVal = byteBuffer.getShort(0);
        return shortVal;

    }

    /**
     * Encrypts a DEK with the RSA encryption algorithm using the asymmetric key specified by the key path.
     *
     * @param encryptionKeyId
     *        - Complete path of an asymmetric key in Azure Key Vault
     * @param encryptionAlgorithm
     *        - Asymmetric Key Encryption Algorithm
     * @param dataEncryptionKey
     *        - Plain text data encryption key
     * @return Encrypted data encryption key
     */
    @Override
    public byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm encryptionAlgorithm,
                          byte[] dataEncryptionKey) throws MicrosoftDataEncryptionException {

        // Validate the input parameters
        this.validateNonEmptyAKVPath(encryptionKeyId);

        if (null == dataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NullDataEncryptionKey"));
        }

        if (0 == dataEncryptionKey.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_EmptyDataEncryptionKey"));
        }

        // Validate encryptionAlgorithm
        KeyWrapAlgorithm keyWrapAlgorithm = this.validateEncryptionAlgorithm(encryptionAlgorithm.toString());

        // Validate whether the key is RSA one or not and then get the key size
        int keySizeInBytes = getAKVKeySize(encryptionKeyId);

        /*
         * Construct the encryptedDataEncryptionKey Format is version + keyPathLength + ciphertextLength + ciphertext +
         * keyPath + signature. We currently only support one version
         */
        byte[] version = new byte[] {firstVersion[0]};

        // Get the Unicode encoded bytes of cultureinvariant lower case encryptionKeyId
        byte[] encryptionKeyIdBytes = encryptionKeyId.toLowerCase(Locale.ENGLISH).getBytes(UTF_16LE);

        byte[] keyPathLength = new byte[2];
        keyPathLength[0] = (byte) (((short) encryptionKeyIdBytes.length) & 0xff);
        keyPathLength[1] = (byte) (((short) encryptionKeyIdBytes.length) >> 8 & 0xff);

        // Encrypt the plain text
        byte[] cipherText = this.azureKeyVaultWrap(encryptionKeyId, keyWrapAlgorithm, dataEncryptionKey);

        byte[] cipherTextLength = new byte[2];
        cipherTextLength[0] = (byte) (((short) cipherText.length) & 0xff);
        cipherTextLength[1] = (byte) (((short) cipherText.length) >> 8 & 0xff);

        if (cipherText.length != keySizeInBytes) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_SignedHashLengthError"));
        }

        /*
         * Compute hash SHA-2-256(version + keyPathLength + ciphertextLength + keyPath + ciphertext)
         */
        byte[] dataToHash = new byte[version.length + keyPathLength.length + cipherTextLength.length
                + encryptionKeyIdBytes.length + cipherText.length];
        int destinationPosition = version.length;
        System.arraycopy(version, 0, dataToHash, 0, version.length);

        System.arraycopy(keyPathLength, 0, dataToHash, destinationPosition, keyPathLength.length);
        destinationPosition += keyPathLength.length;

        System.arraycopy(cipherTextLength, 0, dataToHash, destinationPosition, cipherTextLength.length);
        destinationPosition += cipherTextLength.length;

        System.arraycopy(encryptionKeyIdBytes, 0, dataToHash, destinationPosition, encryptionKeyIdBytes.length);
        destinationPosition += encryptionKeyIdBytes.length;

        System.arraycopy(cipherText, 0, dataToHash, destinationPosition, cipherText.length);

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NoSHA256Algorithm"));
        }
        md.update(dataToHash);
        byte dataToSign[] = md.digest();

        // Sign the hash
        byte[] signedHash = azureKeyVaultSignHashedData(dataToSign, encryptionKeyId);

        if (signedHash.length != keySizeInBytes) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_SignedHashLengthError"));
        }

        if (!this.azureKeyVaultVerifySignature(dataToSign, signedHash, encryptionKeyId)) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_InvalidSignatureComputed"));
        }

        /*
         * Construct the encrypted data encryption key. EncryptedDataEncryptionKey = version + keyPathLength +
         * ciphertextLength + keyPath + ciphertext + signature
         */
        int encryptedDataEncryptionKeyLength = version.length + cipherTextLength.length + keyPathLength.length
                + cipherText.length + encryptionKeyIdBytes.length + signedHash.length;
        byte[] encryptedDataEncryptionKey = new byte[encryptedDataEncryptionKeyLength];

        // Copy version byte
        int currentIndex = 0;
        System.arraycopy(version, 0, encryptedDataEncryptionKey, currentIndex, version.length);
        currentIndex += version.length;

        // Copy key path length
        System.arraycopy(keyPathLength, 0, encryptedDataEncryptionKey, currentIndex, keyPathLength.length);
        currentIndex += keyPathLength.length;

        // Copy ciphertext length
        System.arraycopy(cipherTextLength, 0, encryptedDataEncryptionKey, currentIndex, cipherTextLength.length);
        currentIndex += cipherTextLength.length;

        // Copy key path
        System.arraycopy(encryptionKeyIdBytes, 0, encryptedDataEncryptionKey, currentIndex,
                encryptionKeyIdBytes.length);
        currentIndex += encryptionKeyIdBytes.length;

        // Copy ciphertext
        System.arraycopy(cipherText, 0, encryptedDataEncryptionKey, currentIndex, cipherText.length);
        currentIndex += cipherText.length;

        // copy the signature
        System.arraycopy(signedHash, 0, encryptedDataEncryptionKey, currentIndex, signedHash.length);

        return encryptedDataEncryptionKey;
    }

    @Override
    public byte[] sign(String encryptionKeyId,
                       boolean allowEnclaveComputations) throws MicrosoftDataEncryptionException {
        KeyStoreProviderCommon.validateNonEmptyKeyEncryptionKeyPath(encryptionKeyId);

        byte[] dataToSign = compileKeyEncryptionKeyMetadata(encryptionKeyId, allowEnclaveComputations);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NoSHA256Algorithm"));
        }
        md.update(dataToSign);
        dataToSign = md.digest();

        return azureKeyVaultSignHashedData(dataToSign, encryptionKeyId);
    }

    /**
     * Validates that the encryption algorithm is RSA_OAEP and if it is not, then throws an exception.
     *
     * @param encryptionAlgorithm
     *        - Asymmetric key encryptio algorithm
     * @return The encryption algorithm that is going to be used.
     * @throws MicrosoftDataEncryptionException
     */
    private KeyWrapAlgorithm validateEncryptionAlgorithm(
            String encryptionAlgorithm) throws MicrosoftDataEncryptionException {

        if (null == encryptionAlgorithm) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NullKeyEncryptionAlgorithm"));
        }

        // Transform to standard format (dash instead of underscore) to support enum
        // lookup
        if ("RSA_OAEP".equalsIgnoreCase(encryptionAlgorithm)) {
            encryptionAlgorithm = RSA_ENCRYPTION_ALGORITHM_WITH_OAEP_FOR_AKV;
        }

        if (!RSA_ENCRYPTION_ALGORITHM_WITH_OAEP_FOR_AKV.equalsIgnoreCase(encryptionAlgorithm.trim())) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_InvalidKeyEncryptionAlgorithm"));
            Object[] msgArgs = {encryptionAlgorithm, RSA_ENCRYPTION_ALGORITHM_WITH_OAEP_FOR_AKV};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        return KeyWrapAlgorithm.fromString(encryptionAlgorithm);
    }

    /**
     * Checks if the Azure Key Vault key path is Empty or Null (and raises exception if they are).
     *
     * @param encryptionKeyId
     * @throws MicrosoftDataEncryptionException
     */
    private void validateNonEmptyAKVPath(String encryptionKeyId) throws MicrosoftDataEncryptionException {
        // throw appropriate error if encryptionKeyId is null or empty
        if (null == encryptionKeyId || encryptionKeyId.trim().isEmpty()) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_AKVPathNull"));
            Object[] msgArgs = {encryptionKeyId};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        } else {
            URI parsedUri = null;
            try {
                parsedUri = new URI(encryptionKeyId);

                // A valid URI.
                // Check if it is pointing to a trusted endpoint.
                String host = parsedUri.getHost();
                if (null != host) {
                    host = host.toLowerCase(Locale.ENGLISH);
                }
                for (final String endpoint : akvTrustedEndpoints) {
                    if (null != host && host.endsWith(endpoint)) {
                        return;
                    }
                }
            } catch (URISyntaxException e) {
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionException.getErrString("R_AKVURLInvalid"));
                Object[] msgArgs = {encryptionKeyId};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }

            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionException.getErrString("R_AKVKeyEncryptionKeyPathInvalid"));
            Object[] msgArgs = {encryptionKeyId};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Encrypts the text using the specified Azure Key Vault key.
     *
     * @param encryptionKeyId
     *        - Azure Key Vault key url.
     * @param encryptionAlgorithm
     *        - Encryption Algorithm.
     * @param dataEncryptionKey
     *        - Plain text Data Encryption Key.
     * @return Returns an encrypted blob or throws an exception if there are any errors.
     * @throws MicrosoftDataEncryptionException
     */
    private byte[] azureKeyVaultWrap(String encryptionKeyId, KeyWrapAlgorithm encryptionAlgorithm,
                                     byte[] dataEncryptionKey) throws MicrosoftDataEncryptionException {
        if (null == dataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NullDataEncryptionKey"));
        }

        CryptographyClient cryptoClient = getCryptographyClient(encryptionKeyId);
        WrapResult wrappedKey = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, dataEncryptionKey);
        return wrappedKey.getEncryptedKey();
    }

    /**
     * Encrypts the text using the specified Azure Key Vault key.
     *
     * @param encryptionKeyId
     *        - Azure Key Vault key url.
     * @param encryptionAlgorithm
     *        - Encrypted Data Encryption Key.
     * @param encryptedDataEncryptionKey
     *        - Encrypted Data Encryption Key.
     * @return Returns the decrypted plaintext Data Encryption Key or throws an exception if there are any errors.
     * @throws MicrosoftDataEncryptionException
     */
    private byte[] azureKeyVaultUnWrap(String encryptionKeyId, KeyWrapAlgorithm encryptionAlgorithm,
                                       byte[] encryptedDataEncryptionKey) throws MicrosoftDataEncryptionException {
        if (null == encryptedDataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NullEncryptedDataEncryptionKey"));
        }

        if (0 == encryptedDataEncryptionKey.length) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_EmptyEncryptedDataEncryptionKey"));
        }

        CryptographyClient cryptoClient = getCryptographyClient(encryptionKeyId);

        UnwrapResult unwrappedKey = cryptoClient.unwrapKey(encryptionAlgorithm, encryptedDataEncryptionKey);

        return unwrappedKey.getKey();
    }

    private CryptographyClient getCryptographyClient(String encryptionKeyId) throws MicrosoftDataEncryptionException {
        if (this.cachedCryptographyClients.containsKey(encryptionKeyId)) {
            return cachedCryptographyClients.get(encryptionKeyId);
        }

        KeyVaultKey retrievedKey = getKeyVaultKey(encryptionKeyId);

        CryptographyClient cryptoClient;
        if (null != credential) {
            cryptoClient = new CryptographyClientBuilder().credential(credential).keyIdentifier(retrievedKey.getId())
                    .buildClient();
        } else {
            cryptoClient = new CryptographyClientBuilder().pipeline(keyVaultPipeline)
                    .keyIdentifier(retrievedKey.getId()).buildClient();
        }
        cachedCryptographyClients.putIfAbsent(encryptionKeyId, cryptoClient);
        return cachedCryptographyClients.get(encryptionKeyId);
    }

    /**
     * Generates a signature based on the RSA PKCS#v1.5 scheme using a specified Azure Key Vault Key URL.
     *
     * @param dataToSign
     *        - Text to sign.
     * @param encryptionKeyId
     *        - Azure Key Vault key URL.
     * @return Signature
     * @throws MicrosoftDataEncryptionException
     */
    private byte[] azureKeyVaultSignHashedData(byte[] dataToSign,
            String encryptionKeyId) throws MicrosoftDataEncryptionException {
        assert ((null != dataToSign) && (0 != dataToSign.length));

        CryptographyClient cryptoClient = getCryptographyClient(encryptionKeyId);
        SignResult signedData = cryptoClient.sign(SignatureAlgorithm.RS256, dataToSign);
        return signedData.getSignature();
    }

    /**
     * Verifies the given RSA PKCSv1.5 signature.
     *
     * @param dataToVerify
     * @param signature
     * @param encryptionKeyId
     *        - Azure Key Vault key url.
     * @return true if signature is valid, false if it is not valid
     * @throws MicrosoftDataEncryptionException
     */
    private boolean azureKeyVaultVerifySignature(byte[] dataToVerify, byte[] signature,
            String encryptionKeyId) throws MicrosoftDataEncryptionException {
        assert ((null != dataToVerify) && (0 != dataToVerify.length));
        assert ((null != signature) && (0 != signature.length));

        CryptographyClient cryptoClient = getCryptographyClient(encryptionKeyId);
        VerifyResult valid = cryptoClient.verify(SignatureAlgorithm.RS256, dataToVerify, signature);

        return valid.isValid();
    }

    /**
     * Returns the public Key size in bytes.
     *
     * @param encryptionKeyId
     *        - Azure Key Vault Key path
     * @return Key size in bytes
     * @throws MicrosoftDataEncryptionException
     *         when an error occurs
     */
    private int getAKVKeySize(String encryptionKeyId) throws MicrosoftDataEncryptionException {
        KeyVaultKey retrievedKey = getKeyVaultKey(encryptionKeyId);
        return retrievedKey.getKey().getN().length;
    }

    /**
     * Fetches the key from Azure Key Vault for given key path. If the key path includes a version, then that specific
     * version of the key is retrieved, otherwise the latest key will be retrieved.
     *
     * @param encryptionKeyId
     *        The key path associated with the key
     * @return The Key Vault key.
     * @throws MicrosoftDataEncryptionException
     *         If there was an error retrieving the key from Key Vault.
     */
    private KeyVaultKey getKeyVaultKey(String encryptionKeyId) throws MicrosoftDataEncryptionException {
        String[] keyTokens = encryptionKeyId.split(KEY_URL_DELIMITER);
        String keyName = keyTokens[KEY_NAME_INDEX];
        String keyVersion = null;
        if (keyTokens.length == KEY_URL_SPLIT_LENGTH_WITH_VERSION) {
            keyVersion = keyTokens[keyTokens.length - 1];
        }

        try {
            KeyClient keyClient = getKeyClient(encryptionKeyId);
            KeyVaultKey retrievedKey;
            if (null != keyVersion) {
                retrievedKey = keyClient.getKey(keyName, keyVersion);
            } else {
                retrievedKey = keyClient.getKey(keyName);
            }
            if (null == retrievedKey) {
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionException.getErrString("R_AKVKeyNotFound"));
                Object[] msgArgs = {keyTokens[keyTokens.length - 1]};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }

            if (retrievedKey.getKeyType() != KeyType.RSA && retrievedKey.getKeyType() != KeyType.RSA_HSM) {
                MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_NonRSAKey"));
                Object[] msgArgs = {retrievedKey.getKeyType().toString()};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }
            return retrievedKey;
        } catch (RuntimeException e) {
            throw new MicrosoftDataEncryptionException(e.getMessage());
        }

    }

    /**
     * Creates a new {@link KeyClient} if one does not exist for the given key path. If the client already exists, the
     * client is returned from the cache. As the client is stateless, it's safe to cache the client for each key path.
     *
     * @param encryptionKeyId
     *        The key path for which the {@link KeyClient} will be created, if it does not exist.
     * @return The {@link KeyClient} associated with the key path.
     */
    private KeyClient getKeyClient(String encryptionKeyId) {
        if (cachedKeyClients.containsKey(encryptionKeyId)) {
            return cachedKeyClients.get(encryptionKeyId);
        }
        String vaultUrl = getVaultUrl(encryptionKeyId);

        KeyClient keyClient;
        if (null != credential) {
            keyClient = new KeyClientBuilder().credential(credential).vaultUrl(vaultUrl).buildClient();
        } else {
            keyClient = new KeyClientBuilder().pipeline(keyVaultPipeline).vaultUrl(vaultUrl).buildClient();
        }
        cachedKeyClients.putIfAbsent(encryptionKeyId, keyClient);
        return cachedKeyClients.get(encryptionKeyId);
    }

    /**
     * Returns the vault url extracted from the key encryption key path.
     *
     * @param encryptionKeyId
     *        The key encryption key path.
     * @return The vault url.
     */
    private static String getVaultUrl(String encryptionKeyId) {
        String[] keyTokens = encryptionKeyId.split("/");
        String hostName = keyTokens[2];
        return "https://" + hostName;
    }

    @Override
    public boolean verify(String encryptionKeyId, boolean allowEnclaveComputations,
                          byte[] signature) throws MicrosoftDataEncryptionException {
        if (!allowEnclaveComputations) {
            return false;
        }

        KeyStoreProviderCommon.validateNonEmptyKeyEncryptionKeyPath(encryptionKeyId);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(providerName.toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            md.update(encryptionKeyId.toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            // value of allowEnclaveComputations is always true here
            md.update("true".getBytes(java.nio.charset.StandardCharsets.UTF_16LE));

            byte[] dataToVerify = md.digest();
            if (null == dataToVerify) {
                throw new MicrosoftDataEncryptionException(MicrosoftDataEncryptionException.getErrString("R_HashNull"));
            }

            // Sign the hash
            byte[] signedHash = azureKeyVaultSignHashedData(dataToVerify, encryptionKeyId);
            if (null == signedHash) {
                throw new MicrosoftDataEncryptionException(
                        MicrosoftDataEncryptionException.getErrString("R_SignedHashLengthError"));
            }

            // Validate the signature
            return azureKeyVaultVerifySignature(dataToVerify, signature, encryptionKeyId);
        } catch (NoSuchAlgorithmException e) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionException.getErrString("R_NoSHA256Algorithm"));
        }
    }

    private static List<String> getTrustedEndpoints() {
        List<String> trustedEndpoints = new ArrayList<String>();
        /*
         * List of Azure trusted endpoints https://docs.microsoft.com/en-us/azure/key-vault/key-vault-secure-your-key-
         * vault
         */
        trustedEndpoints.add("vault.azure.net");
        trustedEndpoints.add("vault.azure.cn");
        trustedEndpoints.add("vault.usgovcloudapi.net");
        trustedEndpoints.add("vault.microsoftazure.de");
        trustedEndpoints.add("managedhsm.azure.net");
        trustedEndpoints.add("managedhsm.azure.cn");
        trustedEndpoints.add("managedhsm.usgovcloudapi.net");
        trustedEndpoints.add("managedhsm.microsoftazure.de");
        return trustedEndpoints;
    }

    private byte[] compileKeyEncryptionKeyMetadata(String encryptionKeyId, boolean allowEnclaveComputations) {
        String kekMetadata = providerName + encryptionKeyId + allowEnclaveComputations;
        return kekMetadata.toLowerCase().getBytes(UTF_16LE);
    }
}
