// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.options.AppendBlobCreateOptions;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.AppendBlobSealOptions;
import com.azure.storage.blob.options.AppendBlobAppendBlockFromUrlOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link AppendBlobClient}
 */
public class AppendBlobClientJavaDocCodeSnippets {

    private AppendBlobClient client = new SpecializedBlobClientBuilder().buildAppendBlobClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String leaseId = "leaseId";
    private InputStream data = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
    private long length = 4L;
    private static final Long POSITION = null;
    private Long maxSize = length;
    private String sourceUrl = "https://example.com";
    private long offset = 1024;
    private long count = 1024;

    /**
     * Code snippet for {@link AppendBlobClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.create
        System.out.printf("Created AppendBlob at %s%n", client.create().getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.create
    }

    /**
     * Code snippet for {@link AppendBlobClient#create(boolean)}
     */
    public void createWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.create#boolean
        boolean overwrite = false; // Default value
        System.out.printf("Created AppendBlob at %s%n", client.create(overwrite).getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.create#boolean
    }

    /**
     * Code snippet for {@link AppendBlobClient#createWithResponse(BlobHttpHeaders, Map, BlobRequestConditions,
     * Duration, Context)}
     */
    public void createWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentType("binary")
            .setContentLanguage("en-US");
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Created AppendBlob at %s%n",
            client.createWithResponse(headers, metadata, requestConditions, timeout, context).getValue()
                .getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#createWithResponse(AppendBlobCreateOptions, Duration, Context)}
     */
    public void createWithResponse2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#AppendBlobCreateOptions-Duration-Context
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentType("binary")
            .setContentLanguage("en-US");
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tags", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Created AppendBlob at %s%n",
            client.createWithResponse(new AppendBlobCreateOptions().setHeaders(headers).setMetadata(metadata)
                .setTags(tags).setRequestConditions(requestConditions), timeout, context).getValue()
                .getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#AppendBlobCreateOptions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlock(InputStream, long)}
     */
    public void appendBlock() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlock#InputStream-long
        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlock(data, length).getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlock#InputStream-long
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockWithResponse(InputStream, long, byte[],
     * AppendBlobRequestConditions, Duration, Context)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void appendBlock2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockWithResponse#InputStream-long-byte-AppendBlobRequestConditions-Duration-Context
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        AppendBlobRequestConditions requestConditions = new AppendBlobRequestConditions()
            .setAppendPosition(POSITION)
            .setMaxSize(maxSize);
        Context context = new Context("key", "value");

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockWithResponse(data, length, md5, requestConditions, timeout, context)
                .getValue().getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockWithResponse#InputStream-long-byte-AppendBlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrl(String, BlobRange)}
     */
    public void appendBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#String-BlobRange
        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count)).getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#String-BlobRange
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrlWithResponse(String, BlobRange, byte[],
     * AppendBlobRequestConditions, BlobRequestConditions, Duration, Context)}
     */
    public void appendBlockFromUrlWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#String-BlobRange-byte-AppendBlobRequestConditions-BlobRequestConditions-Duration-Context
        AppendBlobRequestConditions appendBlobRequestConditions = new AppendBlobRequestConditions()
            .setAppendPosition(POSITION)
            .setMaxSize(maxSize);

        BlobRequestConditions modifiedRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("key", "value");

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrlWithResponse(sourceUrl, new BlobRange(offset, count), null,
                appendBlobRequestConditions, modifiedRequestConditions, timeout,
                context).getValue().getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#String-BlobRange-byte-AppendBlobRequestConditions-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrlWithResponse(String, BlobRange, byte[],
     * AppendBlobRequestConditions, BlobRequestConditions, Duration, Context)}
     */
    public void appendBlockFromUrlOptionsBagWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#AppendBlobAppendBlockFromUrlOptions-Duration-Context
        AppendBlobRequestConditions appendBlobRequestConditions = new AppendBlobRequestConditions()
            .setAppendPosition(POSITION)
            .setMaxSize(maxSize);

        BlobRequestConditions modifiedRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("key", "value");

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrlWithResponse(new AppendBlobAppendBlockFromUrlOptions(sourceUrl)
                .setSourceRange(new BlobRange(offset, count))
                .setDestinationRequestConditions(appendBlobRequestConditions)
                .setSourceRequestConditions(modifiedRequestConditions), timeout,
                context).getValue().getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#AppendBlobAppendBlockFromUrlOptions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#seal()}
     */
    public void seal() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.seal
        client.seal();
        System.out.println("Sealed AppendBlob");
        // END: com.azure.storage.blob.specialized.AppendBlobClient.seal
    }

    /**
     * Code snippet for {@link AppendBlobClient#sealWithResponse(AppendBlobSealOptions, Duration, Context)}
     */
    public void seal2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.sealWithResponse#AppendBlobSealOptions-Duration-Context
        AppendBlobRequestConditions requestConditions = new AppendBlobRequestConditions().setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        client.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(requestConditions), timeout, context);
        System.out.println("Sealed AppendBlob");
        // END: com.azure.storage.blob.specialized.AppendBlobClient.sealWithResponse#AppendBlobSealOptions-Duration-Context
    }
}
