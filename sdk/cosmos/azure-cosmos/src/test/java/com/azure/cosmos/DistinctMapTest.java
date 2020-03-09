// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.DistinctMap;
import com.azure.cosmos.implementation.query.DistinctQueryType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DistinctMapTest {
    @DataProvider(name = "distinctMapArgProvider")
    public Object[][] distinctMapArgProvider() {
        return new Object[][] {
            {DistinctQueryType.Ordered},
            {DistinctQueryType.Unordered},
        };
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void integerValue(DistinctQueryType queryType) {
        String resource = String.format("{ " + "\"id\": 5 + \"}");
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        System.out.println("outHash5 = " + outHash.v);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
        resource = String.format("{ " + "\"id\": 3 + \"}");
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void stringValue(DistinctQueryType queryType) {
        String resource = String.format("{ " + "\"id\": \"5\" + \"}");
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
        resource = String.format("{ " + "\"id\": \"6\" + \"}");
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void objectValue(DistinctQueryType queryType) {
        String resource = String.format("{ "
                                            + "\"id\": \"%s\", "
                                            + "\"mypk\": \"%s\", "
                                            + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                            + "}", 117546, "xxyyzz-abc");
        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();

        resource = String.format("{ "
                                     + "\"id\": \"%s\", "
                                     + "\"mypk\": \"%s\", "
                                     + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                     + "}", 117546, "xxy%zz-abc");
        boolean add3 = distinctMap.add(resource, outHash);
        assertThat(add3).as("different value should be added again").isTrue();

    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void nullValue(DistinctQueryType queryType) {
        DistinctMap distinctMap = DistinctMap.create(queryType, null);
        Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(null, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(null, outHash);
        assertThat(add2).as("same value should not be added again").isFalse();
    }

    @Test(groups = "unit", dataProvider = "distinctMapArgProvider")
    public void objectOrder(DistinctQueryType queryType) {
        String resource1 = String.format("{ "
                                            + "\"id\": \"12345\", "
                                            + "\"mypk\": \"abcde\""
                                            + "} ");

        String resource2 = String.format("{ "
                                             + "\"mypk\": \"abcde\","
                                             + "\"id\": \"12345\""
                                             + "} ");

        DistinctMap distinctMap = DistinctMap.create(queryType, null);

        Utils.ValueHolder<String> outHash = new Utils.ValueHolder<>();
        boolean add = distinctMap.add(resource1, outHash);
        assertThat(add).as("Value should be added first time").isTrue();
        boolean add2 = distinctMap.add(resource2, outHash);
        assertThat(add2).as("Order of objects in map should be treated same").isTrue();
        
    }
    
}
