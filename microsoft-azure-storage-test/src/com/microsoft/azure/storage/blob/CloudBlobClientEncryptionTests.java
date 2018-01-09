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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DictionaryKeyResolver;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SendingRequestEvent;
import com.microsoft.azure.storage.StorageEvent;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.SR;

@Category({ CloudTests.class, DevFabricTests.class, DevStoreTests.class })
public class CloudBlobClientEncryptionTests {

    protected CloudBlobContainer container;

    @Before
    public void blobEncryptionTestMethodSetup() throws URISyntaxException, StorageException {
        this.container = BlobTestHelper.getRandomContainerReference();
        this.container.create();
    }

    @After
    public void blobEncryptionTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }
    
    @Test
    public void testBlobBasicEncryption() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, 
        StorageException, IOException, URISyntaxException
    {
        this.doCloudBlobEncryption(BlobType.BLOCK_BLOB, false);
        this.doCloudBlobEncryption(BlobType.PAGE_BLOB, false);
        this.doCloudBlobEncryption(BlobType.APPEND_BLOB, false);

        this.doCloudBlobEncryption(BlobType.BLOCK_BLOB, true);
        this.doCloudBlobEncryption(BlobType.PAGE_BLOB, true);
        this.doCloudBlobEncryption(BlobType.APPEND_BLOB, true);
    }

    private void doCloudBlobEncryption(BlobType type, boolean partial) throws StorageException, IOException, 
        URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        int size = 5 * 1024 * 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(size);
        
        if (partial)
        {
            size = 2 * 1024 * 1024;
        }

        CloudBlob blob = null;
        if (type == BlobType.BLOCK_BLOB) {
            blob = this.container.getBlockBlobReference("blockblob");
        }
        else if (type == BlobType.PAGE_BLOB) {
            blob = this.container.getPageBlobReference("pageblob");
        }
        else if (type == BlobType.APPEND_BLOB) {
            blob = this.container.getAppendBlobReference("appendblob");
        }

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);
        
        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, size, null, uploadOptions, null);

        // Download the encrypted blob.
        // Create the decryption policy to be used for download. There is no need to specify the
        // key when the policy is only going to be used for downloads. Resolver is sufficient.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);

        // Download and decrypt the encrypted contents from the blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream, null, downloadOptions, null);

        // Compare that the decrypted contents match the input data.
        TestHelper.assertStreamsAreEqualAtIndex(stream, new ByteArrayInputStream(outputStream.toByteArray()), 0, 0, 
                size, 2 * 1024);
    }

    @Test
    public void testDownloadUnencryptedBlobWithEncryptionPolicy() throws StorageException, IOException, URISyntaxException, NoSuchAlgorithmException
    {
        String blobName = BlobTestHelper.generateRandomBlobNameWithPrefix("test");
        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
        blob.deleteIfExists();

        byte[] msg = "my message".getBytes();
        // Upload data without encryption
        blob.uploadFromByteArray(msg, 0, msg.length);

        // Create an asymmetric encryption key. The provider must be specified to work around an issue in RsaKey.
        RsaKey rsaKey = new RsaKey("myKey", 1024, KeyPairGenerator.getInstance("RSA").getProvider());

        // Create options with encryption policy
        BlobRequestOptions options = new BlobRequestOptions();
        options.setEncryptionPolicy(new BlobEncryptionPolicy(rsaKey, null));
        options.setRequireEncryption(true);

        try {
            blob.downloadText(Charset.defaultCharset().name(), null, options, null);
            fail("Expect exception");
        }
        catch (StorageException e) {
            assertEquals(SR.ENCRYPTION_DATA_NOT_PRESENT_ERROR, e.getMessage());
        }

        byte[] buffer = new byte[msg.length];
        try {
            blob.downloadRangeToByteArray(0, (long) buffer.length, buffer, 0, null, options, null);
            fail("Expect exception");
        }
        catch (StorageException e) {
            assertEquals(SR.ENCRYPTION_DATA_NOT_PRESENT_ERROR, e.getMessage());
        }
    }

    @Test
    public void testBlobEncryptionWithFile() throws URISyntaxException, StorageException, IOException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        int size = 5 * 1024 * 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(size);

        CloudBlockBlob blob = container.getBlockBlobReference("blockblob");

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        File sourceFile = File.createTempFile("sourceFile", ".tmp");
        File destinationFile = new File(sourceFile.getParentFile(), "destinationFile.tmp");
        try {

            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(buffer);
            fos.close();

            // Upload the encrypted contents to the blob.
            blob.uploadFromFile(sourceFile.getAbsolutePath(), null, uploadOptions, null);

            // Download the encrypted blob.
            // Create the decryption policy to be used for download. There is no need to specify the
            // key when the policy is only going to be used for downloads. Resolver is sufficient.
            BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

            // Set the decryption policy on the request options.
            BlobRequestOptions downloadOptions = new BlobRequestOptions();
            downloadOptions.setEncryptionPolicy(downloadPolicy);

            // Download and decrypt the encrypted contents from the blob.
            blob.downloadToFile(destinationFile.getAbsolutePath(), null, downloadOptions, null);

            // Compare that the decrypted contents match the input data.
            FileInputStream fis = new FileInputStream(destinationFile);

            byte[] readBuffer = new byte[size];
            fis.read(readBuffer);
            fis.close();

            for (int i = 0; i < size; i++) {
                assertEquals("File contents do not match.", buffer[i], readBuffer[i]);
            }
        }
        finally {
            if (sourceFile.exists()) {
                sourceFile.delete();
            }

            if (destinationFile.exists()) {
                destinationFile.delete();
            }
        }
    }

    @Test
    public void testBlobEncryptionWithByteArray() throws StorageException, IOException, URISyntaxException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        int size = 5 * 1024 * 1024;
        byte[] buffer = BlobTestHelper.getRandomBuffer(size);
        byte[] outputBuffer = new byte[size];

        CloudBlockBlob blob = container.getBlockBlobReference("blockblob");

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        blob.uploadFromByteArray(buffer, 0, buffer.length, null, uploadOptions, null);

        // Download the encrypted blob.
        // Create the decryption policy to be used for download. There is no need to specify the
        // key when the policy is only going to be used for downloads. Resolver is sufficient.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);

        // Download and decrypt the encrypted contents from the blob.
        blob.downloadToByteArray(outputBuffer, 0, null, downloadOptions, null);

        // Compare that the decrypted contents match the input data.
        assertArrayEquals(buffer, outputBuffer);
    }

    @Test
    public void testBlobEncryptionWithText() throws StorageException, IOException, URISyntaxException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        String data = "String data";
        CloudBlockBlob blob = container.getBlockBlobReference("blockblob");

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        blob.uploadText(data, null, null, uploadOptions, null);

        // Download the encrypted blob.
        // Create the decryption policy to be used for download. There is no need to specify the
        // key when the policy is only going to be used for downloads. Resolver is sufficient.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);

        // Download and decrypt the encrypted contents from the blob.
        String outputData = blob.downloadText(null, null, downloadOptions, null);

        // Compare that the decrypted contents match the input data.
        assertEquals(data, outputData);
    }
    
    @Test
    public void testBlockBlobEncryptionValidateWrappers() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException, URISyntaxException, IOException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        RsaKey rsaKey = TestHelper.getRSAKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);
        resolver.add(rsaKey);

        doBlockBlobEncryptionValidateWrappers(aesKey, resolver);
        doBlockBlobEncryptionValidateWrappers(rsaKey, resolver);
    }

    private void doBlockBlobEncryptionValidateWrappers(IKey key, DictionaryKeyResolver keyResolver)
            throws StorageException, URISyntaxException, IOException {
        int size = 5 * 1024 * 1024;
        byte[] buffer = TestHelper.getRandomBuffer(size);

        CloudBlockBlob blob = this.container.getBlockBlobReference("blob1");

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(key, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, size, null, uploadOptions, null);

        // Download the encrypted blob.
        // Create the decryption policy to be used for download. There is no need to specify the encryption mode 
        // and the key wrapper when the policy is only going to be used for downloads.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, keyResolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);

        // Download and decrypt the encrypted contents from the blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream, null, downloadOptions, null);

        // Compare that the decrypted contents match the input data.
        byte[] outputArray = outputStream.toByteArray();
        assertArrayEquals(outputArray, buffer);
    }
    
    @Test
    public void testBlockBlobValidateEncryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException, IOException, InvalidAlgorithmParameterException,
            URISyntaxException, InterruptedException, ExecutionException {
        int size = 5 * 1024 * 1024;
        byte[] buffer = TestHelper.getRandomBuffer(size);

        CloudBlockBlob blob = container.getBlockBlobReference("blob1");

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, size, null, uploadOptions, null);

        // Encrypt locally.
        String metadata = blob.getMetadata().get(Constants.EncryptionConstants.BLOB_ENCRYPTION_DATA);
        BlobEncryptionData encryptionData = BlobEncryptionData.deserialize(metadata);

        Cipher myAes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptionData.getContentEncryptionIV());

        byte[] contentEncryptionKey = aesKey.unwrapKeyAsync(encryptionData.getWrappedContentKey().getEncryptedKey(),
                encryptionData.getWrappedContentKey().getAlgorithm()).get();
        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                "AES"); 
        
        myAes.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        CipherInputStream encryptedStream = new CipherInputStream(new ByteArrayInputStream(buffer), myAes);

        // Download the encrypted contents from the blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        for (int i = 0; i < outputStream.size(); i++) {
            assertEquals(encryptedStream.read(), inputStream.read());
        }

        encryptedStream.close();
    }
    
    @Test
    public void testBlockBlobEncryptionWithRangeDecryption() throws StorageException, URISyntaxException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 2 * 512, 1 * 512, 1 * 512L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 2 * 512, 0, null);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 2 * 512, 1 * 512, null);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 2 * 512, 0, 1 * 512L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 2 * 512, 4, 1 * 512L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1325, 368, 495L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1325, 369, 495L);

        // Edge cases
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1024, 1023, 1L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1024, 0, 1L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1024, 512, 1L);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1024, 0, 512L);

        // Check cases outside the blob size but within the padded size
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1025, 1023L, 4L, 2);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1025, 1023L, 16L, 2);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1025, 1023L, 17L, 2);
        this.validateRangeDecryption(BlobType.BLOCK_BLOB, 1025, 1024L, 16L, 1);
    }

    @Test
    public void testPageBlobEncryptionWithRangeDecryption() throws StorageException, URISyntaxException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 2 * 512, 1 * 512, 1 * 512 - 16L);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 2 * 512, 0, null);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 2 * 512, 1 * 512, null);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 2 * 512, 0, 1 * 512L);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 2 * 512, 4, 1 * 512L);

        // Edge cases
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 1024, 1023, 1L);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 1024, 0, 1L);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 1024, 512, 1L);
        this.validateRangeDecryption(BlobType.PAGE_BLOB, 1024, 0, 512L);
    }
    
    @Test
    public void testAppendBlobEncryptionvalidateRangeDecryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException, URISyntaxException, IOException {
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 2 * 512, 1 * 512, 1 * 512L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 2 * 512, 0, null);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 2 * 512, 1 * 512, null);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 2 * 512, 0, 1 * 512L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 2 * 512, 4, 1 * 512L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1325, 368, 495L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1325, 369, 495L);

        // Edge cases
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1024, 1023, 1L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1024, 0, 1L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1024, 512, 1L);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1024, 0, 512L);

        // Check cases outside the blob size but within the padded size
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1025, 1023L, 4L, 2);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1025, 1023L, 16L, 2);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1025, 1023L, 17L, 2);
        this.validateRangeDecryption(BlobType.APPEND_BLOB, 1025, 1024L, 16L, 1);
    }
    
    private void validateRangeDecryption(BlobType type, int blobSize, long blobOffset, Long length)
            throws StorageException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IOException {
        this.validateRangeDecryption(type, blobSize, blobOffset, length, null);
    }

    private void validateRangeDecryption(BlobType type, int blobSize, Long blobOffset, Long length,
            Integer verifyLength) throws StorageException, URISyntaxException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        byte[] buffer = BlobTestHelper.getRandomBuffer(blobSize);

        CloudBlob blob = null;
        if (type == BlobType.BLOCK_BLOB) {
            blob = this.container.getBlockBlobReference("blockblob");
        }
        else if (type == BlobType.PAGE_BLOB) {
            blob = this.container.getPageBlobReference("pageblob");
        }
        else if (type == BlobType.APPEND_BLOB) {
            blob = this.container.getAppendBlobReference("appendblob");
        }

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);
        
        // Set the encryption policy on the request options.
        BlobRequestOptions options = new BlobRequestOptions();
        options.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, blobSize, null, options, null);

        // Download a range in the encrypted blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.downloadRange(blobOffset, length, outputStream, null, options, null);

        // Compare that the decrypted contents match the input data.
        byte[] outputArray = outputStream.toByteArray();
        
        if (length != null)
        {
            if (verifyLength != null)
            {
                assertEquals(verifyLength.intValue(), outputArray.length);
            }
            else
            {
                assertEquals(length.intValue(), outputArray.length);
            }
        }
        
        for (int i = 0; i < outputArray.length; i++)
        {
            int bufferOffset = (int) (blobOffset != null ? blobOffset : 0);
            assertEquals(buffer[bufferOffset + i], outputArray[i]);
        }
    }
    
    @Test
    public void testBlobEncryptedWriteStreamTest() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IOException, StorageException, URISyntaxException, InterruptedException {
        doBlobEncryptedWriteStreamTest(BlobType.BLOCK_BLOB);
        doBlobEncryptedWriteStreamTest(BlobType.PAGE_BLOB);
        doBlobEncryptedWriteStreamTest(BlobType.APPEND_BLOB);
    }

    private void doBlobEncryptedWriteStreamTest(BlobType type) throws IOException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException, URISyntaxException, InterruptedException {
        byte[] buffer = TestHelper.getRandomBuffer(8 * 1024);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        OperationContext opContext = new OperationContext();
        BlobOutputStream blobStream = null;
        CloudBlob blob = null;
        if (type == BlobType.BLOCK_BLOB) {
            blob = this.container.getBlockBlobReference("blockblob");
            blob.setStreamWriteSizeInBytes(16 * 1024);
            blobStream = ((CloudBlockBlob) blob).openOutputStream(null, uploadOptions, opContext);
        }
        else if (type == BlobType.PAGE_BLOB) {
            blob = this.container.getPageBlobReference("pageblob");
            blob.setStreamWriteSizeInBytes(16 * 1024);
            blobStream = ((CloudPageBlob) blob).openWriteNew(40 * 1024, null, uploadOptions, opContext);
        }
        else if (type == BlobType.APPEND_BLOB) {
            blob = this.container.getAppendBlobReference("appendblob");
            blob.setStreamWriteSizeInBytes(16 * 1024);
            blobStream = ((CloudAppendBlob) blob).openWriteNew(null, uploadOptions, opContext);
        }

        ByteArrayOutputStream wholeBlob = new ByteArrayOutputStream();
        for (int i = 0; i < 3; i++) {
            blobStream.write(buffer, 0, buffer.length);
            wholeBlob.write(buffer, 0, buffer.length);
        }
        
        // Wait for writes to complete asynchronously
        Thread.sleep(10000);

        // Page and append blobs have one extra call due to create.
        if (type == BlobType.BLOCK_BLOB) {
            assertEquals(1, opContext.getRequestResults().size());
        }
        else {
            assertEquals(2, opContext.getRequestResults().size());
        }

        blobStream.write(buffer, 0, buffer.length);
        wholeBlob.write(buffer, 0, buffer.length);

        blobStream.write(buffer, 0, buffer.length);
        wholeBlob.write(buffer, 0, buffer.length);
        
        // Wait for writes to complete asynchronously
        Thread.sleep(10000);

        // Page and append blobs have one extra call due to create.
        if (type == BlobType.BLOCK_BLOB) {
            assertEquals(2, opContext.getRequestResults().size());
        }
        else {
            assertEquals(3, opContext.getRequestResults().size());
        }

        blobStream.close();

        // Block blobs have an additional PutBlockList call.
        assertEquals(4, opContext.getRequestResults().size());

        ByteArrayOutputStream downloadedBlob = new ByteArrayOutputStream();
        blob.download(downloadedBlob, null, uploadOptions, null);
        assertArrayEquals(wholeBlob.toByteArray(), downloadedBlob.toByteArray());
    }

    @Test
    @Category({ DevFabricTests.class, DevStoreTests.class })
    public void testBlobEncryptedReadStream() throws URISyntaxException, StorageException, IOException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] buffer = TestHelper.getRandomBuffer(8 * 1024);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        
        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Upload the encrypted contents to the blob.
        CloudBlob blob = this.container.getBlockBlobReference("blockblob");
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, buffer.length, null, uploadOptions, null);

        // Download the encrypted blob.
        // Create the decryption policy to be used for download. There is no need to specify the
        // key when the policy is only going to be used for downloads. Resolver is sufficient.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);
        
        // Open the read stream
        BlobInputStream blobStream = blob.openInputStream(null, downloadOptions, null);
        
        // Compare the streams
        BlobTestHelper.assertStreamsAreEqual(stream, blobStream);
    }
    
    @Test
    public void testBlobUpdateShouldThrowWithEncryption() throws StorageException, IOException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException {
        byte[] buffer = TestHelper.getRandomBuffer(16 * 1024);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        CloudBlockBlob blockBlob = container.getBlockBlobReference("blockblob");
        try {
            blockBlob.uploadBlock(UUID.randomUUID().toString(), stream, buffer.length, null, uploadOptions, null);
            fail("PutBlock does not support encryption.");
        }
        catch (IllegalArgumentException e) {

        }

        CloudPageBlob pageBlob = container.getPageBlobReference("pageblob");
        try {
            pageBlob.uploadPages(stream, 0, buffer.length, null, uploadOptions, null);
            fail("WritePages does not support encryption.");
        }
        catch (IllegalArgumentException e) {

        }

        try {
            pageBlob.clearPages(0, 512, null, uploadOptions, null);
            fail("ClearPages does not support encryption.");
        }
        catch (IllegalArgumentException e) {

        }
    }
    
    @Test
    public void testBlobUploadWorksWithDefaultRequestOptions() throws StorageException, IOException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        CloudBlobContainer container = BlobTestHelper.getRandomContainerReference();
        byte[] buffer = TestHelper.getRandomBuffer(16 * 1024);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy policy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions options = new BlobRequestOptions();
        options.setEncryptionPolicy(policy);
        
        // Set default request options
        container.getServiceClient().setDefaultRequestOptions(options);

        try
        {
            container.create();
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            CloudBlockBlob blockBlob = container.getBlockBlobReference("blockblob");
            blockBlob.upload(stream, buffer.length);
        }
        finally
        {
            container.deleteIfExists();
        }
    }
    
    @Test
    public void testBlobEncryptionWithStrictMode() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException, URISyntaxException, IOException {
        this.doCloudBlobEncryptionWithStrictMode(BlobType.BLOCK_BLOB);
        this.doCloudBlobEncryptionWithStrictMode(BlobType.PAGE_BLOB);
        this.doCloudBlobEncryptionWithStrictMode(BlobType.APPEND_BLOB);
    }

    private void doCloudBlobEncryptionWithStrictMode(BlobType type) throws StorageException, URISyntaxException,
            IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        int size = 5 * 1024 * 1024;
        byte[] buffer = TestHelper.getRandomBuffer(size);

        CloudBlob blob = null;

        if (type == BlobType.BLOCK_BLOB) {
            blob = this.container.getBlockBlobReference("blockblob");
        }
        else if (type == BlobType.PAGE_BLOB) {
            blob = this.container.getPageBlobReference("pageblob");
        }
        else if (type == BlobType.APPEND_BLOB) {
            blob = this.container.getAppendBlobReference("appendblob");
        }

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        // Create the encryption policy to be used for upload.
        BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);

        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        uploadOptions.setEncryptionPolicy(uploadPolicy);

        // Set RequireEncryption flag to true.
        uploadOptions.setRequireEncryption(true);

        // Upload an encrypted blob with the policy set.
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, size, null, uploadOptions, null);

        // Upload the blob when RequireEncryption is true and no policy is set. This should throw an error.
        uploadOptions.setEncryptionPolicy(null);

        stream = new ByteArrayInputStream(buffer);
        try {
            blob.upload(stream, size, null, uploadOptions, null);
            fail("Not specifying a policy when RequireEncryption is set to true should throw.");
        }
        catch (IllegalArgumentException ex) {
        }

        // Create the encryption policy to be used for download.
        BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);

        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        downloadOptions.setEncryptionPolicy(downloadPolicy);

        // Set RequireEncryption flag to true.
        downloadOptions.setRequireEncryption(true);

        // Download the encrypted blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream, null, downloadOptions, null);

        blob.getMetadata().clear();

        // Upload a plain text blob.
        stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, size);

        // Try to download an encrypted blob with RequireEncryption set to true. This should throw.
        outputStream = new ByteArrayOutputStream();
        try {
            blob.download(outputStream, null, downloadOptions, null);
            fail("Downloading with RequireEncryption set to true and no metadata on the service should fail.");
        }
        catch (StorageException ex) {
        }

        // Set RequireEncryption to false and download.
        downloadOptions.setRequireEncryption(false);
        blob.download(outputStream, null, downloadOptions, null);
    }

    @Test
    public void testBlobEncryptionWithStrictModeOnPartialBlob() throws URISyntaxException, StorageException,
            IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        int size = 2 * 1024 * 1024;
        byte[] buffer = TestHelper.getRandomBuffer(size);

        CloudBlob blob = null;

        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        String blockId = UUID.randomUUID().toString();

        BlobRequestOptions options = new BlobRequestOptions();
        options.setRequireEncryption(true);

        blob = container.getBlockBlobReference("blob1");
        try
        {
            ((CloudBlockBlob)blob).uploadBlock(blockId, stream, size, null, options, null);
            fail("PutBlock with RequireEncryption on should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }

        blob = container.getPageBlobReference("blob1");
        try
        {
            ((CloudPageBlob)blob).uploadPages(stream, 0, size, null, options, null);
            fail("WritePages with RequireEncryption on should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }

        blob = container.getAppendBlobReference("blob1");
        try
        {
            ((CloudAppendBlob)blob).appendBlock(stream, size, null, options, null);
            fail("AppendBlock with RequireEncryption on should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        options.setEncryptionPolicy(new BlobEncryptionPolicy(aesKey, null));

        blob = container.getBlockBlobReference("blob1");
        try
        {
            ((CloudBlockBlob)blob).uploadBlock(blockId, stream, size, null, options, null);
            fail("PutBlock with an EncryptionPolicy should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_NOT_SUPPORTED_FOR_OPERATION);
        }

        blob = container.getPageBlobReference("blob1");
        try
        {
            ((CloudPageBlob)blob).uploadPages(stream, 0, size, null, options, null);
            fail("WritePages with an EncryptionPolicy should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_NOT_SUPPORTED_FOR_OPERATION);
        }

        blob = container.getAppendBlobReference("blob1");
        try
        {
            ((CloudAppendBlob)blob).appendBlock(stream, size, null, options, null);
            fail("AppendBlock with an EncryptionPolicy should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_NOT_SUPPORTED_FOR_OPERATION);
        }
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsEncryptCalculateMD5PassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(true, true, true);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsEncryptCalculateMD5NoPassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(true, true, false);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsEncryptNoCalculateMD5PassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(true, false, true);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsEncryptNoCalculateMD5NoPassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(true, false, false);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsNoEncryptCalculateMD5PassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(false, true, true);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsNoEncryptCalculateMD5NoPassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(false, true, false);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsNoEncryptNoCalculateMD5PassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(false, false, true);
    }
    
    @Test
    public void testBlockBlobEncryptionCountOperationsNoEncryptNoCalculateMD5NoPassInLength() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.runBlockBlobEncryptionTests(false, false, false);
    }

    public void runBlockBlobEncryptionTests(boolean encryptData, boolean calculateMD5, boolean passInLength) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException, IOException {
        this.doEncryptionTestCountOperations(0, 1, encryptData, calculateMD5, passInLength);  // Test the zero-byte case
        this.doEncryptionTestCountOperations(10, 1, encryptData, calculateMD5, passInLength);  // Test a case that should definitely fit in one put blob, and is not 16-byte aligned.
        this.doEncryptionTestCountOperations(1 * Constants.MB, 1, encryptData, calculateMD5, passInLength);  // Test a case that is 16-byte aligned, and should fit in one put blob
        this.doEncryptionTestCountOperations(13 * Constants.MB, 5, encryptData, calculateMD5, passInLength);  // Test a case that should not hit put blob, but instead several put block + put block list.
    }
    
    private void doEncryptionTestCountOperations(int size, int count, boolean encryptData, boolean calculateMD5, boolean passInLength) throws URISyntaxException, StorageException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException
    {
        byte[] buffer = BlobTestHelper.getRandomBuffer(size);
    
        CloudBlockBlob blob = this.container.getBlockBlobReference("blockblob");
    
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
    
        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = null;
    
        // Set the encryption policy on the request options.
        BlobRequestOptions uploadOptions = new BlobRequestOptions();
        if (encryptData) {
            resolver = new DictionaryKeyResolver();
            resolver.add(aesKey);

            // Create the encryption policy to be used for upload.
            BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(aesKey, null);
            uploadOptions.setEncryptionPolicy(uploadPolicy);
        }
        
        uploadOptions.setStoreBlobContentMD5(calculateMD5);
        uploadOptions.setUseTransactionalContentMD5(calculateMD5);
        uploadOptions.setDisableContentMD5Validation(!calculateMD5);
        uploadOptions.setSingleBlobPutThresholdInBytes(8 * Constants.MB);
        blob.setStreamWriteSizeInBytes(4 * Constants.MB);
        
        OperationContext opContext = new OperationContext();
        
        final AtomicInteger operationCount = new AtomicInteger();
        
        opContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {
    
            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                operationCount.incrementAndGet();
            }
        });
        
        // Upload the encrypted contents to the blob.
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        blob.upload(stream, passInLength ? size : -1, null, uploadOptions, opContext);
        assertEquals(operationCount.intValue(), count);
    
        // Set the decryption policy on the request options.
        BlobRequestOptions downloadOptions = new BlobRequestOptions();
        if (encryptData) {
            // Download the encrypted blob.
            // Create the decryption policy to be used for download. There is no need to specify the
            // key when the policy is only going to be used for downloads. Resolver is sufficient.
            BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(null, resolver);
            downloadOptions.setEncryptionPolicy(downloadPolicy);
        }
    
        // Download and decrypt the encrypted contents from the blob.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream, null, downloadOptions, null);
    
        // Compare that the decrypted contents match the input data.
        TestHelper.assertStreamsAreEqualAtIndex(stream, new ByteArrayInputStream(outputStream.toByteArray()), 0, 0, 
                size, 2 * 1024);
    }
}