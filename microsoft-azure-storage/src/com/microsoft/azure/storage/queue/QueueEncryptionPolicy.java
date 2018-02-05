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
package com.microsoft.azure.storage.queue;

import java.util.HashMap;

import javax.crypto.Cipher;
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
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.EncryptionAgent;
import com.microsoft.azure.storage.core.EncryptionAlgorithm;
import com.microsoft.azure.storage.core.EncryptionData;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.core.WrappedContentKey;

/**
 * Represents a queue encryption policy that is used to perform envelope encryption/decryption of Azure queue messages.
 */
public final class QueueEncryptionPolicy {

    /**
     * An object of type {@link IKey} that is used to wrap/unwrap the content key during encryption.
     */
    public IKey keyWrapper;

    /**
     * The {@link IKeyResolver} used to select the correct key for decrypting existing queue messages.
     */
    public IKeyResolver keyResolver;

    /**
     * Initializes a new instance of the {@link QueueEncryptionPolicy} class with the specified key and resolver.
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
     *            The key resolver used to select the correct key for decrypting existing queue messages.
     */
    public QueueEncryptionPolicy(IKey key, IKeyResolver keyResolver) {
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
     * Gets the key resolver used to select the correct key for decrypting existing queue messages.
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
     *          An {@link IKey} object.
     */
    public void setKey(IKey key) {
        this.keyWrapper = key;
    }

    /**
     * Sets the key resolver used to select the correct key for decrypting existing queue messages.
     * 
     * @param keyResolver
     *            A resolver that returns an {@link IKey} given a keyId.
     */
    public void setKeyResolver(IKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    /**
     * Return an encrypted base64 encoded message along with encryption related metadata given a plain text message.
     * 
     * @param inputMessage
     *            The input message in bytes.
     * @return The encrypted message that will be uploaded to the service.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    String encryptMessage(byte[] inputMessage) throws StorageException {
        Utility.assertNotNull("inputMessage", inputMessage);

        if (this.keyWrapper == null)
        {
            throw new IllegalArgumentException(SR.KEY_MISSING);
        }

        CloudQueueEncryptedMessage encryptedMessage = new CloudQueueEncryptedMessage();
        EncryptionData encryptionData = new EncryptionData();
        if (encryptionData.getKeyWrappingMetadata() == null) {
            encryptionData.setKeyWrappingMetadata(new HashMap<String, String>());
        }
        
        encryptionData.getKeyWrappingMetadata().put("EncryptionLibrary", "Java " + Constants.HeaderConstants.USER_AGENT_VERSION);
        encryptionData.setEncryptionAgent(new EncryptionAgent(
                Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1, EncryptionAlgorithm.AES_CBC_256));
  
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            
            Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aesKey = keyGen.generateKey();
            myAes.init(Cipher.ENCRYPT_MODE, aesKey);
    
            // Wrap key
            Pair<byte[], String> encryptedKey = this.keyWrapper
                    .wrapKeyAsync(aesKey.getEncoded(), null /* algorithm */).get();
            encryptionData.setWrappedContentKey(new WrappedContentKey(this.keyWrapper.getKid(), encryptedKey.getKey(),
                    encryptedKey.getValue()));
            
            encryptedMessage.setEncryptedMessageContents(new String(
                    Base64.encode(myAes.doFinal(inputMessage, 0, inputMessage.length))));
    
            encryptionData.setContentEncryptionIV(myAes.getIV());
            encryptedMessage.setEncryptionData(encryptionData);
            return encryptedMessage.serialize();
        }
        catch (Exception e) {
            throw StorageException.translateClientException(e);
        }
    }

    /**
     * Returns a plain text message given an encrypted message.
     * 
     * @param inputMessage
     *            The encrypted message.
     * @param requireEncryption
     *          A value to indicate that the data read from the server should be encrypted.
     * @return The plain text message bytes.
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    byte[] decryptMessage(String inputMessage, Boolean requireEncryption) throws StorageException {
        Utility.assertNotNull("inputMessage", inputMessage);
        
        try
        {
            CloudQueueEncryptedMessage encryptedMessage = CloudQueueEncryptedMessage.deserialize(inputMessage);
            
            if (requireEncryption != null && requireEncryption && encryptedMessage.getEncryptionData() == null) {
                throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                        SR.ENCRYPTION_DATA_NOT_PRESENT_ERROR, null);
            }

            if (encryptedMessage.getEncryptionData() != null) {
                EncryptionData encryptionData = encryptedMessage.getEncryptionData();

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

                        Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        
                        IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptionData.getContentEncryptionIV());                       
                        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                                "AES");                        
                        myAes.init(Cipher.DECRYPT_MODE, keySpec,ivParameterSpec);

                        byte[] src = Base64.decode(encryptedMessage.getEncryptedMessageContents());
                        return myAes.doFinal(src, 0, src.length);

                    default:
                        throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                                SR.INVALID_ENCRYPTION_ALGORITHM, null);
                }
            }
            else {
                return Base64.decode(encryptedMessage.getEncryptedMessageContents());
            }
        }
        catch (StorageException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.DECRYPTION_LOGIC_ERROR, ex);
        }
    }

}
