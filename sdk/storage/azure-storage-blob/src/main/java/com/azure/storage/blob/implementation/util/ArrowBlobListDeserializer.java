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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deserializes an Apache Arrow IPC stream from the ListBlobs response into a list of {@link BlobItemInternal} objects.
 */
public final class ArrowBlobListDeserializer {

    /**
     * The authoritative set of Arrow column names this deserializer knows how to read. It must mirror exactly the
     * field names requested by {@link #readRow(Batch, int)}; the {@code alltypes} golden fixture drift-guard test
     * asserts they stay in sync. Any column present in the response schema but absent from this set means the service
     * has emitted a field this SDK version does not understand, which {@link #validateKnownColumns(Batch)} rejects.
     */
    private static final Set<String> KNOWN_COLUMNS = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList("Name", "ResourceType", "Deleted", "Snapshot", "VersionId", "IsCurrentVersion", "HasVersionsOnly",
            "Metadata", "OrMetadata", "Tags", "Creation-Time", "Last-Modified", "Etag", "Content-Length",
            "Content-Type", "Content-Encoding", "Content-Language", "Content-Disposition", "Cache-Control",
            "Content-MD5", "BlobType", "AccessTier", "AccessTierInferred", "AccessTierChangeTime", "SmartAccessTier",
            "LeaseStatus", "LeaseState", "LeaseDuration", "ServerEncrypted", "CustomerProvidedKeySha256",
            "EncryptionScope", "IncrementalCopy", "CopyId", "CopyStatus", "CopySource", "CopyProgress",
            "CopyCompletionTime", "CopyStatusDescription", "CopyDestinationSnapshot", "x-ms-blob-sequence-number",
            "Sealed", "LegalHold", "DeletedTime", "LastAccessTime", "ImmutabilityPolicyUntilDate",
            "ImmutabilityPolicyMode", "ArchiveStatus", "RehydratePriority", "TagCount", "RemainingRetentionDays")));

    private ArrowBlobListDeserializer() {
    }

    /**
     * Exposes the authoritative set of columns this deserializer understands, for the drift-guard test that asserts it
     * stays in sync with the full-schema golden fixture. Package-private: not part of the public API.
     *
     * @return the immutable set of known column names
     */
    static Set<String> knownColumns() {
        return KNOWN_COLUMNS;
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
            validateKnownColumns(batch);
            int rowCount = batch.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                results.add(readRow(batch, i));
            }
        }

        return new ArrowListBlobsResult(results, nextMarker, numberOfRecords);
    }

    /**
     * Fails loudly if the batch schema contains any column this deserializer does not handle, so that a newly added
     * service field surfaces as an error instead of being silently dropped.
     *
     * @param batch the record batch to validate
     * @throws BlobListArrowParseException if the batch contains one or more columns absent from {@link #KNOWN_COLUMNS}
     */
    private static void validateKnownColumns(Batch batch) {
        List<String> unknownColumns = null;
        for (String columnName : batch.getColumnNames()) {
            if (!KNOWN_COLUMNS.contains(columnName)) {
                if (unknownColumns == null) {
                    unknownColumns = new ArrayList<>();
                }
                unknownColumns.add(columnName);
            }
        }

        if (unknownColumns != null) {
            Collections.sort(unknownColumns);
            throw new BlobListArrowParseException(
                "ListBlobs Arrow parse failure: response contains unhandled column(s) " + unknownColumns + ".");
        }
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

    private static <T extends Column> T getColumn(Batch batch, String name, int index, Class<T> type,
        String typeLabel) {
        Column column = batch.getColumn(name);
        if (column == null || column.isNull(index)) {
            return null;
        }
        if (!type.isInstance(column)) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + name + "' has unsupported "
                + typeLabel + " column type '" + column.getClass().getSimpleName() + "'.");
        }
        return type.cast(column);
    }

    // region Arrow helpers

    private static String getVarChar(Batch batch, String name, int index) {
        StringColumn column = getColumn(batch, name, index, StringColumn.class, "string");
        return column == null ? null : column.get(index);
    }

    private static Long getUInt64(Batch batch, String name, int index) {
        IntColumn column = getColumn(batch, name, index, IntColumn.class, "integer");
        return column == null ? null : column.get(index);
    }

    private static Boolean getBit(Batch batch, String name, int index) {
        BoolColumn column = getColumn(batch, name, index, BoolColumn.class, "boolean");
        return column == null ? null : column.get(index);
    }

    private static OffsetDateTime getTimestamp(Batch batch, String name, int index) {
        TimestampColumn column = getColumn(batch, name, index, TimestampColumn.class, "timestamp");
        if (column == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(column.getEpochSeconds(index)), ZoneOffset.UTC);
    }

    private static Map<String, String> getMap(Batch batch, String name, int index) {
        MapColumn column = getColumn(batch, name, index, MapColumn.class, "map");
        return column == null ? null : column.get(index);
    }

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
         * @param blobItems       the deserialized blob items
         * @param nextMarker      the continuation token for the next page, or null if this is the last page
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

    //endregion
}
