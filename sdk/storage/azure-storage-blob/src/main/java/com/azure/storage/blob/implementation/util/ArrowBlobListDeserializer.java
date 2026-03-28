// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobItemPropertiesInternal;
import com.azure.storage.blob.implementation.models.BlobName;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.RehydratePriority;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.TimeStampSecVector;
import org.apache.arrow.vector.UInt8Vector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.complex.MapVector;
import org.apache.arrow.vector.complex.impl.UnionMapReader;
import org.apache.arrow.vector.ipc.ArrowStreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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

        /**
         * Creates an ArrowListBlobsResult.
         *
         * @param blobItems the deserialized blob items
         * @param nextMarker the continuation token for the next page, or null if this is the last page
         */
        public ArrowListBlobsResult(List<BlobItemInternal> blobItems, String nextMarker) {
            this.blobItems = blobItems;
            this.nextMarker = nextMarker;
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
    }

    private ArrowBlobListDeserializer() {
    }

    /**
     * Deserializes an Arrow IPC stream into blob items and pagination metadata.
     *
     * @param arrowStream the Arrow IPC input stream from the service response
     * @return the deserialized result containing blob items and next marker
     * @throws RuntimeException if deserialization fails
     */
    public static ArrowListBlobsResult deserialize(InputStream arrowStream) {
        List<BlobItemInternal> results = new ArrayList<>();
        String nextMarker = null;

        try (BufferAllocator allocator = new RootAllocator();
            ArrowStreamReader reader = new ArrowStreamReader(arrowStream, allocator)) {

            VectorSchemaRoot root = reader.getVectorSchemaRoot();

            // Extract pagination metadata from schema
            Map<String, String> schemaMetadata = root.getSchema().getCustomMetadata();
            if (schemaMetadata != null) {
                nextMarker = schemaMetadata.get("NextMarker");
                if (nextMarker != null && nextMarker.isEmpty()) {
                    nextMarker = null;
                }
            }

            // Read all batches
            while (reader.loadNextBatch()) {
                int rowCount = root.getRowCount();
                for (int i = 0; i < rowCount; i++) {
                    results.add(readRow(root, i));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize Arrow IPC response", e);
        }

        return new ArrowListBlobsResult(results, nextMarker);
    }

    private static BlobItemInternal readRow(VectorSchemaRoot root, int index) {
        BlobItemInternal item = new BlobItemInternal();

        // Name
        String name = getVarChar(root, "Name", index);
        if (name != null) {
            item.setName(new BlobName().setContent(name));
        }

        // ResourceType — hierarchy listings use "blobprefix" for virtual directory rows
        String resourceType = getVarChar(root, "ResourceType", index);
        if ("blobprefix".equals(resourceType)) {
            item.setIsPrefix(true);
            return item;
        }

        BlobItemPropertiesInternal properties = new BlobItemPropertiesInternal();

        // Deleted
        Boolean deleted = getBit(root, "Deleted", index);
        if (deleted != null) {
            item.setDeleted(deleted);
        }

        // Snapshot
        item.setSnapshot(getVarChar(root, "Snapshot", index));

        // VersionId
        item.setVersionId(getVarChar(root, "VersionId", index));

        // IsCurrentVersion
        item.setIsCurrentVersion(getBit(root, "IsCurrentVersion", index));

        // HasVersionsOnly
        item.setHasVersionsOnly(getBit(root, "HasVersionsOnly", index));

        // Metadata
        Map<String, String> metadata = getMap(root, "Metadata", index);
        if (metadata != null) {
            item.setMetadata(metadata);
        }

        // OrMetadata
        Map<String, String> orMetadata = getMap(root, "OrMetadata", index);
        if (orMetadata != null) {
            item.setObjectReplicationMetadata(orMetadata);
        }

        // --- Properties ---

        properties.setCreationTime(getTimestamp(root, "Creation-Time", index));
        properties.setLastModified(getTimestamp(root, "Last-Modified", index));
        properties.setETag(getVarChar(root, "Etag", index));
        properties.setContentLength(getUInt64(root, "Content-Length", index));
        properties.setContentType(getVarChar(root, "Content-Type", index));
        properties.setContentEncoding(getVarChar(root, "Content-Encoding", index));
        properties.setContentLanguage(getVarChar(root, "Content-Language", index));
        properties.setContentDisposition(getVarChar(root, "Content-Disposition", index));
        properties.setCacheControl(getVarChar(root, "Cache-Control", index));

        // Content-MD5: service returns Base64 string, property expects byte[]
        String contentMd5 = getVarChar(root, "Content-MD5", index);
        if (contentMd5 != null) {
            properties.setContentMd5(Base64.getDecoder().decode(contentMd5));
        }

        // BlobType
        String blobType = getVarChar(root, "BlobType", index);
        if (blobType != null) {
            properties.setBlobType(BlobType.fromString(blobType));
        }

        // AccessTier
        String accessTier = getVarChar(root, "AccessTier", index);
        if (accessTier != null) {
            properties.setAccessTier(AccessTier.fromString(accessTier));
        }
        properties.setAccessTierInferred(getBit(root, "AccessTierInferred", index));
        properties.setAccessTierChangeTime(getTimestamp(root, "AccessTierChangeTime", index));

        // Lease
        String leaseStatus = getVarChar(root, "LeaseStatus", index);
        if (leaseStatus != null) {
            properties.setLeaseStatus(LeaseStatusType.fromString(leaseStatus));
        }
        String leaseState = getVarChar(root, "LeaseState", index);
        if (leaseState != null) {
            properties.setLeaseState(LeaseStateType.fromString(leaseState));
        }
        String leaseDuration = getVarChar(root, "LeaseDuration", index);
        if (leaseDuration != null) {
            properties.setLeaseDuration(LeaseDurationType.fromString(leaseDuration));
        }

        // Encryption
        properties.setServerEncrypted(getBit(root, "ServerEncrypted", index));
        properties.setCustomerProvidedKeySha256(getVarChar(root, "CustomerProvidedKeySha256", index));
        properties.setEncryptionScope(getVarChar(root, "EncryptionScope", index));
        properties.setIncrementalCopy(getBit(root, "IncrementalCopy", index));

        // Copy fields
        properties.setCopyId(getVarChar(root, "CopyId", index));
        String copyStatus = getVarChar(root, "CopyStatus", index);
        if (copyStatus != null) {
            properties.setCopyStatus(CopyStatusType.fromString(copyStatus));
        }
        properties.setCopySource(getVarChar(root, "CopySource", index));
        properties.setCopyProgress(getVarChar(root, "CopyProgress", index));
        properties.setCopyCompletionTime(getTimestamp(root, "CopyCompletionTime", index));
        properties.setCopyStatusDescription(getVarChar(root, "CopyStatusDescription", index));
        properties.setDestinationSnapshot(getVarChar(root, "CopyDestinationSnapshot", index));

        // Sequence number
        properties.setBlobSequenceNumber(getUInt64(root, "x-ms-blob-sequence-number", index));

        // Misc properties
        properties.setIsSealed(getBit(root, "Sealed", index));
        properties.setLegalHold(getBit(root, "LegalHold", index));
        properties.setDeletedTime(getTimestamp(root, "DeletedTime", index));
        properties.setLastAccessedOn(getTimestamp(root, "LastAccessTime", index));
        properties.setImmutabilityPolicyExpiresOn(getTimestamp(root, "ImmutabilityPolicyUntilDate", index));

        String immutabilityMode = getVarChar(root, "ImmutabilityPolicyMode", index);
        if (immutabilityMode != null) {
            properties.setImmutabilityPolicyMode(BlobImmutabilityPolicyMode.fromString(immutabilityMode));
        }

        String archiveStatus = getVarChar(root, "ArchiveStatus", index);
        if (archiveStatus != null) {
            properties.setArchiveStatus(ArchiveStatus.fromString(archiveStatus));
        }

        String rehydratePriority = getVarChar(root, "RehydratePriority", index);
        if (rehydratePriority != null) {
            properties.setRehydratePriority(RehydratePriority.fromString(rehydratePriority));
        }

        // TagCount and RemainingRetentionDays — service uses uint64 but property is Integer
        Long tagCount = getUInt64(root, "TagCount", index);
        if (tagCount != null) {
            properties.setTagCount(tagCount.intValue());
        }
        Long remainingRetentionDays = getUInt64(root, "RemainingRetentionDays", index);
        if (remainingRetentionDays != null) {
            properties.setRemainingRetentionDays(remainingRetentionDays.intValue());
        }

        item.setProperties(properties);
        return item;
    }

    // --- Safe vector read helpers ---
    // Each returns null if the column is absent from the schema or the value is null at the given row.

    private static String getVarChar(VectorSchemaRoot root, String name, int index) {
        FieldVector vec = root.getVector(name);
        if (vec == null || vec.isNull(index)) {
            return null;
        }
        return new String(((VarCharVector) vec).get(index), StandardCharsets.UTF_8);
    }

    private static Long getUInt64(VectorSchemaRoot root, String name, int index) {
        FieldVector vec = root.getVector(name);
        if (vec == null || vec.isNull(index)) {
            return null;
        }
        // Arrow may represent uint64 as BigIntVector (signed 64-bit) or UInt8Vector
        if (vec instanceof BigIntVector) {
            return ((BigIntVector) vec).get(index);
        } else if (vec instanceof UInt8Vector) {
            return ((UInt8Vector) vec).get(index);
        }
        return null;
    }

    private static Boolean getBit(VectorSchemaRoot root, String name, int index) {
        FieldVector vec = root.getVector(name);
        if (vec == null || vec.isNull(index)) {
            return null;
        }
        return ((BitVector) vec).get(index) == 1;
    }

    private static OffsetDateTime getTimestamp(VectorSchemaRoot root, String name, int index) {
        FieldVector vec = root.getVector(name);
        if (vec == null || vec.isNull(index)) {
            return null;
        }
        // Service returns Timestamp(SECOND, null) — epoch seconds without timezone, treat as UTC
        if (vec instanceof TimeStampSecVector) {
            long epochSeconds = ((TimeStampSecVector) vec).get(index);
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        }
        return null;
    }

    private static Map<String, String> getMap(VectorSchemaRoot root, String name, int index) {
        FieldVector vec = root.getVector(name);
        if (vec == null || vec.isNull(index)) {
            return null;
        }
        if (vec instanceof MapVector) {
            MapVector mapVec = (MapVector) vec;
            UnionMapReader reader = mapVec.getReader();
            reader.setPosition(index);
            Map<String, String> map = new HashMap<>();
            while (reader.next()) {
                String key = reader.key().readText().toString();
                String value = reader.value().readText().toString();
                map.put(key, value);
            }
            return map.isEmpty() ? null : map;
        }
        return null;
    }
}
