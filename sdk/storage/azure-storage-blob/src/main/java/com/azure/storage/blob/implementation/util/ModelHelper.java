// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadHeadersConstructorProxy;
import com.azure.storage.blob.implementation.accesshelpers.BlobItemConstructorProxy;
import com.azure.storage.blob.implementation.accesshelpers.BlobPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.accesshelpers.BlobQueryHeadersConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobName;
import com.azure.storage.blob.implementation.models.BlobPropertiesInternalDownload;
import com.azure.storage.blob.implementation.models.BlobStorageExceptionInternal;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.implementation.models.BlobTags;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.models.BlobsQueryHeaders;
import com.azure.storage.blob.implementation.models.FilterBlobItem;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.ListBlobContainersIncludeType;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationRule;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * This class provides helper methods for common model patterns.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    public static final long BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 256L * Constants.MB;

    private static final HttpHeaderName X_MS_ERROR_CODE = HttpHeaderName.fromString("x-ms-error-code");

    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Determines whether the passed authority is IP style, that is, it is of the format {@code <host>:<port>}.
     *
     * @param authority The authority of a URL.
     * @throws MalformedURLException If the authority is malformed.
     * @return Whether the authority is IP style.
     */
    public static boolean determineAuthorityIsIpStyle(String authority) throws MalformedURLException {
        return new URL("http://" +  authority).getPort() != -1;
    }

    /**
     * Fills in default values for a ParallelTransferOptions where no value has been set. This will construct a new
     * object for safety.
     *
     * @param other The options to fill in defaults.
     * @return An object with defaults filled in for null values in the original.
     */
    public static ParallelTransferOptions populateAndApplyDefaults(ParallelTransferOptions other) {
        other = other == null ? new ParallelTransferOptions() : other;

        Long blockSize = other.getBlockSizeLong();
        if (blockSize == null) {
            blockSize = (long) BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }

        Integer maxConcurrency = other.getMaxConcurrency();
        if (maxConcurrency == null) {
            maxConcurrency = BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
        }

        Long maxSingleUploadSize = other.getMaxSingleUploadSizeLong();
        if (maxSingleUploadSize == null) {
            maxSingleUploadSize = BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE;
        }

        return new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressListener(other.getProgressListener())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Fills in default values for a ParallelTransferOptions where no value has been set. This will construct a new
     * object for safety.
     *
     * @param other The options to fill in defaults.
     * @return An object with defaults filled in for null values in the original.
     */
    public static com.azure.storage.common.ParallelTransferOptions populateAndApplyDefaults(
        com.azure.storage.common.ParallelTransferOptions other) {
        other = other == null ? new com.azure.storage.common.ParallelTransferOptions() : other;

        Long blockSize = other.getBlockSizeLong();
        if (blockSize == null) {
            blockSize = (long) BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        }

        Integer maxConcurrency = other.getMaxConcurrency();
        if (maxConcurrency == null) {
            maxConcurrency = BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
        }

        Long maxSingleUploadSize = other.getMaxSingleUploadSizeLong();
        if (maxSingleUploadSize == null) {
            maxSingleUploadSize = BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE;
        }

        return new com.azure.storage.common.ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressListener(other.getProgressListener())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Transforms a blob type into a common type.
     * @param blobOptions {@link ParallelTransferOptions}
     * @return {@link com.azure.storage.common.ParallelTransferOptions}
     */
    public static com.azure.storage.common.ParallelTransferOptions wrapBlobOptions(
        ParallelTransferOptions blobOptions) {
        return new com.azure.storage.common.ParallelTransferOptions()
            .setBlockSizeLong(blobOptions.getBlockSizeLong())
            .setMaxConcurrency(blobOptions.getMaxConcurrency())
            .setProgressListener(blobOptions.getProgressListener())
            .setMaxSingleUploadSizeLong(blobOptions.getMaxSingleUploadSizeLong());
    }

    /**
     * Transforms {@link BlobsDownloadHeaders} into a public {@link BlobDownloadHeaders}.
     *
     * @param internalHeaders {@link BlobsDownloadHeaders}
     * @return {@link BlobDownloadHeaders}
     */
    public static BlobDownloadHeaders populateBlobDownloadHeaders(BlobsDownloadHeaders internalHeaders,
        String errorCode) {
        /*
        We have these two types because we needed to update this interface in a way that could not be generated
        (getObjectReplicationSourcePolicies), so we switched to generating BlobDownloadHeaders into implementation and
        wrapping it. Because it's headers type, we couldn't change the name of the generated type.
         */
        return BlobDownloadHeadersConstructorProxy.create(internalHeaders).setErrorCode(errorCode);
    }

    /**
     * Transforms {@link BlobItemInternal} into a public {@link BlobItem}.
     *
     * @param blobItemInternal {@link BlobItemInternal}
     * @return {@link BlobItem}
     */
    public static BlobItem populateBlobItem(BlobItemInternal blobItemInternal) {
        return BlobItemConstructorProxy.create(blobItemInternal);
    }

    public static String toBlobNameString(BlobName blobName) {
        return blobName.isEncoded() != null && blobName.isEncoded()
            ? Utility.urlDecode(blobName.getContent())
            : blobName.getContent();
    }

    public static TaggedBlobItem populateTaggedBlobItem(FilterBlobItem filterBlobItem) {
        return new TaggedBlobItem(filterBlobItem.getContainerName(), filterBlobItem.getName(),
            tagMapFromBlobTags(filterBlobItem.getTags()));
    }

    public static Map<String, String> tagMapFromBlobTags(BlobTags blobTags) {
        if (blobTags == null || CoreUtils.isNullOrEmpty(blobTags.getBlobTagSet())) {
            return Collections.emptyMap();
        } else {
            Map<String, String> tags = new HashMap<>((int) (blobTags.getBlobTagSet().size() / 0.75F));
            for (BlobTag tag : blobTags.getBlobTagSet()) {
                tags.put(tag.getKey(), tag.getValue());
            }
            return tags;
        }
    }

    public static BlobTags toBlobTags(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }

        if (tags.isEmpty()) {
            return new BlobTags().setBlobTagSet(new ArrayList<>());
        }

        List<BlobTag> blobTagSet = new ArrayList<>(tags.size());
        tags.forEach((key, value) -> blobTagSet.add(new BlobTag().setKey(key).setValue(value)));

        return new BlobTags().setBlobTagSet(blobTagSet);
    }

    public static List<ObjectReplicationPolicy> transformObjectReplicationMetadata(
        Map<String, String> objectReplicationMetadata) {
        if (CoreUtils.isNullOrEmpty(objectReplicationMetadata)) {
            return null;
        }

        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        for (Map.Entry<String, String> entry : objectReplicationMetadata.entrySet()) {
            String orString = entry.getKey();
            int startIndex = orString.startsWith("or-") ? 3 : 0;
            int index = orString.indexOf('_', startIndex);
            String policyId = orString.substring(startIndex, index);
            String ruleId = orString.substring(index + 1);
            ObjectReplicationRule rule = new ObjectReplicationRule(ruleId,
                ObjectReplicationStatus.fromString(entry.getValue()));
            if (!internalSourcePolicies.containsKey(policyId)) {
                internalSourcePolicies.put(policyId, new ArrayList<>());
            }
            internalSourcePolicies.get(policyId).add(rule);
        }

        List<ObjectReplicationPolicy> objectReplicationSourcePolicies = new ArrayList<>(internalSourcePolicies.size());
        for (Map.Entry<String, List<ObjectReplicationRule>> entry : internalSourcePolicies.entrySet()) {
            objectReplicationSourcePolicies.add(new ObjectReplicationPolicy(entry.getKey(), entry.getValue()));
        }
        return objectReplicationSourcePolicies;
    }

    public static Map<String, String> toObjectReplicationMetadata(List<ObjectReplicationPolicy> policies) {
        if (policies == null) {
            return null;
        }

        if (policies.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> objectReplicationMetadata = new HashMap<>((int) (policies.size() / 0.75F));
        policies.forEach(policy -> {
            for (ObjectReplicationRule rule : policy.getRules()) {
                String key = "or-" + policy.getPolicyId() + "_" + rule.getRuleId();
                objectReplicationMetadata.put(key, rule.getStatus().toString());
            }
        });

        return objectReplicationMetadata;
    }

    /**
     * Transforms {@link RequestConditions} into a {@link BlobLeaseRequestConditions}.
     *
     * @param requestConditions {@link RequestConditions}
     * @return {@link BlobLeaseRequestConditions}
     */
    public static BlobLeaseRequestConditions populateBlobLeaseRequestConditions(RequestConditions requestConditions) {
        if (requestConditions == null) {
            return null;
        }

        return new BlobLeaseRequestConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince())
            .setTagsConditions(null);
    }

    /**
     * Transforms {@link RequestConditions} into a {@link BlobBeginCopySourceRequestConditions}.
     *
     * @param requestConditions {@link RequestConditions}
     * @return {@link BlobBeginCopySourceRequestConditions}
     */
    public static BlobBeginCopySourceRequestConditions populateBlobSourceRequestConditions(
        RequestConditions requestConditions) {
        if (requestConditions == null) {
            return null;
        }

        return new BlobBeginCopySourceRequestConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince())
            .setTagsConditions(null);
    }

    /**
     * Transforms {@link RequestConditions} into a {@link PageBlobCopyIncrementalRequestConditions}.
     *
     * @param requestConditions {@link RequestConditions}
     * @return {@link PageBlobCopyIncrementalRequestConditions}
     */
    public static PageBlobCopyIncrementalRequestConditions populateBlobDestinationRequestConditions(
        RequestConditions requestConditions) {
        if (requestConditions == null) {
            return null;
        }

        return new PageBlobCopyIncrementalRequestConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince())
            .setTagsConditions(null);
    }

    public static String getObjectReplicationDestinationPolicyId(Map<String, String> objectReplicationStatus) {
        if (CoreUtils.isNullOrEmpty(objectReplicationStatus)) {
            return null;
        }

        return objectReplicationStatus.get("policy-id");
    }

    public static List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies(
        Map<String, String> objectReplicationStatus) {
        if (CoreUtils.isNullOrEmpty(objectReplicationStatus)) {
            return new ArrayList<>();
        }

        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        if (getObjectReplicationDestinationPolicyId(objectReplicationStatus) == null) {
            for (Map.Entry<String, String> entry : objectReplicationStatus.entrySet()) {
                String key = entry.getKey();
                int index = key.indexOf('_');
                String policyId = key.substring(0, index);
                String ruleId = key.substring(index + 1);
                ObjectReplicationRule rule = new ObjectReplicationRule(ruleId,
                    ObjectReplicationStatus.fromString(entry.getValue()));
                if (!internalSourcePolicies.containsKey(policyId)) {
                    internalSourcePolicies.put(policyId, new ArrayList<>());
                }
                internalSourcePolicies.get(policyId).add(rule);
            }
        }
        List<ObjectReplicationPolicy> objectReplicationSourcePolicies = new ArrayList<>(internalSourcePolicies.size());
        for (Map.Entry<String, List<ObjectReplicationRule>> entry : internalSourcePolicies.entrySet()) {
            objectReplicationSourcePolicies.add(new ObjectReplicationPolicy(entry.getKey(), entry.getValue()));
        }
        return objectReplicationSourcePolicies;
    }

    public static String getErrorCode(HttpHeaders headers) {
        return getHeaderValue(headers, X_MS_ERROR_CODE);
    }

    public static String getETag(HttpHeaders headers) {
        return getHeaderValue(headers, HttpHeaderName.ETAG);
    }

    private static String getHeaderValue(HttpHeaders headers, HttpHeaderName headerName) {
        if (headers == null) {
            return null;
        }
        return headers.getValue(headerName);
    }

    public static BlobsDownloadHeaders transformBlobDownloadHeaders(HttpHeaders headers) {
        return new BlobsDownloadHeaders(headers);
    }

    public static BlobQueryHeaders transformQueryHeaders(BlobsQueryHeaders headers, HttpHeaders rawHeaders) {
        return BlobQueryHeadersConstructorProxy.create(headers)
            .setErrorCode(ModelHelper.getErrorCode(rawHeaders));
    }

    public static void validateConditionsNotPresent(BlobRequestConditions requestConditions,
        EnumSet<BlobRequestConditionProperty> invalidConditions, String operationName, String parameterName) {
        if (requestConditions == null) {
            return;
        }
        List<String> invalidConditionsFound = null;

        for (BlobRequestConditionProperty condition : invalidConditions) {
            switch (condition) {
                case LEASE_ID:
                    if (requestConditions.getLeaseId() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.LEASE_ID.toString());
                    }
                    break;
                case TAGS_CONDITIONS:
                    if (requestConditions.getTagsConditions() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.TAGS_CONDITIONS.toString());
                    }
                    break;
                case IF_MODIFIED_SINCE:
                    if (requestConditions.getIfModifiedSince() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.IF_MODIFIED_SINCE.toString());
                    }
                    break;
                case IF_UNMODIFIED_SINCE:
                    if (requestConditions.getIfUnmodifiedSince() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.IF_UNMODIFIED_SINCE.toString());
                    }
                    break;
                case IF_MATCH:
                    if (requestConditions.getIfMatch() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.IF_MATCH.toString());
                    }
                    break;
                case IF_NONE_MATCH:
                    if (requestConditions.getIfNoneMatch() != null) {
                        invalidConditionsFound = invalidConditionsFound == null ? new ArrayList<>()
                            : invalidConditionsFound;
                        invalidConditionsFound.add(BlobRequestConditionProperty.IF_NONE_MATCH.toString());
                    }
                    break;
                default:
                    break;
            }
        }
        if (invalidConditionsFound != null && !invalidConditionsFound.isEmpty()) {
            String unsupported = String.join(", ", invalidConditionsFound);
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("%s does not support the %s request condition(s) for parameter '%s'.",
                    operationName, unsupported, parameterName)));
        }
    }

    public static Response<BlobProperties> buildBlobPropertiesResponse(BlobDownloadAsyncResponse response) {
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            BlobPropertiesConstructorProxy.create(
                new BlobPropertiesInternalDownload(response.getDeserializedHeaders())));
    }

    public static Response<BlobProperties> buildBlobPropertiesResponse(BlobDownloadResponse response) {
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            BlobPropertiesConstructorProxy.create(
                new BlobPropertiesInternalDownload(response.getDeserializedHeaders())));
    }

    public static long getBlobLength(BlobDownloadHeaders headers) {
        return headers.getContentRange() == null ? headers.getContentLength()
            : ChunkedDownloadUtils.extractTotalBlobLength(headers.getContentRange());
    }

    public static boolean checkBlobDoesNotExistStatusCode(Throwable t) {
        if (t instanceof BlobStorageException) {
            BlobStorageException s = (BlobStorageException) t;
            return s.getStatusCode() == 404
                && (s.getErrorCode() == BlobErrorCode.RESOURCE_NOT_FOUND
                || s.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND);
            /* HttpResponseException - file get properties is a head request so a body is not returned. Error
             conversion logic does not properly handle errors that don't return XML. */
        } else if (t instanceof HttpResponseException) {
            HttpResponseException h = (HttpResponseException) t;
            String errorCode = h.getResponse().getHeaderValue(X_MS_ERROR_CODE);
            return h.getResponse().getStatusCode() == 404
                && (BlobErrorCode.RESOURCE_NOT_FOUND.toString().equals(errorCode)
                || BlobErrorCode.BLOB_NOT_FOUND.toString().equals(errorCode));
        } else {
            return false;
        }
    }

    public static boolean checkContainerDoesNotExistStatusCode(Throwable t) {
        if (t instanceof BlobStorageException) {
            BlobStorageException s = (BlobStorageException) t;
            return s.getStatusCode() == 404
                && (s.getErrorCode() == BlobErrorCode.RESOURCE_NOT_FOUND
                || s.getErrorCode() == BlobErrorCode.CONTAINER_NOT_FOUND);
            /* HttpResponseException - file get properties is a head request so a body is not returned. Error
             conversion logic does not properly handle errors that don't return XML. */
        } else if (t instanceof HttpResponseException) {
            HttpResponseException h = (HttpResponseException) t;
            String errorCode = h.getResponse().getHeaderValue(X_MS_ERROR_CODE);
            return h.getResponse().getStatusCode() == 404
                && (BlobErrorCode.RESOURCE_NOT_FOUND.toString().equals(errorCode)
                || BlobErrorCode.CONTAINER_NOT_FOUND.toString().equals(errorCode));
        } else {
            return false;
        }
    }

    public static String tagsToString(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;  // Flag to track if it's the first element in the loop
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            try {
                if (!first) {
                    sb.append("&");  // Append the delimiter before the key-value pair, except for the first
                } else {
                    first = false;  // Update flag after processing the first entry
                }
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        }
        return sb.toString();
    }

    public static LongRunningOperationStatus mapStatusToLongRunningOperationStatus(CopyStatusType status) {
        LongRunningOperationStatus operationStatus;
        switch (status) {
            case SUCCESS:
                operationStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                operationStatus = LongRunningOperationStatus.FAILED;
                break;
            case ABORTED:
                operationStatus = LongRunningOperationStatus.USER_CANCELLED;
                break;
            case PENDING:
                operationStatus = LongRunningOperationStatus.IN_PROGRESS;
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "CopyStatusType is not supported. Status: " + status));
        }
        return operationStatus;
    }

    public static boolean validateNoETag(BlobRequestConditions modifiedRequestConditions) {
        if (modifiedRequestConditions == null) {
            return true;
        }
        return modifiedRequestConditions.getIfMatch() == null && modifiedRequestConditions.getIfNoneMatch() == null;
    }

    /**
     * Converts {@link BlobContainerListDetails} into list of {@link ListBlobContainersIncludeType}
     * that contains only options selected. If no option is selected then null is returned.
     *
     * @return a list of selected options converted into {@link ListBlobContainersIncludeType}, null if none
     * of options has been selected.
     */
    public static List<ListBlobContainersIncludeType> toIncludeTypes(BlobContainerListDetails blobContainerListDetails) {
        boolean hasDetails = blobContainerListDetails != null
            && (blobContainerListDetails.getRetrieveMetadata()
            || blobContainerListDetails.getRetrieveDeleted()
            || blobContainerListDetails.getRetrieveSystemContainers());
        if (hasDetails) {
            List<ListBlobContainersIncludeType> flags = new ArrayList<>(3);
            if (blobContainerListDetails.getRetrieveDeleted()) {
                flags.add(ListBlobContainersIncludeType.DELETED);
            }
            if (blobContainerListDetails.getRetrieveMetadata()) {
                flags.add(ListBlobContainersIncludeType.METADATA);
            }
            if (blobContainerListDetails.getRetrieveSystemContainers()) {
                flags.add(ListBlobContainersIncludeType.SYSTEM);
            }
            return flags;
        } else {
            return null;
        }
    }

    /**
     * Validates a {@link BlobRetentionPolicy} according to service specs for set properties.
     * @param retentionPolicy {@link BlobRetentionPolicy}
     * @param policyName The name of the variable for errors.
     */
    public static void validateRetentionPolicy(BlobRetentionPolicy retentionPolicy, String policyName) {
        if (retentionPolicy == null) {
            return;
        }
        if (retentionPolicy.isEnabled()) {
            StorageImplUtils.assertInBounds(policyName, retentionPolicy.getDays(), 1, 365);
        }
    }

    /**
     * Sets any null fields to "" since the service requires all Cors rules to be set if some are set.
     * @param originalRule {@link BlobCorsRule}
     * @return The validated {@link BlobCorsRule}
     */
    public static BlobCorsRule validatedCorsRule(BlobCorsRule originalRule) {
        if (originalRule == null) {
            return null;
        }
        BlobCorsRule validRule = new BlobCorsRule();
        validRule.setAllowedHeaders(StorageImplUtils.emptyIfNull(originalRule.getAllowedHeaders()));
        validRule.setAllowedMethods(StorageImplUtils.emptyIfNull(originalRule.getAllowedMethods()));
        validRule.setAllowedOrigins(StorageImplUtils.emptyIfNull(originalRule.getAllowedOrigins()));
        validRule.setExposedHeaders(StorageImplUtils.emptyIfNull(originalRule.getExposedHeaders()));
        validRule.setMaxAgeInSeconds(originalRule.getMaxAgeInSeconds());
        return validRule;
    }

    public static String pageRangeToString(PageRange pageRange) {
        if (pageRange.getStart() < 0 || pageRange.getEnd() <= 0) {
            throw new IllegalArgumentException("PageRange's start and end values must be greater than or equal to "
                + "0 if specified.");
        }
        if (pageRange.getStart() % PAGE_BYTES != 0) {
            throw new IllegalArgumentException("PageRange's start value must be a multiple of 512.");
        }
        if (pageRange.getEnd() % PAGE_BYTES != PAGE_BYTES - 1) {
            throw new IllegalArgumentException("PageRange's end value must be 1 less than a multiple of 512.");
        }
        if (pageRange.getEnd() <= pageRange.getStart()) {
            throw new IllegalArgumentException("PageRange's End value must be after the start.");
        }
        return "bytes=" + pageRange.getStart() + '-' + pageRange.getEnd();
    }

    public static List<BlobSignedIdentifier> truncateTimeForBlobSignedIdentifier(List<BlobSignedIdentifier> identifiers) {
        if (identifiers == null) {
            return null;
        }
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        for (BlobSignedIdentifier identifier : identifiers) {
            if (identifier.getAccessPolicy() != null && identifier.getAccessPolicy().getStartsOn() != null) {
                identifier.getAccessPolicy().setStartsOn(
                    identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS));
            }
            if (identifier.getAccessPolicy() != null && identifier.getAccessPolicy().getExpiresOn() != null) {
                identifier.getAccessPolicy().setExpiresOn(
                    identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS));
            }
        }
        return identifiers;
    }

    /**
     * Maps the internal exception to a public exception, if and only if {@code internal} is an instance of
     * {@link BlobStorageExceptionInternal} and it will be mapped to {@link BlobStorageException}.
     * <p>
     * The internal exception is required as the public exception was created using Object as the exception value. This
     * was incorrect and should have been a specific type that was XML deserializable. So, an internal exception was
     * added to handle this and we map that to the public exception, keeping the API the same.
     *
     * @param internal The internal exception.
     * @return The public exception.
     */
    public static Throwable mapToBlobStorageException(Throwable internal) {
        if (internal instanceof BlobStorageExceptionInternal) {
            BlobStorageExceptionInternal internalException = (BlobStorageExceptionInternal) internal;
            return new BlobStorageException(internalException.getMessage(), internalException.getResponse(),
                internalException.getValue());
        }

        return internal;
    }

    public static <T> Callable<T> wrapTimeoutServiceCallWithExceptionMapping(Supplier<T> serviceCall) {
        return () -> {
            try {
                return serviceCall.get();
            } catch (BlobStorageExceptionInternal internal) {
                throw (BlobStorageException) mapToBlobStorageException(internal);
            }
        };
    }

    public static <T> T wrapServiceCallWithExceptionMapping(Supplier<T> serviceCall) {
        try {
            return serviceCall.get();
        } catch (BlobStorageExceptionInternal internal) {
            throw (BlobStorageException) mapToBlobStorageException(internal);
        }
    }

    private ModelHelper() {
    }
}
