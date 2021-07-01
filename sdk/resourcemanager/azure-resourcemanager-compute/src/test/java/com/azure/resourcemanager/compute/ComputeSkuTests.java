// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.ComputeResourceType;
import com.azure.resourcemanager.compute.models.ComputeSku;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.core.management.Region;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComputeSkuTests extends ComputeManagementTest {
    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
    }

//    @Test
//    public void foo() {
//        HashSet<EncryptionStatus> s = new HashSet<>();
//        s.add(EncryptionStatus.NOT_ENCRYPTED);
//        s.add(EncryptionStatus.NOT_ENCRYPTED);
//
//        System.out.println(s.contains(EncryptionStatus.fromString("notEncrypted")));
//    }

    @Test
    public void canListSkus() throws Exception {
        PagedIterable<ComputeSku> skus = this.computeManager.computeSkus().list();

        boolean atleastOneVirtualMachineResourceSku = false;
        boolean atleastOneAvailabilitySetResourceSku = false;
        boolean atleastOneDiskResourceSku = false;
        boolean atleastOneSnapshotResourceSku = false;
        boolean atleastOneRegionWithZones = false;
        for (ComputeSku sku : skus) {
            Assertions.assertNotNull(sku.resourceType());
            Assertions.assertNotNull(sku.regions());
            if (sku.resourceType().equals(ComputeResourceType.VIRTUALMACHINES)) {
                Assertions.assertNotNull(sku.virtualMachineSizeType());
                Assertions
                    .assertEquals(
                        sku.virtualMachineSizeType().toString().toLowerCase(), sku.name().toString().toLowerCase());
                Assertions.assertNull(sku.availabilitySetSkuType());
                Assertions.assertNull(sku.diskSkuType());
                atleastOneVirtualMachineResourceSku = true;

                for (Map.Entry<Region, Set<AvailabilityZoneId>> zoneMapEntry : sku.zones().entrySet()) {
                    Region region = zoneMapEntry.getKey();
                    Assertions.assertNotNull(region);
                    Set<AvailabilityZoneId> zones = zoneMapEntry.getValue();
                    if (zones.size() > 0) {
                        atleastOneRegionWithZones = true;
                    }
                }
            }
            if (sku.resourceType().equals(ComputeResourceType.AVAILABILITYSETS)) {
                Assertions.assertNotNull(sku.availabilitySetSkuType());
                Assertions
                    .assertEquals(
                        sku.availabilitySetSkuType().toString().toLowerCase(), sku.name().toString().toLowerCase());
                Assertions.assertNull(sku.virtualMachineSizeType());
                Assertions.assertNull(sku.diskSkuType());
                atleastOneAvailabilitySetResourceSku = true;
            }
            if (sku.resourceType().equals(ComputeResourceType.DISKS)) {
                Assertions.assertNotNull(sku.diskSkuType().toString());
                Assertions
                    .assertEquals(sku.diskSkuType().toString().toLowerCase(), sku.name().toString().toLowerCase());
                Assertions.assertNull(sku.virtualMachineSizeType());
                Assertions.assertNull(sku.availabilitySetSkuType());
                atleastOneDiskResourceSku = true;
            }
            if (sku.resourceType().equals(ComputeResourceType.SNAPSHOTS)) {
                Assertions.assertNotNull(sku.diskSkuType());
                Assertions
                    .assertEquals(sku.diskSkuType().toString().toLowerCase(), sku.name().toString().toLowerCase());
                Assertions.assertNull(sku.virtualMachineSizeType());
                Assertions.assertNull(sku.availabilitySetSkuType());
                atleastOneSnapshotResourceSku = true;
            }
        }
        Assertions.assertTrue(atleastOneVirtualMachineResourceSku);
        Assertions.assertTrue(atleastOneAvailabilitySetResourceSku);
        Assertions.assertTrue(atleastOneDiskResourceSku);
        Assertions.assertTrue(atleastOneSnapshotResourceSku);
        Assertions.assertTrue(atleastOneRegionWithZones);
    }

    @Test
    public void canListSkusByRegion() throws Exception {
        PagedIterable<ComputeSku> skus = this.computeManager.computeSkus().listByRegion(Region.US_EAST2);
        for (ComputeSku sku : skus) {
            Assertions.assertTrue(sku.regions().contains(Region.US_EAST2));
        }

        skus = this.computeManager.computeSkus().listByRegion(Region.fromName("Unknown"));
        Assertions.assertEquals(0, TestUtilities.getSize(skus));
    }

    @Test
    public void canListSkusByResourceType() throws Exception {
        PagedIterable<ComputeSku> skus =
            this.computeManager.computeSkus().listByResourceType(ComputeResourceType.VIRTUALMACHINES);
        for (ComputeSku sku : skus) {
            Assertions.assertTrue(sku.resourceType().equals(ComputeResourceType.VIRTUALMACHINES));
        }

        skus = this.computeManager.computeSkus().listByResourceType(ComputeResourceType.fromString("Unknown"));
        Assertions.assertEquals(0, TestUtilities.getSize(skus));
    }

    @Test
    public void canListSkusByRegionAndResourceType() throws Exception {
        PagedIterable<ComputeSku> skus =
            this
                .computeManager
                .computeSkus()
                .listByRegionAndResourceType(Region.US_EAST2, ComputeResourceType.VIRTUALMACHINES);
        for (ComputeSku sku : skus) {
            Assertions.assertTrue(sku.resourceType().equals(ComputeResourceType.VIRTUALMACHINES));
            Assertions.assertTrue(sku.regions().contains(Region.US_EAST2));
        }

        skus =
            this
                .computeManager
                .computeSkus()
                .listByRegionAndResourceType(Region.US_EAST2, ComputeResourceType.fromString("Unknown"));
        Assertions.assertNotNull(skus);
        Assertions.assertEquals(0, TestUtilities.getSize(skus));
    }
}
