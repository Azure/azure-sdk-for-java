// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storagemover.models.Connection;
import com.azure.resourcemanager.storagemover.models.ConnectionProperties;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

/**
 * Mirrors {@code ConnectionTests.cs} from the .NET source-of-truth (row #32 of
 * the cross-language matrix). Exercises Connection CRUD against the shared
 * private-link service {@code test-pls-wcs}.
 *
 * <p>Operates in {@code westcentralus} because the upstream PLS is deployed
 * there; the per-test resource group is provisioned in the same region so
 * private endpoint provisioning stays in-region.
 *
 * <p>This test intentionally does not assert on {@code connectionStatus} —
 * PLS provisioning is asynchronous and the status starts as {@code Pending}.
 * The status assertion lives in the C2C-with-private-source scenario (row
 * #31) where the PE is explicitly approved.
 *
 * <p>The {@code description} field on update is also intentionally not asserted
 * to equal the new value — the RP echoes the stale (create-time) description
 * for several seconds after a PATCH and the assertion would be flaky across
 * languages (Python and JS hit the same behaviour).
 */
public class ConnectionTests extends StorageMoverManagementTestBase {

    @Override
    protected Region testRegion() {
        return WEST_CENTRAL_US;
    }

    @Test
    public void createGetListUpdateDelete() {
        String storageMoverName = generateRandomResourceName("stomover-conn-", 24);
        String connectionName = generateRandomResourceName("conn-", 24);

        StorageMover sm = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(testRegion())
            .withExistingResourceGroup(resourceGroupName)
            .create();

        try {
            Connection created = storageMoverManager.connections()
                .define(connectionName)
                .withExistingStorageMover(resourceGroupName, sm.name())
                .withProperties(new ConnectionProperties().withDescription("initial description")
                    .withPrivateLinkServiceId(PRIVATE_LINK_SERVICE_ID))
                .create();
            Assertions.assertEquals(connectionName, created.name());
            Assertions.assertNotNull(created.properties());
            Assertions.assertEquals("initial description", created.properties().description());
            // The body-key sanitizer registered in the base redacts the
            // subscription segment of $..privateLinkServiceId to all-zeros, so
            // assert only the resource-group + name suffix (which is what the
            // round-trip actually verifies). The sub segment is identical in
            // record and playback after sanitization.
            String expectedPlsSuffix = "/resourceGroups/" + PRIVATE_LINK_SERVICE_RG
                + "/providers/Microsoft.Network/privateLinkServices/" + PRIVATE_LINK_SERVICE_NAME;
            Assertions.assertNotNull(created.properties().privateLinkServiceId());
            Assertions.assertTrue(created.properties().privateLinkServiceId().endsWith(expectedPlsSuffix),
                "expected privateLinkServiceId to end with " + expectedPlsSuffix + " but was "
                    + created.properties().privateLinkServiceId());

            Connection fetched = storageMoverManager.connections().get(resourceGroupName, sm.name(), connectionName);
            Assertions.assertEquals(connectionName, fetched.name());
            Assertions.assertNotNull(fetched.properties().privateLinkServiceId());
            Assertions.assertTrue(fetched.properties().privateLinkServiceId().endsWith(expectedPlsSuffix),
                "expected fetched privateLinkServiceId to end with " + expectedPlsSuffix + " but was "
                    + fetched.properties().privateLinkServiceId());

            boolean foundConnection = StreamSupport
                .stream(storageMoverManager.connections().list(resourceGroupName, sm.name()).spliterator(), false)
                .anyMatch(c -> connectionName.equals(c.name()));
            Assertions.assertTrue(foundConnection,
                "expected connection " + connectionName + " in list but it was not found");

            // Update keeps the same PLS id (cannot be changed post-create) and
            // changes only the description. The new description is intentionally
            // NOT asserted because the RP echoes the create-time value for several
            // seconds after the PATCH (cross-language flakiness — see playbook).
            Connection updated = fetched.update()
                .withProperties(new ConnectionProperties().withDescription("updated description")
                    .withPrivateLinkServiceId(PRIVATE_LINK_SERVICE_ID))
                .apply();
            Assertions.assertEquals(connectionName, updated.name());

            Connection refreshed = updated.refresh();
            Assertions.assertEquals(connectionName, refreshed.name());

            storageMoverManager.connections().delete(resourceGroupName, sm.name(), connectionName);
            assertNotFound(() -> storageMoverManager.connections().get(resourceGroupName, sm.name(), connectionName));
        } finally {
            // Best-effort cleanup in case the test failed before the explicit
            // delete above. Idempotent — delete on a missing resource is a 204.
            try {
                storageMoverManager.connections().delete(resourceGroupName, sm.name(), connectionName);
            } catch (RuntimeException ignored) {
                // already deleted or never created.
            }
        }
    }
}
