// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.implementation.ByteBufferOutputStream;
import com.azure.cosmos.implementation.Bytes;
import com.azure.cosmos.implementation.RMResources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class PartitionKeyInternalHelper {

    public static final String MinimumInclusiveEffectivePartitionKey = toHexEncodedBinaryString(PartitionKeyInternal.EmptyPartitionKey.components);
    public static final String MaximumExclusiveEffectivePartitionKey = toHexEncodedBinaryString(PartitionKeyInternal.InfinityPartitionKey.components);

    public static final Range<String> FullRange = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey,
        true,
        false);

    static final int MaxPartitionKeyBinarySize =
            (1 /*type marker */ +
                    9 /* hash value*/ +
                    1 /* type marker*/ + StringPartitionKeyComponent.MAX_STRING_BYTES_TO_APPEND +
                    1 /*trailing zero*/
            ) * 3;
    private static final Int128 MaxHashV2Value = new Int128(new byte[] {
            (byte) 0x3F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

    static byte[] uIntToBytes(UInt128 unit) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
        buffer.putLong(unit.low);
        buffer.putLong(unit.high);
        return buffer.array();
    }

    static long asUnsignedLong(int x) {
        return x & 0x00000000ffffffffL;
    }

    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    static String toHexEncodedBinaryString(IPartitionKeyComponent... components) {
        ByteBufferOutputStream stream = new ByteBufferOutputStream(MaxPartitionKeyBinarySize);
        for (IPartitionKeyComponent component: components) {
            component.writeForBinaryEncoding(stream);
        }

        return HexConvert.bytesToHex(stream.asByteBuffer());
    }

    static String toHexEncodedBinaryString(List<IPartitionKeyComponent> components) {
        ByteBufferOutputStream stream = new ByteBufferOutputStream(MaxPartitionKeyBinarySize);
        for (IPartitionKeyComponent component: components) {
            component.writeForBinaryEncoding(stream);
        }

        return HexConvert.bytesToHex(stream.asByteBuffer());
    }

    static public String getEffectivePartitionKeyForHashPartitioningV2(PartitionKeyInternal partitionKeyInternal) {
        try(ByteBufferOutputStream byteArrayBuffer = new ByteBufferOutputStream())  {
            for (int i = 0; i < partitionKeyInternal.components.size(); i++) {
                partitionKeyInternal.components.get(i).writeForHashingV2(byteArrayBuffer);
            }

            ByteBuffer byteBuffer = byteArrayBuffer.asByteBuffer();
            UInt128 hashAsUnit128 = MurmurHash3_128.hash128(byteBuffer.array(), byteBuffer.limit());

            byte[] hash = uIntToBytes(hashAsUnit128);
            Bytes.reverse(hash);

            // Reset 2 most significant bits, as max exclusive value is 'FF'.
            // Plus one more just in case.
            hash[0] &= 0x3F;

            return HexConvert.bytesToHex(hash);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String getEffectivePartitionKeyForHashPartitioning(PartitionKeyInternal partitionKeyInternal) {
        IPartitionKeyComponent[] truncatedComponents = new IPartitionKeyComponent[partitionKeyInternal.components.size()];

        for (int i = 0; i < truncatedComponents.length; i++) {
            truncatedComponents[i] = partitionKeyInternal.components.get(i).truncate();
        }

        double hash;
        try(ByteBufferOutputStream byteArrayBuffer = new ByteBufferOutputStream())  {
            for (int i = 0; i < truncatedComponents.length; i++) {
                truncatedComponents[i].writeForHashing(byteArrayBuffer);
            }

            ByteBuffer byteBuffer = byteArrayBuffer.asByteBuffer();
            int hashAsInt = MurmurHash3_32.hash(byteBuffer.array(), byteBuffer.limit(), 0);
            hash = (double) asUnsignedLong(hashAsInt);
        } catch (IOException e) {
           throw new IllegalArgumentException(e);
        }

        IPartitionKeyComponent[] partitionKeyComponents = new IPartitionKeyComponent[partitionKeyInternal.components.size() + 1];
        partitionKeyComponents[0] = new NumberPartitionKeyComponent(hash);
        for (int i = 0; i < truncatedComponents.length; i++) {
            partitionKeyComponents[i + 1] = truncatedComponents[i];
        }

        return toHexEncodedBinaryString(partitionKeyComponents);
    }

    public static String getEffectivePartitionKeyString(PartitionKeyInternal partitionKeyInternal, PartitionKeyDefinition partitionKeyDefinition) {
        return getEffectivePartitionKeyString(partitionKeyInternal, partitionKeyDefinition, true);
    }

    public static String getEffectivePartitionKeyString(PartitionKeyInternal partitionKeyInternal, PartitionKeyDefinition partitionKeyDefinition, boolean strict) {
        if (partitionKeyInternal.components == null) {
            throw new IllegalArgumentException(RMResources.TooFewPartitionKeyComponents);
        }

        if (partitionKeyInternal.equals(PartitionKeyInternal.EmptyPartitionKey)) {
            return MinimumInclusiveEffectivePartitionKey;
        }

        if (partitionKeyInternal.equals(PartitionKeyInternal.InfinityPartitionKey)) {
            return MaximumExclusiveEffectivePartitionKey;
        }

        if (partitionKeyInternal.components.size() < partitionKeyDefinition.getPaths().size()) {
            throw new IllegalArgumentException(RMResources.TooFewPartitionKeyComponents);
        }

        if (partitionKeyInternal.components.size() > partitionKeyDefinition.getPaths().size() && strict) {
            throw new IllegalArgumentException(RMResources.TooManyPartitionKeyComponents);
        }

        PartitionKind kind = partitionKeyDefinition.getKind();
        if (kind == null) {
            kind = PartitionKind.HASH;
        }

        switch (kind) {
            case HASH:
                if (ModelBridgeInternal.isV2(partitionKeyDefinition)) {
                    // V2
                    return getEffectivePartitionKeyForHashPartitioningV2(partitionKeyInternal);
                } else {
                    // V1
                    return getEffectivePartitionKeyForHashPartitioning(partitionKeyInternal);
                }

            default:
                return toHexEncodedBinaryString(partitionKeyInternal.components);
        }
    }

    static class HexConvert {
        final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

        public static String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        public static String bytesToHex(ByteBuffer byteBuffer) {
            char[] hexChars = new char[byteBuffer.limit() * 2];
            for (int j = 0; j < byteBuffer.limit(); j++) {
                int v = byteBuffer.array()[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }

            return new String(hexChars);
        }
    }
}
