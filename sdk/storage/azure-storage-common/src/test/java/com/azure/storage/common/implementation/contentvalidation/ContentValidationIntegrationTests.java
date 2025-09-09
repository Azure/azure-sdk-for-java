// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for structured message content validation across all storage services.
 */
public class ContentValidationIntegrationTests {

    @Test
    public void testDownloadContentValidationOptionsAcrossServices() {
        // Create validation options
        DownloadContentValidationOptions validationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true)
            .setMd5ValidationEnabled(false);

        // Test Blob Storage integration
        testBlobStorageIntegration(validationOptions);
        
        // Test Data Lake integration
        testDataLakeIntegration(validationOptions);
        
        // Test File Share integration
        testFileShareIntegration(validationOptions);
    }

    private void testBlobStorageIntegration(DownloadContentValidationOptions validationOptions) {
        // Test BlobDownloadToFileOptions
        BlobDownloadToFileOptions blobDownloadOptions = new BlobDownloadToFileOptions("/tmp/test.txt")
            .setContentValidationOptions(validationOptions);
        
        assertNotNull(blobDownloadOptions.getContentValidationOptions());
        assertTrue(blobDownloadOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertFalse(blobDownloadOptions.getContentValidationOptions().isMd5ValidationEnabled());

        // Test BlobInputStreamOptions
        BlobInputStreamOptions inputStreamOptions = new BlobInputStreamOptions()
            .setContentValidationOptions(validationOptions);
        
        assertNotNull(inputStreamOptions.getContentValidationOptions());
        assertTrue(inputStreamOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());

        // Test BlobSeekableByteChannelReadOptions
        BlobSeekableByteChannelReadOptions seekableOptions = new BlobSeekableByteChannelReadOptions()
            .setContentValidationOptions(validationOptions);
        
        assertNotNull(seekableOptions.getContentValidationOptions());
        assertTrue(seekableOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
    }

    private void testDataLakeIntegration(DownloadContentValidationOptions validationOptions) {
        // Test ReadToFileOptions for Data Lake
        ReadToFileOptions dataLakeOptions = new ReadToFileOptions("/tmp/datalake-test.txt")
            .setContentValidationOptions(validationOptions);
        
        assertNotNull(dataLakeOptions.getContentValidationOptions());
        assertTrue(dataLakeOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertFalse(dataLakeOptions.getContentValidationOptions().isMd5ValidationEnabled());
    }

    private void testFileShareIntegration(DownloadContentValidationOptions validationOptions) {
        // Test ShareFileDownloadOptions
        ShareFileDownloadOptions shareOptions = new ShareFileDownloadOptions()
            .setContentValidationOptions(validationOptions);
        
        assertNotNull(shareOptions.getContentValidationOptions());
        assertTrue(shareOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertFalse(shareOptions.getContentValidationOptions().isMd5ValidationEnabled());
    }

    @Test
    public void testMixedValidationOptions() {
        // Test mixed validation options across services
        DownloadContentValidationOptions mixedOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true)
            .setMd5ValidationEnabled(true);

        // Blob Storage with mixed options
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions("/tmp/mixed-test.txt")
            .setContentValidationOptions(mixedOptions);
        
        assertTrue(blobOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertTrue(blobOptions.getContentValidationOptions().isMd5ValidationEnabled());

        // Data Lake with mixed options
        ReadToFileOptions dataLakeOptions = new ReadToFileOptions("/tmp/mixed-datalake.txt")
            .setContentValidationOptions(mixedOptions);
        
        assertTrue(dataLakeOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertTrue(dataLakeOptions.getContentValidationOptions().isMd5ValidationEnabled());

        // File Share with mixed options
        ShareFileDownloadOptions shareOptions = new ShareFileDownloadOptions()
            .setContentValidationOptions(mixedOptions);
        
        assertTrue(shareOptions.getContentValidationOptions().isStructuredMessageValidationEnabled());
        assertTrue(shareOptions.getContentValidationOptions().isMd5ValidationEnabled());
    }

    @Test
    public void testFluentInterfaceConsistency() {
        // Test that all options classes follow consistent fluent interface patterns
        DownloadContentValidationOptions validationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);

        // All setContentValidationOptions methods should return the same instance (fluent interface)
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions("/tmp/fluent-test.txt");
        BlobDownloadToFileOptions returnedBlobOptions = blobOptions.setContentValidationOptions(validationOptions);
        assertSame(blobOptions, returnedBlobOptions);

        BlobInputStreamOptions inputStreamOptions = new BlobInputStreamOptions();
        BlobInputStreamOptions returnedInputStreamOptions = inputStreamOptions.setContentValidationOptions(validationOptions);
        assertSame(inputStreamOptions, returnedInputStreamOptions);

        BlobSeekableByteChannelReadOptions seekableOptions = new BlobSeekableByteChannelReadOptions();
        BlobSeekableByteChannelReadOptions returnedSeekableOptions = seekableOptions.setContentValidationOptions(validationOptions);
        assertSame(seekableOptions, returnedSeekableOptions);

        ReadToFileOptions dataLakeOptions = new ReadToFileOptions("/tmp/fluent-datalake.txt");
        ReadToFileOptions returnedDataLakeOptions = dataLakeOptions.setContentValidationOptions(validationOptions);
        assertSame(dataLakeOptions, returnedDataLakeOptions);

        ShareFileDownloadOptions shareOptions = new ShareFileDownloadOptions();
        ShareFileDownloadOptions returnedShareOptions = shareOptions.setContentValidationOptions(validationOptions);
        assertSame(shareOptions, returnedShareOptions);
    }

    @Test
    public void testDefaultValues() {
        // Test that default values are consistently null across all option classes
        assertNull(new BlobDownloadToFileOptions("/tmp/test.txt").getContentValidationOptions());
        assertNull(new BlobInputStreamOptions().getContentValidationOptions());
        assertNull(new BlobSeekableByteChannelReadOptions().getContentValidationOptions());
        assertNull(new ReadToFileOptions("/tmp/test.txt").getContentValidationOptions());
        assertNull(new ShareFileDownloadOptions().getContentValidationOptions());
    }
}