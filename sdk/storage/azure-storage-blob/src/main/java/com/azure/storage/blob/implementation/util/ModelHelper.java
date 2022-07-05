// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.implementation.accesshelpers.BlobDownloadHeadersConstructorProxy;
import com.azure.storage.blob.implementation.accesshelpers.BlobPropertiesConstructorProxy;
import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.azure.storage.blob.implementation.models.BlobName;
import com.azure.storage.blob.implementation.models.BlobPropertiesInternalDownload;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.implementation.models.BlobTags;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.models.FilterBlobItem;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationRule;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class provides helper methods for common model patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class ModelHelper {

    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);
    private static final Pattern UNDERSCORE = Pattern.compile("_");

    public static final Pattern FORWARD_SLASH = Pattern.compile("/");

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    public static final long BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 256L * Constants.MB;

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
            .setProgressReceiver(other.getProgressReceiver())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Transforms a blob type into a common type.
     * @param blobOptions {@link ParallelTransferOptions}
     * @return {@link com.azure.storage.common.ParallelTransferOptions}
     */
    public static com.azure.storage.common.ParallelTransferOptions wrapBlobOptions(
        ParallelTransferOptions blobOptions) {
        Long blockSize = blobOptions.getBlockSizeLong();
        Integer maxConcurrency = blobOptions.getMaxConcurrency();
        com.azure.storage.common.ProgressReceiver wrappedReceiver = blobOptions.getProgressListener() == null
            ? null
            : blobOptions.getProgressListener()::handleProgress;
        Long maxSingleUploadSize = blobOptions.getMaxSingleUploadSizeLong();

        return new com.azure.storage.common.ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(wrappedReceiver)
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }


    /**
     * Transforms a common type into a blob type.
     * @param commonProgressReceiver {@link com.azure.storage.common.ProgressReceiver}
     * @return {@link ProgressReceiver}
     */
    public static ProgressReceiver wrapCommonReceiver(
        com.azure.storage.common.ProgressReceiver commonProgressReceiver) {
        return commonProgressReceiver == null ? null : commonProgressReceiver::reportProgress;
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
        BlobItem blobItem = new BlobItem();
        blobItem.setName(toBlobNameString(blobItemInternal.getName()));
        blobItem.setDeleted(blobItemInternal.isDeleted());
        blobItem.setSnapshot(blobItemInternal.getSnapshot());
        blobItem.setProperties(populateBlobItemProperties(blobItemInternal.getProperties()));
        blobItem.setMetadata(blobItemInternal.getMetadata());
        blobItem.setVersionId(blobItemInternal.getVersionId());
        blobItem.setCurrentVersion(blobItemInternal.isCurrentVersion());
        blobItem.setIsPrefix(blobItemInternal.isPrefix());

        blobItem.setTags(tagMapFromBlobTags(blobItemInternal.getBlobTags()));

        blobItem.setObjectReplicationSourcePolicies(
            transformObjectReplicationMetadata(blobItemInternal.getObjectReplicationMetadata()));

        blobItem.setHasVersionsOnly(blobItemInternal.isHasVersionsOnly());

        return blobItem;
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

    private static Map<String, String> tagMapFromBlobTags(BlobTags blobTags) {
        if (blobTags == null || CoreUtils.isNullOrEmpty(blobTags.getBlobTagSet())) {
            return Collections.emptyMap();
        } else {
            Map<String, String> tags = new HashMap<>();
            for (BlobTag tag : blobTags.getBlobTagSet()) {
                tags.put(tag.getKey(), tag.getValue());
            }
            return tags;
        }
    }

    /**
     * Transforms {@link BlobItemPropertiesInternal} into a public {@link BlobItemProperties}.
     *
     * @param blobItemPropertiesInternal {@link BlobItemPropertiesInternal}
     * @return {@link BlobItemProperties}
     */
    public static BlobItemProperties populateBlobItemProperties(BlobItemPropertiesInternal blobItemPropertiesInternal) {
        BlobItemProperties blobItemProperties = new BlobItemProperties();
        blobItemProperties.setCreationTime(blobItemPropertiesInternal.getCreationTime());
        blobItemProperties.setLastModified(blobItemPropertiesInternal.getLastModified());
        blobItemProperties.setETag(blobItemPropertiesInternal.getETag());
        blobItemProperties.setContentLength(blobItemPropertiesInternal.getContentLength());
        blobItemProperties.setContentType(blobItemPropertiesInternal.getContentType());
        blobItemProperties.setContentEncoding(blobItemPropertiesInternal.getContentEncoding());
        blobItemProperties.setContentLanguage(blobItemPropertiesInternal.getContentLanguage());
        blobItemProperties.setContentMd5(blobItemPropertiesInternal.getContentMd5());
        blobItemProperties.setContentDisposition(blobItemPropertiesInternal.getContentDisposition());
        blobItemProperties.setCacheControl(blobItemPropertiesInternal.getCacheControl());
        blobItemProperties.setBlobSequenceNumber(blobItemPropertiesInternal.getBlobSequenceNumber());
        blobItemProperties.setBlobType(blobItemPropertiesInternal.getBlobType());
        blobItemProperties.setLeaseStatus(blobItemPropertiesInternal.getLeaseStatus());
        blobItemProperties.setLeaseState(blobItemPropertiesInternal.getLeaseState());
        blobItemProperties.setLeaseDuration(blobItemPropertiesInternal.getLeaseDuration());
        blobItemProperties.setCopyId(blobItemPropertiesInternal.getCopyId());
        blobItemProperties.setCopyStatus(blobItemPropertiesInternal.getCopyStatus());
        blobItemProperties.setCopySource(blobItemPropertiesInternal.getCopySource());
        blobItemProperties.setCopyProgress(blobItemPropertiesInternal.getCopyProgress());
        blobItemProperties.setCopyCompletionTime(blobItemPropertiesInternal.getCopyCompletionTime());
        blobItemProperties.setCopyStatusDescription(blobItemPropertiesInternal.getCopyStatusDescription());
        blobItemProperties.setServerEncrypted(blobItemPropertiesInternal.isServerEncrypted());
        blobItemProperties.setIncrementalCopy(blobItemPropertiesInternal.isIncrementalCopy());
        blobItemProperties.setDestinationSnapshot(blobItemPropertiesInternal.getDestinationSnapshot());
        blobItemProperties.setDeletedTime(blobItemPropertiesInternal.getDeletedTime());
        blobItemProperties.setRemainingRetentionDays(blobItemPropertiesInternal.getRemainingRetentionDays());
        blobItemProperties.setAccessTier(blobItemPropertiesInternal.getAccessTier());
        blobItemProperties.setAccessTierInferred(blobItemPropertiesInternal.isAccessTierInferred());
        blobItemProperties.setArchiveStatus(blobItemPropertiesInternal.getArchiveStatus());
        blobItemProperties.setCustomerProvidedKeySha256(blobItemPropertiesInternal.getCustomerProvidedKeySha256());
        blobItemProperties.setEncryptionScope(blobItemPropertiesInternal.getEncryptionScope());
        blobItemProperties.setAccessTierChangeTime(blobItemPropertiesInternal.getAccessTierChangeTime());
        blobItemProperties.setTagCount(blobItemPropertiesInternal.getTagCount());
        blobItemProperties.setRehydratePriority(blobItemPropertiesInternal.getRehydratePriority());
        blobItemProperties.setSealed(blobItemPropertiesInternal.isSealed());
        blobItemProperties.setLastAccessedTime(blobItemPropertiesInternal.getLastAccessedOn());
        blobItemProperties.setExpiryTime(blobItemPropertiesInternal.getExpiresOn());
        blobItemProperties.setImmutabilityPolicy(new BlobImmutabilityPolicy()
            .setExpiryTime(blobItemPropertiesInternal.getImmutabilityPolicyExpiresOn())
            .setPolicyMode(blobItemPropertiesInternal.getImmutabilityPolicyMode()));
        blobItemProperties.setHasLegalHold(blobItemPropertiesInternal.isLegalHold());

        return blobItemProperties;
    }

    private static List<ObjectReplicationPolicy> transformObjectReplicationMetadata(
        Map<String, String> objectReplicationMetadata) {

        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        objectReplicationMetadata = objectReplicationMetadata == null ? new HashMap<>() : objectReplicationMetadata;
        for (Map.Entry<String, String> entry : objectReplicationMetadata.entrySet()) {
            String orString = entry.getKey();
            String str = orString.startsWith("or-") ? orString.substring(3) : orString;
            String[] split = UNDERSCORE.split(str, 2);
            String policyId = split[0];
            String ruleId = split[1];
            ObjectReplicationRule rule = new ObjectReplicationRule(ruleId,
                ObjectReplicationStatus.fromString(entry.getValue()));
            if (!internalSourcePolicies.containsKey(policyId)) {
                internalSourcePolicies.put(policyId, new ArrayList<>());
            }
            internalSourcePolicies.get(policyId).add(rule);
        }

        if (internalSourcePolicies.isEmpty()) {
            return null;
        }
        List<ObjectReplicationPolicy> objectReplicationSourcePolicies = new ArrayList<>();
        for (Map.Entry<String, List<ObjectReplicationRule>> entry : internalSourcePolicies.entrySet()) {
            objectReplicationSourcePolicies.add(new ObjectReplicationPolicy(entry.getKey(), entry.getValue()));
        }
        return objectReplicationSourcePolicies;
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
    public static BlobBeginCopySourceRequestConditions populateBlobSourceRequestConditions(RequestConditions requestConditions) {
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
        objectReplicationStatus = objectReplicationStatus == null ? new HashMap<>() : objectReplicationStatus;
        return objectReplicationStatus.getOrDefault("policy-id", null);
    }

    public static List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies(
        Map<String, String> objectReplicationStatus) {
        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        objectReplicationStatus = objectReplicationStatus == null ? new HashMap<>() : objectReplicationStatus;
        if (getObjectReplicationDestinationPolicyId(objectReplicationStatus) == null) {
            for (Map.Entry<String, String> entry : objectReplicationStatus.entrySet()) {
                String[] split = UNDERSCORE.split(entry.getKey(), 2);
                String policyId = split[0];
                String ruleId = split[1];
                ObjectReplicationRule rule = new ObjectReplicationRule(ruleId,
                    ObjectReplicationStatus.fromString(entry.getValue()));
                if (!internalSourcePolicies.containsKey(policyId)) {
                    internalSourcePolicies.put(policyId, new ArrayList<>());
                }
                internalSourcePolicies.get(policyId).add(rule);
            }
        }
        List<ObjectReplicationPolicy> objectReplicationSourcePolicies = new ArrayList<>();
        for (Map.Entry<String, List<ObjectReplicationRule>> entry : internalSourcePolicies.entrySet()) {
            objectReplicationSourcePolicies.add(new ObjectReplicationPolicy(entry.getKey(), entry.getValue()));
        }
        return objectReplicationSourcePolicies;
    }

    public static String getErrorCode(HttpHeaders headers) {
        return getHeaderValue(headers, "x-ms-error-code");
    }

    public static String getETag(HttpHeaders headers) {
        return getHeaderValue(headers, "ETag");
    }

    private static String getHeaderValue(HttpHeaders headers, String headerName) {
        if (headers == null) {
            return null;
        }
        return headers.getValue(headerName);
    }

    public static BlobsDownloadHeaders transformBlobDownloadHeaders(HttpHeaders headers) {
        return transformHeadersToClass(headers, BlobsDownloadHeaders.class);
    }

    public static BlobQueryHeaders transformQueryHeaders(HttpHeaders headers) {
        return transformHeadersToClass(headers, BlobQueryHeaders.class);
    }

    private static <T> T transformHeadersToClass(HttpHeaders headers, Class<T> clazz) {
        if (headers == null) {
            return null;
        }
        try {
            return SERIALIZER.deserialize(headers, clazz);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
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
            BlobPropertiesConstructorProxy.create(new BlobPropertiesInternalDownload(response.getDeserializedHeaders())));
    }

    public static long getBlobLength(BlobDownloadHeaders headers) {
        return headers.getContentRange() == null ? headers.getContentLength()
            : ChunkedDownloadUtils.extractTotalBlobLength(headers.getContentRange());
    }

    private ModelHelper() {
    }
}
