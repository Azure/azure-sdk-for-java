// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.ContentValidationTestUtils.RecordedRequest;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.AppendBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.options.PageBlobOutputStreamOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ValidatableContent;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.storage.blob.ContentValidationTestUtils.allUploadsUseStructuredMessage;
import static com.azure.storage.blob.ContentValidationTestUtils.contentBearingUploadRequests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the various upload and download option types implement {@link ValidatableContent} so that content
 * validation can be configured and inspected agnostically across operations.
 */
public class ValidatableContentTests {

    private static Stream<Arguments> validatableOptions() {
        BinaryData data = BinaryData.fromString("data");
        PageRange pageRange = new PageRange().setStart(0).setEnd(511);
        // Every options type that implements ValidatableContent must be represented here so the shared
        // content-validation contract is exercised for all of them, not just a subset.
        return Stream.of(Arguments.of("AppendBlobAppendBlockOptions", new AppendBlobAppendBlockOptions(data)),
            Arguments.of("AppendBlobOutputStreamOptions", new AppendBlobOutputStreamOptions()),
            Arguments.of("PageBlobUploadPagesOptions", new PageBlobUploadPagesOptions(pageRange, data)),
            Arguments.of("PageBlobOutputStreamOptions", new PageBlobOutputStreamOptions(pageRange)),
            Arguments.of("BlockBlobStageBlockOptions", new BlockBlobStageBlockOptions("blockId", data)),
            Arguments.of("BlockBlobSimpleUploadOptions", new BlockBlobSimpleUploadOptions(data)),
            Arguments.of("BlockBlobOutputStreamOptions", new BlockBlobOutputStreamOptions()),
            Arguments.of("BlockBlobSeekableByteChannelWriteOptions",
                new BlockBlobSeekableByteChannelWriteOptions(
                    BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)),
            Arguments.of("BlobParallelUploadOptions", new BlobParallelUploadOptions(data)),
            Arguments.of("BlobUploadFromFileOptions", new BlobUploadFromFileOptions("file.bin")),
            Arguments.of("BlobDownloadContentOptions", new BlobDownloadContentOptions()),
            Arguments.of("BlobDownloadStreamOptions", new BlobDownloadStreamOptions()),
            Arguments.of("BlobDownloadToFileOptions", new BlobDownloadToFileOptions("file.bin")),
            Arguments.of("BlobInputStreamOptions", new BlobInputStreamOptions()),
            Arguments.of("BlobSeekableByteChannelReadOptions", new BlobSeekableByteChannelReadOptions()));
    }

    /**
     * The number of options types exercised here must match the number of production options types that implement
     * {@link ValidatableContent}. If a new validatable options type is added without being covered here, this test
     * fails to force the coverage to stay complete.
     */
    @Test
    public void allValidatableOptionTypesAreCovered() {
        assertEquals(15, validatableOptions().count(),
            "Every options type implementing ValidatableContent must be represented in validatableOptions().");
    }

    @ParameterizedTest
    @MethodSource("validatableOptions")
    public void optionsImplementValidatableContent(String name, Object options) {
        ValidatableContent validatable = assertInstanceOf(ValidatableContent.class, options, name);

        // Defaults to null (no content validation configured).
        assertNull(validatable.getContentValidationAlgorithm(), name);

        // Fluent setter returns the same instance and the value round-trips through the interface.
        ValidatableContent returned = validatable.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        assertSame(validatable, returned, name);
        assertSame(ContentValidationAlgorithm.CRC64, validatable.getContentValidationAlgorithm(), name);

        // NONE and AUTO round-trip too, and clearing back to null is supported.
        assertSame(ContentValidationAlgorithm.NONE,
            returned.setContentValidationAlgorithm(ContentValidationAlgorithm.NONE).getContentValidationAlgorithm(),
            name);
        assertSame(ContentValidationAlgorithm.AUTO,
            returned.setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO).getContentValidationAlgorithm(),
            name);
        assertNull(returned.setContentValidationAlgorithm(null).getContentValidationAlgorithm(), name);
    }

    @Test
    public void configureAgnostically() {
        // A single helper can operate on any supported options type via the interface.
        AppendBlobAppendBlockOptions appendOptions = new AppendBlobAppendBlockOptions(BinaryData.fromString("data"));
        applyCrc64(appendOptions);
        assertSame(ContentValidationAlgorithm.CRC64, appendOptions.getContentValidationAlgorithm());
    }

    private static void applyCrc64(ValidatableContent content) {
        content.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
    }

    // ===========================================================================================
    // Options contract: null inputs and unknown-length payloads must be rejected before any request is issued.
    // ===========================================================================================

    private static final PageRange CONTRACT_PAGE_RANGE = new PageRange().setStart(0).setEnd(511);

    @Test
    public void nullDataIsRejected() {
        assertThrows(NullPointerException.class, () -> new AppendBlobAppendBlockOptions(null));
        assertThrows(NullPointerException.class, () -> new BlockBlobStageBlockOptions("blockId", null));
        assertThrows(NullPointerException.class, () -> new BlockBlobSimpleUploadOptions((BinaryData) null));
        assertThrows(NullPointerException.class, () -> new PageBlobUploadPagesOptions(CONTRACT_PAGE_RANGE, null));
    }

    @Test
    public void nullBlockIdIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new BlockBlobStageBlockOptions(null, BinaryData.fromString("data")));
    }

    @Test
    public void nullPageRangeIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new PageBlobUploadPagesOptions(null, BinaryData.fromString("data")));
    }

    @Test
    public void unknownLengthDataIsRejected() {
        // BinaryData.fromStream(stream) has no defined length; content validation requires a known length, so the
        // options constructor must reject it up front rather than sending an un-validatable request.
        assertThrows(NullPointerException.class, () -> new AppendBlobAppendBlockOptions(unknownLengthData()));
        assertThrows(NullPointerException.class, () -> new BlockBlobStageBlockOptions("blockId", unknownLengthData()));
        assertThrows(NullPointerException.class, () -> new BlockBlobSimpleUploadOptions(unknownLengthData()));
        assertThrows(NullPointerException.class,
            () -> new PageBlobUploadPagesOptions(CONTRACT_PAGE_RANGE, unknownLengthData()));
    }

    private static BinaryData unknownLengthData() {
        return BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
    }

    // ===========================================================================================
    // Request-helper guards: an upload request missing validation headers must fail the check rather than being
    // silently ignored, so one validated block cannot make an otherwise unvalidated transfer pass.
    // ===========================================================================================

    @Test
    public void helperRejectsTransferWhereOneBlockIsUnvalidated() {
        List<RecordedRequest> recorded = new ArrayList<>();
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        // A content-bearing upload request that carries NO validation headers (the regression this guards against).
        recorded.add(unvalidatedAppendRequest(Constants.MB));

        assertEquals(3, contentBearingUploadRequests(recorded).size());
        assertFalse(allUploadsUseStructuredMessage(recorded),
            "One unvalidated block must make the whole transfer fail the check");
    }

    @Test
    public void helperAcceptsTransferWhereEveryBlockIsValidated() {
        List<RecordedRequest> recorded = new ArrayList<>();
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(Constants.MB));
        assertTrue(allUploadsUseStructuredMessage(recorded));
    }

    @Test
    public void helperIgnoresNonContentBearingRequests() {
        List<RecordedRequest> recorded = new ArrayList<>();
        // Create-append-blob (no body) and commit-block-list requests must not be treated as upload data.
        recorded.add(new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b",
            new HttpHeaders().set(HttpHeaderName.fromString("x-ms-blob-type"), "AppendBlob")
                .set(HttpHeaderName.CONTENT_LENGTH, "0")));
        recorded.add(new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=blocklist",
            new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "40")));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        assertEquals(1, contentBearingUploadRequests(recorded).size());
        assertTrue(allUploadsUseStructuredMessage(recorded));
    }

    private static RecordedRequest structuredAppendRequest(int unencodedLength) {
        HttpHeaders headers = new HttpHeaders()
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE)
            .set(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(unencodedLength))
            .set(HttpHeaderName.CONTENT_LENGTH,
                String.valueOf(ContentValidationTestUtils.expectedStructuredMessageEncodedLength(unencodedLength)));
        return new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=appendblock", headers);
    }

    private static RecordedRequest unvalidatedAppendRequest(int length) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(length));
        return new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=appendblock", headers);
    }
}
