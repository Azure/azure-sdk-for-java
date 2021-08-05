// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.DistinctMap;
import com.azure.cosmos.implementation.query.DistinctQueryType;
import com.azure.cosmos.implementation.routing.UInt128;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DistinctMapTest {
    @DataProvider(name = "distinctMapArgProvider")
    public Object[][] distinctMapArgProvider() {
        return new Object[][] {
            {DistinctQueryType.ORDERED},
            {DistinctQueryType.UNORDERED},
        };
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void integerValue(DistinctQueryType queryType) {
        String doc = String.format("{ " + "\"id\": \"%s\", \"prop\": %d }", UUID.randomUUID().toString(), 5);
        Document resource = new Document(doc);
        DistinctMap distinctMap = DistinctMap.create(queryType, null);
        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
        resource = new Document(String.format("{ " + "\"id\": \"%s\", \"prop\": %d }", UUID.randomUUID().toString(),
                                              3));
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void stringValue(DistinctQueryType queryType) {
        String resourceString = String.format("{ " + "\"id\": \"a\" }");
        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();

        Document resource = new Document(resourceString);
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
        resource = new Document(String.format("{ " + "\"id\": \"b\" }"));
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void objectValue(DistinctQueryType queryType) {
        String resourceString = String.format("{ "
                                            + "\"id\": \"%s\", "
                                            + "\"mypk\": \"%s\", "
                                            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                            + "}", 117546, "xxyyzz-abc");
        Document resource = new Document(resourceString);
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();

        resourceString = String.format("{ "
                                     + "\"id\": \"%s\", "
                                     + "\"mypk\": \"%s\", "
                                     + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                     + "}", 117546, "xxy%zz-abc");
        resource = new Document(resourceString);
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();

    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void arrayValue(DistinctQueryType queryType) {
        String resourceString = String.format("{ "
                                                  + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                  + "}");
        Document resource = new Document(resourceString);
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();

        resourceString = String.format("{ "
                                                  + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671441]]"
                                                  + "}");
        resource = new Document(resourceString);
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();

    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void nullValue(DistinctQueryType queryType) {
        DistinctMap distinctMap = DistinctMap.create(queryType, null);
        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(null, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(null, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void objectOrder(DistinctQueryType queryType) {
        String resource1 = String.format("{ "
                                            + "\"id\": \"12345\","
                                            + "\"mypk\": \"abcde\""
                                            + "} ");

        String resource2 = String.format("{ "
                                             + "\"mypk\": \"abcde\","
                                             + "\"id\": \"12345\""
                                             + "} ");

        DistinctMap distinctMap = DistinctMap.create(queryType, null);
        Utils.ValueHolder<UInt128> outHash = new Utils.ValueHolder<>();

        Document resource = new Document(resource1);
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        resource = new Document(resource2);
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("Order of properties should not matter, so should not add").isFalse();
    }

}
