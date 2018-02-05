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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.storage.DictionaryKeyResolver;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.EncryptionData;

/**
 * Queue Tests
 */
@Category({ CloudTests.class, DevFabricTests.class, DevStoreTests.class })
public class CloudQueueEncryptionTests {

    private CloudQueue queue;

    @Before
    public void queueTestMethodSetUp() throws URISyntaxException, StorageException {
        this.queue = QueueTestHelper.getRandomQueueReference();
        this.queue.createIfNotExists();
    }

    @After
    public void queueTestMethodTearDown() throws StorageException {
        this.queue.deleteIfExists();
    }

    @Test
    public void testQueueAddUpdateEncryptedMessage() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        RsaKey rsaKey = TestHelper.getRSAKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);
        resolver.add(rsaKey);

        doQueueAddUpdateEncryptedMessage(aesKey, resolver);
        doQueueAddUpdateEncryptedMessage(rsaKey, resolver);
    }

    private void doQueueAddUpdateEncryptedMessage(IKey key, DictionaryKeyResolver keyResolver) throws StorageException {
        String messageStr = UUID.randomUUID().toString();
        CloudQueueMessage message = new CloudQueueMessage(messageStr);

        QueueRequestOptions createOptions = new QueueRequestOptions();
        createOptions.setEncryptionPolicy(new QueueEncryptionPolicy(key, null));

        // add message
        this.queue.addMessage(message, 0, 0, createOptions, null);

        // Retrieve message
        QueueRequestOptions retrieveOptions = new QueueRequestOptions();
        retrieveOptions.setEncryptionPolicy(new QueueEncryptionPolicy(null, keyResolver));

        CloudQueueMessage retrMessage = this.queue.retrieveMessage(30, retrieveOptions, null);
        assertEquals(messageStr, retrMessage.getMessageContentAsString());

        // Update message
        String updatedMessage = UUID.randomUUID().toString();
        retrMessage.setMessageContent(updatedMessage);
        this.queue.updateMessage(retrMessage, 0,
                EnumSet.of(MessageUpdateFields.CONTENT, MessageUpdateFields.VISIBILITY), createOptions, null);

        // Retrieve updated message
        retrMessage = this.queue.retrieveMessage(30, retrieveOptions, null);
        assertEquals(updatedMessage, retrMessage.getMessageContentAsString());
    }

    @Test
    public void testQueueAddUpdateEncryptedBinaryMessage() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        byte[] messageBytes = new byte[100];
        Random rand = new Random();
        rand.nextBytes(messageBytes);

        CloudQueueMessage message = new CloudQueueMessage(messageBytes);

        QueueRequestOptions options = new QueueRequestOptions();
        options.setEncryptionPolicy(new QueueEncryptionPolicy(aesKey, null));

        // add message
        this.queue.addMessage(message, 0, 0, options, null);

        // Retrieve message
        CloudQueueMessage retrMessage = this.queue.retrieveMessage(30, options, null);
        assertArrayEquals(messageBytes, retrMessage.getMessageContentAsByte());
    }

    @Test
    public void testQueueAddUpdateEncryptedEncodedMessage() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        byte[] messageBytes = new byte[100];
        Random rand = new Random();
        rand.nextBytes(messageBytes);

        String inputMessage = Base64.encode(messageBytes);
        CloudQueueMessage message = new CloudQueueMessage(inputMessage);
        this.queue.setShouldEncodeMessage(false);

        QueueRequestOptions options = new QueueRequestOptions();
        options.setEncryptionPolicy(new QueueEncryptionPolicy(aesKey, null));

        // add message
        this.queue.addMessage(message, 0, 0, options, null);

        // Retrieve message
        CloudQueueMessage retrMessage = this.queue.retrieveMessage(30, options, null);
        assertEquals(inputMessage, retrMessage.getMessageContentAsString());
    }

    @Test
    public void testQueueAddEncrypted64KMessage() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        String inputMessage = StringUtils.repeat('a', 64 * 1024);
        CloudQueueMessage message = new CloudQueueMessage(inputMessage);
        this.queue.setShouldEncodeMessage(false);

        QueueRequestOptions options = new QueueRequestOptions();
        options.setEncryptionPolicy(new QueueEncryptionPolicy(aesKey, null));

        // add message
        this.queue.addMessage(message);

        // add encrypted Message
        try {
            this.queue.addMessage(message, 0, 0, options, null);
            fail("Adding an encrypted message that exceeds message limits should throw.");
        }
        catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testQueueMessageValidateEncryption() throws StorageException, JsonProcessingException, IOException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InterruptedException, ExecutionException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        byte[] messageBytes = new byte[100];
        Random rand = new Random();
        rand.nextBytes(messageBytes);

        String inputMessage = Base64.encode(messageBytes);
        CloudQueueMessage message = new CloudQueueMessage(inputMessage);
        this.queue.setShouldEncodeMessage(false);

        QueueRequestOptions options = new QueueRequestOptions();
        options.setEncryptionPolicy(new QueueEncryptionPolicy(aesKey, null));

        // add message
        this.queue.addMessage(message, 0, 0, options, null);

        // Retrieve message without decrypting
        CloudQueueMessage retrMessage = this.queue.retrieveMessage();

        // Decrypt locally
        CloudQueueMessage decryptedMessage;
        CloudQueueEncryptedMessage encryptedMessage = CloudQueueEncryptedMessage.deserialize(retrMessage
                .getMessageContentAsString());
        EncryptionData encryptionData = encryptedMessage.getEncryptionData();

        byte[] contentEncryptionKey = aesKey.unwrapKeyAsync(encryptionData.getWrappedContentKey().getEncryptedKey(),
                encryptionData.getWrappedContentKey().getAlgorithm()).get();
        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                "AES"); 

        Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        myAes.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(encryptionData.getContentEncryptionIV()));

        byte[] src = Base64.decode(encryptedMessage.getEncryptedMessageContents());

        decryptedMessage = new CloudQueueMessage(myAes.doFinal(src, 0, src.length));

        assertArrayEquals(message.getMessageContentAsByte(), decryptedMessage.getMessageContentAsByte());
    }
    
    @Test
    public void testQueueMessageEncryptionWithStrictMode() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        String messageStr = UUID.randomUUID().toString();
        CloudQueueMessage message = new CloudQueueMessage(messageStr);

        // Add message with policy.
        QueueRequestOptions createOptions = new QueueRequestOptions();
        createOptions.setEncryptionPolicy(new QueueEncryptionPolicy(aesKey, null));
        createOptions.setRequireEncryption(true);

        this.queue.addMessage(message, 0, 0, createOptions, null);

        // Set policy to null and add message while RequireEncryption flag is still set to true. This should throw.
        createOptions.setEncryptionPolicy(null);

        try {
            this.queue.addMessage(message, 0, 0, createOptions, null);
            fail("Not specifying a policy when RequireEnryption is set to true should throw.");
        }
        catch (IllegalArgumentException ex) {
        }

        // Retrieve message
        QueueRequestOptions retrieveOptions = new QueueRequestOptions();
        retrieveOptions.setEncryptionPolicy(new QueueEncryptionPolicy(null, resolver));
        retrieveOptions.setRequireEncryption(true);

        CloudQueueMessage retrMessage = queue.retrieveMessage(30, retrieveOptions, null);

        // Update message with plain text.
        String updatedMessage = UUID.randomUUID().toString();
        retrMessage.setMessageContent(updatedMessage);

        this.queue.updateMessage(retrMessage, 0,
                EnumSet.of(MessageUpdateFields.CONTENT, MessageUpdateFields.VISIBILITY), null, null);

        // Retrieve updated message with RequireEncryption flag but no metadata on the service. This should throw.
        try {
            this.queue.retrieveMessage(30, retrieveOptions, null);
            fail("Retrieving with RequireEncryption set to true and no metadata on the service should fail.");
        }
        catch (StorageException ex) {
        }

        // Set RequireEncryption to false and retrieve.
        retrieveOptions.setRequireEncryption(false);
        queue.retrieveMessage(30, retrieveOptions, null);
    }
}
