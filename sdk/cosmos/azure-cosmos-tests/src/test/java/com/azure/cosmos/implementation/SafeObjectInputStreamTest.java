// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SafeObjectInputStreamTest {

    @Test(groups = {"unit"})
    public void allowedClassIsDeserialized() throws Exception {
        // Serialize a HashMap (an allowed class)
        HashMap<String, String> original = new HashMap<>();
        original.put("key", "value");
        byte[] serialized = serialize(original);

        Set<String> allowed = new HashSet<>(Arrays.asList(
            HashMap.class.getName(),
            "[Ljava.util.HashMap$Node;", // internal array type used by HashMap
            String.class.getName()
        ));
        Set<String> prefixes = Collections.singleton("java.util.");

        try (SafeObjectInputStream ois = new SafeObjectInputStream(
            new ByteArrayInputStream(serialized), allowed, prefixes)) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> result = (HashMap<String, String>) ois.readObject();
            assertThat(result).containsEntry("key", "value");
        }
    }

    @Test(groups = {"unit"})
    public void blockedClassThrowsInvalidClassException() throws Exception {
        // Serialize a HashMap but only allow String
        HashMap<String, String> original = new HashMap<>();
        original.put("key", "value");
        byte[] serialized = serialize(original);

        Set<String> allowed = Collections.singleton(String.class.getName());

        assertThatThrownBy(() -> {
            try (SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(serialized), allowed)) {
                ois.readObject();
            }
        }).isInstanceOf(InvalidClassException.class)
          .hasMessageContaining("Unauthorized deserialization attempt");
    }

    @Test(groups = {"unit"})
    public void prefixMatchAllowsClasses() throws Exception {
        HashMap<String, String> original = new HashMap<>();
        original.put("test", "data");
        byte[] serialized = serialize(original);

        // Allow via prefix only
        Set<String> prefixes = new HashSet<>(Arrays.asList("java.util.", "java.lang."));

        try (SafeObjectInputStream ois = new SafeObjectInputStream(
            new ByteArrayInputStream(serialized), Collections.emptySet(), prefixes)) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> result = (HashMap<String, String>) ois.readObject();
            assertThat(result).containsEntry("test", "data");
        }
    }

    @Test(groups = {"unit"})
    public void emptyAllowlistBlocksEverything() throws Exception {
        String original = "test";
        byte[] serialized = serialize(original);

        assertThatThrownBy(() -> {
            try (SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(serialized), Collections.emptySet())) {
                ois.readObject();
            }
        }).isInstanceOf(InvalidClassException.class);
    }

    private byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }
}
