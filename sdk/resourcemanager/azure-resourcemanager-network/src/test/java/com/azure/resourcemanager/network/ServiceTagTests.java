// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.network.models.ServiceTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceTagTests {

    @Test
    @DoNotRecord
    public void testServiceTag() {
        // create a valid regional service tag
        ServiceTag storage = ServiceTag.STORAGE;
        ServiceTag storageWestUS = storage.withRegion(Region.US_WEST);
        Assertions.assertEquals(ServiceTag.fromName("Storage.WestUS"), storageWestUS);
        Assertions.assertEquals(storage, storage.withRegion(null));

        // call "withRegion" on a regional service tag will return itself
        Assertions.assertEquals(storageWestUS, storageWestUS.withRegion(Region.ASIA_EAST));
        Assertions.assertEquals(storageWestUS, storageWestUS.withRegion(null));

        // create a valid root service tag by "fromName"
        ServiceTag storageFromName = ServiceTag.fromName("Storage");
        Assertions.assertEquals(storage, storageFromName);

        // create a valid regional service tag by "fromName"
        ServiceTag storageWestUSFromName = ServiceTag.fromName("Storage.WestUS");
        Assertions.assertEquals(storageWestUS, storageWestUSFromName);

        // "fromName" receives invalid service tag names will return null
        Assertions.assertNull(ServiceTag.fromName(null));
        Assertions.assertNull(ServiceTag.fromName("."));
        Assertions.assertNull(ServiceTag.fromName(".."));
        Assertions.assertNull(ServiceTag.fromName("Storage."));
        Assertions.assertNull(ServiceTag.fromName("Storage.."));
        Assertions.assertNull(ServiceTag.fromName("Storage.WestUS.suffix"));
        Assertions.assertNull(ServiceTag.fromName("FooBar.WestUS"));

        Assertions.assertNull(ServiceTag.fromName("*"));
        Assertions.assertNull(ServiceTag.fromName("10.0.0.0/24"));
    }
}
