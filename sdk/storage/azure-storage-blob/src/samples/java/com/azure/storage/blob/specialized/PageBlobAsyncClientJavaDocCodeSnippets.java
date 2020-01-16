// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link PageBlobAsyncClient}
 */
@SuppressWarnings("unused")
public class PageBlobAsyncClientJavaDocCodeSnippets {
    private PageBlobAsyncClient client = new SpecializedBlobClientBuilder().buildPageBlobAsyncClient();
    private Map<String, String> metadata = Collections.singletonMap("metadata", "value");
    private ByteBuffer[] bufferData = new ByteBuffer[]{
        ByteBuffer.wrap(new byte[]{1}),
        ByteBuffer.wrap(new byte[]{2})
    };
    private Flux<ByteBuffer> body = Flux.fromArray(bufferData);
    private long size = 1024;
    private String leaseId = "leaseId";
    private long sequenceNumber = 0;
    private String url = "https://sample.com";
    private long sourceOffset = 0;
    private String data = "data";
    private long offset = 0;

    /**
     * Code snippets for {@link PageBlobAsyncClient#create(long)}
     */
    public void setCreateCodeSnippet() {
        // BEGIN: com.azure.storage.blob.PageBlobAsyncClient.create#long
        client.create(size).subscribe(response -> System.out.printf(
            "Created page blob with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.PageBlobAsyncClient.create#long
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#create(long, boolean)}
     */
    public void createWithOverwrite() {
        // BEGIN: com.azure.storage.blob.PageBlobAsyncClient.create#long-boolean
        boolean overwrite = false; // Default behavior
        client.create(size, overwrite).subscribe(response -> System.out.printf(
            "Created page blob with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.PageBlobAsyncClient.create#long-boolean
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#createWithResponse(long, Long, BlobHttpHeaders, Map, BlobRequestConditions)}
     */
    public void createWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.createWithResponse(size, sequenceNumber, headers, metadata, blobRequestConditions)
            .subscribe(response -> System.out.printf(
                "Created page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));

        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPages(PageRange, Flux)}
     */
    public void uploadPagesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPages#PageRange-Flux
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        client.uploadPages(pageRange, body).subscribe(response -> System.out.printf(
            "Uploaded page blob with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPages#PageRange-Flux
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesWithResponse(PageRange, Flux, byte[], PageBlobRequestConditions)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void uploadPagesWithResponseCodeSnippet() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-byte-PageBlobRequestConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);

        client.uploadPagesWithResponse(pageRange, body, md5, pageBlobRequestConditions)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-byte-PageBlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesFromUrl(PageRange, String, Long)}
     */
    public void uploadPagesFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrl#PageRange-String-Long
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        client.uploadPagesFromUrl(pageRange, url, sourceOffset)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob from URL with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrl#PageRange-String-Long
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesFromUrlWithResponse(PageRange, String, Long, byte[],
     * PageBlobRequestConditions, BlobRequestConditions)}
     */
    public void uploadPagesFromUrlWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        byte[] sourceContentMD5 = new byte[512];
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);
        BlobRequestConditions sourceRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadPagesFromUrlWithResponse(pageRange, url, sourceOffset, sourceContentMD5, pageBlobRequestConditions,
                sourceRequestConditions)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob from URL with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#clearPages(PageRange)}
     */
    public void clearPagesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPages#PageRange
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        client.clearPages(pageRange).subscribe(response -> System.out.printf(
            "Cleared page blob with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPages#PageRange
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#clearPagesWithResponse(PageRange, PageBlobRequestConditions)}
     */
    public void clearPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions().setLeaseId(leaseId);

        client.clearPagesWithResponse(pageRange, pageBlobRequestConditions)
            .subscribe(response -> System.out.printf(
                "Cleared page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#getPageRanges(BlobRange)}
     */
    public void getPageRangesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRanges#BlobRange
        BlobRange blobRange = new BlobRange(offset);

        client.getPageRanges(blobRange).subscribe(response -> {
            System.out.println("Valid Page Ranges are:");
            for (PageRange pageRange : response.getPageRange()) {
                System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
            }
        });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRanges#BlobRange
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#getPageRangesWithResponse(BlobRange, BlobRequestConditions)}
     */
    public void getPageRangesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions
        BlobRange blobRange = new BlobRange(offset);
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.getPageRangesWithResponse(blobRange, blobRequestConditions)
            .subscribe(response -> {
                System.out.println("Valid Page Ranges are:");
                for (PageRange pageRange : response.getValue().getPageRange()) {
                    System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
                }
            });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#getPageRangesDiff(BlobRange, String)}
     */
    public void getPageRangesDiffCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiff#BlobRange-String
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";

        client.getPageRangesDiff(blobRange, prevSnapshot).subscribe(response -> {
            System.out.println("Valid Page Ranges are:");
            for (PageRange pageRange : response.getPageRange()) {
                System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
            }
        });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiff#BlobRange-String
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#getPageRangesDiffWithResponse(BlobRange, String,
     * BlobRequestConditions)}
     */
    public void getPageRangesDiffWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.getPageRangesDiffWithResponse(blobRange, prevSnapshot, blobRequestConditions)
            .subscribe(response -> {
                System.out.println("Valid Page Ranges are:");
                for (PageRange pageRange : response.getValue().getPageRange()) {
                    System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
                }
            });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#resize(long)}
     */
    public void resizeCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.resize#long
        client.resize(size).subscribe(response -> System.out.printf(
            "Page blob resized with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.resize#long
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#resizeWithResponse(long, BlobRequestConditions)}
     */
    public void resizeWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobRequestConditions
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.resizeWithResponse(size, blobRequestConditions)
            .subscribe(response -> System.out.printf(
                "Page blob resized with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#updateSequenceNumber(SequenceNumberActionType, Long)}
     */
    public void updateSequenceNumberCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumber#SequenceNumberActionType-Long
        client.updateSequenceNumber(SequenceNumberActionType.INCREMENT, size)
            .subscribe(response -> System.out.printf(
                "Page blob updated to sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumber#SequenceNumberActionType-Long
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#updateSequenceNumberWithResponse(SequenceNumberActionType, Long,
     * BlobRequestConditions)}
     */
    public void updateSequenceNumberWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT, size, blobRequestConditions)
            .subscribe(response -> System.out.printf(
                "Page blob updated to sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#copyIncremental(String, String)}
     */
    public void copyIncrementalCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#String-String
        final String snapshot = "copy snapshot";
        client.copyIncremental(url, snapshot).subscribe(response -> {
            if (CopyStatusType.SUCCESS == response) {
                System.out.println("Page blob copied successfully");
            } else if (CopyStatusType.FAILED == response) {
                System.out.println("Page blob copied failed");
            } else if (CopyStatusType.ABORTED == response) {
                System.out.println("Page blob copied aborted");
            } else if (CopyStatusType.PENDING == response) {
                System.out.println("Page blob copied pending");
            }
        });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#String-String
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#copyIncrementalWithResponse(String, String, RequestConditions)}
     */
    public void copyIncrementalWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#String-String-RequestConditions
        final String snapshot = "copy snapshot";
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfNoneMatch("snapshotMatch");

        client.copyIncrementalWithResponse(url, snapshot, modifiedRequestConditions)
            .subscribe(response -> {
                CopyStatusType statusType = response.getValue();

                if (CopyStatusType.SUCCESS == statusType) {
                    System.out.println("Page blob copied successfully");
                } else if (CopyStatusType.FAILED == statusType) {
                    System.out.println("Page blob copied failed");
                } else if (CopyStatusType.ABORTED == statusType) {
                    System.out.println("Page blob copied aborted");
                } else if (CopyStatusType.PENDING == statusType) {
                    System.out.println("Page blob copied pending");
                }
            });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#String-String-RequestConditions
    }

}
