/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.ComputeResourceType;
import com.microsoft.azure.management.compute.ComputeSku;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Azure Compute sample for managing Compute SKUs -
 *
 * - list all compute SKUs in the subscription
 * - list compute SKUs for a specific compute resource type (VirtualMachines) in a region
 * - List compute SKUs for a specific compute resource type (Disks).
 */
public final class ListComputeSkus {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {

        //=================================================================
        // List all compute SKUs in the subscription
        //
        System.out.println("Listing Compute SKU in the subscription");
        String format = "%-22s %-16s %-22s %s";

        System.out.println(String.format(format, "Name", "ResourceType", "Size", "Regions [zones]"));
        System.out.println("============================================================================");

        PagedList<ComputeSku> skus = azure.computeSkus().list();
        for (ComputeSku sku : skus) {
            String size = null;
            if (sku.resourceType().equals(ComputeResourceType.VIRTUALMACHINES)) {
                size = sku.virtualMachineSizeType().toString();
            } else if (sku.resourceType().equals(ComputeResourceType.AVAILABILITYSETS)) {
                size = sku.availabilitySetSkuType().toString();
            } else if (sku.resourceType().equals(ComputeResourceType.DISKS)) {
                size = sku.diskSkuType().toString();
            } else if (sku.resourceType().equals(ComputeResourceType.SNAPSHOTS)) {
                size = sku.diskSkuType().toString();
            }
            Map<Region, Set<AvailabilityZoneId>> regionZones = sku.zones();
            System.out.println(String.format(format, sku.name(), sku.resourceType(), size, regionZoneToString(sku.zones())));
        }

        //=================================================================
        // List compute SKUs for a specific compute resource type (VirtualMachines) in a region
        //
        System.out.println("Listing compute SKUs for a specific compute resource type (VirtualMachines) in a region (US East2)");
        format = "%-22s %-22s %s";

        System.out.println(String.format(format, "Name", "Size", "Regions [zones]"));
        System.out.println("============================================================================");

        skus = azure.computeSkus()
                .listbyRegionAndResourceType(Region.US_EAST2, ComputeResourceType.VIRTUALMACHINES);
        for (ComputeSku sku : skus) {
            final String line = String.format(format, sku.name(), sku.virtualMachineSizeType(), regionZoneToString(sku.zones()));
            System.out.println(line);
        }

        //=================================================================
        // List compute SKUs for a specific compute resource type (Disks)
        //
        System.out.println("Listing compute SKUs for a specific compute resource type (Disks)");
        format = "%-22s %-22s %s";

        System.out.println(String.format(format, "Name", "Size", "Regions [zones]"));
        System.out.println("============================================================================");

        skus = azure.computeSkus()
                .listByResourceType(ComputeResourceType.DISKS);
        for (ComputeSku sku : skus) {
            final String line = String.format(format, sku.name(), sku.diskSkuType(), regionZoneToString(sku.zones()));
            System.out.println(line);
        }

        return true;
    }

    private static String regionZoneToString(Map<Region, Set<AvailabilityZoneId>> regionZonesMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Region, Set<AvailabilityZoneId>> regionZones : regionZonesMap.entrySet()) {
            builder.append(regionZones.getKey().toString());
            builder.append(" [ ");
            for (AvailabilityZoneId zone :regionZones.getValue()) {
                builder.append(zone).append(" ");
            }
            builder.append("] ");
        }
        return builder.toString();
    }

    /**
     * The main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ListComputeSkus() {
    }
}
