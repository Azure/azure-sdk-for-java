// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Used internally to represents a Resource ID in the Azure Cosmos DB database service.
 */
public class ResourceId {
    static final short Length = 20;
    static final short OFFER_ID_LENGTH = 3;
    static final short MAX_PATH_FRAGMENT = 8;

    private int database;
    private int documentCollection;
    private long storedProcedure;
    private long trigger;
    private long userDefinedFunction;
    private long conflict;
    private long document;
    private long partitionKeyRange;
    private int user;
    private long permission;
    private int attachment;
    private long offer;
    private int clientEncryptionKey;

    private ResourceId() {
        this.offer = 0;
        this.database = 0;
        this.documentCollection = 0;
        this.storedProcedure = 0;
        this.trigger = 0;
        this.userDefinedFunction = 0;
        this.document = 0;
        this.partitionKeyRange = 0;
        this.user = 0;
        this.conflict = 0;
        this.permission = 0;
        this.attachment = 0;
        this.clientEncryptionKey = 0;
    }

    public static ResourceId parse(String id) throws IllegalArgumentException {
        Pair<Boolean, ResourceId> pair = ResourceId.tryParse(id);

        if (!pair.getKey()) {
            throw new IllegalArgumentException(String.format(
                    "INVALID resource id %s", id));
        }
        return pair.getValue();
    }

    public static byte[] parse(ResourceType type, String id) {
        if (ResourceId.hasNonHierarchicalResourceId(type)) {
            return id.getBytes(StandardCharsets.UTF_8);
        }
        return ResourceId.parse(id).getValue();
    }

    private static boolean hasNonHierarchicalResourceId(ResourceType type) {
        switch (type) {
            case MasterPartition:
            case ServerPartition:
            case RidRange:
                return true;
            default:
                return false;
        }
    }

    public static ResourceId newDatabaseId(int dbid) {
        ResourceId resourceId = new ResourceId();
        resourceId.database = dbid;
        return resourceId;
    }

    public static ResourceId newDocumentCollectionId(String databaseId, int collectionId) {
        ResourceId dbId = ResourceId.parse(databaseId);

        return newDocumentCollectionId(dbId.database, collectionId);
    }

    static ResourceId newDocumentCollectionId(int dbId, int collectionId) {
        ResourceId collectionResourceId = new ResourceId();
        collectionResourceId.database = dbId;
        collectionResourceId.documentCollection = collectionId;

        return collectionResourceId;
    }

    public static ResourceId newUserId(String databaseId, int userId) {
        ResourceId dbId = ResourceId.parse(databaseId);

        ResourceId userResourceId = new ResourceId();
        userResourceId.database = dbId.database;
        userResourceId.user = userId;

        return userResourceId;
    }

    public static ResourceId newPermissionId(String userId, long permissionId) {
        ResourceId usrId = ResourceId.parse(userId);

        ResourceId permissionResourceId = new ResourceId();
        permissionResourceId.database = usrId.database;
        permissionResourceId.user = usrId.user;
        permissionResourceId.permission = permissionId;
        return permissionResourceId;
    }

    public static ResourceId newAttachmentId(String documentId, int attachmentId) {
        ResourceId docId = ResourceId.parse(documentId);

        ResourceId attachmentResourceId = new ResourceId();
        attachmentResourceId.database = docId.database;
        attachmentResourceId.documentCollection = docId.documentCollection;
        attachmentResourceId.document = docId.document;
        attachmentResourceId.attachment = attachmentId;

        return attachmentResourceId;
    }

    public static Pair<Boolean, ResourceId> tryParse(String id) {
        ResourceId rid = null;

        try {
            if (StringUtils.isEmpty(id))
                return Pair.of(false, null);

            if (id.length() % 4 != 0) {
                // our ResourceId string is always padded
                return Pair.of(false, null);
            }

            byte[] buffer = null;

            Pair<Boolean, byte[]> pair = ResourceId.verify(id);

            if (!pair.getKey())
                return Pair.of(false, null);

            buffer = pair.getValue();

            if (buffer.length % 4 != 0 && buffer.length != ResourceId.OFFER_ID_LENGTH) {
                return Pair.of(false, null);
            }

            rid = new ResourceId();

            if (buffer.length == ResourceId.OFFER_ID_LENGTH) {
                rid.offer = 0;
                for (int index = 0; index < ResourceId.OFFER_ID_LENGTH; index++)
                {
                    rid.offer |= (long)(buffer[index] << (index * 8));
                }
                return Pair.of(true, rid);
            }

            if (buffer.length >= 4)
                rid.database = ByteBuffer.wrap(buffer).getInt();

            if (buffer.length >= 8) {
                byte[] temp = new byte[4];
                ResourceId.blockCopy(buffer, 4, temp, 0, 4);

                boolean isCollection = (temp[0] & (128)) > 0;

                if (isCollection) {
                    rid.documentCollection = ByteBuffer.wrap(temp).getInt();

                    if (buffer.length >= 16) {
                        byte[] subCollRes = new byte[8];
                        ResourceId.blockCopy(buffer, 8, subCollRes, 0, 8);

                        long subCollectionResource = ByteBuffer.wrap(buffer, 8, 8).getLong();
                        if ((subCollRes[7] >> 4) == CollectionChildResourceType.Document) {
                            rid.document = subCollectionResource;

                            if (buffer.length == 20) {
                                rid.attachment = ByteBuffer.wrap(buffer, 16, 4).getInt();
                            }
                        } else if (Math.abs(subCollRes[7] >> 4) == CollectionChildResourceType.StoredProcedure) {
                            rid.storedProcedure = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == CollectionChildResourceType.Trigger) {
                            rid.trigger = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == CollectionChildResourceType.UserDefinedFunction) {
                            rid.userDefinedFunction = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == CollectionChildResourceType.Conflict) {
                            rid.conflict = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == CollectionChildResourceType.PartitionKeyRange) {
                            rid.partitionKeyRange = subCollectionResource;
                        } else {
                            return Pair.of(false, rid);
                        }
                    } else if (buffer.length != 8) {
                        return Pair.of(false, rid);
                    }
                } else {
                    rid.user = ByteBuffer.wrap(temp).getInt();

                    if (buffer.length == 16) {
                        rid.permission = ByteBuffer.wrap(buffer, 8, 8).getLong();
                    } else if (buffer.length != 8) {
                        return Pair.of(false, rid);
                    }
                }
            }

            return Pair.of(true, rid);
        } catch (Exception e) {
            return Pair.of(false, null);
        }
    }

    public static Pair<Boolean, byte[]> verify(String id) {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("id");

        byte[] buffer = null;

        try {
            buffer = ResourceId.fromBase64String(id);
        } catch (Exception e) {
        }

        if (buffer == null || buffer.length > ResourceId.Length) {
            return Pair.of(false, null);
        }

        return Pair.of(true, buffer);
    }

    public static boolean verifyBool(String id) {
        return verify(id).getKey();
    }

    static byte[] fromBase64String(String s) {
        return Utils.Base64Decoder.decode(s.replace('-', '/'));
    }

    static String toBase64String(byte[] buffer) {
        return ResourceId.toBase64String(buffer, 0, buffer.length);
    }

    static String toBase64String(byte[] buffer, int offset, int length) {
        byte[] subBuffer = Arrays.copyOfRange(buffer, offset, length);

        return Utils.encodeBase64String(subBuffer).replace('/', '-');
    }

    // Copy the bytes provided with a for loop, faster when there are only a few
    // bytes to copy
    static void blockCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int count) {
        int stop = srcOffset + count;
        for (int i = srcOffset; i < stop; i++)
            dst[dstOffset++] = src[i];
    }

    private static byte[] convertToBytesUsingByteBuffer(int value) {
        ByteOrder order = ByteOrder.BIG_ENDIAN;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(order);
        return buffer.putInt(value).array();
    }

    private static byte[] convertToBytesUsingByteBuffer(long value) {
        ByteOrder order = ByteOrder.BIG_ENDIAN;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(order);
        return buffer.putLong(value).array();
    }

    public boolean isDatabaseId() {
        return this.getDatabase() != 0 && (this.getDocumentCollection() == 0 && this.getUser() == 0 && this.clientEncryptionKey == 0);
    }

    public int getDatabase() {
        return this.database;
    }

    public ResourceId getDatabaseId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        return rid;
    }

    public int getDocumentCollection() {
        return this.documentCollection;
    }

    public ResourceId getDocumentCollectionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        return rid;
    }

    /**
     * Unique (across all databases) Id for the DocumentCollection.
     * First 4 bytes are DatabaseId and next 4 bytes are CollectionId.
     *
     * @return the unique collectionId
     */
    public long getUniqueDocumentCollectionId() {
        return (long) this.database << 32 | this.documentCollection;
    }

    public long getStoredProcedure() {
        return this.storedProcedure;
    }

    public ResourceId getStoredProcedureId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.storedProcedure = this.storedProcedure;
        return rid;
    }

    public long getTrigger() {
        return this.trigger;
    }

    public ResourceId getTriggerId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.trigger = this.trigger;
        return rid;
    }

    public long getUserDefinedFunction() {
        return this.userDefinedFunction;
    }

    public ResourceId getUserDefinedFunctionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.userDefinedFunction = this.userDefinedFunction;
        return rid;
    }

    public int getClientEncryptionKey() {
        return this.clientEncryptionKey;
    }

    public ResourceId getClientEncryptionKeyId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.clientEncryptionKey = this.clientEncryptionKey;
        return rid;
    }

    public long getConflict() {
        return this.conflict;
    }

    public ResourceId getConflictId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.conflict = this.conflict;
        return rid;
    }

    /**
     * Returns the long value of the document. The value computed is in Big Endian, so this method reverses the bytes
     * and returns Little Endian order value of the long
     *
     * @return document long value
     */
    public long getDocument() {
        return Long.reverseBytes(this.document);
    }

    public ResourceId getDocumentId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.document = this.document;
        return rid;
    }

    public long getPartitionKeyRange() {
        return this.partitionKeyRange;
    }

    public ResourceId getPartitionKeyRangeId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.partitionKeyRange = this.partitionKeyRange;
        return rid;
    }

    public int getUser() {
        return this.user;
    }

    public ResourceId getUserId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.user = this.user;
        return rid;
    }

    public long getPermission() {
        return this.permission;
    }

    public ResourceId getPermissionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.user = this.user;
        rid.permission = this.permission;
        return rid;
    }

    public int getAttachment() {
        return this.attachment;
    }

    public ResourceId getAttachmentId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.document = this.document;
        rid.attachment = this.attachment;
        return rid;
    }

    public long getOffer() { return this.offer; }

    public ResourceId getOfferId() {
        ResourceId rid = new ResourceId();
        rid.offer = this.offer;
        return rid;
    }

    public byte[] getValue() {
        int len = 0;
        if (this.offer != 0)
            len += ResourceId.OFFER_ID_LENGTH;
        else if (this.database != 0)
            len += 4;
        if (this.documentCollection != 0 || this.user != 0 || this.clientEncryptionKey != 0 )
            len += 4;
        if (this.document != 0 || this.permission != 0
                || this.storedProcedure != 0 || this.trigger != 0
                || this.userDefinedFunction != 0 || this.conflict != 0
                || this.partitionKeyRange != 0 || this.clientEncryptionKey != 0)
            len += 8;
        if (this.attachment != 0)
            len += 4;

        byte[] val = new byte[len];

        if (this.offer != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.offer),
                    0, val, 0, ResourceId.OFFER_ID_LENGTH);
        else if (this.database != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.database),
                    0, val, 0, 4);

        if (this.documentCollection != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.documentCollection),
                    0, val, 4, 4);
        else if (this.user != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.user),
                    0, val, 4, 4);

        if (this.storedProcedure != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.storedProcedure),
                    0, val, 8, 8);
        else if (this.trigger != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.trigger),
                    0, val, 8, 8);
        else if (this.userDefinedFunction != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.userDefinedFunction),
                    0, val, 8, 8);
        else if (this.conflict != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.conflict),
                    0, val, 8, 8);
        else if (this.document != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.document),
                    0, val, 8, 8);
        else if (this.permission != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.permission),
                    0, val, 8, 8);
        else if (this.partitionKeyRange != 0)
            ResourceId.blockCopy(
                convertToBytesUsingByteBuffer(this.partitionKeyRange),
                0, val, 8, 8);
        else if (this.clientEncryptionKey != 0)
            ResourceId.blockCopy(
                convertToBytesUsingByteBuffer(this.clientEncryptionKey),
                0, val, 8, 4);

        if (this.attachment != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.attachment),
                    0, val, 16, 4);

        return val;
    }

    public String toString() {
        return ResourceId.toBase64String(this.getValue());
    }

    public boolean equals(ResourceId other) {
        if (other == null) {
            return false;
        }

        return Arrays.equals(this.getValue(), other.getValue());
    }

    @Override
    public boolean equals(Object object) {
        // When a class define covariant version of equals(Object) method, in this case
        // equals(ResourceId), it is necessary to define equals(Object) method explicitly.
        // EQ_SELF_USE_OBJECT
        //
        if (object == null) {
            return false;
        }
        if(this == object) {
            return true;
        }
        if(object instanceof ResourceId) {
            return this.equals((ResourceId) object);
        }
        return false;
    }

    public int hashCode() {
        // TODO: https://github.com/Azure/azure-sdk-for-java/issues/9046
        return super.hashCode();
    }

    // Using a byte however, we only need nibble here.
    private static class CollectionChildResourceType {
        public static final byte Document = 0x0;
        public static final byte StoredProcedure = 0x08;
        public static final byte Trigger = 0x07;
        public static final byte UserDefinedFunction = 0x06;
        public static final byte Conflict = 0x04;
        public static final byte PartitionKeyRange = 0x05;
    }
}
