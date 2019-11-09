// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;

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
 * Code snippets for {@link PageBlobClient}
 */
@SuppressWarnings("unused")
public class PageBlobClientJavaDocCodeSnippets {
    private PageBlobClient client = new SpecializedBlobClientBuilder().buildPageBlobClient();
    private Map<String, String> metadata = Collections.singletonMap("metadata", "value");
    private String leaseId = "leaseId";
    private Duration timeout = Duration.ofSeconds(30);
    private long size = 1024;
    private long sequenceNumber = 0;
    private long sourceOffset = 0;
    private long offset = 0;
    private String key = "key";
    private String value = "value";
    private String data = "data";
    private String url = "https://sample.com";

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
     * Code snippets for {@link PageBlobClient#create(long, boolean)}
     */
    public void createWithOverwrite() {
        // BEGIN: com.azure.storage.blob.PageBlobClient.create#long-boolean
        boolean overwrite = false; // Default value
        PageBlobItem pageBlob = client.create(size, overwrite);
        System.out.printf("Created page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.PageBlobClient.create#long-boolean
    }

    /**
     * Code snippets for {@link PageBlobClient#createWithResponse(long, Long, BlobHttpHeaders, Map, BlobRequestConditions, Duration, Context)}
     */
    public void createWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .createWithResponse(size, sequenceNumber, headers, metadata, blobRequestConditions, timeout, context)
            .getValue();

        System.out.printf("Created page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context
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
     * Code snippets for {@link PageBlobClient#uploadPagesWithResponse(PageRange, InputStream, byte[],
     * PageBlobRequestConditions, Duration, Context)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void uploadPagesWithResponseCodeSnippet() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-byte-PageBlobRequestConditions-Duration-Context
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .uploadPagesWithResponse(pageRange, dataStream, md5, pageBlobRequestConditions, timeout, context).getValue();

        System.out.printf("Uploaded page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-byte-PageBlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPagesFromUrl(PageRange, String, Long)}
     */
    public void uploadPagesFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrl#PageRange-String-Long
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        PageBlobItem pageBlob = client.uploadPagesFromUrl(pageRange, url, sourceOffset);

        System.out.printf("Uploaded page blob from URL with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrl#PageRange-String-Long
    }

    /**
     * Code snippets for {@link PageBlobClient#uploadPagesFromUrlWithResponse(PageRange, String, Long, byte[],
     * PageBlobRequestConditions, BlobRequestConditions, Duration, Context)}
     */
    public void uploadPagesFromUrlWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions-Duration-Context
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        byte[] sourceContentMD5 = new byte[512];
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);
        BlobRequestConditions sourceRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .uploadPagesFromUrlWithResponse(pageRange, url, sourceOffset, sourceContentMD5, pageBlobRequestConditions,
                sourceRequestConditions, timeout, context).getValue();

        System.out.printf("Uploaded page blob from URL with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions-Duration-Context
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
     * Code snippets for {@link PageBlobClient#clearPagesWithResponse(PageRange, PageBlobRequestConditions, Duration,
     * Context)}
     */
    public void clearPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions-Duration-Context
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .clearPagesWithResponse(pageRange, pageBlobRequestConditions, timeout, context).getValue();

        System.out.printf("Cleared page blob with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions-Duration-Context
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
     * Code snippets for {@link PageBlobClient#getPageRangesWithResponse(BlobRange, BlobRequestConditions, Duration,
     * Context)}
     */
    public void getPageRangesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions-Duration-Context
        BlobRange blobRange = new BlobRange(offset);
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageList pageList = client
            .getPageRangesWithResponse(blobRange, blobRequestConditions, timeout, context).getValue();

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions-Duration-Context
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
     * Code snippets for {@link PageBlobClient#getPageRangesDiffWithResponse(BlobRange, String, BlobRequestConditions,
     * Duration, Context)}
     */
    public void getPageRangesDiffWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageList pageList = client
            .getPageRangesDiffWithResponse(blobRange, prevSnapshot, blobRequestConditions, timeout, context).getValue();

        System.out.println("Valid Page Ranges are:");
        for (PageRange pageRange : pageList.getPageRange()) {
            System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context
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
     * Code snippets for {@link PageBlobClient#resizeWithResponse(long, BlobRequestConditions, Duration, Context)}
     */
    public void resizeWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobRequestConditions-Duration-Context
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client
            .resizeWithResponse(size, blobRequestConditions, timeout, context).getValue();
        System.out.printf("Page blob resized with sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobRequestConditions-Duration-Context
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
     * BlobRequestConditions, Duration, Context)}
     */
    public void updateSequenceNumberWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions-Duration-Context
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Context context = new Context(key, value);

        PageBlobItem pageBlob = client.updateSequenceNumberWithResponse(
            SequenceNumberActionType.INCREMENT, size, blobRequestConditions, timeout, context).getValue();

        System.out.printf("Page blob updated to sequence number %s%n", pageBlob.getBlobSequenceNumber());
        // END: com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PageBlobClient#copyIncremental(String, String)}
     */
    public void copyIncrementalCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#String-String
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
        // END: com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#String-String
    }

    /**
     * Code snippets for {@link PageBlobClient#copyIncrementalWithResponse(String, String, RequestConditions,
     * Duration, Context)}
     */
    public void copyIncrementalWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#String-String-RequestConditions-Duration-Context
        final String snapshot = "copy snapshot";
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfNoneMatch("snapshotMatch");
        Context context = new Context(key, value);

        CopyStatusType statusType = client
            .copyIncrementalWithResponse(url, snapshot, modifiedRequestConditions, timeout, context).getValue();

        if (CopyStatusType.SUCCESS == statusType) {
            System.out.println("Page blob copied successfully");
        } else if (CopyStatusType.FAILED == statusType) {
            System.out.println("Page blob copied failed");
        } else if (CopyStatusType.ABORTED == statusType) {
            System.out.println("Page blob copied aborted");
        } else if (CopyStatusType.PENDING == statusType) {
            System.out.println("Page blob copied pending");
        }
        // END: com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#String-String-RequestConditions-Duration-Context
    }

}
