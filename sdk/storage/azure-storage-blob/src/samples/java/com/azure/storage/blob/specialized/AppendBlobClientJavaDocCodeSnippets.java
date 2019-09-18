// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;

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
    private URL sourceUrl = new URL("https://example.com");
    private long offset = 1024;
    private long count = 1024;

    /**
     * @throws MalformedURLException Ignore
     */
    public AppendBlobClientJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippet for {@link AppendBlobClient#create()}
     */
    public void setCreate() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.create
        System.out.printf("Created AppendBlob at %s%n", client.create().getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.create
    }

    /**
     * Code snippet for {@link AppendBlobClient#create(BlobHTTPHeaders, Metadata, BlobAccessConditions, Duration)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.create#BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentType("binary")
            .setBlobContentLanguage("en-US");
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        System.out.printf("Created AppendBlob at %s%n",
            client.create(headers, metadata, accessConditions, timeout).getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.create#BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration
    }

    /**
     * Code snippet for {@link AppendBlobClient#createWithResponse(BlobHTTPHeaders, Metadata, BlobAccessConditions,
     * Duration, Context)}
     */
    public void create3() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration-Context
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentType("binary")
            .setBlobContentLanguage("en-US");
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("key", "value");

        System.out.printf("Created AppendBlob at %s%n",
            client.createWithResponse(headers, metadata, accessConditions, timeout, context).getValue().getLastModified());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.createWithResponse#BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration-Context
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
     * Code snippet for {@link AppendBlobClient#appendBlockWithResponse(InputStream, long, AppendBlobAccessConditions,
     * Duration, Context)}
     */
    public void appendBlock2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockWithResponse#InputStream-long-AppendBlobAccessConditions-Duration-Context
        AppendBlobAccessConditions accessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));
        Context context = new Context("key", "value");

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockWithResponse(data, length, accessConditions, timeout,
                context).getValue().getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockWithResponse#InputStream-long-AppendBlobAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrl(URL, BlobRange)}
     */
    public void appendBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#URL-BlobRange
        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count)).getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#URL-BlobRange
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrl(URL, BlobRange, byte[], AppendBlobAccessConditions,
     * SourceModifiedAccessConditions, Duration)}
     */
    public void appendBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions-Duration
        AppendBlobAccessConditions appendBlobAccessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        SourceModifiedAccessConditions modifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count), null,
                appendBlobAccessConditions, modifiedAccessConditions, timeout).getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrl#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions-Duration
    }

    /**
     * Code snippet for {@link AppendBlobClient#appendBlockFromUrlWithResponse(URL, BlobRange, byte[],
     * AppendBlobAccessConditions, SourceModifiedAccessConditions, Duration, Context)}
     */
    public void appendBlockFromUrl3() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions-Duration-Context
        AppendBlobAccessConditions appendBlobAccessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        SourceModifiedAccessConditions modifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("key", "value");

        System.out.printf("AppendBlob has %d committed blocks%n",
            client.appendBlockFromUrlWithResponse(sourceUrl, new BlobRange(offset, count), null,
                appendBlobAccessConditions, modifiedAccessConditions, timeout,
                context).getValue().getBlobCommittedBlockCount());
        // END: com.azure.storage.blob.specialized.AppendBlobClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions-Duration-Context
    }
}
