// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.Region;
import com.azure.core.util.Configuration;

final class StorageMoverTestEnvironment {
    static final String ZERO_SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    static final String PLAYBACK_PRIVATE_LINK_SERVICE_PATH
        = "/resourceGroups/E2E-Management-RGsyn/providers/Microsoft.Network/privateLinkServices/test-pls-wcs";
    // The proxy's sanitizer serializer does not escape regexes as JSON.
    static final String PRIVATE_LINK_SERVICE_PATH_REGEX
        = "(?i)/resourceGroups/[^/]+/providers/Microsoft[.]Network/privateLinkServices/[a-zA-Z0-9_.-]+";
    static final String SIGNED_QUERY_PARAMETERS_REGEX = "([?&])(t|c|s|h)=[^&]+";
    static final String ERROR_IDENTITY_REGEX = "(?i)(?:(?<=Principal )|(?<=directory )|(?<=tenant ))[0-9a-f-]{32,36}";

    final Region location;
    final Region c2cLocation;
    final String syntheticsSubscriptionId;
    final String privateLinkServiceResourceGroup;
    final String privateLinkServiceName;

    StorageMoverTestEnvironment(boolean playback, Configuration configuration) {
        String locationOverride = playback
            ? null
            : setting(configuration, "STORAGE_MOVER_TEST_LOCATION",
                setting(configuration, "AZURE_TEST_LOCATION", null));
        location = locationOverride == null ? Region.US_EAST : Region.fromName(locationOverride);
        c2cLocation = Region.US_WEST_CENTRAL;
        syntheticsSubscriptionId = playback
            ? ZERO_SUBSCRIPTION_ID
            : setting(configuration, "STORAGEMOVER_SYNTHETICS_SUBSCRIPTION_ID",
                setting(configuration, "AZURE_SUBSCRIPTION_ID", ZERO_SUBSCRIPTION_ID));
        privateLinkServiceResourceGroup = playback
            ? "E2E-Management-RGsyn"
            : setting(configuration, "STORAGE_MOVER_PRIVATE_LINK_SERVICE_RESOURCE_GROUP", "E2E-Management-RGsyn");
        privateLinkServiceName = playback
            ? "test-pls-wcs"
            : setting(configuration, "STORAGE_MOVER_PRIVATE_LINK_SERVICE_NAME",
                setting(configuration, "STORAGEMOVER_PRIVATE_LINK_SERVICE_NAME", "test-pls-wcs"));
    }

    String privateLinkServiceId() {
        return "/subscriptions/" + syntheticsSubscriptionId + "/resourceGroups/" + privateLinkServiceResourceGroup
            + "/providers/Microsoft.Network/privateLinkServices/" + privateLinkServiceName;
    }

    private static String setting(Configuration configuration, String name, String fallback) {
        String value = configuration.get(name);
        return value == null || value.isEmpty() ? fallback : value;
    }
}
