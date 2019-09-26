// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PageBlobAccessConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link PageBlobAsyncClient}
 */
@SuppressWarnings("unused")
public class PageBlobAsyncClientJavaDocCodeSnippets {
    private PageBlobAsyncClient client = new SpecializedBlobClientBuilder().buildPageBlobAsyncClient();
    private Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
    private ByteBuffer[] bufferData = new ByteBuffer[]{
        ByteBuffer.wrap(new byte[]{1}),
        ByteBuffer.wrap(new byte[]{2})
    };
    private Flux<ByteBuffer> body = Flux.fromArray(bufferData);
    private long size = 1024;
    private String leaseId = "leaseId";
    private long sequenceNumber = 0;
    private URL url = new URL("https://sample.com");
    private long sourceOffset = 0;
    private String data = "data";
    private long offset = 0;

    /**
     * @throws MalformedURLException ignored
     */
    public PageBlobAsyncClientJavaDocCodeSnippets() throws MalformedURLException {
    }

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
     * Code snippets for {@link PageBlobAsyncClient#createWithResponse(long, Long, BlobHTTPHeaders, Metadata,
     * BlobAccessConditions)}
     */
    public void createWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHTTPHeaders-Metadata-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client
            .createWithResponse(size, sequenceNumber, headers, metadata, blobAccessConditions)
            .subscribe(response -> System.out.printf(
                "Created page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));

        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHTTPHeaders-Metadata-BlobAccessConditions
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
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesWithResponse(PageRange, Flux, PageBlobAccessConditions)}
     */
    public void uploadPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-PageBlobAccessConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.uploadPagesWithResponse(pageRange, body, pageBlobAccessConditions)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-PageBlobAccessConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesFromUrl(PageRange, URL, Long)}
     */
    public void uploadPagesFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURL#PageRange-URL-Long
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);

        client.uploadPagesFromUrl(pageRange, url, sourceOffset)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob from URL with sequence number %s%n", response.getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURL#PageRange-URL-Long
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#uploadPagesFromUrlWithResponse(PageRange, URL, Long, byte[], PageBlobAccessConditions, SourceModifiedAccessConditions)}
     */
    public void uploadPagesFromURLWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURLWithResponse#PageRange-URL-Long-byte-PageBlobAccessConditions-SourceModifiedAccessConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        byte[] sourceContentMD5 = new byte[512];
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));
        SourceModifiedAccessConditions sourceAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(OffsetDateTime.now());

        client.uploadPagesFromUrlWithResponse(pageRange, url, sourceOffset, sourceContentMD5, pageBlobAccessConditions,
                sourceAccessConditions)
            .subscribe(response -> System.out.printf(
                "Uploaded page blob from URL with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromURLWithResponse#PageRange-URL-Long-byte-PageBlobAccessConditions-SourceModifiedAccessConditions
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
     * Code snippets for {@link PageBlobAsyncClient#clearPagesWithResponse(PageRange, PageBlobAccessConditions)}
     */
    public void clearPagesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobAccessConditions
        PageRange pageRange = new PageRange()
            .setStart(0)
            .setEnd(511);
        PageBlobAccessConditions pageBlobAccessConditions = new PageBlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.clearPagesWithResponse(pageRange, pageBlobAccessConditions)
            .subscribe(response -> System.out.printf(
                "Cleared page blob with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobAccessConditions
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
     * Code snippets for {@link PageBlobAsyncClient#getPageRangesWithResponse(BlobRange, BlobAccessConditions)}
     */
    public void getPageRangesWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobAccessConditions
        BlobRange blobRange = new BlobRange(offset);
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.getPageRangesWithResponse(blobRange, blobAccessConditions)
            .subscribe(response -> {
                System.out.println("Valid Page Ranges are:");
                for (PageRange pageRange : response.getValue().getPageRange()) {
                    System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
                }
            });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobAccessConditions
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
     * BlobAccessConditions)}
     */
    public void getPageRangesDiffWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobAccessConditions
        BlobRange blobRange = new BlobRange(offset);
        final String prevSnapshot = "previous snapshot";
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.getPageRangesDiffWithResponse(blobRange, prevSnapshot, blobAccessConditions)
            .subscribe(response -> {
                System.out.println("Valid Page Ranges are:");
                for (PageRange pageRange : response.getValue().getPageRange()) {
                    System.out.printf("Start: %s, End: %s%n", pageRange.getStart(), pageRange.getEnd());
                }
            });
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobAccessConditions
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
     * Code snippets for {@link PageBlobAsyncClient#resizeWithResponse(long, BlobAccessConditions)}
     */
    public void resizeWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.resizeWithResponse(size, blobAccessConditions)
            .subscribe(response -> System.out.printf(
                "Page blob resized with sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobAccessConditions
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
     * BlobAccessConditions)}
     */
    public void updateSequenceNumberWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobAccessConditions
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.updateSequenceNumberWithResponse(SequenceNumberActionType.INCREMENT, size, blobAccessConditions)
            .subscribe(response -> System.out.printf(
                "Page blob updated to sequence number %s%n", response.getValue().getBlobSequenceNumber()));
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobAccessConditions
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#copyIncremental(URL, String)}
     */
    public void copyIncrementalCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#URL-String
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
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#URL-String
    }

    /**
     * Code snippets for {@link PageBlobAsyncClient#copyIncrementalWithResponse(URL, String, ModifiedAccessConditions)}
     */
    public void copyIncrementalWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#URL-String-ModifiedAccessConditions
        final String snapshot = "copy snapshot";
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfNoneMatch("snapshotMatch");

        client.copyIncrementalWithResponse(url, snapshot, modifiedAccessConditions)
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
        // END: com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#URL-String-ModifiedAccessConditions
    }

}
