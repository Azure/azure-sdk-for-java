// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegionTests {

    @Test
    public void testPredefined() {
        Region region = Region.US_WEST;

        Region sameRegion = Region.US_WEST;
        Assertions.assertTrue(region.equals(sameRegion));
        Assertions.assertTrue(region == sameRegion);

        Region differentRegion = Region.US_WEST2;
        Assertions.assertTrue(!region.equals(differentRegion));
        Assertions.assertTrue(region != differentRegion);
    }

    @Test
    public void testCreate() {
        // reuse region if exist
        Region sameRegion = Region.create("WESTUS", "WEST US");
        Assertions.assertEquals(Region.US_WEST, sameRegion);
        Assertions.assertTrue(Region.US_WEST == sameRegion);

        int size = Region.values().size();

        // create region with new name
        Region newRegion = Region.create("centraluseuap2", "Central US 2 EUAP");
        Assertions.assertTrue(Region.values().size() >= size + 1);  // 'testFromLabel' might create region in parallel, hence use >= instead of ==
        Region newRegionSame = Region.fromName("centraluseuap2");
        Assertions.assertTrue(newRegion.equals(newRegionSame));
        Assertions.assertEquals("Central US 2 EUAP", newRegionSame.label());
    }

    @Test
    public void testFromLabel() {
        // reuse region if exist
        Region region = Region.fromName("westus");
        Assertions.assertEquals(Region.US_WEST, region);

        // space is ignored
        Region sameRegion = Region.fromName("west us");
        Assertions.assertEquals(Region.US_WEST, sameRegion);

        // case is ignored
        sameRegion = Region.fromName("WEST US");
        Assertions.assertEquals(Region.US_WEST, sameRegion);

        // create region if not exist
        Region newRegion = Region.fromName("newregion");
        Assertions.assertEquals("newregion", newRegion.label());
        Assertions.assertTrue(Region.values().stream().anyMatch(r -> r.name().equals("newregion")));

        // null
        Assertions.assertNull(Region.fromName(null));
    }

    @Test
    public void testNegative() {
        Assertions.assertThrows(NullPointerException.class, () -> Region.create(null, "desc"));
    }
}
