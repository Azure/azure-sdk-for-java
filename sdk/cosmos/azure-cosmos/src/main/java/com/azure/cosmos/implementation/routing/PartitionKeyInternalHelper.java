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
    public static final byte[] MinimumInclusiveEffectivePartitionKeyBytes = toBinary(PartitionKeyInternal.EmptyPartitionKey.components);
    public static final String MaximumExclusiveEffectivePartitionKey = toHexEncodedBinaryString(PartitionKeyInternal.InfinityPartitionKey.components);
    public static final byte[] MaximumExclusiveEffectivePartitionKeyBytes = toBinary(PartitionKeyInternal.InfinityPartitionKey.components);

    public static final Range<String> FullRange = new Range<>(
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
    public static final Int128 MaxHashV2Value = new Int128(new byte[] {
        (byte) 0x3F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

    private static final int HASH_V2_EPK_LENGTH = 32;

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

    public static String toHexEncodedBinaryString(IPartitionKeyComponent... components) {
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

    static byte[] toBinary(List<IPartitionKeyComponent> components) {
        ByteBufferOutputStream stream = new ByteBufferOutputStream(MaxPartitionKeyBinarySize);
        for (IPartitionKeyComponent component: components) {
            component.writeForBinaryEncoding(stream);
        }

        return stream.toByteArray();
    }

    static byte[] toBinary(IPartitionKeyComponent[] components) {
        ByteBufferOutputStream stream = new ByteBufferOutputStream(MaxPartitionKeyBinarySize);
        for (IPartitionKeyComponent component: components) {
            component.writeForBinaryEncoding(stream);
        }

        return stream.toByteArray();
    }

    static public String getEffectivePartitionKeyForHashPartitioningV2(PartitionKeyInternal partitionKeyInternal) {
        return HexConvert.bytesToHex(getEffectivePartitionKeyBytesForHashPartitioningV2(partitionKeyInternal));
    }

    static public byte[] getEffectivePartitionKeyBytesForHashPartitioningV2(PartitionKeyInternal partitionKeyInternal) {
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

            return hash;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String getEffectivePartitionKeyForMultiHashPartitioning(PartitionKeyInternal partitionKeyInternal) {
        return HexConvert.bytesToHex(getEffectivePartitionKeyBytesForMultiHashPartitioning(partitionKeyInternal));
    }

    static byte[] getEffectivePartitionKeyBytesForMultiHashPartitioning(PartitionKeyInternal partitionKeyInternal) {
        byte[] finalHash = new byte[partitionKeyInternal.components.size() * 2 * Long.BYTES];
        for (int i = 0; i < partitionKeyInternal.components.size(); i++) {
            try(ByteBufferOutputStream byteArrayBuffer = new ByteBufferOutputStream())  {
                partitionKeyInternal.components.get(i).writeForHashingV2(byteArrayBuffer);

                ByteBuffer byteBuffer = byteArrayBuffer.asByteBuffer();
                UInt128 hashAsUnit128 = MurmurHash3_128.hash128(byteBuffer.array(), byteBuffer.limit());

                byte[] hash = uIntToBytes(hashAsUnit128);
                Bytes.reverse(hash);

                // Reset 2 most significant bits, as max exclusive value is 'FF'.
                // Plus one more just in case.
                hash[0] &= 0x3F;

                for (int n = 0; n < hash.length; n++) {
                    finalHash[(i * 2 * Long.BYTES) + n] = hash[n];
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return finalHash;
    }

    static String getEffectivePartitionKeyForHashPartitioning(PartitionKeyInternal partitionKeyInternal) {
        return HexConvert.bytesToHex(getEffectivePartitionKeyBytesForHashPartitioning(partitionKeyInternal));
    }

    static byte[] getEffectivePartitionKeyBytesForHashPartitioning(PartitionKeyInternal partitionKeyInternal) {
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

        return toBinary(partitionKeyComponents);
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

        if (partitionKeyInternal.components.size() < partitionKeyDefinition.getPaths().size() && partitionKeyDefinition.getKind() != PartitionKind.MULTI_HASH) {
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

            case MULTI_HASH:
                return getEffectivePartitionKeyForMultiHashPartitioning(partitionKeyInternal);

            default:
                return toHexEncodedBinaryString(partitionKeyInternal.components);
        }
    }

    public static byte[] getEffectivePartitionKeyBytes(PartitionKeyInternal partitionKeyInternal, PartitionKeyDefinition partitionKeyDefinition) {
        return getEffectivePartitionKeyBytes(partitionKeyInternal, partitionKeyDefinition, true);
    }

    public static byte[] getEffectivePartitionKeyBytes(PartitionKeyInternal partitionKeyInternal, PartitionKeyDefinition partitionKeyDefinition, boolean strict) {
        if (partitionKeyInternal.components == null) {
            throw new IllegalArgumentException(RMResources.TooFewPartitionKeyComponents);
        }

        if (partitionKeyInternal.equals(PartitionKeyInternal.EmptyPartitionKey)) {
            return MinimumInclusiveEffectivePartitionKeyBytes;
        }

        if (partitionKeyInternal.equals(PartitionKeyInternal.InfinityPartitionKey)) {
            return MaximumExclusiveEffectivePartitionKeyBytes;
        }

        if (partitionKeyInternal.components.size() < partitionKeyDefinition.getPaths().size() && partitionKeyDefinition.getKind() != PartitionKind.MULTI_HASH) {
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
                    return getEffectivePartitionKeyBytesForHashPartitioningV2(partitionKeyInternal);
                } else {
                    // V1
                    return getEffectivePartitionKeyBytesForHashPartitioning(partitionKeyInternal);
                }

            case MULTI_HASH:
                return getEffectivePartitionKeyBytesForMultiHashPartitioning(partitionKeyInternal);

            default:
                return toBinary(partitionKeyInternal.components);
        }
    }

    static public Range<String> getEPKRangeForPrefixPartitionKey(
        PartitionKeyInternal internalPartitionKey,
        PartitionKeyDefinition partitionKeyDefinition)
    {
        if(partitionKeyDefinition.getKind() != PartitionKind.MULTI_HASH)
        {
            throw new IllegalArgumentException(RMResources.PartitionKeyMismatch);
        }
        if(internalPartitionKey.getComponents().size() >= partitionKeyDefinition.getPaths().size())
        {
            throw new IllegalArgumentException(RMResources.TooManyPartitionKeyComponents);
        }
        String minEPK = internalPartitionKey.getEffectivePartitionKeyString(internalPartitionKey, partitionKeyDefinition);
        String maxEPK = minEPK + MaximumExclusiveEffectivePartitionKey;
        return new Range<>(minEPK, maxEPK, true, false);
    }
}
