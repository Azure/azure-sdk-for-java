// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccounts;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.cosmos.models.DefaultConsistencyLevel;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;

public class TestCosmosDB extends TestTemplate<CosmosDBAccount, CosmosDBAccounts> {

    @Override
    public CosmosDBAccount createResource(CosmosDBAccounts resources) throws Exception {
        final String newName = "docDB" + resources.manager().resourceManager().internalContext().randomResourceName("", 8);
        CosmosDBAccount databaseAccount =
            resources
                .define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                .withSessionConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_CENTRAL)
                .withIpRangeFilter("")
                .create();
        Assertions.assertEquals(databaseAccount.name(), newName.toLowerCase());
        Assertions.assertEquals(databaseAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assertions.assertEquals(databaseAccount.writableReplications().size(), 1);
        Assertions.assertEquals(databaseAccount.readableReplications().size(), 2);
        Assertions.assertEquals(databaseAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.SESSION);
        return databaseAccount;
    }

    @Override
    public CosmosDBAccount updateResource(CosmosDBAccount resource) throws Exception {
        // Modify existing container service
        resource =
            resource
                .update()
                .withReadReplication(Region.ASIA_SOUTHEAST)
                .withoutReadReplication(Region.US_EAST)
                .withoutReadReplication(Region.US_CENTRAL)
                .apply();

        resource =
            resource
                .update()
                .withEventualConsistency()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assertions.assertEquals(resource.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertTrue(!resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(CosmosDBAccount resource) {
        System
            .out
            .println(
                new StringBuilder()
                    .append("Regsitry: ")
                    .append(resource.id())
                    .append("Name: ")
                    .append(resource.name())
                    .append("\n\tResource group: ")
                    .append(resource.resourceGroupName())
                    .append("\n\tRegion: ")
                    .append(resource.region())
                    .append("\n\tTags: ")
                    .append(resource.tags())
                    .toString());
    }
}
