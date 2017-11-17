/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.cosmosdb.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestCosmosDB extends TestTemplate<CosmosDBAccount, CosmosDBAccounts> {

    @Override
    public CosmosDBAccount createResource(CosmosDBAccounts resources) throws Exception {
        final String newName = "docDB" + this.testId;
        CosmosDBAccount databaseAccount = resources.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                .withSessionConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_CENTRAL)
                .withIpRangeFilter("")
                .create();
        Assert.assertEquals(databaseAccount.name(), newName.toLowerCase());
        Assert.assertEquals(databaseAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assert.assertEquals(databaseAccount.writableReplications().size(), 1);
        Assert.assertEquals(databaseAccount.readableReplications().size(), 2);
        Assert.assertEquals(databaseAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.SESSION);
        return databaseAccount;
    }

    @Override
    public CosmosDBAccount updateResource(CosmosDBAccount resource) throws Exception {
        // Modify existing container service
        resource =  resource.update()
                .withReadReplication(Region.ASIA_SOUTHEAST)
                .withoutReadReplication(Region.US_EAST)
                .withoutReadReplication(Region.US_CENTRAL)
                .apply();

        resource =  resource.update()
                .withEventualConsistency()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
        Assert.assertEquals(resource.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(CosmosDBAccount resource) {
        System.out.println(new StringBuilder().append("Regsitry: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .toString());
    }
}