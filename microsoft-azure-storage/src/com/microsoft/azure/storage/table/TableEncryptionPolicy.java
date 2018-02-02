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
package com.microsoft.azure.storage.table;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
import com.microsoft.azure.storage.core.EncryptionAgent;
import com.microsoft.azure.storage.core.EncryptionAlgorithm;
import com.microsoft.azure.storage.core.EncryptionData;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.core.WrappedContentKey;
import com.microsoft.azure.storage.table.TableRequestOptions.EncryptionResolver;

/**
 * Represents a table encryption policy that is used to perform envelope encryption/decryption of Azure table entities.
 */
public class TableEncryptionPolicy {
    
    /**
     * An object of type {@link IKey} that is used to wrap/unwrap the content key during encryption.
     */
    public IKey keyWrapper;

    /**
     * The {@link IKeyResolver} used to select the correct key for decrypting existing table entities.
     */
    public IKeyResolver keyResolver;

    /**
     * Initializes a new instance of the {@link TableEncryptionPolicy} class with the specified key and resolver.
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
     *            The key resolver used to select the correct key for decrypting existing table entities.
     */
    public TableEncryptionPolicy(IKey key, IKeyResolver keyResolver) {
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
     * Gets the key resolver used to select the correct key for decrypting existing table entities.
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
     * Sets the key resolver used to select the correct key for decrypting existing table entities.
     * 
     * @param keyResolver
     *            A resolver that returns an {@link IKey} given a keyId.
     */
    public void setKeyResolver(IKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }
    
    /**
     * Return an encrypted entity. This method is used for encrypting entity properties.
     */
    Map<String, EntityProperty> encryptEntity(Map<String, EntityProperty> properties, String partitionKey,
            String rowKey, EncryptionResolver encryptionResolver) throws StorageException {
        Utility.assertNotNull("properties", properties);

        // The Key should be set on the policy for encryption. Otherwise, throw an error.
        if (this.keyWrapper == null) {
            throw new IllegalArgumentException(SR.KEY_MISSING);
        }

        EncryptionData encryptionData = new EncryptionData();
        if (encryptionData.getKeyWrappingMetadata() == null) {
            encryptionData.setKeyWrappingMetadata(new HashMap<String, String>());
        }
        
        encryptionData.getKeyWrappingMetadata().put("EncryptionLibrary", "Java " + Constants.HeaderConstants.USER_AGENT_VERSION);
        encryptionData.setEncryptionAgent(new EncryptionAgent(Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1,
                EncryptionAlgorithm.AES_CBC_256));

        try {
            Map<String, EntityProperty> encryptedProperties = new HashMap<String, EntityProperty>();
            HashSet<String> encryptionPropertyDetailsSet = new HashSet<String>();

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

            encryptionData.setContentEncryptionIV(myAes.getIV());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Map.Entry<String, EntityProperty> kvp : properties.entrySet()) {
                if (encryptionResolver != null
                        && encryptionResolver.encryptionResolver(partitionKey, rowKey, kvp.getKey())) {
                    // Throw if users try to encrypt null properties. This could happen in the DynamicTableEntity case
                    // where a user adds a new property as follows - ent.Properties.Add("foo2", null);
                    if (kvp.getValue() == null) {
                        throw new IllegalArgumentException(SR.ENCRYPTING_NULL_PROPERTIES_NOT_ALLOWED);
                    }

                    kvp.getValue().setIsEncrypted(true);
                }

                // IsEncrypted is set to true when either the EncryptPropertyAttribute is set on a property or when it is 
                // specified in the encryption resolver or both.
                if (kvp.getValue() != null && kvp.getValue().isEncrypted()) {
                    // Throw if users try to encrypt non-string properties.
                    if (kvp.getValue().getEdmType() != EdmType.STRING) {
                        throw new IllegalArgumentException(String.format(SR.UNSUPPORTED_PROPERTY_TYPE_FOR_ENCRYPTION, 
                                kvp.getValue().getEdmType()));
                    }

                    byte[] columnIVFull = digest.digest(Utility.binaryAppend(encryptionData.getContentEncryptionIV(), 
                            (partitionKey + rowKey + kvp.getKey()).getBytes(Constants.UTF8_CHARSET)));
                    
                    byte[] columnIV = new byte[16];
                    System.arraycopy(columnIVFull, 0, columnIV, 0, 16);
                    myAes.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(columnIV));

                    // Throw if users try to encrypt null properties. This could happen in the DynamicTableEntity or POCO
                    // case when the property value is null.
                    if (kvp.getValue() == null) {
                        throw new IllegalArgumentException(SR.ENCRYPTING_NULL_PROPERTIES_NOT_ALLOWED);
                    }

                    byte[] src = kvp.getValue().getValueAsString().getBytes(Constants.UTF8_CHARSET);
                    byte[] dest = myAes.doFinal(src, 0, src.length);

                    // Store the encrypted properties as binary values on the service instead of base 64 encoded strings because strings are stored as a sequence of 
                    // WCHARs thereby further reducing the allowed size by half. During retrieve, it is handled by the response parsers correctly 
                    // even when the service does not return the type for JSON no-metadata.
                    encryptedProperties.put(kvp.getKey(), new EntityProperty(dest));
                    encryptionPropertyDetailsSet.add(kvp.getKey());
                }
                else {
                    encryptedProperties.put(kvp.getKey(), kvp.getValue());
                }
            
                // Encrypt the property details set and add it to entity properties.
                byte[] metadataIVFull = digest.digest(Utility.binaryAppend(encryptionData.getContentEncryptionIV(),
                        (partitionKey + rowKey + Constants.EncryptionConstants.TABLE_ENCRYPTION_PROPERTY_DETAILS)
                                .getBytes(Constants.UTF8_CHARSET)));
                
                byte[] metadataIV = new byte[16];
                System.arraycopy(metadataIVFull, 0, metadataIV, 0, 16);
                myAes.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(metadataIV));

                byte[] src = Arrays.toString(encryptionPropertyDetailsSet.toArray()).getBytes(Constants.UTF8_CHARSET);
                byte[] dest = myAes.doFinal(src, 0, src.length);
                encryptedProperties.put(Constants.EncryptionConstants.TABLE_ENCRYPTION_PROPERTY_DETAILS,
                        new EntityProperty(dest));
            }

            encryptedProperties.put(Constants.EncryptionConstants.TABLE_ENCRYPTION_KEY_DETAILS, new EntityProperty(
                    encryptionData.serialize()));

            return encryptedProperties;
        }
        catch (Exception e) {
            throw StorageException.translateClientException(e);
        }
    }
    
    CEKReturn decryptMetadataAndReturnCEK(String partitionKey, String rowKey, EntityProperty encryptionKeyProperty, 
            EntityProperty propertyDetailsProperty, EncryptionData encryptionData) throws StorageException
    {
        // Throw if neither the key nor the key resolver are set.
        if (this.keyWrapper == null && this.keyResolver == null) {
            throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.KEY_AND_RESOLVER_MISSING, null);
        }

        try {
            // Copy the values into the passed in encryption data object so they can be accessed outside of this context
            encryptionData.copyValues(EncryptionData.deserialize(encryptionKeyProperty.getValueAsString()));

            Utility.assertNotNull("contentEncryptionIV", encryptionData.getContentEncryptionIV());
            Utility.assertNotNull("encryptedKey", encryptionData.getWrappedContentKey().getEncryptedKey());
            
            // Throw if the encryption protocol on the entity doesn't match the version that this client library understands
            // and is able to decrypt.
            if (!Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1.equals(encryptionData.getEncryptionAgent()
                    .getProtocol())) {
                throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                        SR.ENCRYPTION_PROTOCOL_VERSION_INVALID, null);
            }

            Boolean isJavaV1 = (encryptionData.getEncryptionAgent().getProtocol().equals(Constants.EncryptionConstants.ENCRYPTION_PROTOCOL_V1)) 
                    && ((encryptionData.getKeyWrappingMetadata() == null)
                            || (encryptionData.getKeyWrappingMetadata().containsKey("EncryptionLibrary") 
                             && encryptionData.getKeyWrappingMetadata().get("EncryptionLibrary").contains("Java")));

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
            
            Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            String IVString = isJavaV1 ? (partitionKey + rowKey + Constants.EncryptionConstants.TABLE_ENCRYPTION_PROPERTY_DETAILS) : (rowKey + partitionKey + Constants.EncryptionConstants.TABLE_ENCRYPTION_PROPERTY_DETAILS);
            byte[] metadataIVFull = sha256.digest(Utility.binaryAppend(encryptionData.getContentEncryptionIV(), IVString.getBytes(Constants.UTF8_CHARSET)));
            
            byte[] metadataIV = new byte[16];
            System.arraycopy(metadataIVFull, 0, metadataIV, 0, 16);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(metadataIV);                       
            SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                    "AES");        
            myAes.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            
            byte[] src = propertyDetailsProperty.getValueAsByteArray();
            propertyDetailsProperty.setValue(myAes.doFinal(src, 0, src.length));

            CEKReturn ret = new CEKReturn();
            ret.key = keySpec;
            ret.isJavaV1 = isJavaV1;
            return ret;
        }
        catch (StorageException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR, SR.DECRYPTION_LOGIC_ERROR, ex);
        }
    }
    
    /**
     * Return a decrypted entity. This method is used for decrypting entity properties.
     */
    HashMap<String, EntityProperty> decryptEntity(HashMap<String, EntityProperty> properties,
            HashSet<String> encryptedPropertyDetailsSet, String partitionKey, String rowKey, Key contentEncryptionKey,
            EncryptionData encryptionData, Boolean isJavaV1) throws StorageException {        
        HashMap<String, EntityProperty> decryptedProperties = new HashMap<String, EntityProperty>();
        
        try {
            switch (encryptionData.getEncryptionAgent().getEncryptionAlgorithm()) {
                case AES_CBC_256:
                    Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");

                    for (Map.Entry<String, EntityProperty> kvp : properties.entrySet()) {
                        if (kvp.getKey() == Constants.EncryptionConstants.TABLE_ENCRYPTION_KEY_DETAILS
                                || kvp.getKey() == Constants.EncryptionConstants.TABLE_ENCRYPTION_PROPERTY_DETAILS) {
                            // Do nothing. Do not add to the result properties.
                        }
                        else if (encryptedPropertyDetailsSet.contains(kvp.getKey())) {
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");

                            String IVString = isJavaV1 ? (partitionKey + rowKey + kvp.getKey()) : (rowKey + partitionKey + kvp.getKey());
                            byte[] columnIVFull = digest.digest(Utility.binaryAppend(encryptionData.getContentEncryptionIV(), IVString.getBytes(Constants.UTF8_CHARSET)));

                            byte[] columnIV = new byte[16];
                            System.arraycopy(columnIVFull, 0, columnIV, 0, 16);

                            myAes.init(Cipher.DECRYPT_MODE, contentEncryptionKey, new IvParameterSpec(columnIV));

                            byte[] src = kvp.getValue().getValueAsByteArray();
                            byte[] dest = myAes.doFinal(src, 0, src.length);
                            String destString = new String(dest, Constants.UTF8_CHARSET);
                            decryptedProperties.put(kvp.getKey(), new EntityProperty(destString));
                        }
                        else {
                            decryptedProperties.put(kvp.getKey(), kvp.getValue());
                        }
                    }
                    return decryptedProperties;

                default:
                    throw new StorageException(StorageErrorCodeStrings.DECRYPTION_ERROR,
                            SR.INVALID_ENCRYPTION_ALGORITHM, null);
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
