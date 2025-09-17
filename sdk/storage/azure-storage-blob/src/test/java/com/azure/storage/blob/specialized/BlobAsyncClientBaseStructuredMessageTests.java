// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.common.implementation.contentvalidation.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageDecoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageEncoder;
import com.azure.storage.common.implementation.structuredmessage.StructuredMessageFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for structured message decoder integration with blob download methods.
 */
public class BlobAsyncClientBaseStructuredMessageTests {

    @Mock
    private BlobAsyncClientBase blobAsyncClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDownloadContentValidationOptionsCreation() {
        // Test creating options with structured message validation enabled
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
        
        assertTrue(options.isStructuredMessageValidationEnabled());
        assertFalse(options.isMd5ValidationEnabled());
    }

    @Test
    public void testDownloadContentValidationOptionsWithMd5() {
        // Test creating options with MD5 validation enabled
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setMd5ValidationEnabled(true);
        
        assertFalse(options.isStructuredMessageValidationEnabled());
        assertTrue(options.isMd5ValidationEnabled());
    }

    @Test
    public void testDownloadContentValidationOptionsBothEnabled() {
        // Test creating options with both validations enabled
        DownloadContentValidationOptions options = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true)
            .setMd5ValidationEnabled(true);
        
        assertTrue(options.isStructuredMessageValidationEnabled());
        assertTrue(options.isMd5ValidationEnabled());
    }

    @Test
    public void testBlobDownloadToFileOptionsWithContentValidation() {
        // Test setting content validation options on BlobDownloadToFileOptions
        DownloadContentValidationOptions contentValidationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
            
        BlobDownloadToFileOptions options = new BlobDownloadToFileOptions("/tmp/test.txt")
            .setContentValidationOptions(contentValidationOptions);
        
        assertNotNull(options.getContentValidationOptions());
        assertTrue(options.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertEquals("/tmp/test.txt", options.getFilePath());
    }

    @Test
    public void testStructuredMessageEncoderDecoderIntegration() throws IOException {
        // Test that encoder and decoder work together correctly
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        
        // Encode the data
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        
        // Decode the data
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedData.remaining());
        ByteBuffer decodedData = decoder.decode(encodedData);
        decoder.finalizeDecoding();
        
        // Verify the decoded data matches original
        byte[] decodedBytes = new byte[decodedData.remaining()];
        decodedData.get(decodedBytes);
        assertArrayEquals(originalData, decodedBytes);
    }

    @Test
    public void testBlobInputStreamOptionsWithContentValidation() {
        // Test setting content validation options on BlobInputStreamOptions
        DownloadContentValidationOptions contentValidationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
            
        BlobInputStreamOptions options = new BlobInputStreamOptions()
            .setContentValidationOptions(contentValidationOptions);
        
        assertNotNull(options.getContentValidationOptions());
        assertTrue(options.getContentValidationOptions().isStructuredMessageValidationEnabled());
    }

    @Test
    public void testStructuredMessageValidationWithCrc64() throws IOException {
        // Test that CRC64 validation works
        byte[] originalData = new byte[2048];
        ThreadLocalRandom.current().nextBytes(originalData);
        
        // Encode with CRC64
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, 1024, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        
        // Decode and verify CRC64 validation
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedData.remaining());
        ByteBuffer decodedData = decoder.decode(encodedData);
        decoder.finalizeDecoding(); // This should not throw if CRC64 validation passes
        
        // Verify the data
        byte[] decodedBytes = new byte[decodedData.remaining()];
        decodedData.get(decodedBytes);
        assertArrayEquals(originalData, decodedBytes);
    }

    @Test
    public void testStructuredMessageValidationWithCorruptedData() throws IOException {
        // Test that CRC64 validation detects corrupted data
        byte[] originalData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(originalData);
        
        // Encode the data
        StructuredMessageEncoder encoder = new StructuredMessageEncoder(originalData.length, 512, StructuredMessageFlags.STORAGE_CRC64);
        ByteBuffer encodedData = encoder.encode(ByteBuffer.wrap(originalData));
        
        // Corrupt some data in the middle
        byte[] encodedArray = encodedData.array();
        if (encodedArray.length > 100) {
            encodedArray[100] = (byte) (encodedArray[100] ^ 0xFF); // Flip all bits
        }
        
        // Try to decode - should fail with CRC mismatch
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(encodedData.remaining());
        assertThrows(IllegalArgumentException.class, () -> {
            decoder.decode(ByteBuffer.wrap(encodedArray));
        });
    }
}