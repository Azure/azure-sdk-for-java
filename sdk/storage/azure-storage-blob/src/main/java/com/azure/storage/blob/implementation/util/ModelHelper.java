// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.implementation.models.BlobDownloadHeaders;
import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationRule;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides helper methods for common model patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class ModelHelper {

    /**
     * Indicates the default size above which the upload will be broken into blocks and parallelized.
     */
    private static final long BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE = 256L * Constants.MB;

    /**
     * Determines whether or not the passed authority is IP style, that is, it is of the format {@code <host>:<port>}.
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
        com.azure.storage.common.ProgressReceiver wrappedReceiver = blobOptions.getProgressReceiver() == null
            ? null
            : blobOptions.getProgressReceiver()::reportProgress;
        Long maxSingleUploadSize = blobOptions.getMaxSingleUploadSizeLong();

        return new com.azure.storage.common.ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(wrappedReceiver)
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Transforms {@link BlobDownloadHeaders} into a public {@link com.azure.storage.blob.models.BlobDownloadHeaders}.
     *
     * @param internalHeaders {@link BlobDownloadHeaders}
     * @return {@link com.azure.storage.blob.models.BlobDownloadHeaders}
     */
    public static com.azure.storage.blob.models.BlobDownloadHeaders populateBlobDownloadHeaders(
        BlobDownloadHeaders internalHeaders) {
        /*
        We have these two types because we needed to update this interface in a way that could not be generated
        (getObjectReplicationSourcePolicies), so we switched to generating BlobDownloadHeaders into implementation and
        wrapping it. Because it's headers type, we couldn't change the name of the generated type.
         */
        com.azure.storage.blob.models.BlobDownloadHeaders headers =
            new com.azure.storage.blob.models.BlobDownloadHeaders();
        headers.setLastModified(internalHeaders.getLastModified());
        headers.setMetadata(internalHeaders.getMetadata());
        headers.setETag(internalHeaders.getETag());
        headers.setContentLength(internalHeaders.getContentLength());
        headers.setContentType(internalHeaders.getContentType());
        headers.setContentRange(internalHeaders.getContentRange());
        headers.setContentEncoding(internalHeaders.getContentEncoding());
        headers.setContentLanguage(internalHeaders.getContentLanguage());
        headers.setContentMd5(internalHeaders.getContentMd5());
        headers.setContentDisposition(internalHeaders.getContentDisposition());
        headers.setCacheControl(internalHeaders.getCacheControl());
        headers.setBlobSequenceNumber(internalHeaders.getBlobSequenceNumber());
        headers.setBlobType(internalHeaders.getBlobType());
        headers.setLeaseStatus(internalHeaders.getLeaseStatus());
        headers.setLeaseState(internalHeaders.getLeaseState());
        headers.setLeaseDuration(internalHeaders.getLeaseDuration());
        headers.setCopyId(internalHeaders.getCopyId());
        headers.setCopyStatus(internalHeaders.getCopyStatus());
        headers.setCopySource(internalHeaders.getCopySource());
        headers.setCopyProgress(internalHeaders.getCopyProgress());
        headers.setCopyCompletionTime(internalHeaders.getCopyCompletionTime());
        headers.setCopyStatusDescription(internalHeaders.getCopyStatusDescription());
        headers.setIsServerEncrypted(internalHeaders.isServerEncrypted());
        headers.setClientRequestId(internalHeaders.getClientRequestId());
        headers.setRequestId(internalHeaders.getRequestId());
        headers.setVersion(internalHeaders.getVersion());
        headers.setVersionId(internalHeaders.getVersionId());
        headers.setAcceptRanges(internalHeaders.getAcceptRanges());
        headers.setDateProperty(internalHeaders.getDateProperty());
        headers.setBlobCommittedBlockCount(internalHeaders.getBlobCommittedBlockCount());
        headers.setEncryptionKeySha256(internalHeaders.getEncryptionKeySha256());
        headers.setEncryptionScope(internalHeaders.getEncryptionScope());
        headers.setBlobContentMD5(internalHeaders.getBlobContentMD5());
        headers.setContentCrc64(internalHeaders.getContentCrc64());
        headers.setErrorCode(internalHeaders.getErrorCode());
        headers.setTagCount(internalHeaders.getTagCount());

        Map<String, String> objectReplicationStatus = internalHeaders.getObjectReplicationRules();
        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        objectReplicationStatus = objectReplicationStatus == null ? new HashMap<>() : objectReplicationStatus;
        headers.setObjectReplicationDestinationPolicyId(objectReplicationStatus.getOrDefault("policy-id", null));
        if (headers.getObjectReplicationDestinationPolicyId() == null) {
            for (Map.Entry<String, String> entry : objectReplicationStatus.entrySet()) {
                String[] split = entry.getKey().split("_");
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
        headers.setObjectReplicationSourcePolicies(objectReplicationSourcePolicies);
        headers.setSealed(internalHeaders.isSealed());

        return headers;
    }

    /**
     * Transforms {@link BlobItemInternal} into a public {@link BlobItem}.
     *
     * @param blobItemInternal {@link BlobItemInternal}
     * @return {@link BlobItem}
     */
    public static BlobItem populateBlobItem(BlobItemInternal blobItemInternal) {
        BlobItem blobItem = new BlobItem();
        blobItem.setName(blobItemInternal.getName());
        blobItem.setDeleted(blobItemInternal.isDeleted());
        blobItem.setSnapshot(blobItemInternal.getSnapshot());
        blobItem.setProperties(populateBlobItemProperties(blobItemInternal.getProperties()));
        blobItem.setMetadata(blobItemInternal.getMetadata());
        blobItem.setVersionId(blobItemInternal.getVersionId());
        blobItem.setCurrentVersion(blobItemInternal.isCurrentVersion());
        blobItem.setIsPrefix(blobItemInternal.isPrefix());

        Map<String, String> tags = new HashMap<>();
        if (blobItemInternal.getBlobTags() != null && blobItemInternal.getBlobTags().getBlobTagSet() != null) {
            for (BlobTag tag : blobItemInternal.getBlobTags().getBlobTagSet()) {
                tags.put(tag.getKey(), tag.getValue());
            }
        }
        blobItem.setTags(tags);

        blobItem.setObjectReplicationSourcePolicies(
            transformObjectReplicationMetadata(blobItemInternal.getObjectReplicationMetadata()));

        return blobItem;
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

        return blobItemProperties;
    }

    private static List<ObjectReplicationPolicy> transformObjectReplicationMetadata(
        Map<String, String> objectReplicationMetadata) {

        Map<String, List<ObjectReplicationRule>> internalSourcePolicies = new HashMap<>();
        objectReplicationMetadata = objectReplicationMetadata == null ? new HashMap<>() : objectReplicationMetadata;
        for (Map.Entry<String, String> entry : objectReplicationMetadata.entrySet()) {
            String orString = entry.getKey();
            String str = orString.startsWith("or-") ? orString.substring(3) : orString;
            String[] split = str.split("_");
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
}
