// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class OrderedDistinctMap extends DistinctMap {
    private final ObjectMapper mapper;
    private String lastHash;

    public OrderedDistinctMap(String lastHash) {
        mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.lastHash = lastHash;
    }

    @Override
    public boolean add(Object resource, Utils.ValueHolder<String> outHash) {
        try {
            String sortedJson = mapper.writeValueAsString(resource);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sortedJson.getBytes(Charset.defaultCharset()));
            outHash.v = Base64.getEncoder().encodeToString(digest);
            // value should be true if hashes are not equal
            boolean value = !StringUtils.equals(lastHash, outHash.v);
            lastHash = outHash.v;
            return value;
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
