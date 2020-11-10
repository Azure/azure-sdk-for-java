// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataFeedDimensionKeyTests {
    @Test
    public void equalityCheckTests() {
        DimensionKey dimensionKey1 = new DimensionKey();
        DimensionKey dimensionKey2 = new DimensionKey();

        Assertions.assertEquals(dimensionKey1, dimensionKey2);

        // Set two dimensions in key1.
        dimensionKey1.put("category", "men/shoes");
        dimensionKey1.put("city", "redmond");
        // Set the same dimensions in key2 as well, but different order.
        dimensionKey2.put("city", "redmond");
        dimensionKey2.put("category", "men/shoes");
        // Both keys should be same irrespective of the order in which dimensions were added.
        Assertions.assertEquals(dimensionKey1, dimensionKey2);

        // Change value of one dimension in key1
        dimensionKey1.put("category", "men/shoes");
        dimensionKey1.put("city", "bellevue");
        // Keys are not equal anymore.
        Assertions.assertNotEquals(dimensionKey1, dimensionKey2);

        // Ensure HashCode for keys are same, if both has same dimensions.
        Set<DimensionKey> keySet = new HashSet<>();
        dimensionKey1.put("category", "men/shoes");
        dimensionKey1.put("city", "redmond");
        keySet.add(dimensionKey1);
        Assertions.assertTrue(keySet.contains(dimensionKey2));
    }

    @Test
    public void getAsMapTests() {
        DimensionKey dimensionKey = new DimensionKey();
        dimensionKey.put("category", "men/shoes");
        dimensionKey.put("city", "redmond");

        Map<String, String> map1 = dimensionKey.asMap();
        Assertions.assertEquals(2, map1.size());
        Assertions.assertTrue(map1.containsKey("category"));
        Assertions.assertTrue(map1.containsKey("city"));

        Assertions.assertEquals("men/shoes", map1.get("category"));
        Assertions.assertEquals("redmond", map1.get("city"));

        // Adding a new dimension to key
        dimensionKey.put("area", "north");
        // should not mutate the already returned map.
        Assertions.assertEquals(2, map1.size());
        Assertions.assertTrue(map1.containsKey("category"));
        Assertions.assertTrue(map1.containsKey("city"));

        // getting as map should reflect current state of the key.
        Map<String, String> map2 = dimensionKey.asMap();
        Assertions.assertEquals(3, map2.size());
    }
}
