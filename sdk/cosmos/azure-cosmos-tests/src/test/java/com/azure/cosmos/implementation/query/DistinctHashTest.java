// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.routing.UInt128;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DistinctHashTest {

    @Test(groups = {"unit"})
    public void nullHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash(null);
        UInt128 hash2 = DistinctHash.getHash(null);

        assertThat(hash1.equals(hash2)).isTrue();
    }

    @Test(groups = {"unit"})
    public void booleanHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash(Boolean.TRUE);
        UInt128 hash2 = DistinctHash.getHash(Boolean.TRUE);
        UInt128 hash3 = DistinctHash.getHash(Boolean.FALSE);

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void integerHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash(Integer.valueOf("2"));
        UInt128 hash2 = DistinctHash.getHash(Integer.valueOf("2"));
        UInt128 hash3 = DistinctHash.getHash(Integer.valueOf("3"));

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void longHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash(Long.valueOf("2"));
        UInt128 hash2 = DistinctHash.getHash(Long.valueOf("2"));
        UInt128 hash3 = DistinctHash.getHash(Long.valueOf("3"));

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void doubleHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash(Double.valueOf("2.0"));
        UInt128 hash2 = DistinctHash.getHash(Double.valueOf("2.00"));
        UInt128 hash3 = DistinctHash.getHash(Double.valueOf("3.0"));

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void stringHash() throws IOException {
        UInt128 hash1 = DistinctHash.getHash("testString1");
        UInt128 hash2 = DistinctHash.getHash("testString1");
        UInt128 hash3 = DistinctHash.getHash("testString2");

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void arrayNodeHash() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode node1 = mapper.createArrayNode();
        node1.add(1);
        node1.add(2);
        UInt128 hash1 = DistinctHash.getHash(node1);

        ArrayNode node2 = mapper.createArrayNode();
        node2.add(1);
        node2.add(2);
        UInt128 hash2 = DistinctHash.getHash(node2);

        ArrayNode node3 = mapper.createArrayNode();
        node2.add(2);
        node2.add(1);
        UInt128 hash3 = DistinctHash.getHash(node3);
        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse(); // sequence in array matters
    }


    @Test(groups = {"unit"})
    public void jsonObjectHash() throws IOException {
        String jsonString1 = "{ \"nameProp\": \"name\", \"idProp\": \"1\"}";
        String jsonString2 = "{ \"idProp\": \"1\", \"nameProp\": \"name\"}";
        String jsonString3 = "{ \"nameProp\": \"name\", \"idProp\": \"2\"}";

        ObjectMapper mapper = new ObjectMapper();

        UInt128 hash1 = DistinctHash.getHash(mapper.readValue(jsonString1, ObjectNode.class));
        UInt128 hash2 = DistinctHash.getHash(mapper.readValue(jsonString2, ObjectNode.class));
        UInt128 hash3 = DistinctHash.getHash(mapper.readValue(jsonString3, ObjectNode.class));

        assertThat(hash1.equals(hash2)).isTrue(); // the sequence of the key does not change the hash
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void listHash() throws IOException {
        List<String> list1 = new ArrayList<>();
        list1.add("string1");
        list1.add("string2");

        List<String> list2 = new ArrayList<>();
        list2.add("string1");
        list2.add("string2");

        List<String> list3 = new ArrayList<>();
        list3.add("string2");
        list3.add("string1");

        UInt128 hash1 = DistinctHash.getHash(list1);
        UInt128 hash2 = DistinctHash.getHash(list2);
        UInt128 hash3 = DistinctHash.getHash(list3);

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }

    @Test(groups = {"unit"})
    public void jsonSerializableHash() throws IOException {
        String queryItemString = "{\"item\":10}";
        String queryItemString2 = "{\"item\":20}";

        QueryItem item1 = new QueryItem(queryItemString);
        QueryItem item2 = new QueryItem(queryItemString);
        QueryItem item3 = new QueryItem(queryItemString2);

        UInt128 hash1 = DistinctHash.getHash(item1);
        UInt128 hash2 = DistinctHash.getHash(item2);
        UInt128 hash3 = DistinctHash.getHash(item3);

        assertThat(hash1.equals(hash2)).isTrue();
        assertThat(hash1.equals(hash3)).isFalse();
    }
}
