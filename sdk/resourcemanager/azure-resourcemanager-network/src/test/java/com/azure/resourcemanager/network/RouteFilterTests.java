// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.Access;
import com.azure.resourcemanager.network.models.RouteFilter;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RouteFilterTests extends NetworkManagementTest {

    @Test
    public void canCRUDRouteFilter() throws Exception {
        String rfName = generateRandomResourceName("rf", 15);

        RouteFilter routeFilter =
            networkManager
                .routeFilters()
                .define(rfName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withTag("tag1", "value1")
                .create();
        Assertions.assertEquals("value1", routeFilter.tags().get("tag1"));

        PagedIterable<RouteFilter> rfList = networkManager.routeFilters().list();
        Assertions.assertTrue(TestUtilities.getSize(rfList) > 0);

        rfList = networkManager.routeFilters().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.getSize(rfList) > 0);

        networkManager.routeFilters().deleteById(routeFilter.id());
        rfList = networkManager.routeFilters().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.isEmpty(rfList));
    }

    @Test
    public void canCreateRouteFilterRule() throws Exception {
        String rfName = generateRandomResourceName("rf", 15);
        String ruleName = "mynewrule";
        RouteFilter routeFilter =
            networkManager
                .routeFilters()
                .define(rfName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .create();

        routeFilter.update().defineRule(ruleName).withBgpCommunity("12076:51004").attach().apply();
        Assertions.assertEquals(1, routeFilter.rules().size());
        Assertions.assertEquals(1, routeFilter.rules().get(ruleName).communities().size());
        Assertions.assertEquals("12076:51004", routeFilter.rules().get(ruleName).communities().get(0));

        routeFilter
            .update()
            .updateRule(ruleName)
            .withBgpCommunities("12076:51005", "12076:51026")
            .denyAccess()
            .parent()
            .apply();
        Assertions.assertEquals(2, routeFilter.rules().get(ruleName).communities().size());
        Assertions.assertEquals(Access.DENY, routeFilter.rules().get(ruleName).access());
    }
}
