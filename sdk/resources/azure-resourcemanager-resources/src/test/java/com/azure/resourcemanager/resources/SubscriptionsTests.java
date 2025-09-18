// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.LocationType;
import com.azure.resourcemanager.resources.models.RegionCategory;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionsTests extends ResourceManagementTest {
    @Test
    public void canListSubscriptions() {
        PagedIterable<Subscription> subscriptions = resourceClient.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptions) > 0);
    }

    @Test
    public void canListLocations() {
        PagedIterable<Location> locations = resourceClient.subscriptions().list().iterator().next().listLocations();
        Assertions.assertTrue(TestUtilities.getSize(locations) > 0);
    }

    /**
     * List Region gaps.
     * Also refer to <a href="https://learn.microsoft.com/azure/reliability/regions-list#azure-regions-list-1">regions-list</a>
     * for double check(remove regions coming soon and restricted regions).
     */
    @Test
    @Disabled
    public void listNewRegions() {
        Set<Location> locations = resourceClient.subscriptions()
            .list()
            .iterator()
            .next()
            .listLocations()
            .stream()
            .collect(Collectors.toSet());
        Map<String, RegionBrief> regionMap = locations.stream()
            .collect(
                Collectors.toMap(Location::name, location -> new RegionBrief(location.name(), location.displayName())));
        Set<String> currentRegions = Region.values().stream().map(Region::name).collect(Collectors.toSet());
        Set<String> allRegions = locations.stream()
            .filter(location -> location.regionCategory() == RegionCategory.RECOMMENDED
                && location.innerModel().type() == LocationType.REGION)
            .map(location -> location.region().name())
            .collect(Collectors.toSet());
        allRegions.removeAll(currentRegions);
        Collection<RegionBrief> newRegions = allRegions.stream().map(regionMap::get).collect(Collectors.toList());

        System.out.println("New regions: " + newRegions);
    }

    private static class RegionBrief {
        String name;
        String label;

        RegionBrief(String name, String label) {
            this.name = name;
            this.label = label;
        }

        @Override
        public String toString() {
            return "{" + "name: \"" + name + '"' + ", displayName: \"" + label + '"' + '}';
        }
    }
}
