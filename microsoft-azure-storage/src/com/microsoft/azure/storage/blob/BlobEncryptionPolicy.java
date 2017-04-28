/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.tuple.Pair;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.EncryptionAgent;
import com.microsoft.azure.storage.core.EncryptionAlgorithm;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.core.WrappedContentKey;


/**
 * Represents a blob encryption policy that is used to perform envelope encryption/decryption of Azure blobs.
 */
public final class BlobEncryptionPolicy {

    /**
     * The {@link IKeyResolver} used to select the correct key for decrypting existing blobs.
     */
    public IKeyResolver keyResolver;

    /**
     * An object of type {@link IKey} that is used to wrap/unwrap the content key during encryption.
     */
    public IKey keyWrapper;

    /**
     * Initializes a new instance of the {@link BlobEncryptionPolicy} class with the specified key and resolver.
     * <p>
     * If the generated policy is intended to be used for encryption, users are expected to provide a key at the
     * minimum. The absence of key will cause an exception to be thrown during encryption. If the generated policy is
     * intended to be used for decryption, users can provide a keyResolver. The client library will - 1. Invoke the key
     * resolver if specified to get the key. 2. If resolver is not specified but a key is specified, match the key id on
     * the key and use it.
     * 
     * @param key
     *            An object of type {@link IKey} that is used to wrap/unwrap the content encryption key.
     * @param keyResolver
     *            The key resolver used to select the correct key for decrypting existing blobs.
     */
    public BlobEncryptionPolicy(IKey key, IKeyResolver keyResolver) {
        this.keyWrapper = key;
        this.keyResolver = keyResolver;
    }

    /**
     * Gets the {@link IKey} that is used to wrap/unwrap the content key during encryption.
     * 
     * @return An {@link IKey} object.
     */
    public IKey getKey() {
        return this.keyWrapper;
    }

    /**
     * Gets the key resolver used to select the correct key for decrypting existing blobs.
     * 
     * @return A resolver that returns an {@link IKey} given a keyId.
     */
    public IKeyResolver getKeyResolver() {
        return this.keyResolver;
    }

    /**
     * Sets the {@link IKey} that is used to wrap/unwrap the content key during encryption.
     * 
     * @param key
     *            An {@link IKey} object.
     */
    public void setKey(IKey key) {
        this.keyWrapper = key;
    }

    /**
     * Sets the key resolver used to select the correct key for decrypting existing blobs.
     * 
     * @param keyResolver
     *            A resolver that returns an {@link IKey} given a keyId.
     */
    public void setKeyResolver(IKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }
    
    /**
     * Return a reference to a {@link OutputStream} given a user stream. This method is used for decrypting blobs.
     * @param userProvidedStream
     *          The output stream provided by the user.
     * @param metadata
     *          Reference to blob metadata object that is used to get the encryption materials.
     * @param requireEncryption
     *          A value to indicate that the data read from the server should be encrypted.
     * @param iv
     *          The iv to use if pre-buffered. Used only for range reads.
     * @param noPadding
     *          Value indicating if the padding mode should be set or not.
     * @return A reference to a {@link OutputStream} that will be written to.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    OutputStream decryptBlob(OutputStream userProvidedStream, Map<String, String> metadata, Boolean requireEncryption,
            byte[] iv, boolean noPadding) throws StorageException {
        Utility.assertNotNull("metadata", metadata);
        String encryptionDataString = metadata.get("encryptiondata");

        try
        {
            if (encryptionDataString != null) {
                BlobEncryptionData encryptionData = BlobEncryptionData.deserialize(encryptionDataString);

                Utility.assertNotNull("encryptionData", encryptionData);
                Utility.assertNotNull("contentEncryptionIV", encryptionData.getContentEncryptionIV());
                Utility.assertNotNull("encryptedKey", encryptionData.getWrappedContentKey().getEncryptedKey());

                // Throw if the encryption protocol on the message doesn't match the version that this client library understands
                // and is able to decrypt.
                if (!Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1.equals(encryptionData.getEncryptionAgent()
                        .getProtocol())) {
                    throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                            SR.ENCRYPTION_PROTOCOL_VERSION_INVALID, null);
                }

                // Throw if neither the key nor the key resolver are set.
                if (this.keyWrapper == null && this.keyResolver == null) {
                    throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.KEY_AND_RESOLVER_MISSING,
                            null);
                }

                byte[] contentEncryptionKey = null;

                // 1. Invoke the key resolver if specified to get the key. If the resolver is specified but does not have a
                // mapping for the key id, an error should be thrown. This is important for key rotation scenario.
                // 2. If resolver is not specified but a key is specified, match the key id on the key and and use it.
                // Calling UnwrapKeyAsync synchronously is fine because for the storage client scenario, unwrap happens
                // locally. No service call is made.
                if (this.keyResolver != null) {
                    IKey keyEncryptionKey = this.keyResolver.resolveKeyAsync(encryptionData.getWrappedContentKey()
                            .getKeyId()).get();
                    
                    Utility.assertNotNull("keyEncryptionKey", keyEncryptionKey);
                    contentEncryptionKey = keyEncryptionKey.unwrapKeyAsync(
                            encryptionData.getWrappedContentKey().getEncryptedKey(), 
                            encryptionData.getWrappedContentKey().getAlgorithm()).get();
                }
                else {
                    if (encryptionData.getWrappedContentKey().getKeyId().equals(this.keyWrapper.getKid())) {
                        contentEncryptionKey = this.keyWrapper.unwrapKeyAsync(
                                encryptionData.getWrappedContentKey().getEncryptedKey(),
                                encryptionData.getWrappedContentKey().getAlgorithm()).get();
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.KEY_MISMATCH, null);
                    }
                }

                switch (encryptionData.getEncryptionAgent().getEncryptionAlgorithm()) {
                    case AES_CBC_256:

                        Cipher myAes;
                        if (noPadding) {
                            myAes = Cipher.getInstance("AES/CBC/NoPadding");
                        }
                        else {
                            myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        }

                        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv != null ? iv
                                : encryptionData.getContentEncryptionIV());
                        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                                "AES");
                        myAes.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

                        return new CipherOutputStream(userProvidedStream, myAes);

                    default:
                        throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                                SR.INVALID_ENCRYPTION_ALGORITHM, null);
                }
            }
            else {
                return userProvidedStream;
            }
        }
        catch (StorageException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.DECRYPTION_LOGIC_ERROR, ex);
        }
    }

    /**
     * Internal helper method to wrap a user provided stream with the appropriate crypto stream.
     */
    static OutputStream wrapUserStreamWithDecryptStream(CloudBlob blob, OutputStream userProvidedStream, 
            BlobRequestOptions options, Map<String, String> metadata, long blobLength, boolean rangeRead, 
            Long endOffset, Long userSpecifiedLength, int discardFirst, boolean bufferIV) throws StorageException
    {
        // If encryption policy is set but the encryption metadata is absent, throw
        // an exception.
        String encryptionDataString = metadata.get("encryptiondata");
        if (options.requireEncryption() != null && options.requireEncryption() && encryptionDataString == null)
        {
            throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.ENCRYPTION_DATA_NOT_PRESENT_ERROR, null);
        }
        
        if (!rangeRead)
        {
            // The user provided stream should be wrapped in a TruncatingNonCloseableStream in order to 
            // avoid closing the user stream when the crypto stream is closed to flush the final decrypted 
            // block of data.
            OutputStream decryptStream = options.getEncryptionPolicy().decryptBlob(userProvidedStream, metadata,
                    options.requireEncryption(), null, blob.getProperties().getBlobType() == BlobType.PAGE_BLOB);
            return decryptStream;
        }
        else
        {
            // Check if end offset lies in the last AES block and send this information over to set the correct padding mode.
            boolean noPadding = blob.getProperties().getBlobType() == BlobType.PAGE_BLOB || 
                    endOffset != null && endOffset < blobLength - 16;
            return new BlobDecryptStream(userProvidedStream, metadata, userSpecifiedLength, 
                    discardFirst, bufferIV, noPadding, options.getEncryptionPolicy(), options.requireEncryption());
        }
    }
    
    /**
     * Set up the encryption context required for encrypting blobs.
     * @param metadata
     *          Reference to blob metadata object that is used to set the encryption materials.
     * @param noPadding
     *          Value indicating if the padding mode should be set or not.
     * @return The Cipher to use to decrypt the blob.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    Cipher createAndSetEncryptionContext(Map<String, String> metadata, boolean noPadding)
            throws StorageException {
        Utility.assertNotNull("metadata", metadata);

        // The Key should be set on the policy for encryption. Otherwise, throw an error.
        if (this.keyWrapper == null)
        {
            throw new IllegalArgumentException(SR.KEY_MISSING);
        }
        
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);

            Cipher myAes;
            if (noPadding) {
                myAes = Cipher.getInstance("AES/CBC/NoPadding");
            }
            else {
                myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            }

            SecretKey aesKey = keyGen.generateKey();
            myAes.init(Cipher.ENCRYPT_MODE, aesKey);

            BlobEncryptionData encryptionData = new BlobEncryptionData();
            if (encryptionData.getKeyWrappingMetadata() == null) {
                encryptionData.setKeyWrappingMetadata(new HashMap<String, String>());
            }
            
            encryptionData.getKeyWrappingMetadata().put(Constants.EncryptionConstants.ENCRYPTION_LIBRARY, "Java " + Constants.HeaderConstants.USER_AGENT_VERSION);
            encryptionData.setEncryptionAgent(new EncryptionAgent(Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1,
                    EncryptionAlgorithm.AES_CBC_256));

            // Wrap key
            Pair<byte[], String> encryptedKey = this.keyWrapper.wrapKeyAsync(aesKey.getEncoded(), null /* algorithm */).get();
            encryptionData.setWrappedContentKey(new WrappedContentKey(this.keyWrapper.getKid(), encryptedKey.getKey(),
                    encryptedKey.getValue()));

            encryptionData.setContentEncryptionIV(myAes.getIV());

            metadata.put(Constants.EncryptionConstants.BLOB_ENCRYPTION_DATA, encryptionData.serialize());
            return myAes;
        }
        catch (Exception e) {
            throw StorageException.translateClientException(e);
        }
    }
}
