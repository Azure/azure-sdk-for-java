package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.MurmurHash3_128;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.JsonSerializable;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DistinctHash {

    private static final UInt128 ArrayHashSeed = new UInt128(0xfa573b014c4dc18eL, 0xa014512c858eb115L);

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static UInt128 getHash(Object resource) throws IOException,
                                                              NoSuchAlgorithmException {

        if (resource instanceof List) {
            return hashList((List) resource);
        }

        if (resource instanceof JsonSerializable) {
            return getHashFromJsonSerializable((JsonSerializable) resource);
        }

        final byte[] bytes = Utils.serializeObjectToByteArray(resource);
        UInt128 uInt128 = MurmurHash3_128.hash128(bytes, bytes.length);
        return uInt128;

//        final Object obj = OBJECT_MAPPER.treeToValue(ModelBridgeInternal.getPropertyBagFromJsonSerializable
//        (resource)
//            , Object.class);
//        final String sortedJson =
//            OBJECT_MAPPER.writeValueAsString(obj);
//        MessageDigest md = MessageDigest.getInstance("SHA-1");
//        byte[] digest = md.digest(sortedJson.getBytes(Charset.defaultCharset()));
//        return Base64.getEncoder().encodeToString(digest);
    }

    private static UInt128 getHashFromJsonSerializable(JsonSerializable resource) {
        final ByteBuffer byteBuffer = ModelBridgeInternal.serializeJsonToByteBuffer(resource, OBJECT_MAPPER);
        final byte[] bytes = byteBuffer.array();
        return MurmurHash3_128.hash128(bytes, bytes.length);
    }

    private static UInt128 hashList(List resource) {
        UInt128 hash = ArrayHashSeed;
        for (Object obj : resource) {
            if (obj instanceof JsonSerializable) {
                byte[] bytes = hash.toByteBuffer().array();
                if (bytes.length == 0) {
                    throw new IllegalStateException("Failed to hash!");
                }
                hash = MurmurHash3_128.hash128(bytes, bytes.length,
                                               getHashFromJsonSerializable((JsonSerializable) obj));
            }
        }
        return hash;
    }
}
