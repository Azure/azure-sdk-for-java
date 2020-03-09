// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;

public class UnorderedDistinctMap extends DistinctMap {
    private final ObjectMapper mapper;
    private final HashSet<String> resultSet;

    public UnorderedDistinctMap() {
        resultSet = new HashSet<>();
        mapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Override
    public boolean add(Object resource, Utils.ValueHolder<String> outHash) {
        try {
            String sortedJson = mapper.writeValueAsString(resource);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sortedJson.getBytes(Charset.defaultCharset()));
            outHash.v = Base64.getEncoder().encodeToString(digest);
            return resultSet.add(outHash.v);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

    }
}
