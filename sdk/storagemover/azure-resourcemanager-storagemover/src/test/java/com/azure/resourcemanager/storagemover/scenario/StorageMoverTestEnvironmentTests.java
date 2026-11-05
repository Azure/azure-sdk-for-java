// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.Region;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Arrays;

public class StorageMoverTestEnvironmentTests {
    @Test
    public void liveOverridesCannotChangePlaybackFixtures() {
        Configuration configuration = overrides();
        StorageMoverTestEnvironment live = new StorageMoverTestEnvironment(false, configuration);
        Assertions.assertEquals(Region.fromName("eastus2euap"), live.location);
        Assertions.assertEquals(Region.US_WEST_CENTRAL, live.c2cLocation);
        Assertions.assertEquals("/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/replacement-rg"
            + "/providers/Microsoft.Network/privateLinkServices/replacement.pls", live.privateLinkServiceId());

        StorageMoverTestEnvironment playback = new StorageMoverTestEnvironment(true, configuration);
        Assertions.assertEquals(Region.US_EAST, playback.location);
        Assertions.assertEquals(Region.US_WEST_CENTRAL, playback.c2cLocation);
        Assertions.assertEquals("/subscriptions/" + StorageMoverTestEnvironment.ZERO_SUBSCRIPTION_ID
            + StorageMoverTestEnvironment.PLAYBACK_PRIVATE_LINK_SERVICE_PATH, playback.privateLinkServiceId());
    }

    @Test
    public void sharedPrivateLinkServiceSettingTakesPrecedenceOverLegacySetting() {
        Configuration configuration = overrides().put("STORAGEMOVER_PRIVATE_LINK_SERVICE_NAME", "legacy-pls");
        Assertions.assertEquals("replacement.pls",
            new StorageMoverTestEnvironment(false, configuration).privateLinkServiceName);
    }

    @Test
    public void privateLinkServiceSanitizationPreservesChildResourcePaths() {
        String path = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/replacement-rg"
            + "/providers/Microsoft.Network/privateLinkServices/replacement.pls/privateEndpointConnections/connection";
        Assertions.assertEquals("/subscriptions/00000000-0000-0000-0000-000000000000"
            + StorageMoverTestEnvironment.PLAYBACK_PRIVATE_LINK_SERVICE_PATH + "/privateEndpointConnections/connection",
            path.replaceAll(StorageMoverTestEnvironment.PRIVATE_LINK_SERVICE_PATH_REGEX,
                StorageMoverTestEnvironment.PLAYBACK_PRIVATE_LINK_SERVICE_PATH));
    }

    @Test
    public void signedOperationSanitizationPreservesApiVersionAndQueryKeys() {
        String uri = "https://management.azure.com/operation?api-version=2026-05-01"
            + "&t=timestamp&c=certificate&s=signature&h=hash";
        Assertions.assertEquals(
            "https://management.azure.com/operation?api-version=2026-05-01"
                + "&t=Sanitized&c=Sanitized&s=Sanitized&h=Sanitized",
            uri.replaceAll(StorageMoverTestEnvironment.SIGNED_QUERY_PARAMETERS_REGEX, "$1$2=Sanitized"));
    }

    @Test
    public void roleAssignmentErrorsDoNotExposeDirectoryOrPrincipalIds() {
        String message = "Principal aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa does not exist in the directory "
            + "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb.";
        Assertions.assertEquals(
            "Principal " + StorageMoverTestEnvironment.ZERO_SUBSCRIPTION_ID + " does not exist in the directory "
                + StorageMoverTestEnvironment.ZERO_SUBSCRIPTION_ID + ".",
            message.replaceAll(StorageMoverTestEnvironment.ERROR_IDENTITY_REGEX,
                StorageMoverTestEnvironment.ZERO_SUBSCRIPTION_ID));
    }

    @Test
    public void privateLinkServiceSanitizersProduceValidProxyRegistration() {
        Assertions.assertDoesNotThrow(() -> TestProxyUtils.createAddSanitizersRequest(
            Arrays.asList(
                new TestProxySanitizer(StorageMoverTestEnvironment.PRIVATE_LINK_SERVICE_PATH_REGEX,
                    StorageMoverTestEnvironment.PLAYBACK_PRIVATE_LINK_SERVICE_PATH, TestProxySanitizerType.URL),
                new TestProxySanitizer(StorageMoverTestEnvironment.PRIVATE_LINK_SERVICE_PATH_REGEX,
                    StorageMoverTestEnvironment.PLAYBACK_PRIVATE_LINK_SERVICE_PATH, TestProxySanitizerType.BODY_REGEX),
                new TestProxySanitizer(StorageMoverTestEnvironment.ERROR_IDENTITY_REGEX,
                    StorageMoverTestEnvironment.ZERO_SUBSCRIPTION_ID, TestProxySanitizerType.BODY_REGEX)),
            new URL("http://localhost:5000")).getBodyAsBinaryData().toObject(Object.class));
    }

    private static Configuration overrides() {
        return new Configuration().put("STORAGE_MOVER_TEST_LOCATION", "eastus2euap")
            .put("STORAGEMOVER_SYNTHETICS_SUBSCRIPTION_ID", "11111111-1111-1111-1111-111111111111")
            .put("STORAGE_MOVER_PRIVATE_LINK_SERVICE_RESOURCE_GROUP", "replacement-rg")
            .put("STORAGE_MOVER_PRIVATE_LINK_SERVICE_NAME", "replacement.pls");
    }
}
