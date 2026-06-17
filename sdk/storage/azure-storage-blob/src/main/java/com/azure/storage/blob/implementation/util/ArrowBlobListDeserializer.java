// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import com.azure.storage.blob.implementation.models.BlobName;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.Batch;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.BoolColumn;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.Column;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.IntColumn;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.MapColumn;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.Parsed;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.StringColumn;
import com.azure.storage.blob.implementation.util.BlobListArrowStreamReader.TimestampColumn;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.RehydratePriority;

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Deserializes an Apache Arrow IPC stream from the ListBlobs response into a list of {@link BlobItemInternal} objects.
 */
public final class ArrowBlobListDeserializer {

    /**
     * Result of deserializing an Arrow ListBlobs response.
     */
    public static final class ArrowListBlobsResult {
        private final List<BlobItemInternal> blobItems;
        private final String nextMarker;
        private final Integer numberOfRecords;

        /**
         * Creates an ArrowListBlobsResult.
         *
         * @param blobItems the deserialized blob items
         * @param nextMarker the continuation token for the next page, or null if this is the last page
         * @param numberOfRecords the total number of records reported by the service, or null if not present
         */
        public ArrowListBlobsResult(List<BlobItemInternal> blobItems, String nextMarker, Integer numberOfRecords) {
            this.blobItems = blobItems;
            this.nextMarker = nextMarker;
            this.numberOfRecords = numberOfRecords;
        }

        /**
         * @return the deserialized blob items
         */
        public List<BlobItemInternal> getBlobItems() {
            return blobItems;
        }

        /**
         * @return the continuation token for the next page, or null if this is the last page
         */
        public String getNextMarker() {
            return nextMarker;
        }

        /**
         * @return the total number of records reported by the service, or null if not present
         */
        public Integer getNumberOfRecords() {
            return numberOfRecords;
        }
    }

    private ArrowBlobListDeserializer() {
    }

    /**
     * Deserializes an Arrow IPC stream into blob items and pagination metadata.
     *
     * @param arrowStream the Arrow IPC input stream from the service response
     * @return the deserialized result containing blob items and next marker
     * @throws BlobListArrowParseException if deserialization fails
     */
    public static ArrowListBlobsResult deserialize(InputStream arrowStream) {
        if (arrowStream == null) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: input stream is null.");
        }

        List<BlobItemInternal> results = new ArrayList<>();
        String nextMarker = null;
        Integer numberOfRecords = null;

        Parsed parsed = BlobListArrowStreamReader.read(arrowStream);

        Map<String, String> schemaMetadata = parsed.getSchemaMetadata();
        if (schemaMetadata != null) {
            nextMarker = schemaMetadata.get("NextMarker");
            if (nextMarker != null && nextMarker.isEmpty()) {
                nextMarker = null;
            }

            String numberOfRecordsStr = schemaMetadata.get("NumberOfRecords");
            if (numberOfRecordsStr != null && !numberOfRecordsStr.isEmpty()) {
                try {
                    numberOfRecords = Integer.parseInt(numberOfRecordsStr);
                } catch (NumberFormatException e) {
                    throw new BlobListArrowParseException(
                        "ListBlobs Arrow parse failure: schema metadata 'NumberOfRecords' isn't a valid integer.", e);
                }
            }
        }

        for (Batch batch : parsed.getBatches()) {
            int rowCount = batch.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                results.add(readRow(batch, i));
            }
        }

        return new ArrowListBlobsResult(results, nextMarker, numberOfRecords);
    }

    private static BlobItemInternal readRow(Batch batch, int index) {
        BlobItemInternal item = new BlobItemInternal();

        String name = getVarChar(batch, "Name", index);
        if (name != null) {
            item.setName(new BlobName().setContent(name));
        }

        String resourceType = getVarChar(batch, "ResourceType", index);
        if ("blobprefix".equals(resourceType)) {
            item.setIsPrefix(true);
            return item;
        }

        BlobItemPropertiesInternal properties = new BlobItemPropertiesInternal();

        Boolean deleted = getBit(batch, "Deleted", index);
        if (deleted != null) {
            item.setDeleted(deleted);
        }

        item.setSnapshot(getVarChar(batch, "Snapshot", index));
        item.setVersionId(getVarChar(batch, "VersionId", index));
        item.setIsCurrentVersion(getBit(batch, "IsCurrentVersion", index));
        item.setHasVersionsOnly(getBit(batch, "HasVersionsOnly", index));

        Map<String, String> metadata = getMap(batch, "Metadata", index);
        if (metadata != null) {
            item.setMetadata(metadata);
        }

        Map<String, String> orMetadata = getMap(batch, "OrMetadata", index);
        if (orMetadata != null) {
            item.setObjectReplicationMetadata(orMetadata);
        }

        Map<String, String> tags = getMap(batch, "Tags", index);
        if (tags != null) {
            item.setBlobTags(ModelHelper.toBlobTags(tags));
        }

        item.setMetadataEncrypted(getBit(batch, "Encrypted", index));

        properties.setCreationTime(getTimestamp(batch, "Creation-Time", index));
        properties.setLastModified(getTimestamp(batch, "Last-Modified", index));
        properties.setETag(getVarChar(batch, "Etag", index));
        properties.setContentLength(getUInt64(batch, "Content-Length", index));
        properties.setContentType(getVarChar(batch, "Content-Type", index));
        properties.setContentEncoding(getVarChar(batch, "Content-Encoding", index));
        properties.setContentLanguage(getVarChar(batch, "Content-Language", index));
        properties.setContentDisposition(getVarChar(batch, "Content-Disposition", index));
        properties.setCacheControl(getVarChar(batch, "Cache-Control", index));

        String contentMd5 = getVarChar(batch, "Content-MD5", index);
        if (contentMd5 != null) {
            properties.setContentMd5(Base64.getDecoder().decode(contentMd5));
        }

        String contentCrc64 = getVarChar(batch, "Content-CRC64", index);
        if (contentCrc64 != null) {
            properties.setContentCrc64(Base64.getDecoder().decode(contentCrc64));
        }

        String blobType = getVarChar(batch, "BlobType", index);
        if (blobType != null) {
            properties.setBlobType(BlobType.fromString(blobType));
        }

        String accessTier = getVarChar(batch, "AccessTier", index);
        if (accessTier != null) {
            properties.setAccessTier(AccessTier.fromString(accessTier));
        }
        properties.setAccessTierInferred(getBit(batch, "AccessTierInferred", index));
        properties.setAccessTierChangeTime(getTimestamp(batch, "AccessTierChangeTime", index));

        String smartAccessTier = getVarChar(batch, "SmartAccessTier", index);
        if (smartAccessTier != null) {
            properties.setSmartAccessTier(AccessTier.fromString(smartAccessTier));
        }

        String leaseStatus = getVarChar(batch, "LeaseStatus", index);
        if (leaseStatus != null) {
            properties.setLeaseStatus(LeaseStatusType.fromString(leaseStatus));
        }
        String leaseState = getVarChar(batch, "LeaseState", index);
        if (leaseState != null) {
            properties.setLeaseState(LeaseStateType.fromString(leaseState));
        }
        String leaseDuration = getVarChar(batch, "LeaseDuration", index);
        if (leaseDuration != null) {
            properties.setLeaseDuration(LeaseDurationType.fromString(leaseDuration));
        }

        properties.setServerEncrypted(getBit(batch, "ServerEncrypted", index));
        properties.setCustomerProvidedKeySha256(getVarChar(batch, "CustomerProvidedKeySha256", index));
        properties.setEncryptionScope(getVarChar(batch, "EncryptionScope", index));
        properties.setIncrementalCopy(getBit(batch, "IncrementalCopy", index));

        properties.setOrsPolicySourceBlob(getVarChar(batch, "OrsPolicySourceBlob", index));
        properties.setAffinityId(getVarChar(batch, "AffinityId", index));

        properties.setCopyId(getVarChar(batch, "CopyId", index));
        String copyStatus = getVarChar(batch, "CopyStatus", index);
        if (copyStatus != null) {
            properties.setCopyStatus(CopyStatusType.fromString(copyStatus));
        }
        properties.setCopySource(getVarChar(batch, "CopySource", index));
        properties.setCopyProgress(getVarChar(batch, "CopyProgress", index));
        properties.setCopyCompletionTime(getTimestamp(batch, "CopyCompletionTime", index));
        properties.setCopyStatusDescription(getVarChar(batch, "CopyStatusDescription", index));
        properties.setDestinationSnapshot(getVarChar(batch, "CopyDestinationSnapshot", index));

        properties.setBlobSequenceNumber(getUInt64(batch, "x-ms-blob-sequence-number", index));

        properties.setIsSealed(getBit(batch, "Sealed", index));
        properties.setLegalHold(getBit(batch, "LegalHold", index));
        properties.setDeletedTime(getTimestamp(batch, "DeletedTime", index));
        properties.setLastAccessedOn(getTimestamp(batch, "LastAccessTime", index));
        properties.setImmutabilityPolicyExpiresOn(getTimestamp(batch, "ImmutabilityPolicyUntilDate", index));

        String immutabilityMode = getVarChar(batch, "ImmutabilityPolicyMode", index);
        if (immutabilityMode != null) {
            properties.setImmutabilityPolicyMode(BlobImmutabilityPolicyMode.fromString(immutabilityMode));
        }

        String archiveStatus = getVarChar(batch, "ArchiveStatus", index);
        if (archiveStatus != null) {
            properties.setArchiveStatus(ArchiveStatus.fromString(archiveStatus));
        }

        String rehydratePriority = getVarChar(batch, "RehydratePriority", index);
        if (rehydratePriority != null) {
            properties.setRehydratePriority(RehydratePriority.fromString(rehydratePriority));
        }

        Long tagCount = getUInt64(batch, "TagCount", index);
        if (tagCount != null) {
            properties.setTagCount(tagCount.intValue());
        }
        Long remainingRetentionDays = getUInt64(batch, "RemainingRetentionDays", index);
        if (remainingRetentionDays != null) {
            properties.setRemainingRetentionDays(remainingRetentionDays.intValue());
        }

        item.setProperties(properties);
        return item;
    }

    private static String getVarChar(Batch batch, String name, int index) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!(column instanceof StringColumn)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name
                + "' has unsupported string column type '" + column.getClass().getSimpleName() + "'.");
        }
        return ((StringColumn) column).get(index);
    }

    private static Long getUInt64(Batch batch, String name, int index) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!(column instanceof IntColumn)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name
                + "' has unsupported integer column type '" + column.getClass().getSimpleName() + "'.");
        }
        return ((IntColumn) column).get(index);
    }

    private static Boolean getBit(Batch batch, String name, int index) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!(column instanceof BoolColumn)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name
                + "' has unsupported boolean column type '" + column.getClass().getSimpleName() + "'.");
        }
        return ((BoolColumn) column).get(index);
    }

    private static OffsetDateTime getTimestamp(Batch batch, String name, int index) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!(column instanceof TimestampColumn)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name
                + "' has unsupported timestamp column type '" + column.getClass().getSimpleName() + "'.");
        }
        long epochSeconds = ((TimestampColumn) column).getEpochSeconds(index);
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private static Map<String, String> getMap(Batch batch, String name, int index) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!(column instanceof MapColumn)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name
                + "' has unsupported map column type '" + column.getClass().getSimpleName() + "'.");
        }
        return ((MapColumn) column).get(index);
    }
}
