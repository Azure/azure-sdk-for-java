// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
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
 * Code snippets for {@link PageBlobClient}
 */
@SuppressWarnings("unused")
public class PageBlobClientJavaDocCodeSnippets {
    private PageBlobClient client = new SpecializedBlobClientBuilder().buildPageBlobClient();
    private Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
    private String leaseId = "leaseId";
    private Duration timeout = Duration.ofSeconds(30);
    private long size = 1024;
    private long sequenceNumber = 0;
    private long sourceOffset = 0;
    private long offset = 0;
    private String key = "key";
    private String value = "value";
    private String data = "data";
    private URL url = new URL("https://sample.com");

    /**
     * @throws MalformedURLException ignored
     */
    public PageBlobClientJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippets for {@link PageBlobClient#create(long)}
     */
    public void createCodeSnippet() {
        // BEGIN: com.azure.storage.blob.PageBlobClient.create#long
        PageBlobItem pageBlob = client.create(size);
        System.out.printf("Created page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.PageBlobClient.create#long
    }

    /**
     * Code snippets for {@link PageBlobClient#createWithResponse(long, Long, BlobHTTPHeaders, Metadata,
     * BlobAccessConditions, Duration, Context)}
     */
    public void createWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration-Context
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .createWithResponse(size, sequenceNumber, headers, metadata, blobAccessConditions, timeout, context)
            .getValue();

        System.out.printf("Created page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHTTPHeaders-Metadata-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPages(PageRange, InputStream)}
     */
    public void uploadPagesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPages#PageRange-InputStream
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        PageBlobItem pageBlob = client.uploadPages(pageRange, dataStream);
        System.out.printf("Uploaded page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPages#PageRange-InputStream
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPagesWithResponse(PageRange, InputStream, PageBlobAccessConditions,
     * Duration, Context)}
     */
    public void uploadPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-PageBlobAccessConditions-Duration-Context
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .uploadPagesWithResponse(pageRange, dataStream, pageBlobAccessConditions, timeout, context).getValue();

        System.out.printf("Uploaded page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-PageBlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPagesFromURL(PageRange, URL, Long)}
     */
    public void uploadPagesFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromURL#PageRange-URL-Long
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        PageBlobItem pageBlob = client.uploadPagesFromURL(pageRange, url, sourceOffset);

        System.out.printf("Uploaded page blob from URL with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromURL#PageRange-URL-Long
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPagesFromURLWithResponse(PageRange, URL, Long, byte[],
     * PageBlobAccessConditions, SourceModifiedAccessConditions, Duration, Context)}
     */
    public void uploadPagesFromURLWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromURLWithResponse#PageRange-URL-Long-byte-PageBlobAccessConditions-SourceModifiedAccessConditions-Duration-Context
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        byte[] sourceContentMD5 = new byte[512];
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        SourceModifiedAccessConditions sourceAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(OffsetDateTime.now());
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .uploadPagesFromURLWithResponse(pageRange, url, sourceOffset, sourceContentMD5, pageBlobAccessConditions,
                sourceAccessConditions, timeout, context).getValue();

        System.out.printf("Uploaded page blob from URL with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromURLWithResponse#PageRange-URL-Long-byte-PageBlobAccessConditions-SourceModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#clearPages(PageRange)}
     */
    public void clearPagesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.clearPages#PageRange
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        PageBlobItem pageBlob = client.clearPages(pageRange);

        System.out.printf("Cleared page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.clearPages#PageRange
    }

    /**
     * Code snippets for {@link PageBlobClient#clearPagesWithResponse(PageRange, PageBlobAccessConditions, Duration,
     * Context)}
     */
    public void clearPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobAccessConditions-Duration-Context
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .clearPagesWithResponse(pageRange, pageBlobAccessConditions, timeout, context).getValue();

        System.out.printf("Cleared page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#getPageRanges(BlobRange)}
     */
    public void getPageRangesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRanges#BlobRange
        BlobRange blobRange = new BlobRange(offset);
        PageList pageList = client.getPageRanges(blobRange);

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRanges#BlobRange
    }

    /**
     * Code snippets for {@link PageBlobClient#getPageRangesWithResponse(BlobRange, BlobAccessConditions, Duration,
     * Context)}
     */
    public void getPageRangesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobAccessConditions-Duration-Context
        BlobRange blobRange = new BlobRange(offset);
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageList pageList = client
            .getPageRangesWithResponse(blobRange, blobAccessConditions, timeout, context).getValue();

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#getPageRangesDiff(BlobRange, String)}
     */
    public void getPageRangesDiffCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiff#BlobRange-String
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";
        PageList pageList = client.getPageRangesDiff(blobRange, prevSnapshot);

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiff#BlobRange-String
    }

    /**
     * Code snippets for {@link PageBlobClient#getPageRangesDiffWithResponse(BlobRange, String, BlobAccessConditions,
     * Duration, Context)}
     */
    public void getPageRangesDiffWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobAccessConditions-Duration-Context
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageList pageList = client
            .getPageRangesDiffWithResponse(blobRange, prevSnapshot, blobAccessConditions, timeout, context).getValue();

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#resize(long)}
     */
    public void resizeCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.resize#long
        PageBlobItem pageBlob = client.resize(size);
        System.out.printf("Page blob resized with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.resize#long
    }

    /**
     * Code snippets for {@link PageBlobClient#resizeWithResponse(long, BlobAccessConditions, Duration, Context)}
     */
    public void resizeWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobAccessConditions-Duration-Context
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .resizeWithResponse(size, blobAccessConditions, timeout, context).getValue();
        System.out.printf("Page blob resized with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#updateSequenceNumber(SequenceNumberActionType, Long)}
     */
    public void updateSequenceNumberCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumber#SequenceNumberActionType-Long
        PageBlobItem pageBlob = client.updateSequenceNumber(SequenceNumberActionType.INCREMENT, size);

        System.out.printf("Page blob updated to sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumber#SequenceNumberActionType-Long
    }

    /**
     * Code snippets for {@link PageBlobClient#updateSequenceNumberWithResponse(SequenceNumberActionType, Long,
     * BlobAccessConditions, Duration, Context)}
     */
    public void updateSequenceNumberWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobAccessConditions-Duration-Context
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client.updateSequenceNumberWithResponse(
            SequenceNumberActionType.INCREMENT, size, blobAccessConditions, timeout, context).getValue();

        System.out.printf("Page blob updated to sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#copyIncremental(URL, String)}
     */
    public void copyIncrementalCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#URL-String
        final String snapshot = "copy snapshot";
        CopyStatusType statusType = client.copyIncremental(url, snapshot);

        if (CopyStatusType.SUCCESS == statusType) {
            System.out.println("Page blob copied successfully");
        } else if (CopyStatusType.FAILED == statusType) {
            System.out.println("Page blob copied failed");
        } else if (CopyStatusType.ABORTED == statusType) {
            System.out.println("Page blob copied aborted");
        } else if (CopyStatusType.PENDING == statusType) {
            System.out.println("Page blob copied pending");
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#URL-String
    }

    /**
     * Code snippets for {@link PageBlobClient#copyIncrementalWithResponse(URL, String, ModifiedAccessConditions,
     * Duration, Context)}
     */
    public void copyIncrementalWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#URL-String-ModifiedAccessConditions-Duration-Context
        final String snapshot = "copy snapshot";
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfNoneMatch("snapshotMatch");
        Context context = new Context(key, value);

        CopyStatusType statusType = client
            .copyIncrementalWithResponse(url, snapshot, modifiedAccessConditions, timeout, context).getValue();

        if (CopyStatusType.SUCCESS == statusType) {
            System.out.println("Page blob copied successfully");
        } else if (CopyStatusType.FAILED == statusType) {
            System.out.println("Page blob copied failed");
        } else if (CopyStatusType.ABORTED == statusType) {
            System.out.println("Page blob copied aborted");
        } else if (CopyStatusType.PENDING == statusType) {
            System.out.println("Page blob copied pending");
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#URL-String-ModifiedAccessConditions-Duration-Context
    }

}
