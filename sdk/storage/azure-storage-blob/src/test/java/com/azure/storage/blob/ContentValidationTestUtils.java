// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageEncoder;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageFlags;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Helpers for inspecting the content-validation headers on recorded upload requests.
 * <p>
 * Upload requests are selected by method/URL (not by "already has a validation header"), so an upload request that
 * is missing content-validation headers is detected instead of being silently ignored. This guards against a single
 * validated block hiding other, unvalidated blocks in the same transfer.
 */
public final class ContentValidationTestUtils {
    private ContentValidationTestUtils() {
    }

    /**
     * Immutable snapshot of a single outgoing request captured at send time (after content-validation encoding ran).
     */
    public static final class RecordedRequest {
        private final HttpMethod method;
        private final String url;
        private final HttpHeaders headers;

        public RecordedRequest(HttpMethod method, String url, HttpHeaders headers) {
            this.method = method;
            this.url = url;
            this.headers = headers;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }
    }

    private static long parseContentLength(HttpHeaders headers) {
        String value = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Identifies a request that actually carries blob content and therefore must be content-validated: a PUT that is
     * a Put Blob, Put Block, Append Block, or (updating) Put Page, and that has a non-empty body. Create-blob and
     * clear-page requests (no body) are intentionally excluded.
     */
    private static boolean isContentBearingUploadRequest(RecordedRequest request) {
        if (request.getMethod() != HttpMethod.PUT) {
            return false;
        }
        String url = request.getUrl();
        boolean isUploadOperation = (url.contains("comp=block") && !url.contains("comp=blocklist"))
            || url.contains("comp=appendblock")
            || url.contains("comp=page")
            || (!url.contains("comp=")
                && request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-blob-type")) != null);
        return isUploadOperation && parseContentLength(request.getHeaders()) > 0;
    }

    public static List<RecordedRequest> contentBearingUploadRequests(List<RecordedRequest> recorded) {
        return recorded.stream()
            .filter(ContentValidationTestUtils::isContentBearingUploadRequest)
            .collect(Collectors.toList());
    }

    public static boolean isStructuredMessageRequest(HttpHeaders headers) {
        String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
        String contentCrc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
        String structuredLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
        if (!StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE.equals(bodyType)
            || contentCrc64 != null
            || structuredLength == null
            || structuredLength.trim().isEmpty()) {
            return false;
        }
        try {
            return Long.parseLong(structuredLength.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isCrc64HeaderRequest(HttpHeaders headers) {
        String contentCrc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
        String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
        String structuredLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
        return contentCrc64 != null && !contentCrc64.trim().isEmpty() && bodyType == null && structuredLength == null;
    }

    private static boolean hasNoContentValidation(HttpHeaders headers) {
        return headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME) == null
            && headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME) == null
            && headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME) == null;
    }

    /**
     * Returns true only if there is at least one content-bearing upload request AND every one of them used a
     * structured message. Upload requests are selected by method/URL so a missing-header upload fails the check.
     */
    public static boolean allUploadsUseStructuredMessage(List<RecordedRequest> recorded) {
        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        return !uploads.isEmpty() && uploads.stream().allMatch(r -> isStructuredMessageRequest(r.getHeaders()));
    }

    /**
     * Returns true only if there is at least one content-bearing upload request AND every one of them used the
     * CRC64 header (and no structured message).
     */
    public static boolean allUploadsUseCrc64Header(List<RecordedRequest> recorded) {
        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        return !uploads.isEmpty() && uploads.stream().allMatch(r -> isCrc64HeaderRequest(r.getHeaders()));
    }

    /**
     * Returns true only if there is at least one content-bearing upload request AND none of them carried any
     * content-validation header.
     */
    public static boolean noUploadUsesContentValidation(List<RecordedRequest> recorded) {
        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        return !uploads.isEmpty() && uploads.stream().allMatch(r -> hasNoContentValidation(r.getHeaders()));
    }

    /**
     * True if a Put Block List (commit) request was issued — used to assert that a failed/cancelled multipart upload
     * does NOT commit incomplete data.
     */
    public static boolean hasCommitBlockListRequest(List<RecordedRequest> recorded) {
        return recorded.stream().anyMatch(r -> r.getUrl().contains("comp=blocklist"));
    }

    /**
     * Expected Base64 of the little-endian 8-byte Storage CRC64 of {@code data}, matching what the production
     * encoding policy writes into the {@code x-ms-content-crc64} header.
     */
    public static String expectedCrc64Base64(byte[] data) {
        long crc64 = StorageCrc64Calculator.compute(data, 0);
        byte[] crc64Bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            crc64Bytes[i] = (byte) (crc64 >>> (i * 8));
        }
        return Base64.getEncoder().encodeToString(crc64Bytes);
    }

    public static long expectedStructuredMessageEncodedLength(int unencodedContentBytes) {
        return new StructuredMessageEncoder(unencodedContentBytes,
            StructuredMessageConstants.V1_DEFAULT_SEGMENT_CONTENT_LENGTH, StructuredMessageFlags.STORAGE_CRC64)
                .getEncodedMessageLength();
    }

    public static void assertCrc64HeaderMatches(HttpHeaders headers, byte[] expectedContent) {
        assertEquals(expectedCrc64Base64(expectedContent),
            headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME),
            "x-ms-content-crc64 must equal the CRC64 of the uploaded bytes");
    }

    public static void assertStructuredMessageLengths(HttpHeaders headers, int unencodedContentBytes) {
        assertEquals(String.valueOf(unencodedContentBytes),
            headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME),
            "x-ms-structured-content-length must equal the unencoded block length");
        assertEquals(String.valueOf(expectedStructuredMessageEncodedLength(unencodedContentBytes)),
            headers.getValue(HttpHeaderName.CONTENT_LENGTH),
            "Content-Length must equal the encoded structured-message length");
    }

    /**
     * Identifies the distinct block a content-bearing upload belongs to: the Put Block {@code blockid} query
     * parameter when present, otherwise the full URL. Grouping by this key makes block counts retry-independent (a
     * retried Put Block reuses the same block id), unlike counting raw requests.
     */
    public static String uploadBlockKey(RecordedRequest request) {
        String url = request.getUrl();
        int idx = url.indexOf("blockid=");
        if (idx < 0) {
            return url;
        }
        int start = idx + "blockid=".length();
        int end = url.indexOf('&', start);
        return end < 0 ? url.substring(start) : url.substring(start, end);
    }

    /**
     * Groups content-bearing uploads by distinct block (see {@link #uploadBlockKey}) and returns each block's
     * unencoded structured-message content length. Retry-independent: a retried block collapses to one entry.
     */
    public static java.util.Map<String, Long> uploadBlockUnencodedLengths(List<RecordedRequest> recorded) {
        java.util.Map<String, Long> blocks = new java.util.LinkedHashMap<>();
        for (RecordedRequest upload : contentBearingUploadRequests(recorded)) {
            String length
                = upload.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            if (length != null) {
                blocks.put(uploadBlockKey(upload), Long.parseLong(length.trim()));
            }
        }
        return blocks;
    }

    /**
     * True if the request is an upload attempt that already carries content-validation headers (a CRC64 header or a
     * structured-message body). Used by fault-injection policies to fail only the data-bearing upload calls.
     */
    public static boolean isUploadAttempt(HttpRequest request) {
        return request.getHeaders().getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME) != null
            || request.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME) != null;
    }

    /**
     * A synthetic storage error response (XML body with code {@code InjectedFailure}) for fault injection.
     */
    public static Mono<HttpResponse> injectedError(HttpRequest request, int status) {
        byte[] body = ("<?xml version=\"1.0\"?><Error><Code>InjectedFailure</Code>"
            + "<Message>Injected failure</Message></Error>").getBytes(StandardCharsets.UTF_8);
        // BlobStorageException.getErrorCode() is populated from the x-ms-error-code response header, not the body.
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml")
            .set(HttpHeaderName.fromString("x-ms-error-code"), "InjectedFailure");
        return Mono.just(new MockHttpResponse(request, status, headers, body));
    }

    /**
     * Terminal mock that answers any request with a 201 carrying the minimal headers the append/page/block response
     * deserializers expect. Lets request-shape tests observe outgoing headers without a live service.
     */
    public static Mono<HttpResponse> terminalSuccess(HttpRequest request) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8DMOCKETAG\"")
            .set(HttpHeaderName.fromString("x-ms-request-server-encrypted"), "true")
            .set(HttpHeaderName.fromString("x-ms-blob-append-offset"), "0")
            .set(HttpHeaderName.fromString("x-ms-blob-committed-block-count"), "1");
        return Mono.just(new MockHttpResponse(request, 201, headers));
    }

    /**
     * Raw 16-byte MD5 digest of {@code data} (the form the {@code setContentMd5} options expect).
     */
    public static byte[] md5(byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
